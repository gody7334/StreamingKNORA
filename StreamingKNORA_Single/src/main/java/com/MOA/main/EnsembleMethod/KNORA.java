package com.MOA.main.EnsembleMethod;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.RowSet;
import javax.sql.rowset.Predicate;

import com.MOA.main.Entity.ValidateInstance;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;

public class KNORA {

	public int N_neighbour;
	public boolean intersect;
	
	public KNORA(int N_neighbour, boolean intersect ){
		this.N_neighbour = N_neighbour; //8
		this.intersect = intersect; // true
	}
	
	public Integer[] findKNNValidateInstances(List<ValidateInstance> list_validate_instances, Instance testInstance){
		
		//create Instances
		ArrayList<Attribute> list_attribute = new ArrayList<Attribute>();
		Enumeration<Attribute> a = testInstance.enumerateAttributes();
		while(a.hasMoreElements()){
			list_attribute.add(a.nextElement());
		}
		list_attribute.add( testInstance.classAttribute());
		Instances val_inst = new Instances( "Val",list_attribute,list_validate_instances.size());
		for(ValidateInstance vi : list_validate_instances){
			val_inst.add(vi.Validate_Inst);
		}
		val_inst.setClassIndex(testInstance.classIndex());
		
//		System.out.println("Test inst: "+testInstance);
		
		//KNN search
		LinearNNSearch lnns = new LinearNNSearch(val_inst);		
		Instances insts = null;
		try {
			insts = lnns.kNearestNeighbours(testInstance, N_neighbour);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
//		for(Instance i : insts){
//			System.out.println("KNN inst: "+i);
//		}
		
		List<ValidateInstance> list_knn_v_instances = new ArrayList<ValidateInstance>();
		
//		for(ValidateInstance v : list_validate_instances){
//			if(insts.contains(v.Validate_Inst))
//				list_knn_v_instances.add(v);
//		}
		
		for(Instance i : insts){
			for(ValidateInstance v : list_validate_instances){
				boolean b = true;
				for(int t = 0; t < i.numAttributes(); t++){
					if(i.value(t) != v.Validate_Inst.value(t))
						b = false;
				}
				if(b == true)
					list_knn_v_instances.add(v);
				b = true;
			}
		}

		Set<Integer> result = new HashSet<Integer>((list_knn_v_instances.get(0).list_positive_learner_num));
		Set<Integer> s1;
		Set<Integer> s2;
		for(int i = 0; i < list_knn_v_instances.size(); i++){ 
			// TODO Need to modify to proper as no intersection
			s1 = result;
			s2 = new HashSet<Integer>((list_knn_v_instances.get(i).list_positive_learner_num));
			if(intersect)
				s1.retainAll(s2);
			else
				s1.addAll(s2);
			result = s1;
		}
		Integer[] FResult = result.toArray(new Integer[result.size()]);
		return FResult;
		
	}
}
