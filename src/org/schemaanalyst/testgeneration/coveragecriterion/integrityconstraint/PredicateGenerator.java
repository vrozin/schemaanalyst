package org.schemaanalyst.testgeneration.coveragecriterion.integrityconstraint;

import org.schemaanalyst.sqlrepresentation.Column;
import org.schemaanalyst.sqlrepresentation.Schema;
import org.schemaanalyst.sqlrepresentation.Table;
import org.schemaanalyst.sqlrepresentation.constraint.*;
import org.schemaanalyst.sqlrepresentation.expression.Expression;
import org.schemaanalyst.testgeneration.coveragecriterion.predicate.*;

import java.util.List;

/**
 * Created by phil on 18/07/2014.
 */
public class PredicateGenerator {

    public static ComposedPredicate generateAcceptancePredicate(Schema schema, Table table) {
        return generateAcceptancePredicate(schema, table, true);
    }

    public static ComposedPredicate generateAcceptancePredicate(Schema schema, Table table, boolean truthValue) {
        return generateAcceptancePredicate(schema, table, truthValue, null);
    }

    public static ComposedPredicate generateAcceptancePredicate(Schema schema, Constraint ignoreConstraint) {
        return generateAcceptancePredicate(schema, ignoreConstraint.getTable(), true, ignoreConstraint);
    }

    public static ComposedPredicate generateAcceptancePredicate(Schema schema, Table table, boolean truthValue, Constraint ignoreConstraint) {

        ComposedPredicate ap = truthValue ? new AndPredicate() : new OrPredicate();

        for (Constraint constraint : schema.getConstraints(table)) {
            if (!constraint.equals(ignoreConstraint)) {
                ap.addPredicate(generateConditionPredicate(constraint, truthValue));
            }
        }

        return ap;
    }

    public static Predicate generateConditionPredicate(Constraint constraint, boolean truthValue) {

        return new ConstraintVisitor() {
            Predicate predicate;
            boolean truthValue;

            Predicate generateConditionPredicate(Constraint constraint, boolean truthValue) {
                this.truthValue = truthValue;
                constraint.accept(this);
                return predicate;
            }

            @Override
            public void visit(CheckConstraint constraint) {
                predicate = generateCheckConstraintPredicate(constraint, truthValue);
            }

            @Override
            public void visit(ForeignKeyConstraint constraint) {
                predicate = generateForeignKeyConstraintPredicate(constraint, truthValue);
            }

            @Override
            public void visit(NotNullConstraint constraint) {
                predicate = generateNotNullConstraintPredicate(constraint, truthValue);
            }

            @Override
            public void visit(PrimaryKeyConstraint constraint) {
                predicate = generatePrimaryKeyConstraintPredicate(constraint, truthValue);
            }

            @Override
            public void visit(UniqueConstraint constraint) {
                predicate = generateUniqueConstraintPredicate(constraint, truthValue);
            }
        }.generateConditionPredicate(constraint, truthValue);
    }

    public static Predicate generateCheckConstraintPredicate(CheckConstraint checkConstraint, boolean truthValue) {
        boolean nullStatus = truthValue;
        return generateCheckConstraintPredicate(checkConstraint, truthValue, nullStatus);
    }

    public static Predicate generateForeignKeyConstraintPredicate(ForeignKeyConstraint foreignKeyConstraint, boolean truthValue) {
        boolean match = truthValue;
        boolean nullStatus = truthValue;
        return generateMultiColumnConstraintPredicate(foreignKeyConstraint, match, nullStatus);
    }

    public static Predicate generateNotNullConstraintPredicate(NotNullConstraint notNullConstraint, boolean truthValue) {
        return new NullPredicate(notNullConstraint.getTable(), notNullConstraint.getColumn(), !truthValue);
    }

    public static Predicate generatePrimaryKeyConstraintPredicate(PrimaryKeyConstraint primaryKeyConstraint, boolean truthValue) {
        boolean match = !truthValue;
        boolean nullStatus = !truthValue;
        return generateMultiColumnConstraintPredicate(primaryKeyConstraint, match, nullStatus);
    }

    public static Predicate generateUniqueConstraintPredicate(UniqueConstraint uniqueConstraint, boolean truthValue) {
        boolean match = !truthValue;
        boolean nullStatus = truthValue;
        return generateMultiColumnConstraintPredicate(uniqueConstraint, match, nullStatus);
    }

    public static Predicate generateCheckConstraintPredicate(CheckConstraint constraint, Boolean truthValue, boolean nullStatus) {
        Table table = constraint.getTable();
        Expression expression = constraint.getExpression();
        List<Column> columns = expression.getColumnsInvolved();

        ComposedPredicate predicate = (nullStatus)
                ? new OrPredicate()
                : new AndPredicate();

        if (truthValue != null) {
            ExpressionPredicate expressionPredicate = new ExpressionPredicate(table, expression, truthValue);
            predicate.addPredicate(expressionPredicate);
        }

        addNullPredicates(predicate, table, columns, nullStatus);

        return predicate;
    }

    public static Predicate generateMultiColumnConstraintPredicate(MultiColumnConstraint constraint, Boolean match, boolean nullStatus) {
        Table table = constraint.getTable();
        List<Column> columns = constraint.getColumns();

        ComposedPredicate predicate = (nullStatus)
                ? new OrPredicate()
                : new AndPredicate();

        if (match != null) {
            Table refTable = table;
            List<Column> refColumns = columns;

            if (constraint instanceof ForeignKeyConstraint) {
                ForeignKeyConstraint fkConstraint = (ForeignKeyConstraint) constraint;
                refTable = fkConstraint.getReferenceTable();
                refColumns = fkConstraint.getReferenceColumns();
            }

            MatchPredicate matchPredicate = (match)
                    ? generateAndMatch(table, columns, refTable, refColumns)
                    : generateOrNonMatch(table, columns, refTable, refColumns);

            predicate.addPredicate(matchPredicate);
        }

        addNullPredicates(predicate, table, columns, nullStatus);

        return predicate;
    }

    public static MatchPredicate generateOrNonMatch(Table table, List<Column> columns, Table refTable, List<Column> refColumns) {
        return new MatchPredicate(
                table,
                MatchPredicate.EMPTY_COLUMN_LIST,
                columns,
                refTable,
                MatchPredicate.EMPTY_COLUMN_LIST,
                refColumns,
                MatchPredicate.Mode.OR);
    }

    public static MatchPredicate generateAndMatch(Table table, List<Column> columns, Table refTable, List<Column> refColumns) {
        return new MatchPredicate(
                table,
                columns,
                MatchPredicate.EMPTY_COLUMN_LIST,
                refTable,
                refColumns,
                MatchPredicate.EMPTY_COLUMN_LIST,
                MatchPredicate.Mode.AND);
    }

    public static ComposedPredicate addNullPredicates(ComposedPredicate composedPredicate, Table table, List<Column> columns, boolean truthValue) {
        for (Column column : columns) {
            composedPredicate.addPredicate(new NullPredicate(table, column, truthValue));
        }
        return composedPredicate;
    }
}
