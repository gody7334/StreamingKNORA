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
dataset_file:Poker\
classifier:AHT
```

## Parameters
- __num_models:__ ensemble size
- __num_insts:__ number of instance in the dataset
- __num_warmups:__ number of instance for warming up streaming model
- __num_Val:__ number of validation instance for KNORA
- __num_neighbour:__ number of nearest neighbour for KNORA
- __intersect:__ 1(interset), 0(union)
- __train_batch_size:__ training batch size
- __validate_batch_size:__ validation batch size
- __test_batch_size:__ testing batch size
- __dataset_file:__ can be Poker, Cover, RRBFno, RRBF0.0001, RRBF0.001, HYP0.0001 or HYP0.001
- __classifier:__ can be AHT, HT, SGD or NB
