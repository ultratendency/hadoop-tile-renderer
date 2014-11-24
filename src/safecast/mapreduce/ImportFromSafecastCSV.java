package safecast.mapreduce;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.GenericOptionsParser;


import safecast.tilerenderer.Quadkey;





public class ImportFromSafecastCSV
{
	public static final String NAME = "ImportFromSafecastCSV";
	public enum Counters { LINES }
	
	static class ImportMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put>
	{
		private byte[] family = null;
		private final int detail = 23;
		private static int lineCounter = 0;
		
		@Override
		protected void setup(Context context) throws IOException, InterruptedException
		{
			String strFamily = context.getConfiguration().get("conf.family");						
			family = Bytes.toBytes(strFamily);
		}
		
		
		@Override
		public void map(LongWritable offset, Text line, Context context) throws IOException
		{
			try
			{
				String lineArr[] = line.toString().split("\\,");
				
				//Skip first line
				if (lineArr[0].equals("Captured Time"))
				{
					return;
				}
								
                double value = Double.parseDouble(lineArr[3]);
                String unit = lineArr[4].toLowerCase();
                String deviceID = lineArr[6];
                
                double microsievert = ConvertRadiationLevelToMicroSievert(deviceID, unit, value);
				
                if (microsievert != -1)
                {
                    String capturedtime = lineArr[0];
                    long timestampInMS = ConvertTimeStampToLong(capturedtime);
                                   	
                    if (timestampInMS != -1)
                	{                		
                    	double latitude = Double.parseDouble(lineArr[1]);
                        double longitude = Double.parseDouble(lineArr[2]);
                    	                    
                        double rounded_value = (double)Math.round(microsievert * 1000) / 1000;
        				
                        String quadkey = Quadkey.ComputeQuadkey(latitude, longitude, detail);
        				byte[] rowkey = Bytes.toBytes(quadkey);
        				//byte[] rowkey = Bytes.toBytes(quadkey  + "_" + rounded_value+ "_"+ Math.random());
        				

        				Put put = new Put(rowkey);
        				put.add(family, Bytes.toBytes("value"), timestampInMS, Bytes.toBytes(latitude +","+longitude+","+rounded_value));

        				//put.add(family, Bytes.toBytes("value"), Bytes.toBytes(latitude +","+longitude+","+rounded_value));
        			       				
        				context.write(new ImmutableBytesWritable(rowkey), put); 
                	}                	  				
                }					
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			context.getCounter(Counters.LINES).increment(1);
		} 
	}
		
	private static CommandLine parseArgs(String[] args) throws ParseException
	{
		Options options = new Options();
			
		Option o = new Option("t", "table", true, "table to import into (must exist)");
		o.setArgName("table-name");
		o.setRequired(true);			
		options.addOption(o);
				
		o = new Option("f", "family", true, "family to store row data into (must exist)");
		o.setArgName("family");
		o.setRequired(true);
		options.addOption(o);
			
		o = new Option("i", "input", true, "the directory or file to read from");
		o.setArgName("path-in-HDFS");
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
	
	
	//-t test -f cf -i shortendMeasurements.csv
	public static void main(String args[]) throws Exception
	{
		Configuration conf = HBaseConfiguration.create();
		
		
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		CommandLine cmd = parseArgs(otherArgs);
		
		String table = cmd.getOptionValue("t");
		String input = cmd.getOptionValue("i");
		String family = cmd.getOptionValue("f");
		
		
		conf.set("conf.family", family);
		
		Job job = new Job(conf, "Import from file " + input + " into table " + table);
		
		job.setJarByClass(ImportFromSafecastCSV.class);
		job.setMapperClass(ImportMapper.class);
		job.setOutputFormatClass(TableOutputFormat.class);
		job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, table);
		job.setOutputKeyClass(ImmutableBytesWritable.class);
		job.setOutputValueClass(Writable.class);
		job.setNumReduceTasks(0);
		
		FileInputFormat.addInputPath(job, new Path(input));
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);				
	}
	
	
    static int[] arr350 = { 5, 15, 16, 17, 18, 22 };
    static int[] arr100 = { 6, 7, 11, 13, 23 };
    static int[] arr132 = { 4, 9, 10, 12, 19, 24 };

    static double ConvertRadiationLevelToMicroSievert(String deviceID, String unit, Double value)
    {
        double microsievert = -1;
                       
        if (unit.equals("microsievert") || unit.equals("usv"))
        {
            microsievert = value;
        }              
        else if (unit.equals("cpm") && deviceID.equals(""))
        {
            microsievert =  value / 350.0;
        }
        else if (unit.equals("cpm") && !deviceID.equals(""))
        {
            int id = 0;
            try
            {
                id = Integer.parseInt(deviceID); 
            }
            catch (NumberFormatException e)
            {
                return -1;
            }
              
            if (TestInArray(arr350, id))
            {
                microsievert =  value / 350.0;
            }
            else if (TestInArray(arr100,id))
            {
                microsievert = value / 100;
            }
            else if (TestInArray(arr132,id))
            {
                microsievert =  value / 132;
            }
            else if (id == 21)
            {
                microsievert = value / 1750;
            }               
        }

        if (microsievert > 0 && microsievert <= 1000)
        {
            return microsievert;
        }
        else
        {               
            return -1;
        }    
    }
    
    public static long ConvertTimeStampToLong(String timestamp)
    {
        java.sql.Timestamp ts1 = java.sql.Timestamp.valueOf(timestamp);
        long timestampInMS = ts1.getTime();
    	
        
        //Skip bad future lines
        if(timestampInMS > System.currentTimeMillis())
        {
        	System.out.println("in future:" + timestamp);
        	return -1;
        }
        
        java.sql.Timestamp ts2010 = java.sql.Timestamp.valueOf("2010-01-01 00:00:00");
        long msOf2010 = ts2010.getTime();
        
        //Skip outdated lines
        if(timestampInMS < msOf2010)
        {
        	System.out.println("outdated:" + timestamp);
        	return -1;
        }
                     
    	return timestampInMS;   	
    }
    
    public static boolean TestInArray(int[] arr,int checkValue)
    {
    	for (int i : arr) 
    	{
			if (i == checkValue)
			{
				return true;
			}
		}
    	return false;
    }
    
}
