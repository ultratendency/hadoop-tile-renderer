package safecast.tilerenderer;

import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;

import com.vividsolutions.jts.geom.Coordinate;

public class GoogleTileHelper 
{
	public static double Tile2Long(int x,int z)
	{
		return (x/Math.pow(2,z)*360-180);
	}
	
	public static double Tile2Lat(int y,int z) 
	{
		double n=Math.PI-2*Math.PI*y/Math.pow(2,z);
		return (180/Math.PI*Math.atan(0.5*(Math.exp(n)-Math.exp(-n)))); 
	}
	
	public static org.opengis.geometry.Envelope GetEnv(int x,int y,int z)
	{	
		double LongBottomLeftCorner = Tile2Long(x, z);
		double LatBottomLeftCorner = Tile2Lat(y+1, z);
		
		
		double LongUpperRigthCorner = Tile2Long(x+1, z);
		double LatUpperRightCorner = Tile2Lat(y, z);


		
		Coordinate bottomLeftPoint = new Coordinate(LongBottomLeftCorner,LatBottomLeftCorner);
		Coordinate topRightPoint = new Coordinate(LongUpperRigthCorner,LatUpperRightCorner);
		
		GeneralDirectPosition mina = new GeneralDirectPosition(bottomLeftPoint.x,bottomLeftPoint.y);
		GeneralDirectPosition maxa = new GeneralDirectPosition(topRightPoint.x,topRightPoint.y);
		
		return new GeneralEnvelope(mina,maxa);
	}
}
