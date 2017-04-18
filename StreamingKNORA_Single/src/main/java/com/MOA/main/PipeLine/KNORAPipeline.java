package com.MOA.main.PipeLine;

import moa.classifiers.Classifier;
import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.functions.SGDMultiClass;
import moa.classifiers.trees.HoeffdingTree;
import moa.classifiers.trees.ASHoeffdingTree;
import moa.core.InstancesHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.MOA.main.EnsembleMethod.KNORA;
import com.MOA.main.EnsembleMethod.MajorityVote;
import com.MOA.main.Entity.LearningModel;
import com.MOA.main.Entity.PredictResult;
import com.MOA.main.Entity.ValidateInstance;
import com.MOA.moa.classifiers.DecisionStumpTutorial;
import moa.evaluation.BasicClassificationPerformanceEvaluator;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.options.AbstractOptionHandler;
import moa.options.FloatOption;
import moa.options.IntOption;

import moa.streams.ArffFileStream;
import moa.streams.generators.RandomRBFGenerator;
import moa.streams.generators.RandomRBFGeneratorDrift;
import moa.streams.generators.SEAGenerator;
import moa.streams.generators.RandomTreeGenerator;
import moa.streams.generators.HyperplaneGenerator;

import moa.tasks.EvaluateModel;
import moa.tasks.LearnModel;
import weka.core.Attribute;
import weka.core.Instance;

public class KNORAPipeline {
	int number_of_learning_models;
	int numInstances;
	int numWarmupInstance;
	int numValidation;
	public int N_neighbour;
	public boolean intersect;
	int train_batch_size;
	int validate_batch_size;
	int test_batch_size;
	String dataset;
	String classifier;

	int dataset_file = 16;
	int classifier_index = 4;
	int batch_size = 5000;
	int batch_temp = 0;

	public enum Stage{
		WARMUP, TRAIN, VALIDATE, TEST
	}

	public KNORAPipeline(
			int number_of_learning_models,
			int numInstances,
			int numWarmupInstance,
			int numValidation,
			int N_neighbour,
			boolean intersect,
			int train_batch_size,
			int validate_batch_size,
			int test_batch_size,
			String dataset,
			String classifier){
		this.number_of_learning_models = number_of_learning_models;
		this.numInstances = numInstances;
		this.numWarmupInstance = numWarmupInstance;
		this.numValidation = numValidation;
		this.N_neighbour = N_neighbour;
		this.intersect = intersect;
		this.train_batch_size = train_batch_size;
		this.validate_batch_size = validate_batch_size;
		this.test_batch_size = test_batch_size;
		this.dataset = dataset;
		this.classifier = classifier;
	}

	public void KNORAPipeline_start() {



	//local variable
	List<LearningModel> list_learning_models = new ArrayList<LearningModel>();
	List<PredictResult> list_predict_results = new ArrayList<PredictResult>();
	List<ValidateInstance> list_validate_instances = new ArrayList<ValidateInstance>();
	List<Instance> list_instance = new ArrayList<Instance>();
	 int numberTestSamplesCorrect =0;
	 int numberSamples =0;
	 int numberTestSamples =0;
	 int inst_index = 0;
	 int train_inst_index = 0;
	 int train_model_offset = 0;
	 boolean isValidate = true;
	Stage stage = Stage.WARMUP;
	KNORA knora = new KNORA(N_neighbour, intersect);

	String ARFFFilePath = dataset;
	ArffFileStream stream = null;
	switch(dataset){
		case "RRBFno":
			ARFFFilePath = "../Dataset_Single/" + "RRBF-no-10a-1m.arff";
			stream = new ArffFileStream(ARFFFilePath,11);
			break;
		case "RRBF0.0001":
			ARFFFilePath = "../Dataset_Single/" + "RRBF-0.0001-10a-1m.arff";
			stream = new ArffFileStream(ARFFFilePath,11);
			break;
		case "RRBF0.001":
			ARFFFilePath = "../Dataset_Single/" + "RRBF-0.001-10a-1m.arff";
			stream = new ArffFileStream(ARFFFilePath,11);
			break;
		case "HYP0.0001":
			ARFFFilePath = "../Dataset_Single/" + "HYP-0.0001-10a-1m.arff";
			stream = new ArffFileStream(ARFFFilePath,11);
			break;
		case "HYP0.001":
			ARFFFilePath = "../Dataset_Single/" + "HYP-0.001-10a-1m.arff";
			stream = new ArffFileStream(ARFFFilePath,11);
			break;
		case "Cover":
			ARFFFilePath = "../Dataset_Single/" + "covtypeNorm.arff";
			stream = new ArffFileStream(ARFFFilePath,55);
			break;
		case "Poker":
			ARFFFilePath = "../Dataset_Single/" + "poker-lsn.arff";
			stream = new ArffFileStream(ARFFFilePath,11);
			break;
	}

	stream.prepareForUse();

	for(int i = 0; i < number_of_learning_models; i++){
		Classifier learner = null;
		switch(classifier){
		case "HT":
			learner = new HoeffdingTree();
			break;
		case "NB":
			learner = new NaiveBayes();
			break;
		case "SGD":
			learner = new SGDMultiClass();
			break;
		case "AHT":
			learner = new ASHoeffdingTree();
			((ASHoeffdingTree) learner).setMaxSize(20);
			// ((ASHoeffdingTree) learner).setResetTree();
			break;
		}
		learner.setModelContext( stream.getHeader() ) ;
		learner.prepareForUse();
		LearningModel LM = new LearningModel(learner, i);
		list_learning_models.add(LM);
	}

	while (stream.hasMoreInstances()){
		list_instance.add(stream.nextInstance());
	}
	this.numInstances = list_instance.size();
	this.numWarmupInstance = (int)(this.numInstances * 0.01)*number_of_learning_models;

	System.out.println(" number_of_learning_models: " + number_of_learning_models);
	System.out.println(" numInstances: " + numInstances);
	System.out.println(" numWarmupInstance: " + numWarmupInstance);
	System.out.println(" numValidation: " + numValidation);
	System.out.println(" N_neighbour: " + N_neighbour);
	System.out.println(" intersect: " + intersect);
	System.out.println(" train_batch_size: " + train_batch_size);
	System.out.println(" validate_batch_size: " + validate_batch_size);
	System.out.println(" test_batch_size: " + test_batch_size);
	System.out.println(" dataset: " + dataset);
	System.out.println(" classifier: " + classifier);
	System.out.println(" dataset_file " + dataset_file);
	System.out.println(" classifier_index " + classifier_index);
	System.out.println(" batch_size " + batch_size);
	System.out.println("");


//	 while ( (stream.hasMoreInstances() && numberSamples < numInstances) ||  numberTestSamples < 100){
	while ( numberTestSamples < (377+batch_size) || numberSamples<numInstances){
		 if(inst_index >= numInstances)
			 inst_index = 0;

		 switch(stage){
			 case WARMUP:
				 if(numberSamples < numWarmupInstance){
					// System.out.println(" WARMUP train ");
					// System.out.println(" numberSamples " + numberSamples);
					 //Warm up train
					 // while(batch_temp < train_batch_size){
					 while(batch_temp < this.numInstances * 0.1){
						 for(int i = 0; i < number_of_learning_models; i++){
							 batch_temp++;
							 Instance trainInst = list_instance.get(inst_index++);
							 numberSamples++;
							 list_learning_models.get(i).learner.trainOnInstance(trainInst);
						 }
					 }
					 train_inst_index = inst_index;
					 batch_temp = 0;
					 //Warm up validate
					 // System.out.println(" WARMUP validate ");
					 // System.out.println(" numberSamples " + numberSamples);
					 if(isValidate){
						 while(batch_temp < validate_batch_size){
							 batch_temp++;
							 Instance valInst = list_instance.get(inst_index++);
							 ValidateInstance v = new ValidateInstance();
							 v.Validate_Inst = valInst;
							 numberSamples++;
							 for(int i = 0; i < number_of_learning_models; i++){
								 double[] result = list_learning_models.get(i).learner.getVotesForInstance(valInst);
								 if(list_learning_models.get(i).learner.correctlyClassifies(valInst))
									 v.list_positive_learner_num.add(i);
							 }
							 if(list_validate_instances.size()> numValidation)
								 list_validate_instances.remove(0);
							 list_validate_instances.add(v);
						 }
						 inst_index = train_inst_index;
						 batch_temp = 0;
					 }
				 }
				 else{
					stage = Stage.TRAIN;
				 }
				 break;
			 case TRAIN:
				 // System.out.println(" Train ");
				 while(batch_temp < train_batch_size){
					 for(int i = 0; i < number_of_learning_models; i++){
						 batch_temp++;
						 if(inst_index >= numInstances)
							 break;
						 Instance trainInst = list_instance.get(inst_index++);
						 numberSamples++;
						 list_learning_models.get((i + train_model_offset) % number_of_learning_models).learner.trainOnInstance(trainInst);
						 train_model_offset++;

					 }
				 }
				 train_inst_index = inst_index;
				 batch_temp = 0;
				 stage = Stage.VALIDATE;
				 break;
			 case VALIDATE:
				 // System.out.println(" Validate ");
				 if(isValidate){
					 while(batch_temp < validate_batch_size){
						 batch_temp++;
						if(inst_index >= numInstances)
							 break;
						 Instance valInst = list_instance.get(inst_index++);
						 ValidateInstance v = new ValidateInstance();
						 v.Validate_Inst = valInst;
						 numberSamples++;
						 for(int i = 0; i < number_of_learning_models; i++){
							 double[] result = list_learning_models.get(i).learner.getVotesForInstance(valInst);
							 if(list_learning_models.get(i).learner.correctlyClassifies(valInst)){
								 v.list_positive_learner_num.add(i);
							 }
						 }
//						 System.out.println("Correct Model Num: "+Arrays.toString(v.list_positive_learner_num.toArray()));
						 if(list_validate_instances.size() >= numValidation)
							 list_validate_instances.remove(0);
						 list_validate_instances.add(v);
					 }
					 batch_temp = 0;
				 }
				 stage = Stage.TEST;
				 break;
			 case TEST:
				 // System.out.println(" Testing ");
				 int testS = 0;
				 int testC = 0;
				 while(batch_temp < test_batch_size){
					 batch_temp++;
					 if(inst_index >= numInstances)
						 break;
					 Instance testInst = list_instance.get(inst_index++);
					 numberSamples++;
					 numberTestSamples++;
					 testS++;
					 for(int i = 0; i < number_of_learning_models; i++){
						 double[] result = list_learning_models.get(i).learner.getVotesForInstance(testInst);
						 PredictResult predict_result = new PredictResult(list_learning_models.get(i).learner_num, result);
						 list_predict_results.add(predict_result);
					 }

//					 for(int i = 0; i < list_validate_instances.size(); i++){
//						 System.out.println("V_inst_list: " + list_validate_instances.get(i).Validate_Inst + Arrays.toString(list_validate_instances.get(i).list_positive_learner_num.toArray()));
//					 }

					 Integer[] Intesect_result =knora.findKNNValidateInstances(list_validate_instances, testInst);
//					 System.out.println("Intersect Model Num: " + Arrays.toString(Intesect_result));

					 List<PredictResult> list_intersect_predict_results = new ArrayList<PredictResult>();
					 for(int i = 0; i < Intesect_result.length; i++){
						 for(int j = 0; j < list_predict_results.size(); j++){
							 if(list_predict_results.get(j).learner_num ==  Intesect_result[i])
								 list_intersect_predict_results.add(list_predict_results.get(j));
						 }
					 }
					 if(list_intersect_predict_results.size() == 0)
						 list_intersect_predict_results.addAll(list_predict_results);

					 int predictClassIdx = MajorityVote.Vote(list_intersect_predict_results, stream.getHeader().numClasses());

					 int trueClassIdx = (int) testInst.classValue();
					 if(predictClassIdx == trueClassIdx){
						 numberTestSamplesCorrect++;
						 testC++;
					 }
					 list_predict_results.clear();
				 }
				 inst_index = train_inst_index;
				 batch_temp = 0;
				 stage = Stage.TRAIN;
				 // System.out.println("Accu: "+100.0*(double)testC /(double)testS);
				 break;
			 default:
				break;

		 }

	 }

	 double accuracy = 100.0*(double)numberTestSamplesCorrect /(double)numberTestSamples ;
	 	System.out.println( number_of_learning_models + "," + numInstances + "," + numWarmupInstance + "," + numValidation + "," + N_neighbour + "," + intersect + "," +dataset_file+ "," + classifier_index+ "," +batch_size+ ": " +accuracy);
	 }
}
