package org.schemaanalyst.util;

import org.schemaanalyst.data.Data;
import org.schemaanalyst.testgeneration.TestSuite;
import org.schemaanalyst.testgeneration.coveragecriterion.TestRequirementDescriptor;
import org.schemaanalyst.testgeneration.coveragecriterion.predicate.checker.PredicateChecker;
import org.schemaanalyst.testgeneration.coveragecriterion.predicate.checker.PredicateCheckerFactory;

public class TestSuiteReduction {
    private TestSuite nonReducdedTS;
    // private TestSuite reducdedTS;

    public TestSuiteReduction(TestSuite nonReducdedTS) {
	this.nonReducdedTS = nonReducdedTS;
    }

    private void combiningDuplicateTestCases() {
	for (int i = 0; i < nonReducdedTS.getTestCases().size(); i++) {
	    for (int j = i + 1; j < nonReducdedTS.getTestCases().size(); j++) {
		Data state1 = nonReducdedTS.getTestCases().get(i).getState();
		Data data1 = nonReducdedTS.getTestCases().get(i).getData();
		Data state2 = nonReducdedTS.getTestCases().get(j).getState();
		Data data2 = nonReducdedTS.getTestCases().get(j).getData();
		if (state1.toString().equals(state2.toString()) && data1.toString().equals(data2.toString())) {
		    for (TestRequirementDescriptor d : nonReducdedTS.getTestCases().get(j).getTestRequirement()
			    .getDescriptors()) {
			nonReducdedTS.getTestCases().get(i).getTestRequirement().addDescriptor(d);
		    }
		    nonReducdedTS.removeTestCase(j);
		    j--;
		}
	    }
	}
    }

    private void reduceByPredicate() {
	for (int i = 0; i < nonReducdedTS.getTestCases().size(); i++) {
	    for (int j = i + 1; j < nonReducdedTS.getTestCases().size(); j++) {
		Data state1 = nonReducdedTS.getTestCases().get(i).getState();
		Data data1 = nonReducdedTS.getTestCases().get(i).getData();

		PredicateChecker predicateChecker = PredicateCheckerFactory.instantiate(
			nonReducdedTS.getTestCases().get(j).getTestRequirement().getPredicate(), true, data1, state1);

		if (predicateChecker.check()) {
		    for (TestRequirementDescriptor d : nonReducdedTS.getTestCases().get(j).getTestRequirement()
			    .getDescriptors()) {
			nonReducdedTS.getTestCases().get(i).getTestRequirement().addDescriptor(d);
		    }
		    nonReducdedTS.removeTestCase(j);
		    j--;
		}
	    }
	}
    }

    public void reducedTestSuiteByEqualTCs() {
	this.combiningDuplicateTestCases();
    }

    public void reducedTestSuiteByEqualPredicateData() {
	this.reduceByPredicate();
    }

    public TestSuite getReducedTestSuite() {
	// this.combiningDuplicateTestCases();
	// this.reducedTestSuite();
	return nonReducdedTS;
    }

}
