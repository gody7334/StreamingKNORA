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
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.log4j.{Level, LogManager, PropertyConfigurator}

object BatchMLlibMain {
  
  var num_testInstance:Int = 0
  var num_testCorrectInst:Int = 0
  var accuracy = 0.0
  
  //Transfer into (serial,rowData)
   def convertoinstance(line: String): (Long, String) = {
    var common = line.indexOf(',')
    var serialNum = line.substring(0, common).toLong
//    var partitionNum = serialNum/64
    var instString = line
	  return (serialNum,instString)
  }
  
  def LibsvmConvertoVector(line: String): LabeledPoint = {
    var common = line.indexOf(',')
    var serialNum = line.substring(0, common).toLong
    var instString = line.substring(line.indexOf(",")+1)
    
     var parsed_line = instString.split(" ")
     var label = parsed_line(0).toDouble
     var indicesarray = parsed_line.slice(1, parsed_line.length).map { x => x.split(":").apply(0).toInt }
     var doublearray = parsed_line.slice(1, parsed_line.length).map { x => x.split(":").apply(1).toDouble }
     
//     var doublearray = parsed_line.slice(0,parsed_line.length-1).map(_.toDouble)
//     var indicesarray = Array(0,1,2,3,4,5,6,7,8,9)
//     var label =  parsed_line(parsed_line.length-1).charAt(5).toInt.toDouble-49.0
//     var point = LabeledPoint(label,Vectors.sparse(10, indicesarray, doublearray))
    
     var point = LabeledPoint(label, Vectors.sparse(100, indicesarray, doublearray))
     return point
  }
  
  def convertoVector(line: String): LabeledPoint = {
    var common = line.indexOf(',')
    var serialNum = line.substring(0, common).toLong
    var instString = line.substring(line.indexOf(",")+1)

     var parsed_line = instString.split(",")
     
     var doublearray = parsed_line.slice(0,parsed_line.length-1).map(_.toDouble)
//     var indicesarray = Array(0,1,2,3,4,5,6,7,8,9)
     var label =  parsed_line(parsed_line.length-1).charAt(5).toInt.toDouble-49.0
     
//     var point = LabeledPoint(label,Vectors.sparse(10, indicesarray, doublearray))
     var point = LabeledPoint(label, Vectors.dense(doublearray))
     return point
  }
   
   def onPredict(model:DecisionTreeModel, point:LabeledPoint): (Double, Double)={
     var prediction: Double = model.predict(point.features)
     return (point.label, prediction)
   }
  
   def main(args: Array[String]): Unit = {
     
    if (args.length < 8) {
    System.err.println(s"""
      |Usage:  
    var Dataset_HDFS:String = args.apply(0)
    var num_train_batch:Int = args.apply(1).toInt
    var num_test_batch:Int = args.apply(2).toInt
    var num_test_repartition:Int = args.apply(3).toInt
    var impurity:String = args.apply(4).split(":").apply(1)
    var maxDepth:Int = args.apply(5).split(":").apply(1).toInt
    var maxBin:Int = args.apply(6).split(":").apply(1).toInt
    var numOfData:Int = args.apply(7).split(":").apply(1).toInt
      """.stripMargin)
      System.exit(1)
    }
    
    var Dataset_HDFS:String = args.apply(0).split(":").apply(1)
    var num_train_batch:Int = args.apply(1).split(":").apply(1).toInt
    var num_test_batch:Int = args.apply(2).split(":").apply(1).toInt
    var num_test_repartition:Int = args.apply(3).split(":").apply(1).toInt
    var impurity:String = args.apply(4).split(":").apply(1)
    var maxDepth:Int = args.apply(5).split(":").apply(1).toInt
    var maxBins:Int = args.apply(6).split(":").apply(1).toInt
    var numOfData:Int = args.apply(7).split(":").apply(1).toInt
     
    var logger = LogManager.getLogger("myLogger")
    logger.info("Dataset_HDFS: "+Dataset_HDFS)
    logger.info("num_train_batch: "+num_train_batch)
    logger.info("num_test_batch: "+num_test_batch)
    logger.info("num_test_repartition: "+num_test_repartition)
    logger.info("impurity: "+impurity)
    logger.info("maxDepth: "+maxDepth)
    logger.info("maxBin: "+maxBins)
    logger.info("numOfData: "+numOfData)
    
    
    println("Dataset_HDFS: "+Dataset_HDFS)
    println("num_train_batch: "+num_train_batch)
    println("num_test_batch: "+num_test_batch)
    println("num_test_repartition: "+num_test_repartition)
    println("impurity: "+impurity)
    println("maxDepth: "+maxDepth)
    println("maxBin: "+maxBins)
    println("numOfData: "+numOfData)
    
     //Set Spark context variable
    val sc = new SparkContext(new SparkConf().setAppName("Spark KNORA"))
    sc.setLocalProperty("spark.shuffle.compress", "true")
    sc.setLocalProperty("spark.shuffle.spill.compress", "true")
    sc.setLocalProperty("spark.eventLog.compress", "true")
    sc.setLocalProperty("spark.broadcast.compress", "true")
    sc.setLocalProperty("spark.io.compression.codec", "org.apache.spark.io.SnappyCompressionCodec")
    sc.setLocalProperty("spark.io.compression.snappy.blockSize", "16k")
    sc.setLocalProperty("spark.rdd.compress", "true")
    sc.setLocalProperty("spark.driver.memory", "2g")
    sc.setLocalProperty("spark.eventLog.enabled", "true")
//    sc.setLocalProperty("spark.default.parallelism", "8")
    
     var trainData = sc.textFile(Dataset_HDFS)
     
     var SD = trainData.map( x => convertoinstance(x) ).cache()
     trainData.unpersist()
     
//     var numOfData = SD.count()
     var RDDIndex = 0
     val TrainN = num_train_batch
     val TestN = num_test_batch
     val repartitionSize = num_test_repartition
     var TrainIndex = 0;
     
      val numClasses = 10
      val categoricalFeaturesInfo = Map[Int, Int]()
//      val impurity = "gini"
//      val maxDepth = 10
//      val maxBins = 32
      
     var timer_onTrainFilter = new Timers("onTrainFilter, ")
     var timer_onTrain = new Timers("onTrain, ")
     var timer_onTestFilter = new Timers("onTestFilter, ")
     var timer_onTest = new Timers("onTest, ")

    var model:DecisionTreeModel = null
     
     //TODO: Under Streaming, Need to merge unused data into new data
     breakable { while(RDDIndex < numOfData){
         if(RDDIndex+TrainN > numOfData-1)
           break;
         var train:RDD[String] = null
         timer_onTrainFilter.time{
         train = SD.filter( x => x._1 >= RDDIndex).filter(x => x._1 < RDDIndex + TrainN).sortByKey(true, repartitionSize).map(x=>x._2).cache()
         train.count()
         }
         RDDIndex += TrainN
         TrainIndex = RDDIndex
         var rdd_data: RDD[LabeledPoint] = null
         timer_onTrain.time{
         rdd_data = train.map(line =>LibsvmConvertoVector(line))         
         model = DecisionTree.trainClassifier(rdd_data, numClasses, categoricalFeaturesInfo, impurity, maxDepth, maxBins)
         model.numNodes
         }
         train.unpersist()
         
         
         if(RDDIndex+TestN > numOfData-1)
           break;
         var test:RDD[String] = null
         timer_onTestFilter.time{
         test = SD.filter(x => x._1 >= RDDIndex).filter(x => x._1 < RDDIndex + TestN).repartition(repartitionSize).map(x=>x._2).cache()
         test.count()
         }
         RDDIndex += TestN
         RDDIndex = TrainIndex
         var labelAndPreds:RDD[(Double, Double)] = null
         var testData: RDD[LabeledPoint] = null
         timer_onTest.time{
           testData = test.map(line =>LibsvmConvertoVector(line))
           labelAndPreds = testData.map { point => onPredict(model,point)}.cache()
            num_testInstance += testData.count().toInt
            num_testCorrectInst += labelAndPreds.filter(r => r._1 == r._2).count().toInt
            accuracy = num_testCorrectInst.toDouble / num_testInstance.toDouble
            var logger = LogManager.getLogger("myLogger")
            logger.info("Accuracy: " + accuracy)
         }
         test.unpersist()
         
       }
    }

     SD.unpersist()   
     
     timer_onTrainFilter.printAverage()
     timer_onTrain.printAverage()
     timer_onTestFilter.printAverage()
     timer_onTest.printAverage()
     println("accuracy: "+accuracy)
     logger.info("accuracy: "+accuracy)
     logger.info("")
     
//    Thread.sleep(86400000);
    
    
   }
}