package Common;

import java.io.File;

public class TestConfig
{
    public static String PROJECT_PATH = "F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2";
    public static String WORKSPACE = "Workspace";
    public static String EXE_PATH = PROJECT_PATH + "\\exe";
    public static String TEST_DRIVER_PATH = PROJECT_PATH + "\\TestDriver";
    public static String COMPILE_OUTPUT = PROJECT_PATH + "\\BuildOutput";
    public static String LINK_OUTPUT = PROJECT_PATH + "\\LinkOutput";
    public static String TESTPATH_FILE = PROJECT_PATH + "\\Testpath";
    public static String INSTRUMENTED_CODE = PROJECT_PATH + "\\InstrumentedCode";

    public static String TESTCASE_NAME = "TestCase1";
    public static String COMPILE_COMMAND_TEMPLATE = "g++ -c -std\u003dc++14 \"%s\" -o\"%s\" -lgtest_main  -lgtest -w";
    public static String LINK_COMMAND_TEMPLATE = "g++ -std\u003dc++14 \"%s\" -o\"%s\" -lgtest_main  -lgtest -w";
    public static String UET_IGNORE_FILE = ".uetignore";
    public static String C_EXTENTION = ".c";
    public static String CPP_EXTENTION = ".cpp";

    public static void SetProjectPath(String projectPath)
    {
        PROJECT_PATH = projectPath;
        String parentPath = new File(PROJECT_PATH).getParent();

        EXE_PATH = parentPath + "\\" + WORKSPACE + "\\exe";
        TEST_DRIVER_PATH = parentPath + "\\" + WORKSPACE + "\\TestDriver";
        COMPILE_OUTPUT = parentPath + "\\" + WORKSPACE + "\\BuildOutput";
        LINK_OUTPUT = parentPath + "\\" + WORKSPACE + "\\LinkOutput";
        TESTPATH_FILE = parentPath + "\\" + WORKSPACE + "\\Testpath";
        INSTRUMENTED_CODE = parentPath + "\\" + WORKSPACE + "\\InstrumentedCode";
    }
}
