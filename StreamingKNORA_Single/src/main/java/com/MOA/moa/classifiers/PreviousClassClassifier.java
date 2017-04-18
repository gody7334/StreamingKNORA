package com.MOA.moa.classifiers;

import weka.core.Instance;
import moa.classifiers.AbstractClassifier;
import moa.core.DoubleVector;
import moa.core.Measurement;

/**
 * Previous-class Classifier determines instances's class using 
 * the previous class value from the observed instances.
 * 
 * @author Arinto Murdopo
 *
 */

public class PreviousClassClassifier extends AbstractClassifier {

	private static final long serialVersionUID = 1L;

	//protected variables to maintain the state of the classifier
	protected DoubleVector observedClassDistribution;
	
	@Override
	public void resetLearningImpl() {
		observedClassDistribution = new DoubleVector();
	}
	
	//main function, called for every training example
	@Override
	public void trainOnInstanceImpl(Instance inst) {
		int numClasses = inst.numClasses();
		for(int i = 0; i < numClasses; i++)
		{
			if((int)inst.classValue() == i){
				//store the current class as the next prediction result
				observedClassDistribution.setValue(i, 1.0);
			}else{
				observedClassDistribution.setValue(i, 0.0);
			}
		}
	}
	
	//@Override
	public double[] getVotesForInstance(Instance inst) {
		return observedClassDistribution.getArrayCopy();
	}

	//@Override
	public boolean isRandomizable() {
		return false;
	}

	@Override
	public void getModelDescription(StringBuilder arg0, int arg1) {
		// do nothing here
	}

	@Override
	protected Measurement[] getModelMeasurementsImpl() {
		return null;
	}
}
