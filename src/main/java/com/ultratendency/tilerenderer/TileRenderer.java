package com.ultratendency.tilerenderer;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
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
import org.opengis.referencing.operation.TransformException;
import org.apache.hadoop.io.Text;
import org.geotools.map.FeatureLayer;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TileRenderer {
    private final StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    private static final String RASTER_SLD = "sievert_colormap_ios.sld";
    private static final String BORDERTILES_PATH = "/home/hadoop/bordertiles_gmap/";
    private final org.opengis.geometry.Envelope env;
    private MapContent map = null;

    public String fileName;
    public byte[] imageByteArray = null;

    public TileRenderer(Envelope env, String outputpath) {
        this.env = env;
        this.map = new MapContent();
    }

    private void createImage(String imageFileName) {
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
        this.imageByteArray = convertImageToByteArray(image);
        this.map.dispose();
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
            this.createImage(fileName);
        } catch (ProcessException | TransformException e) {
            e.printStackTrace();
        }
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

        for (Text line : values) {
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

    private static byte[] convertImageToByteArray(BufferedImage img) {
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
