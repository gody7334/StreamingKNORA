StreamingKNORA-Spark
========
This folder contains S-KNORA implementation using Spark for performance evaluation. Evaluation involves testing various parameters on different datasets to measure throughput and monitor resource utilization.

## Build
This project is built using Apache Maven. To build the project, run:

```mvn clean package```

## Execution
After build the project, copy the .jar and header files to the cluster's master node and execute the following example command:
```
/root/spark/bin/spark-submit \
	--class main.scala.BatchMain \
	--total-executor-cores 32 \
	--master spark://ec2-54-197-58-55.compute-1.amazonaws.com:7077 \
	/home/ec2-user/scala-java-mix-1.0-SNAPSHOT.jar \
	Dataset_HDFS:/user/root/Cover_SD.arff \
	Dataset_header:./Cover_H.arff \
	num_Models:4 \
	ModelType:ASHoeffdingTree \
	num_validate:2000 \
	num_neighbour:8 \
	isIntersect:true \
	num_train_batch:600 \
	num_validate_batch:2000 \
	num_test_batch:600 \
	num_warmup_perModel:100 \
	num_val_test_repartition:32 \
	max_tree_depth:20 \
	numOfData:58101 \
```

## Parameters
- __Dataset_HDFS:__ the dataset stored in cluster's HDFS, which can be Cover_SD.arff, Poker_SD.arff, RRBF_NoDrift_10a_1M_SD.arff, RRBF_Drift0.0001_10a_1M_SD.arff, RRBF_Drift0.001_10a_1M_SD.arff, HyperD0.0001A10_SD.arff or HyperD0.001A10_SD.arff
- __Dataset_header:__ the header file which can be Cover_H.arff, Poker_H.arff or RRBF_10a_H.arff
- __num_Models:__ ensemble size
- __ModelType:__ streaming model which can be ASHoeffdingTree or HoeffdingTree
- __num_validate:__ number of validation instance for KNORA
- __num_neighbour:__ number of nearest neighbour for KNORA
- __isIntersect:__ true(interset), false(union)
- __num_train_batch:__ training batch size
- __num_validate_batch:__ validation batch size
- __num_test_batch:__ testing batch size
- __num_warmup_perModel:__ number of instance for warming up streaming model
- __num_val_test_repartition:__ should be same to --total-executor-cores
- __max_tree_depth:__ max tree depth for ASHoeffdingTree 
- __numOfData:__ number of instance in the dataset
