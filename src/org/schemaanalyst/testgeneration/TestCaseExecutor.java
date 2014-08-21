package org.schemaanalyst.testgeneration;

import org.schemaanalyst.configuration.DatabaseConfiguration;
import org.schemaanalyst.configuration.LocationsConfiguration;
import org.schemaanalyst.data.Cell;
import org.schemaanalyst.data.Data;
import org.schemaanalyst.data.DateValue;
import org.schemaanalyst.data.Row;
import org.schemaanalyst.dbms.DBMS;
import org.schemaanalyst.dbms.DatabaseInteractor;
import org.schemaanalyst.sqlrepresentation.Schema;
import org.schemaanalyst.sqlrepresentation.Table;
import org.schemaanalyst.sqlwriter.SQLWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 03/02/2014.
 */
public class TestCaseExecutor {

    private Schema schema;
    private List<Table> tables;
    private DatabaseInteractor databaseInteractor;
    private SQLWriter sqlWriter;

    public TestCaseExecutor(Schema schema,
                            DBMS dbms,
                            DatabaseConfiguration databaseConfiguration,
                            LocationsConfiguration locationConfiguration) {

        this.schema = schema;
        tables = schema.getTablesInOrder();
        databaseInteractor = dbms.getDatabaseInteractor(schema.getName(), databaseConfiguration, locationConfiguration);
        sqlWriter = dbms.getSQLWriter();
    }

    public void prepare() {
        List<String> createStatements = sqlWriter.writeCreateTableStatements(schema);
        for (String statement : createStatements) {
            Integer result = databaseInteractor.executeUpdate(statement);
            if (result < 0) {
                throw new TestCaseExecutionException(
                        "Could not execute CREATE TABLE statement \"" + statement + "\" while executing test case, result was " + result);
            }
        }
    }

    public void execute(TestSuite testSuite) {
        prepare();
        for (TestCase testCase : testSuite.getTestCases()) {
            execute(testCase);
        }
        close();
    }

    public void execute(TestCase testCase) {

        // clear tables
        List<String> deleteFromTableStatements = sqlWriter.writeDeleteFromTableStatements(schema);
        for (String statement : deleteFromTableStatements) {
            Integer result = databaseInteractor.executeUpdate(statement);
            if (result < 0) {
                throw new TestCaseExecutionException(
                        "Could not execute DELETE FROM TABLE statement \"" + statement + "\" while executing test case, result was " + result);
            }
        }

        // insert state rows
        Data state = testCase.getState();
        List<Table> stateTables = state.getTables();
        for (Table table : tables) {
            if (stateTables.contains(table)) {
                List<Row> rows = state.getRows(table);
                for (Row row : rows) {
                    String statement = sqlWriter.writeInsertStatement(row);
                    Integer result = databaseInteractor.executeUpdate(statement);
                    if (result != 1) {
                        System.out.println(row);
                        Cell cell = row.getCells().get(row.getCells().size() - 1);
                        System.out.println(cell.getValue().getClass());
                        System.out.println(((DateValue) cell.getValue()).getMonth());
                        System.out.println(((DateValue) cell.getValue()).getDay().max);
                        throw new TestCaseExecutionException(
                                "INSERT statement for setting database state \"" + statement + "\" should affect exactly one row, was " + result);
                    }
                }
            }
        }

        // insert test case (data) rows
        Data data = testCase.getData();
        List<Table> dataTables = data.getTables();
        List<Boolean> results = new ArrayList<>();

        for (Table table : tables) {
            if (dataTables.contains(table)) {
                List<Row> rows = data.getRows(table);
                for (Row row : rows) {
                    String statement = sqlWriter.writeInsertStatement(row);
                    Integer result = databaseInteractor.executeUpdate(statement);
                    if (result < 0) {
                        throw new TestCaseExecutionException(
                                "Could not execute INSERT statement \"" + statement + "\" while executing test case - result was " + result);
                    }
                    boolean success = (result == 1);
                    results.add(success);
                }
            }
        }

        testCase.setDBMSResults(results);
    }

    public void close() {
        List<String> dropTableStatements = sqlWriter.writeDropTableStatements(schema, true);
        for (String statement : dropTableStatements) {
            Integer result = databaseInteractor.executeUpdate(statement);
            if (result < 0) {
                throw new TestCaseExecutionException(
                        "Could not execute DROP TABLE statement \"" + statement + "\" while executing test case, result was " + result);
            }
        }
    }

}
