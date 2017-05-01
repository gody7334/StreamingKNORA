StreamingKNORA
========

This repository contains Streaming-KNORA (S-KNORA), an algorithm designed to analyse streaming data in a distributed environment. S-KNORA is implemented using Spark across multiple Hadoop nodes.

This project was developed in the School of Computer Science of the University of Manchester as part of my MSc dissertation under the supervision of Professor John A. Keane (john.keane@manchester.ac.uk). Additional involved staff: Dr. Firat Tekiner (firat.tekiner@manchester.ac.uk).

This project was a proof of concept that aimed to demonstrate the feasibility of using KNORA ensemble learning on high throughput streaming data.

Results show that: <br />
(1) S-KNORA can learn concepts on disjoint streaming data and achieve higher accuracy than the single streaming learning mode; <br />
(2) the pipeline's throughput, running with a large batch size, is up to 6.82 times than the pipeline running on a single thread; <br />
(3) to capture severe concept drift, batch-incremental learning requires more frequent model update in a small batch causing high overhead in a distributed environment.

However, being a proof-of-concept, it requires further work to meet industry standards.

---------

## Repository layout<br />
__./Dataset_Single:__ the datasets used in StreamingKNORA_Single experiments<br />
__./Dataset_Spark:__ the datasets used in StreamingKNORA_Spark experiments<br />
__./StreamingKNORA_Single:__ A Java implementation for batch size selection and  baseline single thread<br />
__./StreamingKNORA_Spark:__ A Spark implementation for performance evaluation on Spark <br />
