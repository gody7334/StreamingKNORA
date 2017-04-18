package com.MOA.main;

import moa.classifiers.Classifier;

import com.MOA.main.PipeLine.MVPipeline;
import com.MOA.moa.classifiers.DecisionStumpTutorial;
import moa.evaluation.BasicClassificationPerformanceEvaluator;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.streams.generators.RandomRBFGeneratorDrift;
import moa.streams.generators.RandomTreeGenerator;
import moa.tasks.EvaluateModel;
import moa.tasks.LearnModel;

public class MainMV {

	
	public static void main(String[] args) {
		for(int i = 8; i <= 8; i++){
			MVPipeline mvp = new MVPipeline((int)Math.pow(2,i), (int)1.1e7, (int)1e6);
			mvp.MVPipeline_start();
		}
	}

}
