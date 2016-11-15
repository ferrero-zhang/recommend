package com.ifeng.myClassifier;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class NaiveBayesTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testClassify() {
		NaiveBayes nb = NaiveBayes.getInstance();
		ArrayList<String> featureList = null;
		String[] result = nb.classify(featureList);
		assertNull(result);
		
		featureList = new ArrayList<String>();
		result = nb.classify(featureList);
		assertNull(result);
		
		featureList.add("周杰伦");
		featureList.add("et");
		featureList.add("1.0");
		
		featureList.add("蒋介石");
		featureList.add("et");
		result = nb.classify(featureList);
		assertNull(result);
	}

}
