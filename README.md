Hadoop Tile Renderer
====================

Tested on Azure HDInsight

## Cluster setup - we do need some RAM!

In *mapred-site.xml*:

    mapreduce.map.memory.mb: 4096
    mapreduce.reduce.memory.mb: 8192

Each Container will run JVMs for the Map and Reduce tasks. The JVM heap size should be set to lower than the Map and Reduce memory defined above, so that they are within the bounds of the Container memory allocated by YARN.

In *mapred-site.xml*:

    mapreduce.map.java.opts: -Xmx3072m
    mapreduce.reduce.java.opts: -Xmx6144m

### only in case of changes to the cluster

replace conf in *hbaseapp\conf* with *C:\apps\dist\hbase-0.98.0.2.1.6.0-2103-hadoop2\conf\hbase-site.xml* and rebuild.

### copy to cluster:

- hbaseapp-1.0-SNAPSHOT.jar
- TileViewer.html (starts zoom level 9 Center Tokyo)
- measurements.csv (Safecast Download: https://api.safecast.org/system/measurements.csv)
- sievert_points_thermal_z9.sld
- sievert_points_thermal_z10.sld
- sievert_points_thermal_z11.sld


    $ hadoop fs -mkdir /Output
    $ hadoop fs -mkdir /Input
    $ hadoop fs -put D:\Users\safecast.headnode0\Desktop\measurements.csv /Input

## Renderer

    $ hadoop fs -mkdir /Renderer
    $ hadoop fs -put D:\Users\safecast.headnode0\Desktop\sievert_points_thermal_z9.sld /Renderer
    $ hadoop fs -put D:\Users\safecast.headnode0\Desktop\sievert_points_thermal_z10.sld /Renderer
    $ hadoop fs -put D:\Users\safecast.headnode0\Desktop\sievert_points_thermal_z11.sld /Renderer

## HBASE

hbase shell

    $ hbase ( main ) :001:0 > create 'safecast23','cf'

######################################## Import-Job
    $ hadoop jar D:\Users\safecast.headnode0\Desktop\hbaseapp-1.0-SNAPSHOT.jar com.ultratendency.mapreduce.ImportFromSafecastCSV -t safecast23 -f cf -i /Input/measurements.csv

######################################## PointTileRenderer-Job 
    $ hadoop jar D:\Users\safecast.headnode0\Desktop\hbaseapp-1.0-SNAPSHOT.jar com.ultratendency.mapreduce.PointTileMapReduce -t safecast23 -f cf -c value -o /Output/ptiles9 -zmin 9 -zmax 9 -r 16

- zmin = zmax !!!!!! ATTENTION: only zoom levels 9-12 supported; set zmin = zmax 
- -t Table
- -f ColumnFam
- -c column
- -o OutputDir
- -r Reducer Anzahl

### to set #mappers, region split

########################################CopyToLocal Desktop
    $ hadoop fs -copyToLocal /Output/ptiles9 D:\Users\safecast.headnode0\Desktop\

## TUNING

### split for more mappers 
hbase shell

    $ hbase ( main ) :001:0 > split ‘safecast23’

### problems

Check if anything works at all with

    $ hadoop jar D:\Users\safecast.headnode0\Desktop\hbaseapp-1.0-SNAPSHOT.jar com.microsoft.examples.CreateTable