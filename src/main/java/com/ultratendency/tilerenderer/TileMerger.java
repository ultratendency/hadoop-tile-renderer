package com.ultratendency.tilerenderer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.imageio.ImageIO;

public final class TileMerger  {
    private String inputDir;
    private String outputDir;
    private HashMap<String, List<String>> map = new HashMap<>();
    private boolean hasSuffix = false;

    private TileMerger(String Input, String Output) {
        this.inputDir = Input;
        this.outputDir = Output;
    }

    public static void main(String[] args) {
        String inputDir = args[0];
        String outputDir = args[1];

        System.out.println("Mergin Tiles in: " + inputDir);

        boolean success = (new File(outputDir)).mkdirs();

        if (success) {
            TileMerger tr = new TileMerger(inputDir, outputDir);
            tr.mapImages();

            try {
                tr.reduceImages();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Tiles Merged");

        } else {
            System.out.println("Output Directory creation failed");
        }
    }

    private void mapImages() {
        File[] files = new File(this.inputDir).listFiles();

        if (files != null) {
            for (File file : files) {
                String tempFileName = file.getName();
                String fileType = "";

                try {
                    fileType = tempFileName.substring(tempFileName.length() - 4, tempFileName.length());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (fileType.equals(".png")) {
                    if (tempFileName.contains("_P")) {
                        hasSuffix = true;
                    }

                    String[] strarr = tempFileName.split("\\.");
                    String[] secsplit = strarr[0].split("_");
                    int zoom = Integer.parseInt(secsplit[0]);
                    int tileX = Integer.parseInt(secsplit[1]);
                    int tileY = Integer.parseInt(secsplit[2]);
                    String qkey = Quadkey.tileXYToQuadKey(tileX, tileY, zoom);
                    String cuttedKey = qkey.substring(0, qkey.length() - 1);
                    String pos = qkey.substring(qkey.length() - 1);

                    addToMap(map, cuttedKey, pos + ";" + tempFileName);
                }
            }
        }
    }

    private void addToMap(HashMap<String, List<String>> map, String key, String value) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }

        map.get(key).add(value);
    }

    private void reduceImages() throws IOException {
        for (Entry<String, List<String>> entry : this.map.entrySet()) {
            BufferedImage containerImg = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            Graphics g = containerImg.getGraphics();

            BufferedImage tempImg;

            for (String line : entry.getValue()) {
                String[] split = line.split(";");
                int pos = Integer.parseInt(split[0]);
                String fileName = split[1];
                tempImg = ImageIO.read(new File(this.inputDir + "/" + fileName));

                switch (pos) {
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

                int[] tileXY = Quadkey.quadKeyToTileXY(entry.getKey());
                String saveName;

                if (hasSuffix) {
                    saveName = entry.getKey().length() + "_" + tileXY[0] + "_" + tileXY[1] + "_P.png";

                } else {
                    saveName = entry.getKey().length() + "_" + tileXY[0] + "_" + tileXY[1] + ".png";
                }

                ImageIO.write(containerImg, "png", new File(this.outputDir + "/" + saveName));
            }
        }
    }
}
