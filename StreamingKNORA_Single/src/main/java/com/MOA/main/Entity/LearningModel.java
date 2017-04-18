package com.MOA.main.Entity;

import moa.classifiers.Classifier;

public class LearningModel {
	public int learner_num;
	public Classifier learner;
	
	public LearningModel(Classifier learner, int learner_num){
		this.learner = learner;
		this.learner_num = learner_num;
	}
}
