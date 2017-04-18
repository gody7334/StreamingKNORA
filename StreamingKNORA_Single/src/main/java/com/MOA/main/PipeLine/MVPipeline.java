package com.MOA.main.PipeLine;

import moa.classifiers.Classifier;
import moa.classifiers.trees.HoeffdingTree;
import moa.core.InstancesHeader;

import java.util.ArrayList;
import java.util.List;

import com.MOA.main.EnsembleMethod.MajorityVote;
import com.MOA.main.Entity.LearningModel;
import com.MOA.main.Entity.PredictResult;
import com.MOA.moa.classifiers.DecisionStumpTutorial;
import moa.evaluation.BasicClassificationPerformanceEvaluator;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.streams.generators.RandomRBFGenerator;
import moa.streams.generators.RandomRBFGeneratorDrift;
import moa.streams.generators.RandomTreeGenerator;
import moa.tasks.EvaluateModel;
import moa.tasks.LearnModel;
import weka.core.Attribute;
import weka.core.Instance;

public class MVPipeline {
	
	public int number_of_learning_models;
	public int numInstances;
	public int numWarmupInstance;
	
	public enum Stage{
		WARMUP, TRAIN, VALIDATE, TEST
	}
	
	public MVPipeline( int number_of_learning_models, int numInstances,	 int numWarmupInstance){
		this.number_of_learning_models = number_of_learning_models; //10~100;
		this.numInstances = numInstances;	//(int) 1.1e6;
		this.numWarmupInstance = numWarmupInstance; //(int) 1e5;
		
	}

	public void MVPipeline_start() {
	
		System.out.println(" number_of_learning_models " + number_of_learning_models);
		System.out.println(" numInstances " + numInstances);
		System.out.println(" numWarmupInstance " + numWarmupInstance);
	//local vairable
     int numberTestSamplesCorrect =0;
	 int numberSamples =0;
	 int numberTestSamples =0;
	 boolean isValidate = false;
	 Stage stage = Stage.WARMUP;
	 
	 List<LearningModel> list_learning_models = new ArrayList<LearningModel>();
	 List<PredictResult> list_predict_results = new ArrayList<PredictResult>();
		
	RandomRBFGenerator stream = new RandomRBFGenerator();
	stream.prepareForUse();
	
	for(int i = 0; i < number_of_learning_models; i++){
		Classifier learner = new HoeffdingTree();
		learner.setModelContext( stream.getHeader() ) ;
		learner.prepareForUse();
		LearningModel LM = new LearningModel(learner, i);
		list_learning_models.add(LM);
	}
		
	 
 
	 while ( stream.hasMoreInstances() && numberSamples < numInstances ){
		 switch(stage){
			 case WARMUP:
				 if(numberSamples < numWarmupInstance){
					 //Warm up train
					 for(int i = 0; i < number_of_learning_models; i++){
						 Instance trainInst = stream.nextInstance();
						 numberSamples++;
						 list_learning_models.get(i).learner.trainOnInstance(trainInst);
					 }
					 //Warm up validate
					 if(isValidate){
						 Instance valInst = stream.nextInstance();
						 numberSamples++;
						 for(int i = 0; i < number_of_learning_models; i++){
							 double[] result = list_learning_models.get(i).learner.getVotesForInstance(valInst);
						 }
					 }
				 }
				 else{
					stage = Stage.TRAIN; 
				 }
				 break;
			 case TRAIN:
				 for(int i = 0; i < number_of_learning_models; i++){
					 Instance trainInst = stream.nextInstance();
					 numberSamples++;
					 list_learning_models.get(i).learner.trainOnInstance(trainInst);
				 }
				 stage = Stage.VALIDATE;
				 break;
			 case VALIDATE:
				 if(isValidate){
					 Instance valInst = stream.nextInstance();
					 numberSamples++;
					 for(int i = 0; i < number_of_learning_models; i++){
						 double[] result = list_learning_models.get(i).learner.getVotesForInstance(valInst);
					 }
				 }
				 stage = Stage.TEST;
				 break;
			 case TEST:
				 Instance testInst = stream.nextInstance();
				 numberSamples++;
				 numberTestSamples++;
				 for(int i = 0; i < number_of_learning_models; i++){
					 double[] result = list_learning_models.get(i).learner.getVotesForInstance(testInst);
					 PredictResult predict_result = new PredictResult(list_learning_models.get(i).learner_num, result);
					 list_predict_results.add(predict_result);
				 }
				 
				 int predictClassIdx = MajorityVote.Vote(list_predict_results, stream.getHeader().numClasses());
				 int trueClassIdx = (int) testInst.classValue();
				 if(predictClassIdx == trueClassIdx){
					 numberTestSamplesCorrect++;
				 }		 
				 list_predict_results.clear();
				 stage = Stage.TRAIN;
				 break;
			 default:
				break;
		 	
		 }
		 
	 }
	 
	 double accuracy = 100.0*(double)numberTestSamplesCorrect /(double)numberTestSamples ;
	 System.out.println(numberSamples+ " instances processed with " + accuracy + "% accuracy") ;
	 }
}
