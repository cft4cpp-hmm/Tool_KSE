package testcase_execution;

import Common.TestConfig;
import cfg.testpath.ITestpathInCFG;
import compiler.AvailableCompiler;
import compiler.Compiler;
import compiler.Terminal;
import coverage.EnviroCoverageTypeNode;
import coverage.FunctionCoverageComputation;
import coverage.SourcecodeCoverageComputation;
import coverage.TestPathUtils;
import instrument.FunctionInstrumentationForStatementvsBranch_Markerv2;
import testcase_execution.testdriver.TestDriverGeneration;
import testcase_manager.ITestCase;
import testcase_manager.TestCase;
import testdata.object.TestpathString_Marker;
import tree.object.IFunctionNode;
import tree.object.ISourcecodeFileNode;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static Common.TestConfig.COMPILE_COMMAND_TEMPLATE;

public abstract class AbstractTestcaseExecution implements ITestcaseExecution
{
    private int mode = IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE;

    private ITestCase testCase;

    protected TestDriverGeneration testDriverGen;

    public int getMode()
    {
        return mode;
    }

    public void setMode(int mode)
    {
        this.mode = mode;
    }

    public ITestCase getTestCase()
    {
        return testCase;
    }

    public void setTestCase(ITestCase testcase)
    {
        this.testCase = testcase;
        //setTestCaseInfo();
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

    public String compileAndLink() throws IOException, InterruptedException
    {
        StringBuilder output = new StringBuilder();

        TestCase tc = (TestCase) testCase;

        String testCaseName = testCase.getName();
        String sourceFile = tc.getSourceCodeFile();
        String sourceFileName = (new File(sourceFile)).getName();
        String instrumentedFile = TestConfig.INSTRUMENTED_CODE + "\\" +
                sourceFileName.substring(0, sourceFileName.lastIndexOf(".")) +
                TestConfig.UET_IGNORE_FILE + TestConfig.CPP_EXTENTION;

        String outFile = TestConfig.COMPILE_OUTPUT + "\\" + testCaseName + ".out";

        String testDriverFile = TestConfig.TEST_DRIVER_PATH + "\\" +
                testCaseName + TestConfig.CPP_EXTENTION;

        String exeFile = TestConfig.EXE_PATH + "\\" + testCaseName + ".exe";

        String compilationCommand = String.format(TestConfig.COMPILE_COMMAND_TEMPLATE, testDriverFile, outFile);

        String linkCommand = String.format(TestConfig.LINK_COMMAND_TEMPLATE, outFile, exeFile);

        String[] script = CompilerUtils.prepareForTerminal(Utils.getCompiler(), compilationCommand);

        String response = new Terminal(script, TestConfig.COMPILE_OUTPUT).get();

        output.append(response).append("\n");

        String[] linkScript = CompilerUtils
                .prepareForTerminal(Utils.getCompiler(), linkCommand);
        String linkResponse = new Terminal(linkScript, TestConfig.LINK_OUTPUT).get();
        output.append(linkResponse);

        return output.toString().trim();
    }

    public String compileAndLink(TestCase testCase) throws IOException, InterruptedException
    {
        StringBuilder output = new StringBuilder();

        String testCaseName = testCase.getName();
        String sourceFile = testCase.getSourceCodeFile();
        String sourceFileName = (new File(sourceFile)).getName();
        String instrumentedFile = TestConfig.INSTRUMENTED_CODE + "\\" +
                sourceFileName.substring(0, sourceFileName.lastIndexOf(".")) +
                TestConfig.UET_IGNORE_FILE + TestConfig.CPP_EXTENTION;

        String outFile = TestConfig.COMPILE_OUTPUT + "\\" + testCaseName + ".out";

        String testDriverFile = TestConfig.TEST_DRIVER_PATH + "\\" +
                testCaseName + TestConfig.CPP_EXTENTION;

        String exeFile = TestConfig.EXE_PATH + "\\" + testCaseName + ".exe";

        String compilationCommand = String.format(TestConfig.COMPILE_COMMAND_TEMPLATE, testDriverFile, outFile);

        String linkCommand = String.format(TestConfig.LINK_COMMAND_TEMPLATE, outFile, exeFile);

        String[] script = CompilerUtils.prepareForTerminal(Utils.getCompiler(), compilationCommand);

        String response = new Terminal(script, TestConfig.COMPILE_OUTPUT).get();

        output.append(response).append("\n");

        String[] linkScript = CompilerUtils
                .prepareForTerminal(Utils.getCompiler(), linkCommand);
        String linkResponse = new Terminal(linkScript, TestConfig.LINK_OUTPUT).get();
        output.append(linkResponse);

        return output.toString().trim();
    }

    protected TestpathString_Marker readTestpathFromFile(ITestCase testCase) throws InterruptedException
    {
        TestpathString_Marker encodedTestpath = new TestpathString_Marker();

        int MAX_READ_FILE_NUMBER = 10;
        int countReadFile = 0;

        String testPathFile = TestConfig.TESTPATH_FILE + "\\" + testCase.getName() + TestConfig.TESTPATH_EXTENTION;

        do
        {
            encodedTestpath.setEncodedTestpath(normalizeTestpathFromFile(
                    Utils.readFileContent(testPathFile)));//testCase.getTestPathFile()

            if (encodedTestpath.getEncodedTestpath().length() == 0)
            {
                //initialization = "";
                Thread.sleep(10);
            }

            countReadFile++;
        }
        while (encodedTestpath.getEncodedTestpath().length() == 0 && countReadFile <= MAX_READ_FILE_NUMBER);

        return encodedTestpath;
    }

    protected String normalizeTestpathFromFile(String testpath)
    {
        testpath = testpath.replace("\r\n", ITestpathInCFG.SEPARATE_BETWEEN_NODES)
                .replace("\n\r", ITestpathInCFG.SEPARATE_BETWEEN_NODES)
                .replace("\n", ITestpathInCFG.SEPARATE_BETWEEN_NODES)
                .replace("\r", ITestpathInCFG.SEPARATE_BETWEEN_NODES);
        if (testpath.equals(ITestpathInCFG.SEPARATE_BETWEEN_NODES))
        {
            testpath = "";
        }
        return testpath;
    }

    protected TestpathString_Marker shortenTestpath(TestpathString_Marker encodedTestpath)
    {
        String[] executedStms = encodedTestpath.getEncodedTestpath().split(ITestpathInCFG.SEPARATE_BETWEEN_NODES);
        if (executedStms.length > 0)
        {
            int THRESHOLD = 200; // by default
            if (executedStms.length >= THRESHOLD)
            {
                StringBuilder tmp_shortenTp = new StringBuilder();

                for (int i = 0; i < THRESHOLD - 1; i++)
                {
                    tmp_shortenTp.append(executedStms[i]).append(ITestpathInCFG.SEPARATE_BETWEEN_NODES);
                }

                tmp_shortenTp.append(executedStms[THRESHOLD - 1]);
                encodedTestpath.setEncodedTestpath(tmp_shortenTp.toString());
            }
            else
            {
            }
        }
        return encodedTestpath;
    }


    protected boolean analyzeTestpathFile(TestCase testCase) throws Exception
    {
        // Read hard disk until the test path is written into file completely
        TestpathString_Marker encodedTestpath = readTestpathFromFile(testCase);

        boolean success = true;

        // shorten test path if it is too long
        encodedTestpath = shortenTestpath(encodedTestpath);

        if (encodedTestpath.getEncodedTestpath().length() > 0)
        {
            // Only for logging
            success = computeCoverage(encodedTestpath, testCase);

        }
        else
        {
            String msg = "The content of test path file is empty after execution";
            if (/*getMode() == IN_EXECUTION_WITHOUT_GTEST_MODE
                    || */getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE)
            {
                //testCase.setStatus(TestCase.STATUS_FAILED);
            }
            success = false;
            throw new Exception(msg);
        }

        return success;
    }

    protected boolean computeCoverage(TestpathString_Marker encodedTestpath, TestCase testCase) throws Exception
    {
        // compute coverage

        // coverage computation
        ISourcecodeFileNode srcNode = Utils.getSourcecodeFile(testCase.getFunctionNode());

        String testPathFile = TestConfig.TESTPATH_FILE + "\\" + testCase.getName() + TestConfig.TESTPATH_EXTENTION;

        String tpContent = Utils.readFileContent(testPathFile);//testCase.getTestPathFile()

        //SourcecodeCoverageComputation computator = new SourcecodeCoverageComputation();
        FunctionCoverageComputation computator = new FunctionCoverageComputation();

        try
        {
            computator.setTestpathContent(tpContent);
            computator.setConsideredSourcecodeNode(srcNode);
            computator.setCoverage(EnviroCoverageTypeNode.BRANCH);
            computator.setFunctionNode(testCase.getFunctionNode());
            computator.compute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // log to details tab of the testcase
        switch (getMode())
        {
            case IN_AUTOMATED_TESTDATA_GENERATION_MODE:
            {
                // log to details tab of the testcase
                StringBuilder tp = new StringBuilder();
                List<String> stms = encodedTestpath
                        .getStandardTestpathByProperty(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION);
                if (stms.size() > 0)
                {
                    for (String stm : stms)
                    {
                        tp.append(stm).append("=>");
                    }
                    tp = new StringBuilder(tp.substring(0, tp.length() - 2));
                }
                else
                {
                }
                break;
            }
        }

        return tpContent.contains(TestPathUtils.END_TAG);
    }

    public FunctionCoverageComputation computeCoverage(IFunctionNode functionNode, List<TestCase> testCases) throws Exception
    {
        // compute coverage

        // coverage computation
        ISourcecodeFileNode srcNode = Utils.getSourcecodeFile(functionNode);

        FunctionCoverageComputation computator = new FunctionCoverageComputation();
        computator.setConsideredSourcecodeNode(srcNode);
        computator.setCoverage(EnviroCoverageTypeNode.BRANCH);
        computator.setFunctionNode(functionNode);

        int nInstructions = computator.getNumberOfInstructions(functionNode, EnviroCoverageTypeNode.STATEMENT);

        int nvisitedInstructions = computator.getNumberOfVisitedInstructions(functionNode, EnviroCoverageTypeNode.STATEMENT, testCases);

        int nBranches = computator.getNumberOfInstructions(functionNode, EnviroCoverageTypeNode.BRANCH);

        int nvisitedBranches = computator.getNumberOfVisitedInstructions(functionNode, EnviroCoverageTypeNode.BRANCH, testCases);

        computator.setNumberOfInstructions(nInstructions);
        computator.setNumberOfVisitedInstructions(nvisitedInstructions);

        computator.setNumberOfBranches(nBranches);
        computator.setNumberOfVisitedBranches(nvisitedBranches);

        return computator;
    }


    protected String runExecutableFile(String executableFile) throws IOException, InterruptedException
    {

        String directory = TestConfig.PROJECT_PATH;

        Terminal terminal;

        terminal = new Terminal(executableFile, directory);

        Process p = terminal.getProcess();
        p.waitFor(10, TimeUnit.SECONDS); // give it a chance to stop

        if (p.isAlive())
        {
            p.destroy(); // tell the process to stop
            p.waitFor(10, TimeUnit.SECONDS); // give it a chance to stop
            p.destroyForcibly(); // tell the OS to kill the process
            p.waitFor();
        }

        //testCase.setExecutedTime(terminal.getTime());

        return terminal.get();
    }

    public TestDriverGeneration getTestDriverGeneration()
    {
        return testDriverGen;
    }

    public void setTestDriverGeneration(TestDriverGeneration testDriverGeneration)
    {
        this.testDriverGen = testDriverGeneration;
    }
}
