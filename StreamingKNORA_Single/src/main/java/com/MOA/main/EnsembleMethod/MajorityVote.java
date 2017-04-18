package com.MOA.main.EnsembleMethod;

import java.util.List;

import com.MOA.main.Entity.PredictResult;

import weka.core.Utils;

public class MajorityVote {
	
	public static int Vote( List<PredictResult> list_predict_results, int numOfClass){
		int numClass = numOfClass;
		int[] classVote = new int[numClass];
		for(PredictResult pr : list_predict_results){
			int classIdx = Utils.maxIndex(pr.result); 		
			classVote[classIdx]++;
		}
		
		int[] array = classVote;
		int largest = array[0];
		int index = 0;
		for (int i = 1; i < array.length; i++) {
		  if ( array[i] > largest ) {
		      largest = array[i];
		      index = i;
		   }
		}
		
		return index;
	}
}
