package com.MOA.main;

import moa.classifiers.Classifier;

import com.MOA.main.PipeLine.KNORAPipeline;
import com.MOA.main.PipeLine.MVPipeline;
import com.MOA.moa.classifiers.DecisionStumpTutorial;
import moa.evaluation.BasicClassificationPerformanceEvaluator;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.streams.generators.RandomRBFGeneratorDrift;
import moa.streams.generators.RandomTreeGenerator;
import moa.tasks.EvaluateModel;
import moa.tasks.LearnModel;

public class MainKNORA {
	
	public static void main(String[] args) {
	
		int num_models = Integer.parseInt(args[0].split(":")[1]);
		int num_insts = Integer.parseInt(args[1].split(":")[1]);
		int num_warmups = Integer.parseInt(args[2].split(":")[1]);
		int num_Val = Integer.parseInt(args[3].split(":")[1]);
		int num_neighbour = Integer.parseInt(args[4].split(":")[1]);
		int intersect = Integer.parseInt(args[5].split(":")[1]);
		int train_batch_size = Integer.parseInt(args[6].split(":")[1]);
		int validate_batch_size = Integer.parseInt(args[7].split(":")[1]);
		int test_batch_size = Integer.parseInt(args[8].split(":")[1]);
		String dataset = args[9].split(":")[1];
		String classifier = args[10].split(":")[1];
		
		boolean if_intersect = true;
		if(intersect == 0)
			if_intersect = false;
		
		long startTime = System.nanoTime();
			KNORAPipeline knorap = new KNORAPipeline(
				num_models, 
				num_insts, 
				num_warmups, 
				num_Val, 
				num_neighbour, 
				if_intersect, 
				train_batch_size, 
				validate_batch_size, 
				test_batch_size,
				dataset,
				classifier);
			knorap.KNORAPipeline_start();
		long stopTime = System.nanoTime();
		System.out.println((stopTime - startTime)*(1e-9));
	}
}
