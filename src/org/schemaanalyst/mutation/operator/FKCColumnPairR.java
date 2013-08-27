package org.schemaanalyst.mutation.operator;

import java.util.List;

import org.schemaanalyst.mutation.Mutant;
import org.schemaanalyst.mutation.MutantProducer;
import org.schemaanalyst.mutation.mutator.ListElementRemover;
import org.schemaanalyst.mutation.supplier.Supplier;
import org.schemaanalyst.mutation.supplier.SupplyChain;
import org.schemaanalyst.mutation.supplier.schema.ForeignKeyColumnSupplier;
import org.schemaanalyst.mutation.supplier.schema.ForeignKeyConstraintSupplier;
import org.schemaanalyst.sqlrepresentation.Column;
import org.schemaanalyst.sqlrepresentation.Schema;
import org.schemaanalyst.util.Pair;

/**
 * 
 * @author Phil McMinn
 *
 */
public class FKCColumnPairR implements MutantProducer<Schema> {

    private Schema schema;

    public FKCColumnPairR(Schema schema) {
        this.schema = schema;
    }

    public List<Mutant<Schema>> mutate() {

        Supplier<Schema, List<Pair<Column>>> supplier = SupplyChain.chain(
                new ForeignKeyConstraintSupplier(),
                new ForeignKeyColumnSupplier());
        
        supplier.initialise(schema);
        ListElementRemover<Schema, Pair<Column>> mutator = new ListElementRemover<>(
                supplier);
        return mutator.mutate();
    }

}
