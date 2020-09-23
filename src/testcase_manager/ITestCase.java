package testcase_manager;

import config.CommandConfig;

import java.time.LocalDateTime;
import java.util.List;

public interface ITestCase {
    String POSTFIX_TESTCASE_BY_USER = ".manual";
    String POSTFIX_TESTCASE_BY_RANDOM = ".random";
    String POSTFIX_TESTCASE_BY_DIRECTED_METHOD = ".directed";

    String COMPOUND_SIGNAL = "COMPOUND";
    String PROTOTYPE_SIGNAL = "PROTOTYPE_";
    String AKA_SIGNAL = ".";

    String STATUS_NA = "N/A";
    String STATUS_EXECUTING = "executing";
    String STATUS_SUCCESS = "success";
    String STATUS_FAILED = "failed";
    String STATUS_RUNTIME_ERR = "runtime error";
    String STATUS_EMPTY = "no status";

    String getName();

    void setName(String name);

    void setCreationDateTime(LocalDateTime creationDateTime);

    LocalDateTime getCreationDateTime();

    String getCreationDate();

    String getCreationTime();

    void setExecutionDateTime(LocalDateTime executionDateTime);

    LocalDateTime getExecutionDateTime();

    String getExecutionDate();

    String getExecutionTime();

    String getStatus();

    void setStatus(String status);

    String getPath();

    void setPathDefault();

    void setPath(String path);

    String getSourceCodeFile();

    void setSourceCodeFile(String sourcecodeFile);

    String getTestPathFile();

    void setTestPathFileDefault();

    String getExecutionResultTrace();

    void setTestPathFile(String testPathFile);

    String getExecutableFile();

    void setExecutableFileDefault();

    void setExecutableFile(String executableFile);

    String getCommandConfigFile();

    void setCommandConfigFileDefault();

    void setCommandConfigFile(String commandConfigFile);

    String getCommandDebugFile();

    void setCommandDebugFileDefault();

    void setCommandDebugFile(String commandDebugFile);

    String getBreakpointPath();

    void setBreakpointPathDefault();

    void setBreakpointPath(String breakpointPath);

    void setDebugExecutableFile(String debugExecutableFile);

    void setDebugExecutableFileDefault();

    @Deprecated
    String getExecutionResultFile();

    void setExecutionResultFile(String executionResultFile);

    void setExecutionResultFileDefault();

    String getDebugExecutableFile();

    void setCurrentCoverageDefault();

    void setCurrentProgressDefault();

    /**
     * Given a test case, we try to generate compilation commands + linking command.
     * <p>
     * The original project has its own commands, all the thing we need to do right now is
     * modify these commands.
     *
     * @return compile command, linker command and execute file path
     */
    CommandConfig generateCommands(String commandFile, String executableFilePath, boolean includeGTest);

    void deleteOldData(); // delete all files related to the current test case

    void deleteOldDataExceptValue(); // delete all files related to the current test case except the file storing the value of test cas

//    void generateDebugCommands();

    String getAdditionalHeaders();

    void setAdditionalHeaders(String additionalHeaders);

    void setSourcecodeFileDefault();

    boolean isPrototypeTestcase();

    List<String> getAdditionalIncludes();

    String getExecuteLog();

    void setExecuteLog(String executeLog);

    double getExecutedTime();

    void setExecutedTime(double executedTime);

    String STATEMENT_COVERAGE_FILE_EXTENSION = ".stm.cov";
    String BRANCH_COVERAGE_FILE_EXTENSION = ".branch.cov";

    String STATEMENT_PROGRESS_FILE_EXTENSION = ".stm.pro";
    String BRANCH_PROGRESS_FILE_EXTENSION = ".branch.pro";
}