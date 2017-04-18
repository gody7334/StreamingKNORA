package com.MOA.moa.classifiers;

import weka.core.Instance;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.core.AttributeSplitSuggestion;
import moa.classifiers.core.attributeclassobservers.AttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.GaussianNumericAttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.NominalAttributeClassObserver;
import moa.classifiers.core.splitcriteria.SplitCriterion;
import moa.core.AutoExpandVector;
import moa.core.DoubleVector;
import moa.core.Measurement;
import moa.options.ClassOption;
import moa.options.FlagOption;
import moa.options.IntOption;

public class DecisionStumpTutorial extends AbstractClassifier {
	
	private static final long serialVersionUID = 1L;
	
	//option handling for this classifier
	public IntOption gracePeriodOption = new IntOption("gracePeriod", 'g', 
			"The number of instances to observer between model changes.", 
			1000, 0, Integer.MAX_VALUE);

	public FlagOption binarySplitsOption = new FlagOption("binarySplits", 'b',
			"Only allow binary splits.");
	
	public ClassOption splitCriterionOption = new ClassOption("splitCriterion", 'c', 
			"Split criterion to use", SplitCriterion.class, "InfoGainSplitCriterion");
	
	//four protected variables to maintain the state of the classifier. 
	protected AttributeSplitSuggestion bestSplit; //maintain the current stump that chosen by classifier
	
	protected DoubleVector observedClassDistribution; //overall distribution of class labels that have been observed by classifier
	
	protected AutoExpandVector<AttributeClassObserver> attributeObservers; //collection of attribute class observers
	
	protected double weightSeenAtLastSplit; //last time an evaluation was performed
	
	//@Override
	public boolean isRandomizable() {
		return false;
	}

	//invoked before any learning begins, set the default state (no info supplied, no training executed)
	@Override
	public void resetLearningImpl() {
		this.bestSplit = null;
		this.observedClassDistribution = new DoubleVector();
		this.attributeObservers = new AutoExpandVector<AttributeClassObserver>();
		this.weightSeenAtLastSplit = 0.0;
	}
	
	//main function, called for every training example
	@Override
	public void trainOnInstanceImpl(Instance inst) {
		this.observedClassDistribution.addToValue((int) inst.classValue(), 
				inst.weight());
		for(int i = 0; i < inst.numAttributes() - 1; i++){
			int instAttIndex = AbstractClassifier.modelAttIndexToInstanceAttIndex(i, inst);
			AttributeClassObserver obs = this.attributeObservers.get(i);
			if(obs == null){
				obs = inst.attribute(instAttIndex).isNominal() ? 
						newNominalClassObserver() : newNumericClassObserver(); 
				this.attributeObservers.set(i, obs);
			}
			
			obs.observeAttributeClass(inst.value(instAttIndex), (int) inst.classValue(), inst.weight());
			
		}
		
		if(this.trainingWeightSeenByModel - this.weightSeenAtLastSplit >= 
				this.gracePeriodOption.getValue()){
			this.bestSplit = findBestSplit(
					(SplitCriterion) getPreparedClassOption(this.splitCriterionOption));
			this.weightSeenAtLastSplit = this.trainingWeightSeenByModel;
		}
		
	}
	
	//method to predict the class of examples based on induced learning
	//@Override
	public double[] getVotesForInstance(Instance inst) {
		if (this.bestSplit != null){
			int branch = this.bestSplit.splitTest.branchForInstance(inst);
			if (branch >= 0) {
				return this.bestSplit.resultingClassDistributionFromSplit(branch);
			}
			
		}
		return this.observedClassDistribution.getArrayCopy();
	}
	
	@Override
	public void getModelDescription(StringBuilder arg0, int arg1) {
		// do nothing here
	}

	@Override
	protected Measurement[] getModelMeasurementsImpl() {
		return null;
	}
	
	//helper methods
	protected AttributeClassObserver newNominalClassObserver(){
		return new NominalAttributeClassObserver();
	}
	
	protected AttributeClassObserver newNumericClassObserver(){
		return new GaussianNumericAttributeClassObserver();
	}
	
	protected AttributeSplitSuggestion findBestSplit(SplitCriterion criterion){
		AttributeSplitSuggestion bestFound = null;
		double bestMerit = Double.NEGATIVE_INFINITY;
		double[] preSplitDist = this.observedClassDistribution.getArrayCopy();
		for (int i = 0; i < this.attributeObservers.size(); i++) {
			AttributeClassObserver obs = this.attributeObservers.get(i);
			if(obs != null) {
				AttributeSplitSuggestion suggestion = 
						obs.getBestEvaluatedSplitSuggestion(
								criterion, 
								preSplitDist, 
								i, 
								this.binarySplitsOption.isSet());
				
				if(suggestion.merit > bestMerit){
					bestMerit = suggestion.merit;
					bestFound = suggestion;
				}
			}
		}
		return bestFound;
	}
}
