package testcase_execution;

import auto_testcase_generation.cfg.testpath.ITestpathInCFG;
import auto_testcase_generation.instrument.FunctionInstrumentationForStatementvsBranch_Markerv2;
import auto_testcase_generation.testdata.object.TestpathString_Marker;
import com.dse.compiler.Terminal;
import com.dse.config.AkaConfig;
import com.dse.config.CommandConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.coverage.SourcecodeCoverageComputation;
import com.dse.coverage.TestPathUtils;
import com.dse.coverage.highlight.SourcecodeHighlighterForCoverage;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.TCExecutionDetailLogger;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.TestCaseExecutionDataNode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.testcase_execution.testdriver.TestDriverGeneration;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.util.CompilerUtils;
import com.dse.util.IGTestConstant;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import javafx.application.Platform;
import javafx.scene.control.Alert;

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

    public String compileAndLink(CommandConfig customCommandConfig) throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();

        Map<String, String> compilationCommands = customCommandConfig.getCompilationCommands();

        String workspace = new AkaConfig().fromJson().getOpeningWorkspaceDirectory();
        String directory = new File(workspace).getParentFile().getParentFile().getPath();

        // Create an executable file
        logger.debug("Compiling source code files");
        for (String filePath : compilationCommands.keySet()) {
            String compilationCommand = compilationCommands.get(filePath);

            logger.debug("Executing " + compilationCommand);
//            UILogger.getUiLogger().log("Executing " + compilationCommand);

            String[] script = CompilerUtils.prepareForTerminal(Environment.getInstance().getCompiler(), compilationCommand);

            String response = new Terminal(script, directory).get();
//            logger.debug("Execute done: " + compilationCommand);

            output.append(response).append("\n");
        }

        logger.debug("Linking object file. Command: " + customCommandConfig.getLinkingCommand());
//        UILogger.getUiLogger().log("Linking object file. Command: " + customCommandConfig.getLinkingCommand());

        String[] linkScript = CompilerUtils
                .prepareForTerminal(Environment.getInstance().getCompiler(), customCommandConfig.getLinkingCommand());
        String linkResponse = new Terminal(linkScript, directory).get();
        output.append(linkResponse);

        logger.debug("Execute done: " + customCommandConfig.getLinkingCommand());
        logger.debug("in directory " + directory);


        return output.toString().trim();
    }

    protected CommandConfig initializeCommandConfigToRunTestCase(ITestCase testCase, boolean shouldIncludeGtestLib) {
        /*
         * create the command file of the test case from the original command file
         */
        String rootCommandFile = new WorkspaceConfig().fromJson().getCommandFile();

        CommandConfig commandConfig = testCase.generateCommands(rootCommandFile,
                testCase.getExecutableFile(), shouldIncludeGtestLib);

        commandConfig.exportToJson(new File(testCase.getCommandConfigFile()));

        logger.debug("Create the command file for test case " + testCase.getName() + " at "
                + testCase.getCommandConfigFile());

        return commandConfig;
    }

    protected TestpathString_Marker readTestpathFromFile(ITestCase testCase) throws InterruptedException {
        TestpathString_Marker encodedTestpath = new TestpathString_Marker();

        int MAX_READ_FILE_NUMBER = 10;
        int countReadFile = 0;

        do {
            logger.debug("Finish. We are getting a execution path from hard disk");
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
                logger.debug("Shorten test path to enhance code coverage computation speed: from "
                        + executedStms.length + " to " + THRESHOLD);
                StringBuilder tmp_shortenTp = new StringBuilder();

                for (int i = 0; i < THRESHOLD - 1; i++) {
                    tmp_shortenTp.append(executedStms[i]).append(ITestpathInCFG.SEPARATE_BETWEEN_NODES);
                }

                tmp_shortenTp.append(executedStms[THRESHOLD - 1]);
                encodedTestpath.setEncodedTestpath(tmp_shortenTp.toString());
            } else {
                logger.debug("No need for shortening test path because it is not too long");
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

            logger.debug("Retrieve the test path file "
                    + testCase.getTestPathFile() + " successfully.");
            logger.debug("Generate test paths for " + testCase.getName() + " sucessfully");

        } else {
            String msg = "The content of test path file is empty after execution";
            logger.debug(msg);
            if (/*getMode() == IN_EXECUTION_WITHOUT_GTEST_MODE
                    || */getMode() == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE) {
                UIController.showErrorDialog(msg, "Test case execution", "Fail");
                testCase.setStatus(TestCase.STATUS_FAILED);
            }
            success = false;
            throw new Exception(msg);
        }

        return success;
    }

    protected boolean computeCoverage(TestpathString_Marker encodedTestpath, TestCase testCase) throws Exception {
        // compute coverage
        logger.debug("Compute coverage given the test path");

        // coverage computation
        ISourcecodeFileNode srcNode = Utils.getSourcecodeFile(testCase.getFunctionNode());
        String tpContent = Utils.readFileContent(testCase.getTestPathFile());

        SourcecodeCoverageComputation computator = new SourcecodeCoverageComputation();
        try {
            computator.setTestpathContent(tpContent);
            computator.setConsideredSourcecodeNode(srcNode);
            computator.setCoverage(Environment.getInstance().getTypeofCoverage());
            computator.compute();
        }catch (Exception e){
            e.printStackTrace();
        }

        // highlighter
        try {
            SourcecodeHighlighterForCoverage highlighter = new SourcecodeHighlighterForCoverage();
            highlighter.setSourcecode(srcNode.getAST().getRawSignature());
            highlighter.setTestpathContent(tpContent);
            highlighter.setSourcecodePath(srcNode.getAbsolutePath());
            highlighter.setAllCFG(computator.getAllCFG());
            highlighter.setTypeOfCoverage(computator.getCoverage());
            highlighter.highlight();
        }catch (Exception e){
            e.printStackTrace();
        }

        // log to details tab of the testcase
        switch (getMode()) {
            case IN_AUTOMATED_TESTDATA_GENERATION_MODE: {
                // get TestCaseExecutionDataNode tuong ung
                TestCaseExecutionDataNode dataNode = TCExecutionDetailLogger.getExecutionDataNodeByTestCase(testCase);

                // log to details tab of the testcase
                StringBuilder tp = new StringBuilder();
                List<String> stms = encodedTestpath
                        .getStandardTestpathByProperty(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION);
                if (stms.size() > 0) {
                    for (String stm : stms)
                        tp.append(stm).append("=>");
                    tp = new StringBuilder(tp.substring(0, tp.length() - 2)); //
                    logger.debug("Done. Offsets of execution test path [length=" + stms.size() + "] = " + tp);
                    TCExecutionDetailLogger.logDetailOfTestCase(testCase, "Test case path: " + tp.toString());
                } else{
                    logger.debug("Done. Offsets of execution test path [length=0]");
                    TCExecutionDetailLogger.logDetailOfTestCase(testCase, "No path");
                }
                break;
            }
        }

        return tpContent.contains(TestPathUtils.END_TAG);
    }

    protected void showExecutionResultDialog(ITestCase testCase, String result) {
        Alert.AlertType type;
        String headerText;

        if (result.contains(IGTestConstant.FAILED_FLAG)) {
            type = Alert.AlertType.ERROR;
            headerText = "Fail to execute test case " + testCase.getName();
        } else if (result.contains(IGTestConstant.PASSED_FLAG)) {
            type = Alert.AlertType.INFORMATION;
            headerText = "Execute test case " + testCase.getName() + " successfully";
        } else {
            type = Alert.AlertType.WARNING;
            headerText = "Fail to execute test case " + testCase.getName();

            if (!result.endsWith(SpecialCharacter.LINE_BREAK))
                result += SpecialCharacter.LINE_BREAK;

            result += "Catch a runtime error when execute test case " + testCase.getName();
        }

        String content = result;

        Platform.runLater(() -> UIController.showDetailDialog(type, "Execution Result", headerText, content));
    }

    @Override
    public void initializeConfigurationOfTestcase(ITestCase testCase) {
        /*
         * Update test case
         */
        // test path
        testCase.setTestPathFileDefault();
        logger.debug("The test path file of test case " + testCase.getName() + ": " + testCase.getTestPathFile());

        // executable file
        testCase.setExecutableFileDefault();
        logger.debug("Executable file of test case " + testCase.getName() + ": " + testCase.getExecutableFile());

        // debug executable file
        testCase.setDebugExecutableFileDefault();
        logger.debug("Debug executable file of test case " + testCase.getName() + ": " + testCase.getDebugExecutableFile());

        // command file
        testCase.setCommandConfigFileDefault();
        logger.debug("Command file of test case " + testCase.getName() + ": " + testCase.getCommandConfigFile());

        // debug file
        testCase.setCommandDebugFileDefault();
        logger.debug("Debug command file of test case " + testCase.getName() + ": " + testCase.getCommandDebugFile());

        // breakpoint
        testCase.setBreakpointPathDefault();
        logger.debug("Breakpoint file of test case " + testCase.getName() + ": " + testCase.getBreakpointPath());

        // test case path
        testCase.setTestPathFileDefault();
        logger.debug("Path of the test case " + testCase.getName() + ": " + testCase.getTestPathFile());

        // exec result path
        if (Environment.getInstance().isC()) {
            String path = new WorkspaceConfig().fromJson().getExecutionResultDirectory() + File.separator + testCase.getName() + "-Results.xml";
            testCase.setExecutionResultFile(path);
        } else {
            testCase.setExecutionResultFileDefault();
        }
        logger.debug("Execute Result Path of the test case " + testCase.getName() + ": " + testCase.getExecutionResultTrace());

        // source code file path
        testCase.setSourcecodeFileDefault();
        logger.debug("The source code file containing the test case " + testCase.getName() + ": " + testCase.getSourceCodeFile());

        // execution date and time
        testCase.setExecutionDateTime(LocalDateTime.now());

        testCase.setExecutedTime(-1);

        TestCaseManager.exportTestCaseToFile(testCase);
    }

    protected String runExecutableFile(CommandConfig commandConfig) throws IOException, InterruptedException {
        String executableFilePath = commandConfig.getExecutablePath();

        String workspace = new AkaConfig().fromJson().getOpeningWorkspaceDirectory();
        String directory = new File(workspace).getParentFile().getParentFile().getPath();


        Terminal terminal;

//        if (mode == IN_EXECUTION_WITH_FRAMEWORK_TESTING_MODE) {
//            String[] executeCommand = new String[] {executableFilePath, "--gtest_output="
//                    + String.format("xml:%s", testCase.getExecutionResultFile())};
//
//            terminal = new Terminal(executeCommand, directory);
//
//        } else
            terminal = new Terminal(executableFilePath, directory);

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
