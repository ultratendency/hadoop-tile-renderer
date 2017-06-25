package com.ultratendency.tilerenderer;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;

public class InverseDistanceWeighting {

	private final Envelope env;
	
	private int xNumCells;

    private int yNumCells;
	  
    private double dx;

    private double dy;

    private double maxDist;
    
    private final Map<DirectPosition2D, Float> positions;
    
	private final float[][] grid2D;

	public InverseDistanceWeighting(HashMap <DirectPosition2D, Float> positions,
                                    Envelope envelope,
                                    double maxDist,
                                    int xNumOfCells,
                                    int yNumOfCells) {

		this.positions = positions;
		
		this.env = envelope;
		
		this.maxDist = maxDist;
		
        this.xNumCells = xNumOfCells;

        this.yNumCells = yNumOfCells;

        this.dx = this.env.getSpan(0) / xNumOfCells;

        this.dy = this.env.getSpan(1) / yNumOfCells;
        
        this.grid2D = new float[yNumCells + 1][xNumCells + 1];
	}

	public float[][] get2DGrid() {

        boolean AllZero = true;
		
		for (int i = 0; i <= yNumCells; i++) {

            for (int j = 0; j <= xNumCells; j++) {

            	DirectPosition dp = new DirectPosition2D (env.getLowerCorner().getOrdinate(0) + (j * dx),
					env.getUpperCorner().getOrdinate(1) - (i * dy));
            	
            	float value = calculateValue(dp);
            	
            	if (value != 0.0) {
            		AllZero = false;
            	}
            	
                grid2D[i][j] = value;
            }
        }
		
		if (AllZero) {
			return null;

		} else {
			return grid2D;
		}      
    }

    private float calculateValue(DirectPosition p) {

        HashMap<DirectPosition, Double> nearest = getNearestPositions(p);

        float value;

        double sumdValue = 0;

        double sumweight = 0;

        for (Iterator<DirectPosition> i = nearest.keySet().iterator(); i.hasNext();) {

            DirectPosition dp = (DirectPosition) i.next();

            double distance = ((Double) nearest.get(dp)).doubleValue();

            double weight = (1 / Math.pow(distance, 2));

            sumdValue = sumdValue + (float) ( (this.positions.get(dp)) * weight);

            sumweight = sumweight + weight;
        }

        if (sumdValue == 0) {

        	return (float) 0.0;
        }
        
        value = (float) (sumdValue / sumweight);

        return value;
    }
    
    private HashMap<DirectPosition, Double> getNearestPositions(DirectPosition p) {

        HashMap<DirectPosition, Double> nearest = new HashMap<DirectPosition, Double>();

        DirectPosition source = null;

        double dist;

        for (Iterator<DirectPosition2D> i = this.positions.keySet().iterator(); i.hasNext();) {

            source = (DirectPosition) i.next();

            dist = ((Point2D) p).distance((Point2D) source);
            
            if (dist < this.maxDist) {
            	nearest.put(source, new Double(dist));
            }
        }
        
        return nearest;
    }
}
