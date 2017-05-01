StreamingKNORA
========

This repository contains Streaming-KNORA (S-KNORA), an algorithm designed to analyse streaming data in a distributed environment. S-KNORA is implemented using Spark across multiple Hadoop nodes.

This project was developed in the School of Computer Science of the University of Manchester as part of my MSc dissertation under the supervision of Professor John A. Keane (john.keane@manchester.ac.uk). Additional involved staff: Dr. Firat Tekiner (firat.tekiner@manchester.ac.uk).

## Repository layout<br />
__./Dataset_Single:__ the datasets used in StreamingKNORA_Single experiments<br />
__./Dataset_Spark:__ the datasets used in StreamingKNORA_Spark experiments<br />
__./StreamingKNORA_Single:__ A Java implementation for batch size selection and  baseline single thread<br />
__./StreamingKNORA_Spark:__ A Spark implementation for performance evaluation on Spark <br />

---------
This project was a proof of concept that aimed to demonstrate the feasibility of using Weka's libraries on
Big Data problems and to study the effects of different caching strategies on Big Data Mining workloads.

During benchmarks on AWS, the system demonstrated an average weak scaling efficiency of 91% on clusters up to 128 cores.
Additionally, it was found to be 2-4 times faster than Weka on Hadoop (more to follow on this).

However, being a proof-of-concept, it requires further work to meet industry standards.

Currently, only <b>CSV</b> files can be processed.

The classifier evaluation classes have been successfully ported and executed on top of <b>Spark Streaming</b>. I will upload the code in a separate repository.

This is a Maven project. Building with Maven (goal: package) would yield a deployable uber-jar that
can be directly executed on a Spark cluster.

Execution
========

Copy the uber-jar to the cluster's master node and execute the following:

bin/spark-submit --master (master-node) \ <br>
 --class uk.ac.manchester.ariskk.distributedWekaSpark.main.distributedWekaSpark \ <br>
 --executor-memory (per-node-memory-to-use) \ <br>
  (other spark parameters e.g. number of cores etc) <br>
/path/to/distributedWekaSpark-0.1.0-SNAPSHOT.jar \ <br>
(application parameters)

The parameters are structured as follows:
-parameter1-name value -parameter2-name value etc. The order is irrelevant

Full details on the supported parameters as well as examples of usage can be found in the <b>wiki</b> section of the repository.

I will try to add unit tests, Travis, Logging and error handling as soon as I have the time.


More to follow..
