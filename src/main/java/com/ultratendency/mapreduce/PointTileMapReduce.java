package com.ultratendency.mapreduce;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.ultratendency.tilerenderer.GoogleTileHelper;
import com.ultratendency.tilerenderer.Quadkey;
import com.ultratendency.tilerenderer.TileRenderer;
import org.opengis.geometry.Envelope;

import java.io.IOException;
import java.net.URI;

public final class PointTileMapReduce {
    private static final String NAME = "PointTileMapReduce";

    private PointTileMapReduce() {
    }

    public static final class PointTileMapper extends TableMapper<Text, Text> {
        private PointTileMapper() {
        }

        @Override
        public void map(ImmutableBytesWritable row, Result columns, Context context) throws IOException {
            for (Cell cell : columns.listCells()) {
                String key = Bytes.toStringBinary(CellUtil.cloneRow(cell));
                String value = Bytes.toStringBinary(CellUtil.cloneValue(cell));
                Configuration conf = context.getConfiguration();
                int zoomMin = Integer.parseInt(conf.get("zoom_min"));
                int zoomMax = Integer.parseInt(conf.get("zoom_max"));

                for (int zoomLevel = zoomMin; zoomLevel <= zoomMax; zoomLevel++) {
                    String cutKey = key.substring(0, zoomLevel);

                    try {
                        context.write(new Text(cutKey), new Text(value));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static final class PointTileReducer extends Reducer<Text, Text, Text, Text> {
        BinaryWritable bw = new BinaryWritable();

        private PointTileReducer() {
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            int zoom = Quadkey.getZoomLevelFromQuadKey(key.toString());
            int[] tileXY = Quadkey.quadKeyToTileXY(key.toString());
            Envelope env = GoogleTileHelper.getEnv(tileXY[0], tileXY[1], zoom);
            TileRenderer tr = new TileRenderer(env, FileOutputFormat.getOutputPath(context).toString());

            tr.renderPoints(values, zoom, zoom + "_" + tileXY[0] + "_" + tileXY[1] + "_P");

            byte[] bytesToWrite = tr.imageByteArray;
            Path file = new Path(FileOutputFormat.getOutputPath(context).toString() + "/" + tr.fileName);
            FSDataOutputStream out = file.getFileSystem(context.getConfiguration()).create(file);

            bw.setBytes(bytesToWrite);
            bw.write(out);
            out.close();

            context.write(key, new Text(""));
        }
    }

    private static CommandLine parseArgs(String[] args) throws ParseException {
        Options options = new Options();

        Option o = new Option("t", "table", true, "table to read from (must exist)");
        o.setArgName("table-name");
        o.setRequired(true);
        options.addOption(o);

        o = new Option("f", "family", true, "family to read row data from (must exist)");
        o.setArgName("family");
        o.setRequired(true);
        options.addOption(o);

        o = new Option("c", "column", true, "column to read data from (must exist)");
        o.setArgName("column");
        o.setRequired(true);
        options.addOption(o);

        o = new Option("o", "output file", true, "output file to write data to (must exist)");
        o.setArgName("output");
        o.setRequired(true);
        options.addOption(o);

        o = new Option("zmin", "zoom minimum", true, "the minimal zoom level (must exist)");
        o.setArgName("ZoomMin");
        o.setRequired(true);
        options.addOption(o);

        o = new Option("zmax", "zoom minimum", true, "the maximum zoom level (must exist)");
        o.setArgName("ZoomMax");
        o.setRequired(true);
        options.addOption(o);

        o = new Option("r", "reducetasks", true, "the number of reducer tasks (must exist)");
        o.setArgName("reducetasks");
        o.setRequired(true);
        options.addOption(o);

        options.addOption("d", "debug", false, "switch on DEBUG log level");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage() + "\n");

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(NAME + " ", options, true);

            System.exit(-1);
        }

        return cmd;
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        CommandLine cmd = parseArgs(otherArgs);
        String table = cmd.getOptionValue("t");
        String family = cmd.getOptionValue("f");
        String column = cmd.getOptionValue("c");
        String output = cmd.getOptionValue("o");
        String zmin = cmd.getOptionValue("zmin");
        String zmax = cmd.getOptionValue("zmax");
        int reducerTasks = Integer.parseInt(cmd.getOptionValue("r"));

        Scan scan = new Scan();
        scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
        scan.setCaching(1000);
        scan.setCacheBlocks(false);  // don't set to true for MR jobs

        conf.set("zoom_min", zmin);
        conf.set("zoom_max", zmax);

        Job job = Job.getInstance(conf, NAME);
        job.addCacheFile(new URI("/Renderer/sievert_points_thermal_z9.sld"));
        job.addCacheFile(new URI("/Renderer/sievert_points_thermal_z10.sld"));
        job.addCacheFile(new URI("/Renderer/sievert_points_thermal_z11.sld"));
        job.setJarByClass(PointTileMapReduce.class);

        TableMapReduceUtil.initTableMapperJob(table, scan, PointTileMapper.class, Text.class, Text.class, job);

        job.setReducerClass(PointTileReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(BinaryWritable.class);
        job.setNumReduceTasks(reducerTasks);

        FileOutputFormat.setOutputPath(job, new Path(output));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
