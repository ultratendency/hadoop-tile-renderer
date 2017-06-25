package com.ultratendency.tilerenderer;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.process.ProcessException;
import org.geotools.process.vector.PointStackerProcess;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.apache.hadoop.io.Text;
import org.geotools.map.FeatureLayer;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;

public class TileRenderer {
    private final StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    private final String rasterSld = "sievert_colormap_ios.sld";
    private final String bordertilesPath = "/home/hadoop/bordertiles_gmap/";
    private final org.opengis.geometry.Envelope env;
    private MapContent map = null;

    public  final String outPutPath;
    public String fileName;
    public byte[] imageByteArray = null;
    public int pointCounter = 0;

    public TileRenderer(Envelope env, String outputpath) {
        this.env = env;
        this.outPutPath = outputpath;
        this.map = new MapContent();
    }

    private void CreateImage(String imageFileName, boolean AddBorder) {
        GTRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(this.map);

        Rectangle imageBounds;
        ReferencedEnvelope mapBounds;

        mapBounds = ReferencedEnvelope.create(env, DefaultEngineeringCRS.CARTESIAN_2D);
        imageBounds = new Rectangle(0, 0, 256, Math.round(256));
        BufferedImage image = new BufferedImage(imageBounds.width,
                imageBounds.height, BufferedImage.TYPE_INT_ARGB);

        //Fill transparent
        Graphics2D gr = image.createGraphics();
        Color transparent = new Color(0, 0, 0, 0);

        gr.setColor(transparent);
        gr.fill(imageBounds);

        renderer.paint(gr, imageBounds, mapBounds);

        this.fileName = imageFileName + ".png";
        this.imageByteArray = ConvertImageToByteArray(image);
        this.map.dispose();
    }

    private BufferedImage CreateBorderImage(String tileName, BufferedImage image) {
        File borderTile = new File(bordertilesPath + tileName + ".png");

        if (borderTile.exists()) {
            BufferedImage overlay;

            try {
                overlay = ImageIO.read(borderTile);
                BufferedImage mergedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics g = mergedImage.getGraphics();

                g.drawImage(image, 0, 0, null);
                g.drawImage(overlay, 0, 0, null);

                return mergedImage;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return image;
    }

    private Style createFromSLD(File sld) {
        try {
            SLDParser stylereader = new SLDParser(styleFactory, sld.toURI().toURL());
            Style[] style = stylereader.readXML();

            return style[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void renderIDW(Iterable<Text> values, String fileName) {
        InverseDistanceWeightingLite idwLite = new InverseDistanceWeightingLite(getHashMapFromValues(values),
                env,
                127,
                127);

        float[][] idWgrid = idwLite.get2DGrid();

        if (idWgrid != null) {
            GridCoverageFactory gcf = new GridCoverageFactory();
            GridCoverage2D mycov = gcf.create("Intepolated Coverage", idWgrid, env);
            File sld = new File(getClass().getResource(rasterSld).getPath());
            Style mystyle = createFromSLD(sld);
            GridCoverageLayer heatLayer = new GridCoverageLayer(mycov, mystyle);

            this.map.addLayer(heatLayer);
            this.CreateImage(fileName, true);
        }
    }

    public void renderPoints(Iterable<Text> values, int zoom, String fileName) {
        try {
            File pointsld = new File("sievert_points_thermal_z" + zoom + ".sld");
            Style pointstyle = createFromSLD(pointsld);
            ReferencedEnvelope bounds =  ReferencedEnvelope.create(this.env, DefaultEngineeringCRS.CARTESIAN_2D);
            int cellSize = zoom - 6;
            PointStackerProcess psp = new PointStackerProcess();
            SimpleFeatureCollection result = psp.execute(
                       getFeatureCollectionFromValues(values),
                       cellSize,
                       null, // normalize
                       org.geotools.process.vector.PointStackerProcess.PreserveLocation.Single,
                       bounds, // outputBBOX
                       256, // outputWidth
                       256, // outputHeight
                       null);
            Layer pointLayer = new FeatureLayer(result, pointstyle);

            this.map.addLayer(pointLayer);
            this.CreateImage(fileName, false);
        } catch (ProcessException | TransformException e) {
            e.printStackTrace();
        }
    }

    private HashMap<DirectPosition2D, Float> getHashMapFromValues(Iterable<Text> values) {
        HashMap<DirectPosition2D, Float> data = new HashMap<>();
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;
        this.pointCounter = 0;

        for (Iterator<Text> i = values.iterator(); i.hasNext();) {
            Text line = i.next();
            String[] tokens = line.toString().split("\\,");
            double latitude = Double.parseDouble(tokens[0]);
            double longitude = Double.parseDouble(tokens[1]);
            float value = Float.parseFloat(tokens[2]);

            data.put(new DirectPosition2D(crs, longitude, latitude), value);
        }

        this.pointCounter = data.size();

        return data;
    }

    private SimpleFeatureCollection getFeatureCollectionFromValues(Iterable<Text> values) {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();

        tb.setName("obsType");
        tb.setCRS(DefaultEngineeringCRS.CARTESIAN_2D);
        tb.add("shape", Point.class);
        tb.add("value", Double.class);

        SimpleFeatureType type = tb.buildFeatureType();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(type);
        GeometryFactory factory = new GeometryFactory();
        DefaultFeatureCollection features = new DefaultFeatureCollection();

        for (Iterator<Text> i = values.iterator(); i.hasNext();) {
            Text line = i.next();
            String[] tokens = line.toString().split("\\,");
            double latitude = Double.parseDouble(tokens[0]);
            double longitude = Double.parseDouble(tokens[1]);
            double value = Double.parseDouble(tokens[2]);
            Coordinate p = new Coordinate(longitude, latitude, value);
            Geometry point = factory.createPoint(p);

            fb.add(point);
            fb.add(p.z);

            features.add(fb.buildFeature(null));
        }

        return features;
    }

    public static byte[] ConvertImageToByteArray(BufferedImage img) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = null;

        try {
            ImageIO.write(img, "png", baos);
            baos.flush();
            bytes = baos.toByteArray();
            baos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }
}
