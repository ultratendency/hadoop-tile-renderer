package com.ultratendency.tilerenderer;

public final class Quadkey {
    private static final double MIN_LATITUDE = -85.05112878;
    private static final double MAX_LATITUDE = 85.05112878;
    private static final double MIN_LONGITUDE = -180;
    private static final double MAX_LONGITUDE = 180;

    private Quadkey() {
    }

    public static String computeQuadkey(double latitude, double longitude, int levelOfDetail) {
        latitude = clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
        longitude = clip(longitude, MIN_LONGITUDE, MAX_LONGITUDE);

        double x = (longitude + 180) / 360;
        double sinLatitude = Math.sin(latitude * Math.PI / 180);
        double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);
        long mapSize = mapSize(levelOfDetail);

        int pixelX = (int) clip(x * mapSize + 0.5, 0, mapSize - 1);
        int pixelY = (int) clip(y * mapSize + 0.5, 0, mapSize - 1);

        int tileX = pixelX / 256;
        int tileY = pixelY / 256;

        return tileXYToQuadKey(tileX, tileY, levelOfDetail);
    }

    public static int[] quadKeyToTileXY(String quadKey) {
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

    public static int getZoomLevelFromQuadKey(String quadkey) {
        return quadkey.length();
    }

    static String tileXYToQuadKey(int tileX, int tileY, int levelOfDetail) {
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

    private static double clip(double n, double minValue, double maxValue) {
        return Math.min(Math.max(n, minValue), maxValue);
    }

    private static long mapSize(int levelOfDetail) {
        return (long) 256 << levelOfDetail;
    }
}
