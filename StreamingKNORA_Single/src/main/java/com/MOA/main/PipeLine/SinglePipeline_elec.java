package com.MOA.main.PipeLine;

import moa.classifiers.Classifier;
import moa.classifiers.trees.HoeffdingTree;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.MOA.moa.classifiers.DecisionStumpTutorial;
import moa.evaluation.BasicClassificationPerformanceEvaluator;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.streams.ArffFileStream2;
import moa.streams.generators.RandomRBFGenerator;
import moa.streams.generators.RandomRBFGeneratorDrift;
import moa.streams.generators.RandomTreeGenerator;
import moa.tasks.EvaluateModel;
import moa.tasks.LearnModel;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.InstanceMaker;

public class SinglePipeline_elec {

	public static void main(String[] args) throws IOException {
	int numInstances = (int) 1.1e6;
	
	Classifier learner = new HoeffdingTree();
//	RandomRBFGenerator stream = new RandomRBFGenerator() ;
//	stream.prepareForUse();
	
	String CSVFilePath = "/home/gody7334/Desktop/HDP_Share/HDP_Share/mvn/1.KNORA_MOA/Files/" + "elecNormNew_header.arff"; 
	ArffFileStream2 arffStream = new ArffFileStream2(CSVFilePath, -1);
	arffStream.prepareForUse();
	ArffFileStream2 stream = arffStream;
	
	String CSVFilePath2 = "/home/gody7334/Desktop/HDP_Share/HDP_Share/mvn/1.KNORA_MOA/Files/" + "elecNormNew_TP.arff";
	FileInputStream fstream = null;
	try {
		fstream = new FileInputStream(CSVFilePath2);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
	String strLine;
	Instance inst = null;
	InstanceMaker im = new InstanceMaker();
	Instances m = stream.instances;
	
		
	learner.setModelContext( stream.getHeader() ) ;
	learner.prepareForUse();
	
	 int numberSamplesCorrect =0;
	 int numberSamples =0;
	 int numberTestSamples = 0;
	 boolean isTesting = false;
//	 while ( stream.hasMoreInstances() && numberSamples < numInstances ){
	 while ((strLine = br.readLine()) != null)   {
		  // Print the content on the console
		inst = im.convertToInstance(strLine, m);

		 Instance trainInst = inst;
		 if(isTesting){
			 if(learner.correctlyClassifies(trainInst)){
				 numberSamplesCorrect++;
			 }
			 numberTestSamples++;
		 }
		numberSamples++;
		
		if(numberSamples >= 30000)
			isTesting = true;
		
		if(isTesting == false)
			learner.trainOnInstance(trainInst);
	 }
	 double accuracy = 100.0*(double)numberSamplesCorrect /(double)numberTestSamples ;
	 System.out.println(numberTestSamples+ " instances processed with " + accuracy + "% accuracy") ;
	 }
}
