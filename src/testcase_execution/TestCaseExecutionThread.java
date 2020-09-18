package testcase_execution;

import com.dse.coverage.AbstractCoverageManager;
import com.dse.environment.Environment;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.report.ExecutionResultReport;
import com.dse.report.ReportManager;
import com.dse.testcase_manager.*;
import com.dse.thread.AbstractAkaTask;
import com.dse.util.AkaLogger;

import java.time.LocalDateTime;

public class TestCaseExecutionThread extends AbstractAkaTask<ITestCase> {
    private final static AkaLogger logger = AkaLogger.get(TestCaseExecutionThread.class);
    private ITestCase testCase;
    private int executionMode = TestcaseExecution.IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE;//TestcaseExecution.IN_EXECUTION_WITHOUT_GTEST_MODE;// execute with google test, execute without google test, v.v.
    boolean shouldShowReport = true;

    public TestCaseExecutionThread(ITestCase testCase) {
        this.testCase = testCase;
        this.testCase.setStatus(TestCase.STATUS_NA);
        setOnSucceeded(event -> {
            // Generate test case data report
            ExecutionResultReport report = new ExecutionResultReport(testCase, LocalDateTime.now());
            ReportManager.export(report);

            if (shouldShowReport)
                MDIWindowController.getMDIWindowController().viewReport(testCase.getName(), report.toHtml());
        });
    }

    @Override
    protected ITestCase call() {
        testCase.setStatus(TestCase.STATUS_EXECUTING);

        try {
            if (testCase instanceof TestCase)
                return callWithTestCase((TestCase) testCase);
            else if (testCase instanceof CompoundTestCase)
                return callWithCompoundTestCase((CompoundTestCase) testCase);
        } catch (Exception e) {
            testCase.setStatus(TestCase.STATUS_FAILED);
            e.printStackTrace();
            logger.error("Can not execute test case " + testCase.getName());
        }

        return testCase;
    }

    private ITestCase callWithTestCase(TestCase testCase) throws Exception {
        TestCaseManager.exportBasicTestCaseToFile(testCase);
        TestCasesNavigatorController.getInstance().refreshNavigatorTree();

        ICommonFunctionNode function = testCase.getRootDataNode().getFunctionNode();

        // execute test case
        TestcaseExecution executor = new TestcaseExecution();
        executor.setFunction(function);
        if (testCase.getFunctionNode() == null)
            testCase.setFunctionNode(function);
        executor.setTestCase(testCase);
        executor.setMode(getExecutionMode());
        executor.execute();

        // run after the thread is done
        TestCaseManager.exportBasicTestCaseToFile(testCase);
        // export coverage of testcase to file
        AbstractCoverageManager.exportCoveragesOfTestCaseToFile(testCase, Environment.getInstance().getTypeofCoverage());
        TestCasesNavigatorController.getInstance().refreshNavigatorTree();

        return testCase;
    }

    private ITestCase callWithCompoundTestCase(CompoundTestCase testCase) throws Exception {
        TestCaseManager.exportCompoundTestCaseToFile(testCase);
        TestCasesNavigatorController.getInstance().refreshNavigatorTree();

        CompoundTestcaseExecution executor = new CompoundTestcaseExecution();
        executor.setTestCase(testCase);
        executor.setMode(getExecutionMode());
        executor.execute();

        // run after the thread is done
        TestCaseManager.exportCompoundTestCaseToFile(testCase);

        String coverageType = Environment.getInstance().getTypeofCoverage();
        for (TestCaseSlot slot : testCase.getSlots()) {
            TestCase element = TestCaseManager.getBasicTestCaseByName(slot.getTestcaseName());
            assert element != null;
            AbstractCoverageManager.exportCoveragesOfTestCaseToFile(element, coverageType);
        }

        TestCasesNavigatorController.getInstance().refreshNavigatorTree();
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
