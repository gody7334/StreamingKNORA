StreamingKNORA-Spark
========
This folder contains S-KNORA implementation using Spark for performance evaluation. Evaluation involves testing various parameters on different datasets to measure accuracy and throughput, and monitor resource utilization.

## Build
using Maven, This is a Maven project. To compile the project, run:

```mvn clean package```

## Execution
After compiling mvn project, run following example command:
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
- num_models:
- num_insts:
- num_warmups:
- num_Val:
- num_neighbour:
- intersect:
- train_batch_size:
- validate_batch_size:
- test_batch_size:
- dataset_file:
- classifier:
