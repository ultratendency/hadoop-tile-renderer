package safecast.tilerenderer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.imageio.ImageIO;


public class TileMerger 
{
	private String InputDir;
	private String OutputDir;
	private HashMap <String, List<String>> map = new HashMap <String, List<String>>();
	
	private boolean hasSuffix = false;
	
	public TileMerger(String Input, String Output)
	{
		this.InputDir = Input;
		this.OutputDir = Output;
	}
	
	public void MapImages()
	{
		File[] files = new File(this.InputDir).listFiles();
		
		for (File file : files) 
		{			
			String tempFileName = file.getName();
			String fileType = "";
			try 
			{
				fileType = tempFileName.substring(tempFileName.length()-4, tempFileName.length());
			} 
			catch (Exception e) {}
									
			if (fileType.equals(".png")) 
			{
				if (tempFileName.contains("_P"))
				{
					hasSuffix = true;
				}
				
				
				String[] strarr = tempFileName.split("\\.");
				String[] secsplit = strarr[0].split("_");
				int zoom = Integer.parseInt(secsplit[0]);
				int tileX = Integer.parseInt(secsplit[1]);
				int tileY = Integer.parseInt(secsplit[2]);
				
				String qkey = Quadkey.TileXYToQuadKey(tileX, tileY, zoom);					
				String cuttedKey = qkey.substring(0, qkey.length() -1);
				String pos = qkey.substring(qkey.length() - 1);
				
				addToMap(map,cuttedKey,pos + ";" + tempFileName);
			}
		}
	}
	
	private void addToMap(HashMap <String, List<String>> map, String key, String value)
	{
		  if(!map.containsKey(key))
		  {
		    map.put(key, new ArrayList<String>());
		  }
		  
		  map.get(key).add(value);		
	}
	
	public void ReduceImages() throws IOException
	{
		for (Entry<String, List<String>> entry : this.map.entrySet())
		{
			BufferedImage containerImg = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
			Graphics g = containerImg.getGraphics();
			
			BufferedImage tempImg = null;
			
			for (String line : entry.getValue()) 
		    {				
				String[] split = line.toString().split(";");
				int pos = Integer.parseInt(split[0]);
				String fileName = split[1];				
				
				tempImg = ImageIO.read(new File(this.InputDir + "/" + fileName));
								
				switch (pos) 
				{								
					case 0:						
						g.drawImage(tempImg, 0, 0, 128, 128, 0, 0, 256, 256, null);
						break;			
					case 1:
						g.drawImage(tempImg, 128, 0, 256, 128, 0, 0, 256, 256, null);
						break;
					case 2:
						g.drawImage(tempImg, 0, 128, 128, 256, 0, 0, 256, 256, null);
						break;
					case 3:
						g.drawImage(tempImg, 128, 128, 256, 256, 0, 0, 256, 256, null);
						break;
							
					default:
						break;
				}
				
				int[] TileXY = Quadkey.QuadKeyToTileXY(entry.getKey());
				
				String saveName = "";
				if(hasSuffix)
				{
					saveName = entry.getKey().length() + "_" + TileXY[0] + "_" + TileXY[1] + "_P.png";
				}
				else
				{
					saveName = entry.getKey().length() + "_" + TileXY[0] + "_" + TileXY[1] + ".png";
				}

									
				ImageIO.write(containerImg, "png", new File(this.OutputDir + "/" + saveName));						
			}	
		}
	}
	
		
	public static void main(String[] args) 
	{	
		String inputDir = args[0];
		String outputDir = args[1];
		
		System.out.println("Mergin Tiles in: " + inputDir);
		
		boolean success = (new File(outputDir)).mkdirs();
		if (success) 
		{
			TileMerger tr = new TileMerger(inputDir, outputDir);
			tr.MapImages();
			try 
			{
				tr.ReduceImages();
			} 
			catch (IOException e) 
			{
				System.out.println(e.toString());
				e.printStackTrace();
			}
			
			System.out.println("Tiles Merged");		
			
		}
		else
		{
		    System.out.println("Output Directory creation failed");
		}		
	}
}
