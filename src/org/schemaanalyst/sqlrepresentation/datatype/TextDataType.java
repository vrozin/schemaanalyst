package org.schemaanalyst.sqlrepresentation.datatype;

public class TextDataType extends DataType {

    private static final long serialVersionUID = -98925722746243549L;

    @Override
    public void accept(DataTypeVisitor typeVisitor) {
        typeVisitor.visit(this);
    }

    @Override
    public void accept(DataTypeCategoryVisitor categoryVisitor) {
        categoryVisitor.visit(this);
    }
}
