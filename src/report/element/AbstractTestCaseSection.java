package report.element;

import testcase_execution.result_trace.gtest.Execution;
import testcase_manager.ITestCase;

public abstract class AbstractTestCaseSection extends Section {

    public AbstractTestCaseSection(ITestCase testCase, Execution execution) {
        super(testCase.getName());

        generate(testCase, execution);
    }

    protected abstract void generate(ITestCase testCase, Execution execution);
}
