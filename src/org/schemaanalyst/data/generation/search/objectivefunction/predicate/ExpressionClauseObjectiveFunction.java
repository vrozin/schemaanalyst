package org.schemaanalyst.data.generation.search.objectivefunction.predicate;

import org.schemaanalyst.data.Data;
import org.schemaanalyst.data.Row;
import org.schemaanalyst._deprecated.datageneration.search.objective.ObjectiveFunction;
import org.schemaanalyst._deprecated.datageneration.search.objective.ObjectiveValue;
import org.schemaanalyst._deprecated.datageneration.search.objective.SumOfMultiObjectiveValue;
import org.schemaanalyst._deprecated.datageneration.search.objective.row.ExpressionRowObjectiveFunctionFactory;
import org.schemaanalyst.logic.predicate.clause.ExpressionClause;

import java.util.List;

/**
 * Created by phil on 24/01/2014.
 */
public class ExpressionClauseObjectiveFunction extends ObjectiveFunction<Data> {

    private ExpressionClause expressionClause;

    public ExpressionClauseObjectiveFunction(ExpressionClause expressionClause) {
        this.expressionClause = expressionClause;
    }

    @Override
    public ObjectiveValue evaluate(Data data) {
        String description = expressionClause.toString();
        List<Row> rows = data.getRows(expressionClause.getTable());

        if (rows.size() > 0) {
            SumOfMultiObjectiveValue objVal = new SumOfMultiObjectiveValue(description);

            for (Row row : rows) {
                objVal.add(
                        new ExpressionRowObjectiveFunctionFactory(
                                expressionClause.getExpression(),
                                expressionClause.getSatisfy(),
                                true).create().evaluate(row)
                );
            }

            return objVal;
        }

        return ObjectiveValue.worstObjectiveValue(description);
    }
}