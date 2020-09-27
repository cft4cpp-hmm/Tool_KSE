package testcase_execution;

import parser.projectparser.ICommonFunctionNode;
import testcase_manager.CompoundTestCase;
import testcase_manager.ITestCase;
import testcase_manager.TestCase;
import testcase_manager.TestCaseManager;
import tree.object.IFunctionNode;

import java.time.LocalDateTime;

public class TestCaseExecutionThread extends AbstractUETTask<ITestCase>
{
    private ITestCase testCase;
    private int executionMode = TestcaseExecution.IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE;//TestcaseExecution.IN_EXECUTION_WITHOUT_GTEST_MODE;// execute with google test, execute without google test, v.v.
    boolean shouldShowReport = true;

    public TestCaseExecutionThread(ITestCase testCase) {
        this.testCase = testCase;
        //this.testCase.setStatus(TestCase.STATUS_NA);
        setOnSucceeded(event -> {
            // Generate test case data report
//            ExecutionResultReport report = new ExecutionResultReport(testCase, LocalDateTime.now());
//            ReportManager.export(report);
//
//            if (shouldShowReport)
//                MDIWindowController.getMDIWindowController().viewReport(testCase.getName(), report.toHtml());
        });
    }

    @Override
    protected ITestCase call() {
        //testCase.setStatus(TestCase.STATUS_EXECUTING);

        try {
            if (testCase instanceof TestCase)
                return callWithTestCase((TestCase) testCase);
        } catch (Exception e) {
            //testCase.setStatus(TestCase.STATUS_FAILED);
            e.printStackTrace();
        }

        return testCase;
    }

    private ITestCase callWithTestCase(TestCase testCase) throws Exception {

        IFunctionNode function = testCase.getFunctionNode();

        // execute test case
        TestcaseExecution executor = new TestcaseExecution();
        executor.setFunction(function);
        if (testCase.getFunctionNode() == null)
            testCase.setFunctionNode(function);
        executor.setTestCase(testCase);
        executor.setMode(getExecutionMode());
        executor.execute();

        // run after the thread is done
//        TestCaseManager.exportBasicTestCaseToFile(testCase);
//        // export coverage of testcase to file
//        AbstractCoverageManager.exportCoveragesOfTestCaseToFile(testCase, Environment.getInstance().getTypeofCoverage());
//        TestCasesNavigatorController.getInstance().refreshNavigatorTree();

        return testCase;
    }



    public int getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(int executionMode) {
        this.executionMode = executionMode;
    }

    public void setShouldShowReport(boolean shouldShowReport) {
        this.shouldShowReport = shouldShowReport;
    }

    public boolean isShouldShowReport() {
        return shouldShowReport;
    }
}
