package testcase_execution.testdriver;

import Common.DSEConstants;
import Common.TestConfig;
import compiler.AvailableCompiler;
import compiler.Compiler;
import project_init.IGTestConstant;
import testcase_execution.DriverConstant;
import testcase_manager.ITestCase;
import testcase_manager.TestCase;
import tree.object.*;
import utils.PathUtils;
import utils.SpecialCharacter;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class TestDriverGeneration implements ITestDriverGeneration {

    protected List<String> testScripts;

    protected ITestCase testCase;

    protected String testPathFilePath;

    protected String testDriver = SpecialCharacter.EMPTY;

    protected List<String> clonedFilePaths;

    @Override
    public void generate() throws Exception {
        testPathFilePath = TestConfig.TESTPATH_FILE + "\\" + (testCase.getName()) + ".tp";

        testScripts = new ArrayList<>();
        clonedFilePaths = new ArrayList<>();

        if (testCase instanceof TestCase) {
            String script = generateTestScript((TestCase) testCase);
            testScripts.add(script);
        }

        StringBuilder testScriptPart = new StringBuilder();
        for (String item : testScripts) {
            testScriptPart.append(item).append(SpecialCharacter.LINE_BREAK);
        }

        String includedPart = generateIncludePaths();
        String additionalIncludes = generateAdditionalHeaders();

        testDriver = getTestDriverTemplate()
                .replace(TEST_PATH_TAG, Utils.doubleNormalizePath(testPathFilePath))
                .replace(CLONED_SOURCE_FILE_PATH_TAG, includedPart)
                .replace(TEST_SCRIPTS_TAG, testScriptPart.toString())
                .replace(ADDITIONAL_HEADERS_TAG, additionalIncludes)
                //.replace(EXEC_TRACE_FILE_TAG, testCase.getExecutionResultTrace())
                .replace(DriverConstant.ADD_TESTS_TAG, generateAddTestStm(testCase));
    }

    @Override
    public String generateTestScript(TestCase testCase) throws Exception {
        String body = generateBodyScript(testCase);

        String testCaseName = TestConfig.TESTCASE_NAME;

        return String.format("void " + UET_TEST_PREFIX + "%s(void) {\n%s\n}\n", testCaseName, body);
    }

    protected static final String UET_TEST_PREFIX = "UET_TEST_";

    private String generateAddTestStm(ITestCase testCase) {
        StringBuilder out = new StringBuilder();

        if (testCase instanceof TestCase) {
            String runStm = generateRunStatement((TestCase) testCase, 1);
            out.append(runStm);
        }

        return out.toString();
    }

    private String generateRunStatement(TestCase testCase, int iterator) {
        String testCaseName = testCase.getName();
        String testName = testCaseName.toUpperCase();
        testCaseName = testCaseName.replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE);
        String test = UET_TEST_PREFIX + testCaseName;
        return String.format(RUN_FORMAT, testName, test, iterator);
    }

    private static final String RUN_FORMAT = "\t" + DriverConstant.RUN_TEST + "(\"%s\", &%s, %d);\n";

    private String generateAdditionalHeaders() {
        StringBuilder builder = new StringBuilder();

//        if (testCase.getAdditionalHeaders() != null)
//            builder.append(testCase.getAdditionalHeaders()).append(SpecialCharacter.LINE_BREAK);

        return builder.toString();
    }

    public boolean isC() {
        return !getCompiler().getName().contains("C++");
    }

    private Compiler createTemporaryCompiler(String opt)
    {
        if (opt != null)
        {
            for (Class<?> c : AvailableCompiler.class.getClasses())
            {
                try
                {
                    String name = c.getField("NAME").get(null).toString();

                    if (name.equals(opt))
                    {
                        return new Compiler(c);
                    }
                }
                catch (Exception ex)
                {
                }
            }
        }

        return null;
    }
    public Compiler getCompiler()
    {
        Compiler compiler = createTemporaryCompiler("[GNU Native] C++ 11");

        compiler.setCompileCommand(AvailableCompiler.CPP_11_GNU_NATIVE.COMPILE_CMD);
        compiler.setPreprocessCommand(AvailableCompiler.CPP_11_GNU_NATIVE.PRE_PRECESS_CMD);
        compiler.setLinkCommand(AvailableCompiler.CPP_11_GNU_NATIVE.LINK_CMD);
        compiler.setDebugCommand(AvailableCompiler.CPP_11_GNU_NATIVE.DEBUG_CMD);
        compiler.setIncludeFlag(AvailableCompiler.CPP_11_GNU_NATIVE.INCLUDE_FLAG);
        compiler.setDefineFlag(AvailableCompiler.CPP_11_GNU_NATIVE.DEFINE_FLAG);
        compiler.setOutputFlag(AvailableCompiler.CPP_11_GNU_NATIVE.OUTPUT_FLAG);
        compiler.setDebugFlag(AvailableCompiler.CPP_11_GNU_NATIVE.DEBUG_FLAG);
        compiler.setOutputExtension(AvailableCompiler.CPP_11_GNU_NATIVE.OUTPUT_EXTENSION);

        return compiler;
    }
    protected String generateIncludePaths() {
        String includedPart = "";

        if (testCase instanceof TestCase) {
            String path = TestConfig.PROJECT_PATH;
            clonedFilePaths.add(path);

            includedPart += String.format("#include \"%s\"\n", path);

            if (!isC()) {
                IFunctionNode sut = ((TestCase) testCase).getFunctionNode();

                if (sut instanceof AbstractFunctionNode) {
                    INode realParent = ((AbstractFunctionNode) sut).getRealParent();
                    if (realParent == null) realParent = sut.getParent();

                    while (!(realParent instanceof SourcecodeFileNode)) {
                        if (realParent instanceof NamespaceNode)
                            break;

                        realParent = realParent.getParent();
                    }
                }
            }

        }

        return includedPart;
    }

    protected String generateBodyScript(TestCase testCase) throws Exception {
        // STEP 1: assign aka test case name
        String testCaseNameAssign = String.format("%s=\"%s\";", "TestCase1", testCase.getName());

        // STEP 2: Generate initialization of variables
        String initialization = "";

        // STEP 3: Generate full function call
        String functionCall = generateFunctionCall(testCase);

        // STEP 4: FCALLS++ - Returned from UUT
        String increaseFcall;
        if (testCase.getFunctionNode() instanceof ConstructorNode)
            increaseFcall = SpecialCharacter.EMPTY;
        else
            increaseFcall = IGTestConstant.INCREASE_FCALLS + generateReturnMark(testCase);


        // STEP 5: Repeat iterator
        String singleScript = String.format(
                    "{\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                    "}",
                testCaseNameAssign,
                //testCase.getTestCaseUserCode().getSetUpContent(),
                initialization,
                functionCall,
                increaseFcall
                //testCase.getTestCaseUserCode().getTearDownContent()
        );

//        StringBuilder script = new StringBuilder();
//        for (int i = 0; i < iterator; i++)
//            script.append(singleScript).append(SpecialCharacter.LINE_BREAK);

        // STEP 6: mark beginning and end of test case
//        script = new StringBuilder(wrapScriptInMark(testCase, script.toString()));
//        script = new StringBuilder(wrapScriptInTryCatch(script.toString()));
//
//        return script.toString();
        singleScript = wrapScriptInTryCatch(singleScript);
        return singleScript;
    }

    protected String generateReturnMark(TestCase testCase) {
        IFunctionNode sut = testCase.getFunctionNode();

        String markStm = "";

        if (sut instanceof FunctionNode) {
            String relativePath = PathUtils.toRelative(sut.getAbsolutePath());
            markStm = String.format(DriverConstant.MARK + "(\"Return from: %s\");", Utils.doubleNormalizePath(relativePath));
        }

        return markStm;
    }

    protected abstract String wrapScriptInTryCatch(String script);

//    protected String wrapScriptInMark(TestCase testCase, String script) {
//        String beginMark = generateTestPathMark(MarkPosition.BEGIN, testCase);
//        String endMark = generateTestPathMark(MarkPosition.END, testCase);
//
//        return beginMark + SpecialCharacter.LINE_BREAK + script + endMark;
//    }
//
//    enum MarkPosition {
//        BEGIN,
//        END
//    }
//
//    private String generateTestPathMark(MarkPosition pos, TestCase testCase) {
//        return String.format(DriverConstant.MARK + "(\"%s OF %s\");", pos, testCase.getName().toUpperCase());
//    }

    protected String generateFunctionCall(TestCase testCase) {
        IFunctionNode functionNode = testCase.getFunctionNode();

        String functionCall = "";

        if (functionNode instanceof ConstructorNode) {
            return SpecialCharacter.EMPTY;
        }

        String returnType = functionNode.getReturnType().trim();


        functionCall = functionCall.replaceAll("\\bmain\\b", "UET_MAIN");

        functionCall = String.format(DriverConstant.MARK + "(\"<<PRE-CALLING>> Test %s\");%s", testCase.getName(), functionCall);

        return functionCall;
    }

    @Override
    public String toString() {
        return "TestDriverGeneration: " + testDriver;
    }

    @Override
    public List<String> getTestScripts() {
        return testScripts;
    }

    @Override
    public void setTestScripts(List<String> testScripts) {
        this.testScripts = testScripts;
    }

    @Override
    public String getTestDriver() {
        return testDriver;
    }

    public String getTestPathFilePath() {
        return testPathFilePath;
    }

    public void setTestPathFilePath(String testPathFilePath) {
        this.testPathFilePath = testPathFilePath;
    }

    @Override
    public ITestCase getTestCase() {
        return testCase;
    }

    @Override
    public void setTestCase(ITestCase testCase) {
        this.testCase = testCase;
    }

    @Override
    public List<String> getClonedFilePaths() {
        return clonedFilePaths;
    }

    @Override
    public void setClonedFilePaths(List<String> clonedFilePaths) {
        this.clonedFilePaths = clonedFilePaths;
    }
}
