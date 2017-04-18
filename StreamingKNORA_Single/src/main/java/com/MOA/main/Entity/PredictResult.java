package com.MOA.main.Entity;

public class PredictResult {
	public int learner_num;
	public double[] result;
	
	public PredictResult( int learner_num, double[] result){
		this.learner_num = learner_num;
		this.result = result;
	}
	
}
