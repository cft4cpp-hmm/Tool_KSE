package report.element;

import testcase_execution.result_trace.gtest.Execution;
import testcase_manager.ITestCase;

public abstract class AbstractExecutionResult extends Section {

    protected static final String C_FAILURE_TAG = "<CUNIT_RUN_TEST_FAILURE>";

    public AbstractExecutionResult(ITestCase testCase, Execution execution) {
        super(testCase.getName() + "-result");
        generate(testCase, execution);
    }

    protected abstract void generate(ITestCase testCase, Execution execution);
}
