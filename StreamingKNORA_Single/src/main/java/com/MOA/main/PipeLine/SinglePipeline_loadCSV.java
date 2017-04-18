package com.MOA.main.PipeLine;

import moa.classifiers.Classifier;
import moa.classifiers.trees.HoeffdingTree;
import moa.core.InstancesHeader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.MOA.moa.classifiers.DecisionStumpTutorial;

import moa.evaluation.BasicClassificationPerformanceEvaluator;
import moa.evaluation.ClassificationPerformanceEvaluator;
import moa.streams.generators.RandomRBFGenerator;
import moa.streams.generators.RandomRBFGeneratorDrift;
import moa.streams.generators.RandomTreeGenerator;
import moa.tasks.EvaluateModel;
import moa.tasks.LearnModel;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.InstanceMaker;
import moa.streams.ArffFileStream2;

public class SinglePipeline_loadCSV {

	public static void main(String[] args) {
	int numInstances = (int) 1.1e6;
	
	Classifier learner = new HoeffdingTree();
	RandomRBFGenerator stream = new RandomRBFGenerator() ;
	stream.prepareForUse();
	
	String CSVFilePath = "/home/gody7334/Desktop/HDP_Share/HDP_Share/mvn/1.KNORA_MOA/Files/" + "elecNormNew2.arff"; 
	CSVLoader loader = new CSVLoader();
	
	//Try Arff file
	ArffFileStream2 arffStream = new ArffFileStream2(CSVFilePath, -1);
	arffStream.prepareForUse();
	arffStream.getHeader();
	learner.setModelContext(arffStream.getHeader());
	learner.prepareForUse();
	InstanceMaker im = new InstanceMaker();
	String firstLine = "0,2,0,0.056443,0.439155,0.003467,0.422915,0.414912,UP";
	Instance i = im.convertToInstance(firstLine, arffStream.instances);
	
	
	
	try {
		loader.setSource(new File(CSVFilePath));
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	String[] options = new String[1]; 
	options[0] = "-H";
	try {
		loader.setOptions(options);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	Instances data = null;
	try {
		data = loader.getDataSet();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	data.setClassIndex(data.numAttributes()-1);
	
//	learner.setModelContext( stream.getHeader() ) ;
	learner.setModelContext(new InstancesHeader(data));
	learner.prepareForUse();
	
	
//try to make a instance
//	InstanceMaker im = new InstanceMaker();
	im.setNoHeaderRowPresent(true);
//	String firstLine = "0,2,0,0.056443,0.439155,0.003467,0.422915,0.414912,UP";
	try {
		im.readHeader(firstLine);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	Instances m = im.m_structure;
	
//	Instance i = im.convertToInstance(firstLine, m);
	m.setClassIndex(m.numAttributes()-1);
	learner.setModelContext(new InstancesHeader(m));
	learner.prepareForUse();
	
	 int numberSamplesCorrect =0;
	 int numberSamples =0;
	 boolean isTesting = true;
	 
	 FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(CSVFilePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		Instance inst = null;
		try {
			while ((strLine = br.readLine()) != null)   {
				  // Print the content on the console
				inst = im.convertToInstance(strLine, m);
				Instance trainInst = inst;
				 if(isTesting){
					 if(trainInst != null && learner.correctlyClassifies(trainInst)){
						 numberSamplesCorrect++;
					 }
				 }
				numberSamples++;
				 if(trainInst != null)
					 {learner.trainOnInstance(trainInst);}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 double accuracy2 = 100.0*(double)numberSamplesCorrect /(double)numberSamples ;
		 System.out.println(numberSamples+ " instances processed with " + accuracy2 + "% accuracy") ;
		
	 
////	 while ( stream.hasMoreInstances() && numberSamples < numInstances ){
//	 while(numberSamples < data.size()){
//		 Instance trainInst = stream.nextInstance();
//		 if(isTesting){
//			 if(learner.correctlyClassifies(trainInst)){
//				 numberSamplesCorrect++;
//			 }
//		 }
//		numberSamples++;
//		learner.trainOnInstance(trainInst);
//	 }
//	 double accuracy = 100.0*(double)numberSamplesCorrect /(double)numberSamples ;
//	 System.out.println(numberSamples+ " instances processed with " + accuracy + "% accuracy") ;
	 }
}
