package report.converter;

import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.parser.object.INode;
import com.dse.report.element.Table;
import com.dse.search.Search2;
import com.dse.testdata.comparable.AssertMethod;
import com.dse.testdata.object.*;
import com.dse.testdata.object.stl.ListBaseDataNode;
import com.dse.util.Utils;

import java.util.List;
import java.util.Map;

public class LastAssertionConverter extends AssertionConverter {
    private SubprogramNode sut;

    private Map<ValueDataNode, ValueDataNode> expectedGlobalMap;

    public LastAssertionConverter(List<IResultTrace> failures, int fcalls) {
        super(failures, fcalls);
    }

    @Override
    public Table execute(SubprogramNode root) {
        this.sut = root;

        Table table = new Table(false);

        table.getRows().add(new Table.Row("Parameter", "Type", "Expected Value", "Actual Value"));

        INode sourceNode = Utils.getSourcecodeFile(root.getFunctionNode());
        String unitName = sourceNode.getName();
        Table.Row uutRow = new Table.Row("Return from UUT: " + unitName, "", "", "");
        table.getRows().add(uutRow);

        RootDataNode globalRoot = Search2.findGlobalRoot(root.getTestCaseRoot());
        if (globalRoot != null) {
            expectedGlobalMap = globalRoot.getGlobalInputExpOutputMap();

            for (IDataNode child : expectedGlobalMap.keySet()) {
                String nodeValue = getNodeValue(child);
                if (nodeValue != null && !nodeValue.equals("<<null>>"))
                    table = recursiveConvert(table, child, 1);
            }
        }
        Table.Row sutRow = new Table.Row(generateTab(1) + "Subprogram: " + root.getName(), "", "", "");
        table.getRows().add(sutRow);

        for (IDataNode child : root.getChildren())
            table = recursiveConvert(table, child, 2);

        return table;
    }

    @Override
    public Table recursiveConvert(Table table, IDataNode node, int level) {
        table = super.recursiveConvert(table, node, level);

        if (node instanceof PointerDataNode || node instanceof ArrayDataNode || node instanceof ListBaseDataNode) {
            ValueDataNode expectedNode = Search2.getExpectedValue((ValueDataNode) node);

            if (expectedNode != null) {
                for (IDataNode expectedChild : expectedNode.getChildren()) {
                    String expectedName = expectedChild.getName();
                    String expectedValue = getNodeValue(expectedChild);

                    if (expectedValue == null || expectedValue.equals("<<null>>"))
                        continue;

                    boolean found = false;

                    for (IDataNode actualChild : node.getChildren()) {
                        if (actualChild.getName().equals(expectedName)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        table = super.recursiveConvert(table, expectedChild, level + 1);
                    }
                }
            }
        }

        return table;
    }

    @Override
    protected boolean isValuable(IDataNode node) {
        if (node instanceof ArrayDataNode || node instanceof PointerDataNode
                || node instanceof VoidPointerDataNode || node instanceof FunctionPointerDataNode) {
            String assertMethod = ((ValueDataNode) node).getAssertMethod();
            if (AssertMethod.ASSERT_NULL.equals(assertMethod)
                    || AssertMethod.ASSERT_NOT_NULL.equals(assertMethod))
                return true;
        }

        return super.isValuable(node);
    }

    @Override
    protected boolean isHaveExpected(ValueDataNode dataNode) {
        if (dataNode.getAssertMethod() == null)
            return false;

        if (notNeedValue(dataNode))
            return true;

        ValueDataNode expectedNode = Search2.getExpectedValue(dataNode);
        if (expectedNode != null) {
            return expectedNode.haveValue();
        }

        if (expectedGlobalMap.containsKey(dataNode)) {
            return expectedGlobalMap.get(dataNode).haveValue();
        }

        return super.isHaveExpected(dataNode);
    }

//    private Table.Cell[] findParamValue(ValueDataNode dataNode) {
//
//        Map<ValueDataNode, ValueDataNode> map = sut.getInputToExpectedOutputMap();
//
//        Table.Cell[] valueCells = findValuesActualCase(dataNode);
//
//        ValueDataNode expectedNode = Search2.getExpectedValue(dataNode);
//        if (expectedNode != null) {
//            String expectedValue = getNodeValue(expectedNode);
//            if (expectedValue != null && !expectedValue.equals("<<null>>")) {
//                String expectedName = expectedNode.getVituralName();
//                valueCells = findValuesExpectCase(dataNode, expectedName);
//            }
//        }
//
//        return valueCells;
//    }
}
