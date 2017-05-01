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


object BatchMain {
   def main(args: Array[String]): Unit = {

   // get argument
   if (args.length < 14) {
     System.err.println(s"""
      |Usage:
    var Dataset_HDFS:String = args.apply(0)
    var Dataset_header:String = args.apply(1)
    var num_Models: Int = args.apply(2).toInt
    var ModelType:String = args.apply(3)
    var num_validate:Int = args.apply(4).toInt
    var num_neighbour:Int = args.apply(5).toInt
    var isIntersect:Boolean = args.apply(6).toBoolean
    var num_train_batch:Int = args.apply(7).toInt
    var num_validate_batch:Int = args.apply(8).toInt
    var num_test_batch:Int = args.apply(9).toInt
    var num_val_test_repartition:Int = args.apply(10).toInt
    var max_tree_depth:Int = args.apply(11).toInt
    var numOfData:Int = args.apply(12).split(":").apply(1).toInt
      """.stripMargin)
      System.exit(1)
    }

    var Dataset_HDFS:String = args.apply(0).split(":").apply(1)
    var Dataset_header:String = args.apply(1).split(":").apply(1)
    var num_Models:Int = args.apply(2).split(":").apply(1).toInt
    var ModelType:String = args.apply(3).split(":").apply(1)
    var num_validate:Int = args.apply(4).split(":").apply(1).toInt
    var num_neighbour:Int = args.apply(5).split(":").apply(1).toInt
    var isIntersect:Boolean = args.apply(6).split(":").apply(1).toBoolean
    var num_train_batch:Int = args.apply(7).split(":").apply(1).toInt
    var num_validate_batch:Int = args.apply(8).split(":").apply(1).toInt
    var num_test_batch:Int = args.apply(9).split(":").apply(1).toInt
    var num_warmup_perModel:Int = args.apply(10).split(":").apply(1).toInt
    var num_val_test_repartition:Int = args.apply(11).split(":").apply(1).toInt
    var max_tree_depth:Int = args.apply(12).split(":").apply(1).toInt
    var numOfData:Int = args.apply(13).split(":").apply(1).toInt

    println("Dataset_HDFS: "+Dataset_HDFS)
    println("Dataset_header: "+Dataset_header)
    println("num_Models: "+num_Models)
    println("ModelType: "+ModelType)
    println("num_validate: "+num_validate)
    println("num_neighbour: "+num_neighbour)
    println("isIntersect: "+isIntersect)
    println("num_train_batch: "+num_train_batch)
    println("num_validate_batch: "+num_validate_batch)
    println("num_test_batch: "+num_test_batch)
    println("num_warmup_perModel: "+num_warmup_perModel)
    println("num_val_test_repartition: "+num_val_test_repartition)
    println("max_tree_depth: "+max_tree_depth)
    println("numOfData: "+numOfData)
    println("Complie Test")

    //Set Streaming KNORA variables
    var streamingKNORA = new StreamingKNORA()
    streamingKNORA.setNumModel(num_Models)
    streamingKNORA.setModelType(ModelType)
    streamingKNORA.setNumValidate(num_validate)
    streamingKNORA.setNumNeighbour(num_neighbour)
    streamingKNORA.setIfIntersect(isIntersect)
    streamingKNORA.setMaxTreeDepth(max_tree_depth)
    Constant.instance_header_path = Dataset_header

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

     var trainData = sc.textFile(Dataset_HDFS)

     var SD = trainData.map( x => convertoinstance(x) ).cache()
     trainData.unpersist()


     var timer_onTrainFilter = new Timers("onTrainFilter, ")
     var timer_onTrain = new Timers("onTrain, ")
     var timer_onValidateFilter = new Timers("onValidateFilter, ")
     var timer_onValidate = new Timers("onValidate, ")
     var timer_onTestFilter = new Timers("onTestFilter, ")
     var timer_onTest = new Timers("onTest, ")

//     var numOfData = SD.count()
     var RDDIndex = 0
     val TrainN = num_train_batch
     val ValidateN = num_validate_batch
     val TestN = num_test_batch
     var TrainIndex = 0;
     //TODO: Under Streaming, Need to merge unused data into new data

     breakable { while(RDDIndex < numOfData){
         if(RDDIndex+TrainN > numOfData)
           break;
         var train:RDD[String] = null
         timer_onTrain.time{
           train = SD.filter( x => x._1 >= RDDIndex).filter(x => x._1 < RDDIndex + TrainN).sortByKey(true, num_Models).map(x=>x._2).cache()
           train.count()
           RDDIndex += TrainN
           TrainIndex = RDDIndex
           streamingKNORA.onTrain(train)
         }
         train.unpersist()

         if(RDDIndex > numOfData-1)
           break;
         RDDIndex -= ValidateN
         var validate:RDD[String] = null
         timer_onValidate.time{
           validate = SD.filter(x => x._1 >= RDDIndex).filter(x => x._1 < RDDIndex + ValidateN).repartition(num_val_test_repartition).map(x=>x._2).cache()
           validate.count()
           RDDIndex += ValidateN
           streamingKNORA.onValidate(validate)
         }
         validate.unpersist()

         if(RDDIndex+TestN > numOfData)
           break;
         if(RDDIndex > num_warmup_perModel*num_Models){
           var test:RDD[String] = null
           timer_onTest.time{
             test = SD.filter(x => x._1 >= RDDIndex).filter(x => x._1 < RDDIndex + TestN).repartition(num_val_test_repartition).map(x=>x._2).cache()
             test.count()
             RDDIndex += TestN
             RDDIndex = TrainIndex
             streamingKNORA.onPredict(test)
           }
           test.unpersist()
         }
       }
     }
     SD.unpersist()

     timer_onTrain.printAverage()
     timer_onValidate.printAverage()
     timer_onTest.printAverage()

     println("accuracy: "+streamingKNORA.accuracy)
    //  Thread.sleep(86400000);

   }

   //Transfer into (serial,rowData)
   def convertoinstance(line: String): (Long,String) = {
    var common = line.indexOf(',')
    var serialNum = line.substring(0, common).toLong
    var instString = line
	  return (serialNum,instString)
  }

}
