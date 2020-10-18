package testcase_execution.testdriver;

import testcase_execution.DriverConstant;

import utils.Utils;

/**
 * Old name: TestdriverGenerationforCpp
 *
 * Generate test driver for function put in an .cpp file in executing test data entering by users
 * <p>
 * comparing EO and RO
 *
 * @author ducanhnguyen
 */
public class TestDriverGenerationForCpp extends TestDriverGeneration {

    @Override
    public String getTestDriverTemplate() {
        return Utils.readResourceContent(CPP_TEST_DRIVER_PATH);
    }

    protected String wrapScriptInTryCatch(String script) {
        return String.format(
                "try {\n" +
                        "%s\n" +
                        "} catch (std::exception& error) {\n" +
                        DriverConstant.MARK + "(\"Phat hien loi runtime\");\n" +
                        "}", script);
    }
}
