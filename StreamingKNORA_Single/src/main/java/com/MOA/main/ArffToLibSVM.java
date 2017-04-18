package com.MOA.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.LibSVMSaver;

public class ArffToLibSVM{
	
	public static void main(String[] args)  throws IOException{
		BufferedReader reader =
		new BufferedReader(new FileReader("/home/gody7334/Desktop/Dataset/RRBF_1M_HD.arff"));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(data.numAttributes() - 1);
		
		LibSVMSaver saver = new LibSVMSaver();
		 saver.setInstances(data);
		 saver.setFile(new File("/home/gody7334/Desktop/Dataset/RRBF_1M_HD.libsvm"));
		 saver.writeBatch();
	}
	
}