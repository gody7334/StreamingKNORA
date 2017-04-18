package com.MOA.main;

import moa.classifiers.Classifier;
import com.MOA.moa.classifiers.DecisionStumpTutorial;
import moa.evaluation.BasicClassificationPerformanceEvaluator;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.streams.generators.RandomRBFGeneratorDrift;
import moa.streams.generators.RandomTreeGenerator;
import moa.tasks.EvaluateModel;
import moa.tasks.LearnModel;

public class Main {

	/**
	 * Main class for Decision Stump Tutorial
	 * @param args
	 */
	public static void main(String[] args) {
		//Learning on RandomTreeGeneratorStream
		//Evaluation on Random RBF Generator Drift with speed change of 0.001
		//Using DecisionStumpTutorial to learn the model
		
		Classifier learner = new DecisionStumpTutorial();
		
		//1st stream for learning the model, Random tree generator stream
		RandomTreeGenerator rtStream = new RandomTreeGenerator();
		rtStream.prepareForUse();
		
		//instantiate necessary variables for learning the model
		int maxInstances = 1000000;
		int numPasses = 1;
		LearnModel lm = new LearnModel(learner, rtStream, maxInstances, numPasses);
		Object resultingModel = lm.doTask();

		//2nd stream for evaluation, Random RBF  with speed change 0.001
		double speedChange = 0.001;
		RandomRBFGeneratorDrift rbfDriftStream = 
				new RandomRBFGeneratorDrift();
		rbfDriftStream.speedChangeOption.setValue(speedChange);
		rbfDriftStream.prepareForUse();
		
		//Prepare and start the evaluation
		ClassificationPerformanceEvaluator evaluator = 
				new BasicClassificationPerformanceEvaluator();
		EvaluateModel em = new EvaluateModel();
		em.modelOption.setCurrentObject(resultingModel);
		em.streamOption.setCurrentObject(rbfDriftStream);
		em.maxInstancesOption.setValue(maxInstances);
		em.evaluatorOption.setCurrentObject(evaluator);
		Object resultingEvaluation = em.doTask();
		
		System.out.println("Learning on RandomTreeGenerator stream, Evaluation on " +
				"Random RBF Generator Drift with speed change of 0.001");
		System.out.println("Using DecisionStumpTutorial class");
		
		System.out.println(resultingEvaluation);
		
		System.out.println("Finished!");
	}

}
