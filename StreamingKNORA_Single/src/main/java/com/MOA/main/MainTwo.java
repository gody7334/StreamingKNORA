package com.MOA.main;

import moa.classifiers.Classifier;
import com.MOA.moa.classifiers.PreviousClassClassifier;
import moa.evaluation.LearningCurve;
import moa.evaluation.WindowClassificationPerformanceEvaluator;
import moa.streams.ArffFileStream;
import moa.tasks.EvaluatePrequential;

public class MainTwo {

	/**
	 * Main class for PreviousClassClassifier
	 * @param args
	 */
	private static final String DEFAULT_INPUT_FILE="elecNormNew.arff";

	public static void main(String[] args) {

		//prepare classifier
		Classifier prevClassClasifier = new PreviousClassClassifier();
		
		//prepare input file for streaming evaluation
		String arffFilePath = null;
		if ((args == null) || (args.length < 1)) {
//			arffFilePath = System.getenv("HOME") + "/gody7334/workspace/mvn/MOA/Files/" + DEFAULT_INPUT_FILE;
			arffFilePath = "/home/gody7334/workspace/mvn/MOA/Files/" + DEFAULT_INPUT_FILE;
		} else {
			arffFilePath = args[0];
		}
		ArffFileStream electricityArff = null;
		try {
			electricityArff = new ArffFileStream(arffFilePath, -1);
			electricityArff.prepareForUse();
		} catch (Exception e) {
			System.out.println("Problem with loading arff file. Quit the program");
			System.exit(-1);
		}
		
		//prepare classification performance evaluator
		WindowClassificationPerformanceEvaluator windowClassEvaluator = 
				new WindowClassificationPerformanceEvaluator();
		windowClassEvaluator.widthOption.setValue(1000);
		windowClassEvaluator.prepareForUse();
		
		//set EvaluatePrequential's parameter
		int maxInstances = 1000000;
		int timeLimit = -1;
		int sampleFrequencyOption = 1000;
		
		//do the learning and checking using evaluate-prequential technique
		EvaluatePrequential ep = new EvaluatePrequential();
		ep.instanceLimitOption.setValue(maxInstances);
		ep.learnerOption.setCurrentObject(prevClassClasifier);
		ep.streamOption.setCurrentObject(electricityArff);
		ep.sampleFrequencyOption.setValue(sampleFrequencyOption);
		ep.timeLimitOption.setValue(timeLimit);
		ep.evaluatorOption.setCurrentObject(windowClassEvaluator);
		ep.prepareForUse();
		
		//do the task and get the result
		LearningCurve le = (LearningCurve) ep.doTask();
		System.out.println("Evaluate prequential using PreviousClassClassifier");
		System.out.println(le);
	}
}
