package report.converter;

import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.report.element.Table;
import com.dse.testdata.Iterator;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.SubprogramNode;
import com.dse.testdata.object.ValueDataNode;

import java.util.List;

public class MiddleAssertionConverter extends AssertionConverter {
    private int iterator;

    public MiddleAssertionConverter(List<IResultTrace> failures, int fcalls, int iterator) {
        super(failures, fcalls);
        this.iterator = iterator;
    }

    @Override
    public Table execute(SubprogramNode root) {
        Table table = generateHeaderRows(root);

        for (IDataNode child : root.getChildren()) {
            ValueDataNode node = getCorrespondingIterator((ValueDataNode) child);
            table = recursiveConvert(table, node, 2);
        }

        return table;
    }

    private ValueDataNode getCorrespondingIterator(ValueDataNode dataNode) {
        for (Iterator iterator : dataNode.getIterators()) {
            int start = iterator.getStartIdx();
            int repeat = iterator.getRepeat();

            if (repeat == Iterator.FILL_ALL
                    || (this.iterator >= start && this.iterator < start + repeat)) {
                return iterator.getDataNode();
            }
        }

        return dataNode;
    }
}
