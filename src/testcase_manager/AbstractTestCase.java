package testcase_manager;

import com.dse.testcasescript.object.TestNewNode;
import compiler.AvailableCompiler;
import compiler.Compiler;
import config.CommandConfig;
import coverage.EnviroCoverageTypeNode;
import project_init.IGTestConstant;
import utils.CompilerUtils;
import utils.DateTimeUtils;
import utils.PathUtils;
import utils.SpecialCharacter;

import java.io.File;
import java.time.LocalDateTime;

public abstract class AbstractTestCase implements ITestCase {

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

    private String executeLog;

    private double executedTime;

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
    @Override
    public CommandConfig generateCommands(String commandFile, String executableFilePath, boolean includeGTest) {
        if (commandFile != null && commandFile.length() > 0 && new File(commandFile).exists()) {
            CommandConfig config = new CommandConfig();

            Compiler compiler = getCompiler();

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

            config.setExecutablePath(relativeExePath);

            return config;
        } else {
            return null;
        }
    }

    protected abstract String generateDefinitionCompileCmd();

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

    private static final String C_TRACE_RESULT_FILE_EXT = ".trc";

    public void setExecutionResultFile(String executionResultFile) {
        this.executionResultFile = removeSysPathInName(executionResultFile);
    }


    public String getProgressCoveragePath(String typeOfCoverage) {
        switch (typeOfCoverage) {
            case EnviroCoverageTypeNode.STATEMENT:
                return getProgressPathForStmCov();
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH:
                return getProgressPathForBranchCov();
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
