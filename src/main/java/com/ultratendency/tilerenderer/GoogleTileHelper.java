package com.ultratendency.tilerenderer;

import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;

import com.vividsolutions.jts.geom.Coordinate;

public final class GoogleTileHelper {
    private GoogleTileHelper() {
    }

    public static org.opengis.geometry.Envelope getEnv(int x, int y, int z) {
        double longBottomLeftCorner = tile2Long(x, z);
        double latBottomLeftCorner = tile2Lat(y + 1, z);
        double longUpperRigthCorner = tile2Long(x + 1, z);
        double latUpperRightCorner = tile2Lat(y, z);
        Coordinate bottomLeftPoint = new Coordinate(longBottomLeftCorner, latBottomLeftCorner);
        Coordinate topRightPoint = new Coordinate(longUpperRigthCorner, latUpperRightCorner);
        GeneralDirectPosition mina = new GeneralDirectPosition(bottomLeftPoint.x, bottomLeftPoint.y);
        GeneralDirectPosition maxa = new GeneralDirectPosition(topRightPoint.x, topRightPoint.y);

        return new GeneralEnvelope(mina, maxa);
    }

    private static double tile2Long(int x, int z) {
        return x / Math.pow(2, z) * 360 - 180;
    }

    private static double tile2Lat(int y, int z) {
        double n = Math.PI - 2 * Math.PI * y / Math.pow(2, z);

        return 180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
    }
}
