package main.scala.Utils

import java.util.Arrays;

import org.apache.spark.streaming.dstream.DStream
import main.java.Entity.LearningModel
import main.java.Entity.PredictResult
import main.java.Entity.ValidateInstance
import main.java.Utils.MajorityVote
import moa.classifiers.trees.HoeffdingTree
import moa.classifiers.trees.ASHoeffdingTree
import weka.core.converters.InstanceMaker
import org.apache.spark.rdd.RDD
import weka.core.Instances
import weka.core.Instance
import moa.streams.ArffFileReader
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import main.java.Utils.KNORA;
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import main.scala.Utils.Constant
import main.scala.MachineLearning
import main.scala.Utils.Timers
import com.madhukaraphatak.sizeof.SizeEstimator
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.broadcast.Broadcast
import moa.options.ClassOption;
import moa.options.FlagOption;
import moa.options.FloatOption;
import moa.options.IntOption;
import org.apache.log4j.{Level, LogManager, PropertyConfigurator}

class StreamingKNORA extends MachineLearning with Serializable{

  var models: Array[LearningModel] = _
  var ValidateList: Array[ValidateInstance] = _
  var validateList_index = 0
  var num_Models: Int = _
  var num_validate: Int = _
  var ModelType: String = _
  var isInitial: Boolean = false
  var instances: Instances = _
  var instanceMaker: InstanceMaker = _
  var num_classes: Int = _
  var num_neighbour: Int = _
  var intersect: Boolean = _
  var knora: KNORA = _
  var instance_header_path = "./File/RRBF_1M_H.arff"
  var num_testInstance: Int = 0
  var num_testCorrectInst: Int = 0
  var accuracy:Double = 0.0
  var maxTreeDepth:Int = _

  def setNumModel(num_Models: Int) = {
    this.num_Models = num_Models
  }

  def setModelType(ModelType: String) = {
    this.ModelType = ModelType
  }

  def setNumClasses(num_classes: Int) = {
    this.num_classes = num_classes
  }

  def setNumValidate(num_validate: Int) = {
    this.num_validate = num_validate
  }

  def setNumNeighbour(num_neighbour: Int) = {
    this.num_neighbour = num_neighbour
  }

  def setIfIntersect(intersect: Boolean) = {
    this.intersect = intersect
  }

  def setMaxTreeDepth(max_tree_depth: Int) = {
    this.maxTreeDepth = max_tree_depth
  }

  def onTrain(data: RDD[String]) = {
    TrainOnInstances(data)
  }

  def onTrain(data: DStream[String]) = {
    data.foreachRDD { rdd => TrainOnInstances(rdd)}
  }

  def onValidate(data: RDD[String]) = {
    ValidateOnInstances_KNORA(data)
  }

  def onValidate(data: DStream[String]) = {

  }

  def onPredict(data: RDD[String]) = {
//    PredictOnInstances_MV(data)
    PredictOnInstances_KNORA(data)
  }

  def onPredict(data: DStream[String]) = {

  }

  def convertoinstance(line: String): (Long,Instance) = {
    var common = line.indexOf(',')
    var serialNum = line.substring(0, common).toLong
    var instString = line.substring(line.indexOf(",")+1)
	  var inst = instanceMaker.convertToInstance(instString, instances)
	  return (serialNum,inst)
  }

  def setInitialStreamingLearningModel( instance_header_path: String) = {

    var arff = new ArffFileReader()
    val whereami = System.getProperty("user.dir")
    System.out.println(whereami)

    var arffStream = arff.ArffRead(instance_header_path)
    instanceMaker = new InstanceMaker()
    instances = arffStream.instances

    this.models = new Array[LearningModel](num_Models)
    this.ValidateList = new Array[ValidateInstance](num_validate)
    this.knora = new KNORA(num_neighbour, intersect)

    //!! learner_number 1:n
    for( i <- 1 to num_Models){
      var LM = initialModel()
      LM.learner.setModelContext(arffStream.getHeader())
      LM.learner.prepareForUse()
      LM.learner_num = i
      models.update(i-1, LM)
    }

    println("Number of models: " +models.length)
    System.out.println("Number of classes: " + instances.numClasses())
    System.out.println("Inintial Learning Model Done!!!")
  }

  def initialModel(): LearningModel = {

      ModelType match{
      case StreamingModel.HoeffdingTree =>{
        var model = new HoeffdingTree()
        var LM = new LearningModel(model, 0)
        return LM
      }
      case StreamingModel.ASHoeffdingTree =>{
        var model = new ASHoeffdingTree()
        model.setMaxSize(20)
        model.setResetTree()
        var LM = new LearningModel(model, 0)
        return LM
      }
    }
  }

  def TrainOnInstances(data: RDD[String]) = {
    if(isInitial == false){
      new Timers("Initial Learning Model, ").time{
      setInitialStreamingLearningModel(Constant.instance_header_path)
      isInitial = true
      }
    }

    var broadcastModels:Broadcast[Array[LearningModel]] = null
    broadcastModels = StreamingKNORA.sc.broadcast(models)

    var update_Models:RDD[LearningModel] = null

    //** train models on instances on Each partition
      update_Models = data.mapPartitionsWithIndex((index, x) => TrainOnInstancesTransform(index, x, broadcastModels))
      update_Models.cache()
      models = update_Models.collect()

    update_Models.unpersist()
    broadcastModels.destroy()
  }

  def TrainOnInstancesTransform(key: Long, line: Iterator[String], broadcastModels:Broadcast[Array[LearningModel]]): Iterator[LearningModel] = {

    //find corresponding learning model
    var ML:LearningModel = null
    var num_models = broadcastModels.value.length
    for(i <- 0 until num_models){
       //!! learner_number=1:n, key=0:n-1
      var MLtemp =broadcastModels.value.apply(i)
      if(MLtemp.learner_num-1 == key)
        ML = MLtemp
    }

    //Convert to Instance and Train on instance
    //Utilize the CPU cache, extremely fast
    while(line.hasNext){
      var strLine = line.next()
      var common = strLine.indexOf(',')
      var serialNum = strLine.substring(0, common).toLong
      var instString = strLine.substring(common+1)
  	  var inst = instanceMaker.convertToInstance(instString, instances)
  	  ML.learner.trainOnInstance(inst)
    }

    var ItorModels: Iterator[LearningModel] = Iterator(ML)
    return ItorModels
  }

  def ValidateOnInstances_KNORA(data: RDD[String]) = {

    var broadcastModels:Broadcast[Array[LearningModel]] = null
    broadcastModels = StreamingKNORA.sc.broadcast(models)

    //for each instance, validate on every model and record the model which correct predict the instance
    var validateresult:RDD[ValidateInstance] = null
    var VRArray:Array[ValidateInstance] = null
    validateresult = data.map(line => ValidateOnInstances_KNORA_Transformation(line, broadcastModels))
    validateresult.cache()
        VRArray = validateresult.collect()

    //Assign validate data into ValidateList
    for(i <- 0 until VRArray.length){
      ValidateList.update(validateList_index, VRArray.apply(i))
      validateList_index+=1
      if(validateList_index>=ValidateList.length){
        validateList_index = 0
      }
    }

    StreamingKNORA.broadcastModels = broadcastModels
    broadcastModels.destroy()
    validateresult.unpersist()
  }

  def ValidateOnInstances_KNORA_Transformation(line:String, broadcastModels:Broadcast[Array[LearningModel]]): ValidateInstance = {

    var common = line.indexOf(',')
    var serialNum = line.substring(0, common).toLong
    var instString = line.substring(common+1)
	  var inst = instanceMaker.convertToInstance(instString, instances)

    var v = new ValidateInstance();
    v.Validate_Inst = inst;
    var num_model = broadcastModels.value.length
    for(i <- 0 until num_model){
      var model = broadcastModels.value.apply(i)
		  if(model.learner.correctlyClassifies(inst)){
				   v.list_positive_learner_num.add(model.learner_num);
		  }
		}

    return v
  }

  def PredictOnInstances_MV(data: RDD[String]) = {

    var broadcastModels:Broadcast[Array[LearningModel]] = null
    broadcastModels = StreamingKNORA.sc.broadcast(models)

    //for each instance, predict on every model and do majority vote
    var result:RDD[Boolean] = null
    result = data.map(line => PredictOnInstances_MV_Transformation(line, broadcastModels))

    //Calculate Accuracy
    var num_insts = data.count()
    this.num_testInstance += num_insts.toInt
    this.num_testCorrectInst += result.filter(x => x == true).count.toInt
    this.accuracy = num_testCorrectInst.toDouble / num_testInstance.toDouble
    println("Accuracy: "+ this.accuracy)

    broadcastModels.destroy()
  }

  def PredictOnInstances_MV_Transformation(line:String, broadcastModels:Broadcast[Array[LearningModel]]): Boolean = {

    var common = line.indexOf(',')
    var serialNum = line.substring(0, common).toLong
    var instString = line.substring(common+1)
	  var inst = instanceMaker.convertToInstance(instString, instances)

    //Get predict class (getVotesForInstance)
    var num_model = broadcastModels.value.length
    var PredictResultArray = new Array[PredictResult](num_model)
    for(i <- 0 until num_model){
      var ML = broadcastModels.value.apply(i)
      var result = ML.learner.getVotesForInstance(inst)
      var predict_result = new PredictResult(ML.learner_num, result);
      PredictResultArray.update(i, predict_result)
    }

    //Majority vote
    var predictClassIdx = MajorityVote.Vote(PredictResultArray, instances.numClasses())
    var trueClassIdx = inst.classValue().toInt
    if(predictClassIdx == trueClassIdx){
      return true
		}
    else{
      return false
    }
  }

  def PredictOnInstances_KNORA(data: RDD[String]) = {

    var broadcastModels:Broadcast[Array[LearningModel]] = null
    var broadcastValidate: Broadcast[Array[ValidateInstance]] = null

    broadcastModels = StreamingKNORA.sc.broadcast(models)
    broadcastValidate = StreamingKNORA.sc.broadcast(ValidateList)

    //for each instance, predict on every model and do KNORA & majority vote
    var result:RDD[(Double, Double)] = null

    result = data.map{line => PredictOnInstances_KNORA_Transformation(line, broadcastModels, broadcastValidate)}.cache()

    var num_insts = data.count()
    this.num_testInstance += num_insts.toInt
    this.num_testCorrectInst += result.filter(x => x._1 == x._2).count.toInt
    this.accuracy = num_testCorrectInst.toDouble / num_testInstance.toDouble
    var logger = LogManager.getLogger("myLogger")
    logger.info("Accuracy: " + this.accuracy)
    logger.info("Accuracy: " +  result.filter(x => x._1 == x._2).count.toInt)
    println("Accuracy: "+result.filter(x => x._1 == x._2).count.toInt)

    result.unpersist()
    broadcastModels.destroy()
    broadcastValidate.destroy()

    if(ModelType == StreamingModel.HoeffdingTree){
      for(i <- 0 until models.length){
        if(models.apply(i).learner.asInstanceOf[HoeffdingTree].measureTreeDepth()>maxTreeDepth){
          models.apply(i).learner.asInstanceOf[HoeffdingTree].resetLearning()
        }
      }
    }
  }

  def PredictOnInstances_KNORA_Transformation(line:String, broadcastModels:Broadcast[Array[LearningModel]], broadcastValidate:Broadcast[Array[ValidateInstance]]): (Double, Double) = {

    var common = line.indexOf(',')
    var serialNum = line.substring(0, common).toLong
    var instString = line.substring(common+1)
    var inst: Instance = null

	  inst = instanceMaker.convertToInstance(instString, instances)

    //Get predict class (getVotesForInstance)
    var num_model = broadcastModels.value.length

    //Find KNN instances' classifiers which correctly predict the instance(inst._2)
    var Intesect_classifier_number:Array[Integer]=null
    Intesect_classifier_number =knora.findKNNValidateInstances(broadcastValidate.value, inst, instances);

    //Predict on Intersect/Union Models
    var num_interset = Intesect_classifier_number.length
    var interset_PredictResult_Array = new Array[PredictResult](num_interset)
    for(i <- 0 until num_model){
      for(j <- 0 until num_interset){
        var ML = broadcastModels.value.apply(i)
        if(ML.learner_num == Intesect_classifier_number.apply(j)){
          var result = ML.learner.getVotesForInstance(inst)
          var predict_result = new PredictResult(ML.learner_num, result);
          interset_PredictResult_Array.update(j, predict_result)
        }
      }
    }

    //If no intesect, use all classifier (MV)
    if(num_interset == 0){
      var PredictResultArray = new Array[PredictResult](num_model)
      for(i <- 0 until num_model){
        var ML = broadcastModels.value.apply(i)
        var result = ML.learner.getVotesForInstance(inst)
        var predict_result = new PredictResult(ML.learner_num, result);
        PredictResultArray.update(i, predict_result)
      }
      interset_PredictResult_Array = PredictResultArray
    }

    //Do Majority vote on intesect classifier
    var predictClassIdx:Int = 0
    predictClassIdx = MajorityVote.Vote(interset_PredictResult_Array, instances.numClasses())
    return (inst.classValue(), predictClassIdx.toDouble)

  }

  def triggerbroadcast(line:String, broadcastModels:Broadcast[Array[LearningModel]], broadcastValidate:Broadcast[Array[ValidateInstance]])={
    line
  }
}

object StreamingKNORA {
  var sc: SparkContext = _
  var broadcastModels: Broadcast[Array[LearningModel]] = _
//  var broadcastValidateList: Broadcast[Array[ValidateInstance]] = _

  def setSparkContext(sc: SparkContext) = {
    this.sc = sc
  }
}
