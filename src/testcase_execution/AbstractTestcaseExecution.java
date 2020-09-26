package testcase_execution;

import cfg.testpath.ITestpathInCFG;
import compiler.AvailableCompiler;
import compiler.Compiler;
import compiler.Terminal;
import config.CommandConfig;
import coverage.EnviroCoverageTypeNode;
import coverage.SourcecodeCoverageComputation;
import coverage.TestPathUtils;
import instrument.FunctionInstrumentationForStatementvsBranch_Markerv2;
import project_init.IGTestConstant;
import testcase_execution.testdriver.TestDriverGeneration;
import testcase_manager.ITestCase;
import testcase_manager.TestCase;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import testcase_manager.TestCaseManager;
import testdata.object.TestpathString_Marker;
import tree.object.ISourcecodeFileNode;
import utils.CompilerUtils;
import utils.SpecialCharacter;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class AbstractTestcaseExecution implements ITestcaseExecution {
    private int mode = IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE; //IN_EXECUTION_WITHOUT_GTEST_MODE; // by default

    private ITestCase testCase;

    protected TestDriverGeneration testDriverGen;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public ITestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(ITestCase testcase) {
        this.testCase = testcase;
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
    public String compileAndLink() throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();

        String directory = "F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2";
        String compilationCommand = "g++ -c -std\u003dc++14 \"F:\\\\VietData\\\\GitLab\\\\bai10\\\\data-test\\\\aka-working-space\\\\TestCPP14_4\\\\test-drivers\\grade.random.0.cpp\" -o\"F:\\\\VietData\\\\GitLab\\\\bai10\\\\data-test\\\\aka-working-space\\\\TestCPP14_4\\\\test-drivers\\grade.random.0.out\" -DAKA_TC_GRADE_RANDOM_0 -lgtest_main  -lgtest -w";
        String linkCommand = "g++ -std\u003dc++14 \"F:\\\\VietData\\\\GitLab\\\\bai10\\\\data-test\\\\aka-working-space\\\\TestCPP14_4\\\\test-drivers\\grade.random.0.out\" -o\"F:\\\\VietData\\\\GitLab\\\\bai10\\\\data-test\\\\aka-working-space\\\\TestCPP14_4\\\\exe\\grade.random.0.exe\" -lgtest_main  -lgtest -w";

        String executablePath = "F:\\\\VietData\\\\GitLab\\\\bai10\\\\data-test\\\\aka-working-space\\\\TestCPP14_4\\\\exe\\grade.random.0.exe";

        String[] script = CompilerUtils.prepareForTerminal(getCompiler(), compilationCommand);

        String response = new Terminal(script, directory).get();

        output.append(response).append("\n");

        String[] linkScript = CompilerUtils
                .prepareForTerminal(getCompiler(), linkCommand);
        String linkResponse = new Terminal(linkScript, directory).get();
        output.append(linkResponse);

        return output.toString().trim();
    }

    protected TestpathString_Marker readTestpathFromFile(ITestCase testCase) throws InterruptedException {
        TestpathString_Marker encodedTestpath = new TestpathString_Marker();

        int MAX_READ_FILE_NUMBER = 10;
        int countReadFile = 0;

        do {
            encodedTestpath.setEncodedTestpath(normalizeTestpathFromFile(
                    Utils.readFileContent(testCase.getTestPathFile())));

            if (encodedTestpath.getEncodedTestpath().length() == 0) {
                //initialization = "";
                Thread.sleep(10);
            }

            countReadFile++;
        } while (encodedTestpath.getEncodedTestpath().length() == 0 && countReadFile <= MAX_READ_FILE_NUMBER);

        return encodedTestpath;
    }

    protected String normalizeTestpathFromFile(String testpath) {
        testpath = testpath.replace("\r\n", ITestpathInCFG.SEPARATE_BETWEEN_NODES)
                .replace("\n\r", ITestpathInCFG.SEPARATE_BETWEEN_NODES)
                .replace("\n", ITestpathInCFG.SEPARATE_BETWEEN_NODES)
                .replace("\r", ITestpathInCFG.SEPARATE_BETWEEN_NODES);
        if (testpath.equals(ITestpathInCFG.SEPARATE_BETWEEN_NODES))
            testpath = "";
        return testpath;
    }

    protected TestpathString_Marker shortenTestpath(TestpathString_Marker encodedTestpath) {
        String[] executedStms = encodedTestpath.getEncodedTestpath().split(ITestpathInCFG.SEPARATE_BETWEEN_NODES);
        if (executedStms.length > 0) {
            int THRESHOLD = 200; // by default
            if (executedStms.length >= THRESHOLD) {
                StringBuilder tmp_shortenTp = new StringBuilder();

                for (int i = 0; i < THRESHOLD - 1; i++) {
                    tmp_shortenTp.append(executedStms[i]).append(ITestpathInCFG.SEPARATE_BETWEEN_NODES);
                }

                tmp_shortenTp.append(executedStms[THRESHOLD - 1]);
                encodedTestpath.setEncodedTestpath(tmp_shortenTp.toString());
            } else {
            }
        }
        return encodedTestpath;
    }

    protected void refactorResultTrace(ITestCase testCase) {
        final String END_TAG = ",\n";
        String path = testCase.getExecutionResultTrace();
        if (new File(path).exists()) {
            String oldContent = Utils.readFileContent(path);
            String newContent = oldContent;
            if (oldContent.endsWith(END_TAG)) {
                newContent = SpecialCharacter.OPEN_SQUARE_BRACE
                        + newContent.substring(0, newContent.length() - END_TAG.length())
                        + SpecialCharacter.CLOSE_SQUARE_BRACE;
            }
            Utils.writeContentToFile(newContent, path);
        }
    }

    protected boolean analyzeTestpathFile(TestCase testCase) throws Exception {
        // Read hard disk until the test path is written into file completely
        TestpathString_Marker encodedTestpath = readTestpathFromFile(testCase);

        boolean success = true;

        // shorten test path if it is too long
        encodedTestpath = shortenTestpath(encodedTestpath);

        if (encodedTestpath.getEncodedTestpath().length() > 0) {
            // Only for logging
            success = computeCoverage(encodedTestpath, testCase);

        } else {
            String msg = "The content of test path file is empty after execution";
            if (/*getMode() == IN_EXECUTION_WITHOUT_GTEST_MODE
                    || */getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE) {
                testCase.setStatus(TestCase.STATUS_FAILED);
            }
            success = false;
            throw new Exception(msg);
        }

        return success;
    }

    protected boolean computeCoverage(TestpathString_Marker encodedTestpath, TestCase testCase) throws Exception {
        // compute coverage

        // coverage computation
        ISourcecodeFileNode srcNode = Utils.getSourcecodeFile(testCase.getFunctionNode());
        String tpContent = Utils.readFileContent(testCase.getTestPathFile());

        SourcecodeCoverageComputation computator = new SourcecodeCoverageComputation();
        try {
            computator.setTestpathContent(tpContent);
            computator.setConsideredSourcecodeNode(srcNode);
            computator.setCoverage(EnviroCoverageTypeNode.BRANCH);
            computator.compute();
        }catch (Exception e){
            e.printStackTrace();
        }

        // log to details tab of the testcase
        switch (getMode()) {
            case IN_AUTOMATED_TESTDATA_GENERATION_MODE: {
                // log to details tab of the testcase
                StringBuilder tp = new StringBuilder();
                List<String> stms = encodedTestpath
                        .getStandardTestpathByProperty(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION);
                if (stms.size() > 0) {
                    for (String stm : stms)
                        tp.append(stm).append("=>");
                    tp = new StringBuilder(tp.substring(0, tp.length() - 2));
                } else{
                }
                break;
            }
        }

        return tpContent.contains(TestPathUtils.END_TAG);
    }


    public void initializeConfigurationOfTestcase(ITestCase testCase) {
        /*
         * Update test case
         */
        // test path
        testCase.setTestPathFileDefault();
        // executable file
        testCase.setExecutableFileDefault();
        // debug executable file
        testCase.setDebugExecutableFileDefault();
        // command file
        testCase.setCommandConfigFileDefault();
        // debug file
        testCase.setCommandDebugFileDefault();
        // breakpoint
        testCase.setBreakpointPathDefault();
        // test case path
        testCase.setTestPathFileDefault();
        // exec result path
            testCase.setExecutionResultFileDefault();
        // source code file path
        testCase.setSourcecodeFileDefault();
        // execution date and time
        testCase.setExecutionDateTime(LocalDateTime.now());

        testCase.setExecutedTime(-1);

    }

    protected String runExecutableFile(String executableFile) throws IOException, InterruptedException {

        String directory = "F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2";


        Terminal terminal;

        terminal = new Terminal(executableFile, directory);

        Process p = terminal.getProcess();
        p.waitFor(10, TimeUnit.SECONDS); // give it a chance to stop

        if (p.isAlive()) {
            p.destroy(); // tell the process to stop
            p.waitFor(10, TimeUnit.SECONDS); // give it a chance to stop
            p.destroyForcibly(); // tell the OS to kill the process
            p.waitFor();
        }

        testCase.setExecutedTime(terminal.getTime());

        return  terminal.get();
    }

    public TestDriverGeneration getTestDriverGeneration() {
        return testDriverGen;
    }

    public void setTestDriverGeneration(TestDriverGeneration testDriverGeneration) {
        this.testDriverGen = testDriverGeneration;
    }
}
