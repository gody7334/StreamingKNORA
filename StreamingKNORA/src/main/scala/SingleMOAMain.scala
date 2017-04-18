package main.scala

import kafka.serializer.StringDecoder
import org.apache.spark.streaming.dstream.DStream
import main.java.Entity.LearningModel
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import main.scala.Utils.Timers
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import scala.util.control.Breaks._
import main.scala.Utils.StreamingKNORA
import main.scala.Utils.StreamingModel
import main.scala.Utils.Constant
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, LogManager, PropertyConfigurator}
import weka.core.converters.InstanceMaker
import moa.streams.ArffFileReader
import moa.classifiers.trees.HoeffdingTree


object SingleMOAMain {
   def main(args: Array[String]): Unit = {

   if (args.length < 2) {
     System.err.println(s"""
      |Usage:  
    var Dataset_HDFS:String = args.apply(0)
    var Dataset_header:String = args.apply(1) 
      """.stripMargin)
      System.exit(1)
   }
    
     
    var Dataset_HDFS:String = args.apply(0).split(":").apply(1)
    var Dataset_header:String = args.apply(1).split(":").apply(1) 
    
    var logger = LogManager.getLogger("myLogger")
    logger.info("Dataset_HDFS: "+Dataset_HDFS)
    logger.info("Dataset_header: "+Dataset_header)
    
    
    println("Dataset_HDFS: "+Dataset_HDFS)
    println("Dataset_header: "+Dataset_header)
    
    
    //Set Spark context variable
    val sc = new SparkContext(new SparkConf().setAppName("Spark KNORA"))
    StreamingKNORA.setSparkContext(sc)
    sc.setLocalProperty("spark.shuffle.compress", "true")
    sc.setLocalProperty("spark.shuffle.spill.compress", "true")
    sc.setLocalProperty("spark.eventLog.compress", "true")
    sc.setLocalProperty("spark.broadcast.compress", "true")
    sc.setLocalProperty("spark.io.compression.codec", "org.apache.spark.io.SnappyCompressionCodec")
    sc.setLocalProperty("spark.io.compression.snappy.blockSize", "16k")
    sc.setLocalProperty("spark.rdd.compress", "true")
    sc.setLocalProperty("spark.driver.memory", "2g")
    sc.setLocalProperty("spark.eventLog.enabled", "true")
//    sc.setLocalProperty("spark.default.parallelism", Constant.num_Models.toString())
     var timer = new Timers("singleMOA")
     timer.time{
     var trainData = sc.textFile(Dataset_HDFS)
     
     var tdataset = trainData.collect()

       var arff = new ArffFileReader()
       var arffStream = arff.ArffRead(Dataset_header)
       var instanceMaker = new InstanceMaker()
       var instances = arffStream.instances
       
       var learner = new HoeffdingTree()
       learner.setModelContext(arffStream.getHeader())
       learner.prepareForUse()

       
       for(i <- 0 until tdataset.length){
        var strLine = tdataset.apply(i)
        var common = strLine.indexOf(',')
        var serialNum = strLine.substring(0, common).toLong
        var instString = strLine.substring(common+1)
    	  var inst = instanceMaker.convertToInstance(instString, instances)
    	  learner.getVotesForInstance(inst)
    	  learner.trainOnInstance(inst)
       }
     }
 
          
   }
   
}