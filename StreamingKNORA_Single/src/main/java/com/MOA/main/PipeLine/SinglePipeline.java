package com.MOA.main.PipeLine;

import moa.classifiers.Classifier;
import moa.classifiers.trees.HoeffdingTree;
import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.functions.SGDMultiClass;
import moa.classifiers.functions.SGD;
import moa.classifiers.trees.ASHoeffdingTree;
import moa.classifiers.*;

import com.MOA.moa.classifiers.DecisionStumpTutorial;
import moa.evaluation.BasicClassificationPerformanceEvaluator;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.options.FloatOption;
import moa.streams.ArffFileStream;
import moa.streams.ArffFileStream2;
import moa.streams.generators.HyperplaneGenerator;
import moa.streams.generators.RandomRBFGenerator;
import moa.streams.generators.RandomRBFGeneratorDrift;
import moa.streams.generators.RandomTreeGenerator;
import moa.streams.generators.SEAGenerator;
import moa.tasks.EvaluateModel;
import moa.tasks.LearnModel;
import weka.core.Instance;
import weka.core.converters.CSVLoader;
import org.github.jamm.*;

public class SinglePipeline {

	public static void main(String[] args) {
	int numInstances = (int) 1.1e6;
	long startTime = System.nanoTime();
	Classifier learner = new HoeffdingTree();
//	Classifier learner = new ASHoeffdingTree();
//		((ASHoeffdingTree) learner).setMaxSize(20);
//	Classifier learner = new NaiveBayes();
//	SGDMultiClass learner = new SGDMultiClass();
//	Classifier learner = new SGD();
	
//	RandomRBFGenerator stream = new RandomRBFGenerator();
	
//	RandomRBFGeneratorDrift stream = new RandomRBFGeneratorDrift() ;
//	stream.speedChangeOption = new FloatOption("speedChange", 's', "Speed of change of centroids in the model.", 0.0001, 0, Float.MAX_VALUE);
	
//	SEAGenerator stream = new SEAGenerator() ;
	
//	HyperplaneGenerator stream = new HyperplaneGenerator() ;
//	stream.magChangeOption = new FloatOption("magChange", 't', "Magnitude of the change for every example", 0.0001, 0.0, 1.0);
	
//	RandomRBFGeneratorDrift stream = new RandomRBFGeneratorDrift() ;
//	stream.speedChangeOption.setValue(0.001); 
	
//	HyperplaneGenerator stream = new HyperplaneGenerator() ;
//	stream.magChangeOption.setValue(0.001);
	
//	RandomRBFGeneratorDrift stream = new RandomRBFGeneratorDrift() ;
//	stream.speedChangeOption.setValue(0.001);
//	stream.numCentroidsOption.setValue(10);
	
//	RandomRBFGeneratorDrift stream = new RandomRBFGeneratorDrift() ;
//	stream.speedChangeOption.setValue(0.0001);
//	stream.numCentroidsOption.setValue(10);
	
//	String ARFFFilePath = "/home/gody7334/Desktop/HDP_Share/HDP_Share/mvn/1.KNORA_MOA/Files/" + "IMDB-F.arff";
//	ArffFileStream stream = new ArffFileStream(ARFFFilePath,18);
//	String ARFFFilePath = "/home/gody7334/Desktop/HDP_Share/HDP_Share/mvn/1.KNORA_MOA/Files/" + "IMDB-D.arff";
//	ArffFileStream stream = new ArffFileStream(ARFFFilePath,1002);
//	String ARFFFilePath = "/home/gody7334/Desktop/HDP_Share/HDP_Share/mvn/1.KNORA_MOA/Files/" + "airlines.arff";
//	ArffFileStream stream = new ArffFileStream(ARFFFilePath,8);
//	String ARFFFilePath = "/home/gody7334/Desktop/HDP_Share/HDP_Share/mvn/1.KNORA_MOA/Files/" + "poker-lsn.arff";
//	ArffFileStream stream = new ArffFileStream(ARFFFilePath,11);
//	String ARFFFilePath = "/home/gody7334/Desktop/HDP_Share/HDP_Share/mvn/1.KNORA_MOA/Files/" + "covtypeNorm.arff";
//	ArffFileStream stream = new ArffFileStream(ARFFFilePath,55);
//	String ARFFFilePath = "/home/gody7334/Desktop/HDP_Share/HDP_Share/mvn/1.KNORA_MOA/Files/" + "elecNormNew.arff";
//	ArffFileStream stream = new ArffFileStream(ARFFFilePath,9);
	String ARFFFilePath = "/home/gody7334/Desktop/HDP_Share/HDP_Share/mvn/1.KNORA_MOA/Files/" + "covtypeNorm.arff";
	ArffFileStream stream = new ArffFileStream(ARFFFilePath,11);
	
	stream.prepareForUse();
	
	learner.setModelContext( stream.getHeader() ) ;
	learner.prepareForUse();
	
//	learner.setLearningRate(0.001);
	
	 int numberSamplesCorrect =0;
	 int numberSamplesCorrect_inteval = 0;
	 int numberSamples =0;
	 boolean isTesting = true;

	 while ( stream.hasMoreInstances() && numberSamples < numInstances ){
		 Instance trainInst = stream.nextInstance();
		 if(isTesting){
			 if(learner.correctlyClassifies(trainInst)){
				 numberSamplesCorrect_inteval++;
				 numberSamplesCorrect++;
			 }
		 }
		numberSamples++;
		learner.trainOnInstance(trainInst);
		if(numberSamples%1000 == 0){
//			double accumu_accuracy = 100.0*(double)numberSamplesCorrect /(double)numberSamples ;
//			System.out.println(accumu_accuracy) ;
//			System.out.println(numberSamples +","+accumu_accuracy) ;
//			double thousand_accuracy = 100.0*(double)numberSamplesCorrect_inteval /(double)1000 ;
//			System.out.println(thousand_accuracy) ;
//			numberSamplesCorrect_inteval = 0;
			
//			MemoryMeter meter = new MemoryMeter();
//			System.out.println(meter.measureDeep(learner));
		}
	 }
	 long stopTime = System.nanoTime();
	 System.out.println((stopTime - startTime)*(1e-9));
	 double accuracy = 100.0*(double)numberSamplesCorrect /(double)numberSamples ;
	 System.out.println(numberSamples+ " instances processed with " + accuracy + "% accuracy") ;
	 }
}
