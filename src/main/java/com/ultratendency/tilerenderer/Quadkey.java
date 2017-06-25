package com.ultratendency.tilerenderer;

public final class Quadkey {
    private static final double MIN_LATITUDE = -85.05112878;
    private static final double MAX_LATITUDE = 85.05112878;
    private static final double MIN_LONGITUDE = -180;
    private static final double MAX_LONGITUDE = 180;

    private Quadkey() {
    }

    private static double Clip(double n, double minValue, double maxValue) {
        return Math.min(Math.max(n, minValue), maxValue);
    }

    public static long MapSize(int levelOfDetail) {
        return (long) 256 << levelOfDetail;
    }

    public static String ComputeQuadkey(double latitude, double longitude, int levelOfDetail) {
        latitude = Clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
        longitude = Clip(longitude, MIN_LONGITUDE, MAX_LONGITUDE);

        double x = (longitude + 180) / 360;
        double sinLatitude = Math.sin(latitude * Math.PI / 180);
        double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);
        long mapSize = MapSize(levelOfDetail);

        int pixelX = (int) Clip(x * mapSize + 0.5, 0, mapSize - 1);
        int pixelY = (int) Clip(y * mapSize + 0.5, 0, mapSize - 1);

        //TileXYToPixelXY
        int tileX = pixelX / 256;
        int tileY = pixelY / 256;

        return TileXYToQuadKey(tileX, tileY, levelOfDetail);
    }

    public static String TileXYToQuadKey(int tileX, int tileY, int levelOfDetail) {
        StringBuilder quadKey = new StringBuilder();

        for (int i = levelOfDetail; i > 0; i--) {
            char digit = '0';
            int mask = 1 << (i - 1);

            if ((tileX & mask) != 0) {
                digit++;
            }

            if ((tileY & mask) != 0) {
                digit++;
                digit++;
            }

            quadKey.append(digit);
        }

        return quadKey.toString();
    }

    public static int[] QuadKeyToTileXY(String quadKey) {
        int tileX = 0;
        int tileY = 0;
        int levelOfDetail = quadKey.length();

        for (int i = levelOfDetail; i > 0; i--) {
            int mask = 1 << (i - 1);

            switch (quadKey.charAt(levelOfDetail - i)) {
                case '0':
                    break;
                case '1':
                    tileX |= mask;
                    break;
                case '2':
                    tileY |= mask;
                    break;
                case '3':
                    tileX |= mask;
                    tileY |= mask;
                    break;
                default:
                    break;
            }
        }

        return new int[]{ tileX, tileY };
    }

    public static int[] TileXYToPixelXY(int[] tileXY) {
        int pixelX = tileXY[0] * 256;
        int pixelY = tileXY[1] * 256;

        return new int[]{ pixelX, pixelY };
    }

    public static double[] PixelXYToLatLong(int pixelX, int pixelY, int levelOfDetail) {
        double mapSize = MapSize(levelOfDetail);
        double x = (Clip(pixelX, 0, mapSize - 1) / mapSize) - 0.5;
        double y = 0.5 - (Clip(pixelY, 0, mapSize - 1) / mapSize);
        double latitude = 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
        double longitude = 360 * x;

        return new double[]{ latitude, longitude };
    }

    public static int GetZoomLevelFromQuadKey(String quadkey) {
        return quadkey.length();
    }
}
