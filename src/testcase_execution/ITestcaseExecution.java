package testcase_execution;

import config.CommandConfig;
import testcase_execution.testdriver.TestDriverGeneration;
import testcase_manager.ITestCase;

import java.io.IOException;

/**
 * Execute a test case
 */
public interface ITestcaseExecution {
    /**
     * Execute the test data in debug mode. In this mode, there is no comparison between Expected Output and Real Output
     */
    int IN_DEBUG_MODE = 0;

    /**
     * Execute the test data in debug mode. In this mode, there is no comparison between Expected Output and Real Output
     */
    int IN_AUTOMATED_TESTDATA_GENERATION_MODE = 1;

//    /**
//     * Execute the test data entering by users. In this mode, there is no comparison between Expected Output and Real Output
//     */
//    int IN_EXECUTION_WITHOUT_GTEST_MODE = 2;

    /**
     * Cunit or Google Test
     *
     * Execute the test data in debug mode. In this mode, there is a comparison between Expected Output and Real Output
     */
    int IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE = 3;

    int getMode();

    void setMode(int mode);

    void execute() throws Exception;

    TestDriverGeneration generateTestDriver(ITestCase testCase) throws Exception;

    String compileAndLink() throws IOException, InterruptedException;
}