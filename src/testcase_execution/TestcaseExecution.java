package testcase_execution;

import config.CommandConfig;
import javafx.scene.control.Alert;
import parser.projectparser.ICommonFunctionNode;
import testcase_execution.testdriver.TestDriverGeneration;
import testcase_execution.testdriver.TestDriverGenerationForC;
import testcase_execution.testdriver.TestDriverGenerationForCpp;
import testcase_manager.ITestCase;
import testcase_manager.TestCase;
import utils.Utils;


import java.io.File;

/**
 * Execute a test case
 */
public class TestcaseExecution extends AbstractTestcaseExecution
{
    /**
     * node corresponding with subprogram under test
     */
    private ICommonFunctionNode function;

    @Override
    public void execute() throws Exception {
        if (!(getTestCase() instanceof TestCase)) {
            return;
        }

        TestCase testCase = (TestCase) getTestCase();

        initializeConfigurationOfTestcase(testCase);
        testCase.deleteOldDataExceptValue();

        // create the right version of test driver generation
//        UILogger.getUiLogger().log("Generating test driver of test case " + testCase.getPath());
        testDriverGen = generateTestDriver(testCase);

        if (testDriverGen != null) {
            if (getMode() != IN_AUTOMATED_TESTDATA_GENERATION_MODE) {
//                UILogger.getUiLogger().log("Generating stub code of test case " + testCase.getPath());
            }

            CommandConfig testCaseCommandConfig = new CommandConfig().fromJson(testCase.getCommandConfigFile());

            String compileAndLinkMessage = compileAndLink(testCaseCommandConfig);
//            logger.debug(String.format("Compile & Link Message:\n%s\n", compileAndLinkMessage));

            // Run the executable file
            if (new File(testCase.getExecutableFile()).exists()) {

                String message = runExecutableFile(testCaseCommandConfig);
                testCase.setExecuteLog(message);


                if (getMode() == IN_DEBUG_MODE) {
                    // nothing to do
                } else {
                    if (new File(testCase.getTestPathFile()).exists()) {
                        refactorResultTrace(testCase);
                        boolean completed = analyzeTestpathFile(testCase);

                        if (!completed) {
                            String msg = "Runtime error " + testCase.getExecutableFile();

//                            if (/*getMode() == IN_EXECUTION_WITHOUT_GTEST_MODE
//                                ||*/ getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE) {
                                testCase.setStatus(ITestCase.STATUS_RUNTIME_ERR);
//                                TestCaseManager.exportBasicTestCaseToFile(testCase);
                                return;
//                            }
                        }

                    } else {
                        String msg = "Does not found the test path file when executing " + testCase.getExecutableFile();

                        if (/*getMode() == IN_EXECUTION_WITHOUT_GTEST_MODE
                                ||*/ getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE) {
//                            UILogger.getUiLogger().log("Execute " + testCase.getPath() + " failed.\nMessage = " + msg);
                            testCase.setStatus(TestCase.STATUS_FAILED);
                            return;
                        }
                        //throw new Exception(msg);
                    }
                }
            } else {
                String msg = "Can not generate executable file " + testCase.getFunctionNode().getAbsolutePath() + "\nError:" + compileAndLinkMessage;

                if (/*getMode() == IN_EXECUTION_WITHOUT_GTEST_MODE
                        ||*/ getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE) {
//                    UILogger.getUiLogger().log("Execute " + testCase.getPath() + " failed.\nMessage = " + msg);
                    testCase.setStatus(TestCase.STATUS_FAILED);
                    return;
                } else if (getMode() == IN_AUTOMATED_TESTDATA_GENERATION_MODE) {
                    testCase.setStatus(TestCase.STATUS_FAILED);
                    return;
                }

                testCase.setStatus(TestCase.STATUS_FAILED);
                return;
//                throw new Exception(msg);
            }

        } else {
            String msg = "Can not generate test driver of the test case for the function "
                    + testCase.getFunctionNode().getAbsolutePath();
            //logger.debug(msg);
            if (/*getMode() == IN_EXECUTION_WITHOUT_GTEST_MODE
                    ||*/ getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE) {
                //UIController.showErrorDialog(msg, "Test driver generation", "Fail");
//                UILogger.getUiLogger().log("Can not generate test driver for " + testCase.getPath() + ".\nMessage = " + msg);
                testCase.setStatus(TestCase.STATUS_FAILED);
                return;
            }
//            throw new Exception(msg);
        }
        testCase.setStatus(TestCase.STATUS_SUCCESS);
    }

    public boolean isC() {
        return !getCompiler().getName().contains("C++");
    }
    public TestDriverGeneration generateTestDriver(ITestCase testCase) throws Exception {
        TestDriverGeneration testDriver = null;

        // create the right version of test driver generation
        switch (getMode()) {
            case IN_AUTOMATED_TESTDATA_GENERATION_MODE:

            case IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE:
                /*case IN_EXECUTION_WITHOUT_GTEST_MODE: */{
                initializeCommandConfigToRunTestCase(testCase, true);
                if (isC()){
//                if (Utils.getSourcecodeFile(function) instanceof CFileNode) {
                    testDriver = new TestDriverGenerationForC();

                } else {
                    testDriver = new TestDriverGenerationForCpp();
                }
                break;
            }//                    testDriver = new TestDriverGenerationforCWithGoogleTest();

        }

        if (testDriver != null) {
            // generate test driver
            testDriver.setTestCase(testCase);
            testDriver.generate();
            String testdriverContent = testDriver.getTestDriver();

            Utils.writeContentToFile(testdriverContent, testCase.getSourceCodeFile());

        }

        return testDriver;
    }

    public ICommonFunctionNode getFunction() {
        return function;
    }

    public void setFunction(ICommonFunctionNode function) {
        this.function = function;
    }
}