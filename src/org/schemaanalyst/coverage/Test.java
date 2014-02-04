package org.schemaanalyst.coverage;

import org.schemaanalyst.configuration.DatabaseConfiguration;
import org.schemaanalyst.configuration.LocationsConfiguration;
import org.schemaanalyst.coverage.criterion.ConstraintRACC;
import org.schemaanalyst.coverage.criterion.Predicate;
import org.schemaanalyst.coverage.search.AVSTestCaseGenerator;
import org.schemaanalyst.coverage.testgeneration.TestCaseExecutor;
import org.schemaanalyst.coverage.testgeneration.TestSuiteGenerator;
import org.schemaanalyst.coverage.testgeneration.TestCase;
import org.schemaanalyst.coverage.testgeneration.TestSuite;
import org.schemaanalyst.data.Data;

import org.schemaanalyst.dbms.postgres.PostgresDBMS;
import org.schemaanalyst.dbms.sqlite.SQLiteDBMS;
import org.schemaanalyst.util.runner.Runner;
import parsedcasestudy.Flights;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by phil on 21/01/2014.
 */
public class Test extends Runner {

    private final static Logger LOGGER = Logger.getLogger(Test.class.getName());


    @Override
    protected void task() {

        LOGGER.log(Level.SEVERE,"Please work...");

        Flights flights = new Flights();

        TestSuiteGenerator dg = new TestSuiteGenerator(
                flights,
                new ConstraintRACC(),
                new PostgresDBMS(),
                new AVSTestCaseGenerator());

        TestSuite testSuite = dg.generate();

        TestCaseExecutor executor = new TestCaseExecutor(flights, new SQLiteDBMS(), new DatabaseConfiguration(), new LocationsConfiguration());

        boolean first = true;
        for (TestCase testCase : testSuite.getTestCases()) {
            if (first) {
                System.out.println();
            } else {
                first = false;
            }
            for (Predicate predicate : testCase.getPredicates()) {
                System.out.println("PURPOSE:   " + predicate.getPurpose());
                System.out.println("PREDICATE: " + predicate);
            }
            Data state = testCase.getState();
            if (state.getCells().size() > 0) {
                System.out.println("STATE:     " + testCase.getState());
            }
            System.out.println("DATA:      " + testCase.getData());
            //System.out.println("OBJ VAL:   " + testCase.getInfo("objval"));


            executor.execute(testCase);
        }
    }

    @Override
    protected void validateParameters() {

    }

    public static void main(String... args) {
        new Test().run(args);
    }
}