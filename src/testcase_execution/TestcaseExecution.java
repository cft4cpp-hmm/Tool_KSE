package testcase_execution;

import Common.TestConfig;
import config.CommandConfig;
import javafx.scene.control.Alert;
import parser.projectparser.ICommonFunctionNode;
import testcase_execution.testdriver.TestDriverGeneration;
import testcase_execution.testdriver.TestDriverGenerationForC;
import testcase_execution.testdriver.TestDriverGenerationForCpp;
import testcase_manager.ITestCase;
import testcase_manager.TestCase;
import tree.object.IFunctionNode;
import utils.Utils;

import java.util.List;


import java.io.File;

/**
 * Execute a test case
 */
public class TestcaseExecution extends AbstractTestcaseExecution
{
    /**
     * node corresponding with subprogram under test
     */
    private IFunctionNode function;

    @Override
    public void execute() throws Exception
    {
        if (!(getTestCase() instanceof TestCase))
        {
            return;
        }

        TestCase testCase = (TestCase) getTestCase();

        //delete old execution result
        String testPathFile = TestConfig.TESTPATH_FILE + "\\" + testCase.getName() + TestConfig.TESTPATH_EXTENTION;
        utils.Utils.deleteFileOrFolder(new File(testPathFile));

        testDriverGen = generateTestDriver(testCase);

        if (testDriverGen != null)
        {
            if (getMode() != IN_AUTOMATED_TESTDATA_GENERATION_MODE)
            {
            }

            String compileAndLinkMessage = compileAndLink();
            String executableFile = TestConfig.EXE_PATH + "\\" + testCase.getName() + TestConfig.EXE_EXTENTION;

            // Run the executable file
            if (new File(executableFile).exists())
            {

                String message = runExecutableFile(executableFile);
                //testCase.setExecuteLog(message);


                if (getMode() == IN_DEBUG_MODE)
                {
                    // nothing to do
                }
                else
                {
                    if (new File(((TestCase) testCase).getSourceCodeFile()).exists())
                    {
                        //refactorResultTrace(testCase);
                        boolean completed = analyzeTestpathFile(testCase);

                        if (!completed)
                        {
                            String msg = "Runtime error " + ((TestCase) testCase).getSourceCodeFile();
                            //testCase.setStatus(ITestCase.STATUS_RUNTIME_ERR);
                            return;
                        }

                    }
                    else
                    {
                        String msg = "Does not found the test path file when executing " + ((TestCase) testCase).getSourceCodeFile();

                        if (getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE)
                        {
                            //testCase.setStatus(TestCase.STATUS_FAILED);
                            return;
                        }
                    }
                }
            }
            else
            {
                String msg = "Cannot generate executable file " + testCase.getFunctionNode().getAbsolutePath() + "\nError:" + compileAndLinkMessage;

                if (getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE)
                {
                    //testCase.setStatus(TestCase.STATUS_FAILED);
                    return;
                }
                else if (getMode() == IN_AUTOMATED_TESTDATA_GENERATION_MODE)
                {
                    //testCase.setStatus(TestCase.STATUS_FAILED);
                    return;
                }

                //testCase.setStatus(TestCase.STATUS_FAILED);
                return;
            }

        }
        else
        {
            String msg = "Can not generate test driver of the test case for the function "
                    + testCase.getFunctionNode().getAbsolutePath();
            if (getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE)
            {
                //testCase.setStatus(TestCase.STATUS_FAILED);
                return;
            }
        }
        //testCase.setStatus(TestCase.STATUS_SUCCESS);
    }

    public void execute(List<TestCase> testCaseList) throws Exception
    {
        if (testCaseList == null || testCaseList.size() == 0)
        {
            return;
        }

        for (TestCase testCase : testCaseList)
        {
            //delete old execution result
            String testPathFile = TestConfig.TESTPATH_FILE + "\\" + testCase.getName() + TestConfig.TESTPATH_EXTENTION;
            utils.Utils.deleteFileOrFolder(new File(testPathFile));

            testDriverGen = generateTestDriver(testCase);

            if (testDriverGen != null)
            {
                String compileAndLinkMessage = compileAndLink(testCase);

                String executableFile = TestConfig.EXE_PATH + "\\" + testCase.getName() + TestConfig.EXE_EXTENTION;

                // Run the executable file
                if (new File(executableFile).exists())
                {
                    String message = runExecutableFile(executableFile);
                }

            }
        }
    }

    public void analyzeTestpathFiles()
    {

    }

    public TestDriverGeneration generateTestDriver(ITestCase testCase) throws Exception
    {
        TestDriverGeneration testDriver = null;

        switch (getMode())
        {
            case IN_AUTOMATED_TESTDATA_GENERATION_MODE:

            case IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE:
                if (Utils.isC())
                {
                    testDriver = new TestDriverGenerationForC();

                }
                else
                {
                    testDriver = new TestDriverGenerationForCpp();
                }
                break;
        }

        if (testDriver != null)
        {
            // generate test driver
            testDriver.setTestCase(testCase);
            testDriver.generate();
            String testdriverContent = testDriver.getTestDriver();

            String testDriverFile = TestConfig.TEST_DRIVER_PATH + "\\" + (testCase.getName()) + ".cpp";

            Utils.writeContentToFile(testdriverContent, testDriverFile);

        }

        return testDriver;
    }

    public IFunctionNode getFunction()
    {
        return function;
    }

    public void setFunction(IFunctionNode function)
    {
        this.function = function;
    }
}