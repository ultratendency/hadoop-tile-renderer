###Nur falls Cluster Änderungen oder Probleme
conf in 
hbaseapp\conf 
mit 
C:\apps\dist\hbase-0.98.0.2.1.6.0-2103-hadoop2\conf\hbase-site.xml 
ersetzen und rebuilden

###Auf den Desktop des Clusters:
-hbaseapp-1.0-SNAPSHOT.jar
-TileViewer.html (Startet ZoomStufe 9 Center Tokyo)
-measurements.csv (Safecast Download: https://api.safecast.org/system/measurements.csv)
-sievert_points_thermal_z9.sld
-sievert_points_thermal_z10.sld
-sievert_points_thermal_z11.sld


hadoop fs -mkdir /Output
hadoop fs -mkdir /Input

hadoop fs -put D:\Users\safecast.headnode0\Desktop\measurements.csv /Input



###Renderer

hadoop fs -mkdir /Renderer

hadoop fs -put D:\Users\safecast.headnode0\Desktop\sievert_points_thermal_z9.sld /Renderer
hadoop fs -put D:\Users\safecast.headnode0\Desktop\sievert_points_thermal_z10.sld /Renderer
hadoop fs -put D:\Users\safecast.headnode0\Desktop\sievert_points_thermal_z11.sld /Renderer



###HBASE

hbase shell
hbase ( main ) :001:0 > create 'safecast23','cf'



########################################Import-Job
hadoop jar D:\Users\safecast.headnode0\Desktop\hbaseapp-1.0-SNAPSHOT.jar safecast.mapreduce.ImportFromSafecastCSV -t safecast23 -f cf -i /Input/measurements.csv



########################################PointTileRenderer-Job 
hadoop jar D:\Users\safecast.headnode0\Desktop\hbaseapp-1.0-SNAPSHOT.jar safecast.mapreduce.PointTileMapReduce -t safecast23 -f cf -c value -o /Output/ptiles9 -zmin 9 -zmax 9 -r 16


-zmin=zmax !!!!!!ACHTUNG nur die Zoomstufen 9-11 werden unterst¸tzt; zmin = zmax setzen 
-t Table
-f ColumnFam
-c column
-o OutputDir
-r Reducer Anzahl
###f¸r anzahl der Mapper regions split (s.o.)


########################################CopyToLocal Desktop
hadoop fs -copyToLocal /Output/ptiles9 D:\Users\safecast.headnode0\Desktop\



###TUNING
###splitten f¸r mehr Mapper 
hbase shell
hbase ( main ) :001:0 > split ‘safecast23’



###Probleme
Um zu pr¸fen ob ¸berhaupt was l‰uft eignet sich dieser job. Erstellt hbase table 'people' mit ein paar entries.
hadoop jar D:\Users\safecast.headnode0\Desktop\hbaseapp-1.0-SNAPSHOT.jar com.microsoft.examples.CreateTable
















