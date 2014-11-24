package safecast.mapreduce;

import java.io.IOException;
import java.net.URI;

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
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
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


import safecast.tilerenderer.GoogleTileHelper;
import safecast.tilerenderer.Quadkey;
import safecast.tilerenderer.TileRenderer;


public class PointTileMapReduce 
{
	public static final String NAME = "PointTileMapReduce";
	
	static class PointTileMapper extends TableMapper<Text, Text>
	{				
		@Override
		public void map(ImmutableBytesWritable row, Result columns, Context context) throws IOException
		{			
			for(KeyValue kv : columns.list())
			{
				String key = Bytes.toStringBinary(kv.getRow());	
				String value = Bytes.toStringBinary(kv.getValue());
	
				Configuration conf = context.getConfiguration();
				int zoom_min = Integer.parseInt(conf.get("zoom_min"));
				int zoom_max = Integer.parseInt(conf.get("zoom_max"));
				
				for (int zoomLevel = zoom_min; zoomLevel <= zoom_max; zoomLevel++) 
				{
					String cuttedKey = key.substring(0, zoomLevel);

					try 
					{
						context.write(new Text(cuttedKey), new Text(value));
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	static class PointTileReducer extends Reducer<Text, Text, Text, Text>
	{
		BinaryWritable bw = new BinaryWritable();
		
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{																			  						
			int zoom = Quadkey.GetZoomLevelFromQuadKey(key.toString());			
			int[] TileXY = Quadkey.QuadKeyToTileXY(key.toString());
    		org.opengis.geometry.Envelope env = GoogleTileHelper.GetEnv(TileXY[0], TileXY[1], zoom);
               		
    		TileRenderer tr = new TileRenderer(env,FileOutputFormat.getOutputPath(context).toString());
    		
    		
    		//tr.context = context;
    		
    		tr.renderPoints(values, zoom, zoom+"_"+TileXY[0]+"_"+TileXY[1]+"_P");
    		    		   		
    		byte[] bytesToWrite = tr.ImageByteArray;   		
			Path file = new Path(FileOutputFormat.getOutputPath(context).toString() + "/" + tr.FileName);			
			FSDataOutputStream out = file.getFileSystem(context.getConfiguration()).create(file);
			
			bw.setBytes(bytesToWrite);
			bw.write(out);
			out.close();
			
			context.write(key, new Text(""));
		}
	}
		
	private static CommandLine parseArgs(String[] args) throws ParseException
	{
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
			
		try
		{
			cmd = parser.parse(options, args);
		}
		catch(Exception e)
		{
			System.err.println("ERROR: " + e.getMessage() + "\n");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(NAME + " ", options, true);
			System.exit(-1);
		}
				
		return cmd;
	}
	
	
	//-t test -f cf -c value -o testoutputdir -zmin 7 -zmax 8
	public static void main(String args[]) throws Exception
	{
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

			
		Job job = new Job(conf, NAME);
		
		
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
