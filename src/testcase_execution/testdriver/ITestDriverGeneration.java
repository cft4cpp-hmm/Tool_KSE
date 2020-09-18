package testcase_execution.testdriver;

import com.dse.testcase_execution.DriverConstant;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;

import java.util.List;

/**
 * Generate test driver
 *
 * @author Vu + D.Anh
 */
public interface ITestDriverGeneration {

	String TEST_PATH_TAG = DriverConstant.TEST_PATH_FILE_TAG;
	String CLONED_SOURCE_FILE_PATH_TAG = DriverConstant.INCLUDE_CLONE_TAG;
	String TEST_SCRIPTS_TAG = DriverConstant.TEST_SCRIPTS_TAG;
	String EXEC_TRACE_FILE_TAG = DriverConstant.EXEC_TRACE_FILE_TAG;

	// Some test cases needs to include specific headers
	// generated by automated test data generation
	String ADDITIONAL_HEADERS_TAG = DriverConstant.ADDITIONAL_HEADERS_TAG;

	String COMPOUND_TEST_CASE_SETUP = DriverConstant.COMPOUND_SET_UP;
	String COMPOUND_TEST_CASE_TEARDOWN = DriverConstant.COMPOUND_TEAR_DOWN;

	String C_DEBUG_TEST_DRIVER_PATH = "/test-driver-templates/testdriver_debug.c";
	String CPP_DEBUG_TEST_DRIVER_PATH = "/test-driver-templates/testdriver_debug.cpp";
	String C_TEST_DRIVER_PATH = "/test-driver-templates/testdriver.c";
	String CPP_TEST_DRIVER_PATH = "/test-driver-templates/testdriver.cpp";

	String getTestDriverTemplate();

	void generate() throws Exception;

	void setTestCase(ITestCase testCases);

	ITestCase getTestCase();

	String generateTestScript(TestCase testCase) throws Exception;

	List<String> getTestScripts();

	void setTestScripts(List<String> testScripts);

	String getTestDriver();

	String getTestPathFilePath();

	void setTestPathFilePath(String testPathFilePath);

	List<String> getClonedFilePaths();

	void setClonedFilePaths(List<String> paths);
}
