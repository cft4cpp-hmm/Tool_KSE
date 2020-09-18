package testcase_manager;

import com.dse.config.CommandConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.debugger.utils.DebugCommandConfig;
import com.dse.environment.Environment;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcasescript.object.TestNameNode;
import com.dse.testcasescript.object.TestNewNode;
import com.dse.user_code.objects.TestCaseUserCode;
import com.dse.util.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;

public abstract class AbstractTestCase implements ITestCase {

    private final static AkaLogger logger = AkaLogger.get(AbstractTestCase.class);

    // some test cases need to be added some specified headers
    private String additionalHeaders = SpecialCharacter.EMPTY;

    // name of test case
    private String name;

    private TestNewNode testNewNode;

    // Not executed (by default)
    private String status = TestCase.STATUS_NA;

    // the file containing the test path after executing this test case
    private String testPathFile;
    // the file containing the commands to compile and linking
    private String commandConfigFile;
    // the file containing the commands to compile and linking in debug mode
    private String commandDebugFile;
    private String executableFile;
    private String debugExecutableFile;

    private String executionResultFile;
    // the path of the file containing breakpoints
    private String breakpointPath;
    // the path of the test case
    private String path;

    private LocalDateTime creationDateTime, executionDateTime;

    // test case user code
    private TestCaseUserCode testCaseUserCode;

    private String executeLog;

    private double executedTime;

    /**
     * [0] pass
     * [1] all
     * Ex: 4 / 5
     */
    private AssertionResult executionResult;

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public String getCreationDate() {
        return DateTimeUtils.getDate(creationDateTime);
    }

    public String getCreationTime() {
        return DateTimeUtils.getTime(creationDateTime);
    }

    public void setExecutionDateTime(LocalDateTime executionDateTime) {
        this.executionDateTime = executionDateTime;
    }

    public LocalDateTime getExecutionDateTime() {
        return executionDateTime;
    }

    public String getExecutionDate() {
        return DateTimeUtils.getDate(creationDateTime);
    }

    public String getExecutionTime() {
        return DateTimeUtils.getTime(creationDateTime);
    }

    private String highlightedFunctionPathForBasisPathCov; // the path of file storing the statement coverage
    private String progressPathForBasisPathCov; // the path of file storing the statement progress

    private String highlightedFunctionPathForStmCov; // the path of file storing the statement coverage
    private String progressPathForStmCov; // the path of file storing the statement progress

    private String highlightedFunctionPathForBranchCov; // the path of file storing the branch coverage
    private String progressPathForBranchCov; // the path of file storing the branch progress

    private String highlightedFunctionPathForMCDCCov; // the path of file storing the MCDC coverage
    private String progressPathForMCDCCov; // the path of file storing the MCDC progress

    // Is a part of test driver
    // The test driver of a test case has two files.
    // This file contains the main function to run a test case
    // This file is stored in {working-directory}/testdrivers)
    private String sourcecodeFile;

    // Is a part of test driver
    // This file contains the modification of the original source code file containing the function we need to test with.
    // This file is stored in the same directory of the original source code file
//    private String sourcecodeFile2;

    /**
     * Delete all files related to the test case except the file storing the value of test case
     */
    public void deleteOldDataExceptValue() {
        // todo: need to validate and set coverage, progress coverage file for testcase
        logger.debug("Deleting all files related to the test case " + getName() + " before continuing");
        if (getTestPathFile() != null)
            Utils.deleteFileOrFolder(new File(this.getTestPathFile()));
        if (getExecutableFile() != null)
            Utils.deleteFileOrFolder(new File(this.getExecutableFile()));
        if (getCommandConfigFile() != null)
            Utils.deleteFileOrFolder(new File(this.getCommandConfigFile()));
        if (getCommandDebugFile() != null)
            Utils.deleteFileOrFolder(new File(getCommandDebugFile()));
        if (getExecutionResultFile() != null) {
            Utils.deleteFileOrFolder(new File(getExecutionResultFile()));
            Utils.deleteFileOrFolder(new File(getExecutionResultTrace()));
        }

        /*
         * Delete test drivers
         */
        if (getSourceCodeFile() != null)
            Utils.deleteFileOrFolder(new File(getSourceCodeFile()));

        /*
         * Delete related-coverage files
         */
        if (getProgressPathForStmCov() != null)
            Utils.deleteFileOrFolder(new File(getProgressPathForStmCov()));
        if (getHighlightedFunctionPathForStmCov() != null)
            Utils.deleteFileOrFolder(new File(getHighlightedFunctionPathForStmCov()));

        if (getProgressPathForBranchCov() != null)
            Utils.deleteFileOrFolder(new File(getProgressPathForBranchCov()));
        if (getHighlightedFunctionPathForBranchCov() != null)
            Utils.deleteFileOrFolder(new File(getHighlightedFunctionPathForBranchCov()));

        if (getProgressPathForMCDCCov() != null)
            Utils.deleteFileOrFolder(new File(getProgressPathForMCDCCov()));
        if (getHighlightedFunctionPathForMCDCCov() != null)
            Utils.deleteFileOrFolder(new File(getHighlightedFunctionPathForMCDCCov()));
    }

    /**
     * Given a test case, we try to generate compilation commands + linking command.
     * <p>
     * The original project has its own commands, all the thing we need to do right now is
     * modify these commands.
     */
    @Override
    public CommandConfig generateCommands(String commandFile, String executableFilePath, boolean includeGTest) {
        if (commandFile != null && commandFile.length() > 0 && new File(commandFile).exists()) {
            CommandConfig config = new CommandConfig();
            Compiler compiler = Environment.getInstance().getCompiler();

            // step 1: generate compilation command
            String relativePath = PathUtils.toRelative(getSourceCodeFile());
            String newCompileCommand = compiler.generateCompileCommand(relativePath);
            newCompileCommand += SpecialCharacter.SPACE + generateDefinitionCompileCmd();
            newCompileCommand = IGTestConstant.getGTestCommand(newCompileCommand, includeGTest);

            config.getCompilationCommands().put(relativePath, newCompileCommand);

            // step 2: generate linking command
            String outputFilePath = CompilerUtils.getOutfilePath(relativePath, compiler.getOutputExtension());
            String relativeExePath = PathUtils.toRelative(executableFilePath);

            String newLinkingCommand = compiler.generateLinkCommand(relativeExePath, outputFilePath);
            newLinkingCommand = IGTestConstant.getGTestCommand(newLinkingCommand, includeGTest);
            config.setLinkingCommand(newLinkingCommand);

            // step 3: set executable file path
            config.setExecutablePath(relativeExePath);

            File compilationDir = new File(new WorkspaceConfig().fromJson().getTestcaseCommandsDirectory());
            compilationDir.mkdirs();
            String commandFileOfTestcase = compilationDir.getAbsolutePath() + File.separator + removeSpecialCharacter(getName()) + ".json";
            config.exportToJson(new File(commandFileOfTestcase));
            logger.debug("Export the compilation of test case to file " + commandFileOfTestcase);

            this.setCommandConfigFile(commandFileOfTestcase);
            return config;
        } else {
            logger.debug("The root command file " + commandFile + " does not exist");
            return null;
        }
    }

    protected abstract String generateDefinitionCompileCmd();

    @Override
    public DebugCommandConfig generateDebugCommands() {

        CommandConfig source = new CommandConfig().fromJson(getCommandConfigFile());
        DebugCommandConfig debugCmdConfig = new DebugCommandConfig();

        Map.Entry<String, String> entry = source
                .getCompilationCommands()
                .entrySet()
                .stream()
                .findFirst()
                .orElse(null);

        assert entry != null;
        String compileCmd = entry.getValue().replace("-c", "-c -ggdb");
        compileCmd = IGTestConstant.getGTestCommand(compileCmd, true);
        debugCmdConfig.getDebugCommands().put(entry.getKey(), compileCmd);

        String linkCmd = source.getLinkingCommand()
                .replace("exe" + File.separator, "exe" + File.separator + "debug_");

        linkCmd = IGTestConstant.getGTestCommand(linkCmd, true);

        debugCmdConfig.setLinkingCommand(linkCmd);

        debugCmdConfig.exportToJson(new File(getCommandDebugFile()));

        return debugCmdConfig;
    }

    public void deleteOldData() {
        deleteOldDataExceptValue();
        if (getPath() != null)
            Utils.deleteFileOrFolder(new File(getPath()));
    }

    @Override
    public String getSourceCodeFile() {
        return sourcecodeFile;
    }

    @Override
    public void setSourceCodeFile(String sourcecodeFile) {
        this.sourcecodeFile = removeSysPathInName(sourcecodeFile);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = removeSpecialCharacter(name);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = removeSysPathInName(path);
    }

    @Override
    public TestNewNode getTestNewNode() {
        return testNewNode;
    }

    @Override
    public void setTestNewNode(TestNewNode testNewNode) {
        this.testNewNode = testNewNode;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getTestPathFile() {
        return testPathFile;
    }

    @Override
    public void setTestPathFile(String testPathFile) {
        this.testPathFile = removeSysPathInName(testPathFile);
    }

    @Override
    public String getExecutableFile() {
        return executableFile;
    }

    @Override
    public void setExecutableFile(String executableFile) {
        this.executableFile = removeSysPathInName(executableFile);
    }

    @Override
    public String getCommandConfigFile() {
        return commandConfigFile;
    }

    @Override
    public void setCommandConfigFile(String commandConfigFile) {
        this.commandConfigFile = removeSysPathInName(commandConfigFile);
    }

    @Override
    public String getCommandDebugFile() {
        return commandDebugFile;
    }

    @Override
    public void setCommandDebugFile(String commandDebugFile) {
        this.commandDebugFile = removeSysPathInName(commandDebugFile);
    }

    @Override
    public String getBreakpointPath() {
        return breakpointPath;
    }

    @Override
    public void setBreakpointPath(String breakpointPath) {
        this.breakpointPath = removeSysPathInName(breakpointPath);
    }

    @Override
    public void setDebugExecutableFile(String debugExecutableFile) {
        this.debugExecutableFile = removeSysPathInName(debugExecutableFile);
    }

    @Override
    public String getDebugExecutableFile() {
        return debugExecutableFile;
    }

    public void setSourcecodeFileDefault() {
        if (Environment.getInstance().getCompiler().isGPlusPlusCommand()) {
            String srcFile = new WorkspaceConfig().fromJson().getTestDriverDirectory() + File.separator + removeSpecialCharacter(getName()) + ".cpp";
            setSourceCodeFile(srcFile);
        } else if (Environment.getInstance().getCompiler().isGccCommand()) {
            String srcFile = new WorkspaceConfig().fromJson().getTestDriverDirectory() + File.separator + removeSpecialCharacter(getName()) + ".c";
            setSourceCodeFile(srcFile);
        }
    }

    @Override
    public void setTestPathFileDefault() {
        String testpathFile = new WorkspaceConfig().fromJson().getTestpathDirectory() + File.separator + removeSpecialCharacter(getName()) + ".tp";
        setTestPathFile(testpathFile);
    }

    @Override
    public void setExecutableFileDefault() {
        String executableFile = new WorkspaceConfig().fromJson().getExecutableFolderDirectory() + File.separator + removeSpecialCharacter(getName()) + ".exe";
        setExecutableFile(executableFile);
    }

    @Override
    public void setCommandConfigFileDefault() {
        String commandFile = new WorkspaceConfig().fromJson().getTestcaseCommandsDirectory() + File.separator + removeSpecialCharacter(getName()) + ".json";
        setCommandConfigFile(commandFile);
    }

    @Override
    public void setCommandDebugFileDefault() {
        String debugCommandFile = new WorkspaceConfig().fromJson().getDebugCommandsDirectory() + File.separator + removeSpecialCharacter(getName()) + ".json";
        setCommandDebugFile(debugCommandFile);
    }

    @Override
    public void setBreakpointPathDefault() {
        String breakpoint = new WorkspaceConfig().fromJson().getBreakpointDirectory() + File.separator + removeSpecialCharacter(getName()) + ".br.json";
        setBreakpointPath(breakpoint);
    }

    @Override
    public void setDebugExecutableFileDefault() {
        String debugExecutableFile = new WorkspaceConfig().fromJson().getExecutableFolderDirectory() + File.separator + "debug_" + removeSpecialCharacter(getName()) + ".exe";
        setDebugExecutableFile(debugExecutableFile);
    }

    public void setCurrentCoverageDefault() {
        String stmCovPath = new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + removeSpecialCharacter(getName()) + AbstractTestCase.STATEMENT_COVERAGE_FILE_EXTENSION;
        setHighlightedFunctionPathForStmCov(stmCovPath);

        String branchCovPath = new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + removeSpecialCharacter(getName()) + AbstractTestCase.BRANCH_COVERAGE_FILE_EXTENSION;
        setHighlightedFunctionPathForBranchCov(branchCovPath);

        String mcdcCovPath = new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + removeSpecialCharacter(getName()) + AbstractTestCase.MCDC_COVERAGE_FILE_EXTENSION;
        setHighlightedFunctionPathForMCDCCov(mcdcCovPath);
    }

    public void setCurrentProgressDefault() {
        String stmProPath = new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + removeSpecialCharacter(getName()) + AbstractTestCase.STATEMENT_PROGRESS_FILE_EXTENSION;
        setProgressPathForStmCov(stmProPath);

        String branchProPath = new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + removeSpecialCharacter(getName()) + AbstractTestCase.BRANCH_PROGRESS_FILE_EXTENSION;
        setProgressPathForBranchCov(branchProPath);

        String mcdcProPath = new WorkspaceConfig().fromJson().getCoverageDirectory() + File.separator + removeSpecialCharacter(getName()) + AbstractTestCase.MCDC_PROGRESS_FILE_EXTENSION;
        setProgressPathForMCDCCov(mcdcProPath);
    }

    protected void updateTestNewNode(String name) {
        testNewNode = new TestNewNode();
        TestNameNode testNameNode = new TestNameNode();
        testNameNode.setName(name);
        testNewNode.getChildren().add(testNameNode);
        testNameNode.setParent(testNewNode);
    }

    public AssertionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(AssertionResult result) {
        executionResult = result;
    }

    public void appendExecutionResult(AssertionResult result) {
        if (executionResult == null)
            executionResult = result;
        else if (result != null) {
            executionResult.append(result);
        }
    }

    public String getProgressPathForStmCov() {
        return progressPathForStmCov;
    }

    public void setHighlightedFunctionPathForStmCov(String highlightedFunctionPathForStmCov) {
        this.highlightedFunctionPathForStmCov = removeSysPathInName(highlightedFunctionPathForStmCov);
    }

    public String getHighlightedFunctionPathForStmCov() {
        return highlightedFunctionPathForStmCov;
    }

    public void setProgressPathForStmCov(String progressPathForStmCov) {
        this.progressPathForStmCov = removeSysPathInName(progressPathForStmCov);
    }

    public void setProgressPathForBranchCov(String progressPathForBranchCov) {
        this.progressPathForBranchCov = removeSysPathInName(progressPathForBranchCov);
    }

    public String getProgressPathForBranchCov() {
        return progressPathForBranchCov;
    }

    public void setProgressPathForMCDCCov(String progressPathForMCDCCov) {
        this.progressPathForMCDCCov = removeSysPathInName(progressPathForMCDCCov);
    }

    public String getHighlightedFunctionPathForMCDCCov() {
        return highlightedFunctionPathForMCDCCov;
    }

    public String getProgressPathForMCDCCov() {
        return progressPathForMCDCCov;
    }

    public void setHighlightedFunctionPathForMCDCCov(String highlightedFunctionPathForMCDCCov) {
        this.highlightedFunctionPathForMCDCCov = removeSysPathInName(highlightedFunctionPathForMCDCCov);
    }

    public String getHighlightedFunctionPathForBranchCov() {
        return highlightedFunctionPathForBranchCov;
    }

    public void setHighlightedFunctionPathForBranchCov(String highlightedFunctionPathForBranchCov) {
        this.highlightedFunctionPathForBranchCov = removeSysPathInName(highlightedFunctionPathForBranchCov);
    }

    public String getHighlightedFunctionPathForBasisPathCov() {
        return highlightedFunctionPathForBasisPathCov;
    }

    public void setHighlightedFunctionPathForBasisPathCov(String highlightedFunctionPathForBasisPathCov) {
        this.highlightedFunctionPathForBasisPathCov = removeSysPathInName(highlightedFunctionPathForBasisPathCov);
    }

    @Deprecated
    public String getExecutionResultFile() {
        return executionResultFile;
    }

    public String getExecutionResultTrace() {
        if (Environment.getInstance().isC())
            return executionResultFile.replace("-Results.xml", C_TRACE_RESULT_FILE_EXT);
        else
            return executionResultFile.replace(".xml", C_TRACE_RESULT_FILE_EXT);
    }

    private static final String C_TRACE_RESULT_FILE_EXT = ".trc";

    public void setExecutionResultFile(String executionResultFile) {
        this.executionResultFile = removeSysPathInName(executionResultFile);
    }

    public void setExecutionResultFileDefault() {
        String path = new WorkspaceConfig().fromJson().getExecutionResultDirectory() + File.separator + getName() + ".xml";
        setExecutionResultFile(path);
    }

    public String getHighlightedFunctionPath(String typeOfCoverage) {
        switch (typeOfCoverage) {
            case EnviroCoverageTypeNode.STATEMENT:
                return getHighlightedFunctionPathForStmCov();
            case EnviroCoverageTypeNode.BRANCH:
                return getHighlightedFunctionPathForBranchCov();
            case EnviroCoverageTypeNode.BASIS_PATH:
                return getHighlightedFunctionPathForBasisPathCov();
            case EnviroCoverageTypeNode.MCDC:
                return getHighlightedFunctionPathForMCDCCov();
            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH:
                // does not accept multiple coverage types
                return null;
            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC:
                // does not accept multiple coverage types
                return null;
            default:
                return null;
        }
    }

    public String getProgressCoveragePath(String typeOfCoverage) {
        switch (typeOfCoverage) {
            case EnviroCoverageTypeNode.STATEMENT:
                return getProgressPathForStmCov();
            case EnviroCoverageTypeNode.BRANCH:
                return getProgressPathForBranchCov();
            case EnviroCoverageTypeNode.MCDC:
                return getProgressPathForMCDCCov();
            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH:
                // does not accept multiple coverage types
                return "";
            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC:
                // does not accept multiple coverage types
                return "";
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, status);
    }

    public String getAdditionalHeaders() {
        return additionalHeaders;
    }

    public void setAdditionalHeaders(String additionalHeaders) {
        this.additionalHeaders = additionalHeaders;
    }

    public void appendAdditionHeader(String includeStm) {
        if (additionalHeaders == null)
            additionalHeaders = includeStm;
        else if (!additionalHeaders.contains(includeStm))
            additionalHeaders += SpecialCharacter.LINE_BREAK + includeStm;
    }

    public static String removeSysPathInName(String path) {
        // name could not have File.separator
        return path.replaceAll("operator\\s*/", "operator_division");
    }

    public static String redoTheReplacementOfSysPathInName(String path) {
        // name could not have File.separator
        return path.replace("operator_division", "operator /");
    }

    public static String removeSpecialCharacter(String name) {
        return name.replace("+", "plus").
                replace("-", "minus")
                .replace("*", "multiply")
                .replace("/", "division")
                .replace("%", "mod")
                .replace("=", "equal")
                .replaceAll("[^a-zA-Z0-9_\\.]", "__");
    }

    public void setTestCaseUserCode(TestCaseUserCode testCaseUserCode) {
        this.testCaseUserCode = testCaseUserCode;
    }

    public TestCaseUserCode getTestCaseUserCode() {
        if (testCaseUserCode == null) {
            testCaseUserCode = new TestCaseUserCode();
            testCaseUserCode.setSetUpContent("// set up\n");
            testCaseUserCode.setTearDownContent("// tear down\n");
        }
        return testCaseUserCode;
    }

    @Override
    public boolean isPrototypeTestcase() {
        return getName() != null && getName().startsWith(ITestCase.PROTOTYPE_SIGNAL);
    }

    public String getExecuteLog() {
        return executeLog.trim();
    }

    public void setExecuteLog(String executeLog) {
        this.executeLog = executeLog;
    }

    public double getExecutedTime() {
        return executedTime;
    }

    public void setExecutedTime(double executedTime) {
        this.executedTime = executedTime;
    }
}
