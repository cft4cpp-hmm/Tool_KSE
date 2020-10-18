package testcase_execution.testdriver;

import Common.DSEConstants;
import Common.TestConfig;
import HybridAutoTestGen.TestData;
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
import javafx.util.Pair;

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

        String testCaseName = testCase.getName();

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
    //include uetignore file
    protected String generateIncludePaths() {
        String includedPart = "";
        String sourceFileName = ((TestCase) testCase).getRealParentSourceFileName();
        sourceFileName = sourceFileName.substring(0, sourceFileName.lastIndexOf("."));

        if (testCase instanceof TestCase) {
            String path = TestConfig.INSTRUMENTED_CODE + "\\" + sourceFileName + TestConfig.UET_IGNORE_FILE + TestConfig.CPP_EXTENTION;
            clonedFilePaths.add(path);

            includedPart += String.format("#include \"%s\"\n", path);
        }

        return includedPart;
    }

    protected String generateBodyScript(TestCase testCase) throws Exception {
        // STEP 1: assign aka test case name
        String testCaseNameAssign = String.format("%s=\"%s\";", TestConfig.UET_TEST_CASE_NAME, testCase.getName());

        // STEP 2: Generate initialization of variables
        String initialization = generateInitialization(testCase);

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
                    "}",
                testCaseNameAssign,
                initialization,
                functionCall,
                increaseFcall
        );

        singleScript = wrapScriptInTryCatch(singleScript);
        return singleScript;
    }

    protected String generateInitialization(TestCase testCase) throws Exception {
        String initialization = "";

        TestData testData = testCase.getTestData();

        IFunctionNode functionNode = testCase.getFunctionNode();

        List<String> declaredArrayName = new ArrayList<>();

        if (testData != null) {
            for (Pair<String, Object> child : testData.getTestData()) {

                for (IVariableNode v : functionNode.getArguments())
                {
                    String key = child.getKey();

                    boolean isArray = false;
                    String arrayName = "";
                    int elementCount = 0;

                    if (key.contains("["))
                    {
                        //array type
                        key = key.substring(0, key.indexOf("["));
                        isArray = true;
                        arrayName = key;

                        //search in testdata list to count the number of element of the array
                        for (Pair<String, Object> td : testData.getTestData())
                        {
                            String tempKey =td.getKey();
                            if (tempKey.contains("[") && key.equals(tempKey.substring(0,tempKey.indexOf("["))))
                            {
                                elementCount += 1;
                            }
                        }

                    }

                    if (v.getName().equals(key))
                    {
                        if (!isArray)
                        {
                            initialization += v.getCoreType() + " " + v.getName() + " = " + child.getValue() + ";";
                        }
                        else
                        {
                            if (!declaredArrayName.contains(key))
                            {
                                initialization += v.getCoreType() + " " + key + "[" + elementCount + "];";
                                declaredArrayName.add(key);
                            }
                            initialization += child.getKey() + " = " + child.getValue() + ";";
                        }

                        break;
                    }
                }
            }
        }

        initialization = initialization.replace(DriverConstant.MARK + "(\"<<PRE-CALLING>>\");",
                String.format(DriverConstant.MARK + "(\"<<PRE-CALLING>> Test %s\");", testCase.getName()));

        initialization = initialization.replaceAll("\\bconst\\s+\\b", SpecialCharacter.EMPTY);

        return initialization;
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

        String functionCall;

        if (functionNode instanceof ConstructorNode) {
            return SpecialCharacter.EMPTY;
        }

        functionCall = getFullFunctionCall(functionNode);

        functionCall = functionCall.replaceAll("\\bmain\\b", "UET_MAIN");

        functionCall = String.format(DriverConstant.MARK + "(\"<<PRE-CALLING>> Test %s\");%s", testCase.getName(), functionCall);

        return functionCall;
    }

    public static String getFullFunctionCall(IFunctionNode functionNode) {
        INode realParent = functionNode.getParent();

        if (functionNode instanceof IFunctionNode) {
            INode tmpRealParent = ((IFunctionNode) functionNode).getRealParent();
            if (tmpRealParent != null)
                realParent = tmpRealParent;
        }

        StringBuilder functionCall = new StringBuilder();

        if (realParent instanceof SourcecodeFileNode) {
            functionCall.append(functionNode.getSimpleName())
                    .append(generateCallOfArguments(functionNode));

        }

        return functionCall.toString();
    }
    public static StringBuilder generateCallOfArguments(IFunctionNode functionNode){
        StringBuilder functionCall = new StringBuilder();
        functionCall.append("(");
        for (IVariableNode v : functionNode.getArguments())
        {
            functionCall.append(v.getName()).append(",");
        }
        functionCall.append(")");
        functionCall = new StringBuilder(functionCall.toString().replace(",)", ")") + SpecialCharacter.END_OF_STATEMENT);
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
