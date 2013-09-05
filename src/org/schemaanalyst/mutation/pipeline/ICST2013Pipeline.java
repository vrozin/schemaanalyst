package org.schemaanalyst.mutation.pipeline;

import org.schemaanalyst.mutation.operator.CCNullifier;
import org.schemaanalyst.mutation.operator.FKCColumnPairR;
import org.schemaanalyst.mutation.operator.NNCAR;
import org.schemaanalyst.mutation.operator.PKCColumnARE;
import org.schemaanalyst.mutation.operator.UCColumnARE;
import org.schemaanalyst.mutation.redundancy.EquivalentMutantRemover;
import org.schemaanalyst.mutation.redundancy.IdenticalMutantRemover;
import org.schemaanalyst.mutation.redundancy.PrimaryKeyColumnNotNullRemover;
import org.schemaanalyst.mutation.redundancy.PrimaryKeyColumnsUniqueRemover;
import org.schemaanalyst.sqlrepresentation.Schema;

/**
 * {@link ICST2013Pipeline} is an implementation of the mutation pipeline used in
 * the original ICST 2013 paper.
 * 
 * @author Phil McMinn
 *
 */
public class ICST2013Pipeline extends MutationPipeline<Schema> {

	public ICST2013Pipeline(Schema schema) {		
		addProducer(new CCNullifier(schema));
		addProducer(new FKCColumnPairR(schema));
		addProducer(new PKCColumnARE(schema));
		addProducer(new NNCAR(schema));
		addProducer(new UCColumnARE(schema));
		
		addRemover(new PrimaryKeyColumnNotNullRemover());
		addRemover(new PrimaryKeyColumnsUniqueRemover());
		addRemover(new IdenticalMutantRemover<Schema>());
		addRemover(new EquivalentMutantRemover<Schema>(schema));
	}	
}
