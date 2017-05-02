StreamingKNORA-Single
========
This folder contains S-KNORA implementation using Java. This implementation is considered as an ideal program without overhead and used to select optimal batch size.

## Build
This project is built using Apache Maven. To build the project, run:
```mvn clean package```

## Execution
After build the project, run following example command:
```
java -classpath ./target/MOA-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.MOA.main.MainKNORA \
num_models:64 \
num_insts:1000000 \
num_warmups:1000 \
num_Val:1000 \
num_neighbour:8 \
intersect:1 \
train_batch_size:10000 \
validate_batch_size:1000 \
test_batch_size:10000 \
dataset_file: Poker\
classifier:AHT
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
