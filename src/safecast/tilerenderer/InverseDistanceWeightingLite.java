package safecast.tilerenderer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;

public class InverseDistanceWeightingLite 
{
	private final Envelope env;
	
	private int xNumCells;
    private int yNumCells;
	  
    private double dx;
    private double dy;   
         
    private double [][] points;
    
	private final float[][] grid2D;
	
	
	public InverseDistanceWeightingLite(HashMap <DirectPosition2D, Float> positions,Envelope envelope,int xNumOfCells,int yNumOfCells)
	{		
		this.env = envelope;
				
        this.xNumCells = xNumOfCells;
        this.yNumCells = yNumOfCells;

        this.dx = this.env.getSpan(0) / xNumOfCells;
        this.dy = this.env.getSpan(1) / yNumOfCells;
        
        this.grid2D = new float[yNumCells + 1][xNumCells + 1];
        
        
        this.FillPoints(positions);
	}
	
	public void FillPoints(Map<DirectPosition2D, Float> positions)
	{		
		this.points = new double[positions.size()][3];
		int p = 0;
		for (Iterator<DirectPosition2D> i = positions.keySet().iterator(); i.hasNext();)         
		{
			DirectPosition source = (DirectPosition) i.next();
	        double[] coords = source.getCoordinate();
	            
	        this.points[p][0] = coords[0];
	        this.points[p][1] = coords[1];
	        this.points[p][2] = positions.get(source);
	        
	        p++;
		}
	}
	
	
	public float[][] get2DGrid() 
    { 			
		for (int i = 0; i <= yNumCells; i++) 
        {
            for (int j = 0; j <= xNumCells; j++) 
            {
            	DirectPosition dp = new DirectPosition2D
    			(
					env.getLowerCorner().getOrdinate(0) + (j * dx),
					env.getUpperCorner().getOrdinate(1) - (i * dy)
    			);
            	
            	float value = calculateValue(dp.getCoordinate());
            	
            	
                grid2D[i][j] = value;
            }
        }
					
		return grid2D;
      
    }
  
    private float calculateValue(double[] ds)
    {
    	float value;
    	double sumdValue = 0;
        double sumweight = 0;

        for (int i = this.points.length; -- i >= 0; )
        {
         	double dist = Math.sqrt(
                    (this.points[i][0] - ds[0]) *  (this.points[i][0] - ds[0]) + 
                    (this.points[i][1] - ds[1]) *  (this.points[i][1] - ds[1])
                );
         	
         	if (dist <= 0.5)
         	{  	
	         	double weight = 1.0 / (dist * dist);
	
	        	sumdValue = sumdValue + this.points[i][2] * weight;
	        	sumweight = sumweight + weight;	        	        	
         	}
        }
        
        value = (float) (sumdValue / sumweight);
        

        return value;
    }
    
}
