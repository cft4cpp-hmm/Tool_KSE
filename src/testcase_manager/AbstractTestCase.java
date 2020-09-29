package testcase_manager;

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

    private String realParentSourceFileName;

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
    protected abstract String generateDefinitionCompileCmd();

    public String getSourceCodeFile() {
        return sourcecodeFile;
    }

    public void setSourcecodeFile(String sourceFile)
    {
        sourcecodeFile = sourceFile;
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

    public static String removeSpecialCharacter(String name) {
        return name.replace("+", "plus").
                replace("-", "minus")
                .replace("*", "multiply")
                .replace("/", "division")
                .replace("%", "mod")
                .replace("=", "equal")
                .replaceAll("[^a-zA-Z0-9_\\.]", "__");
    }

    public String getRealParentSourceFileName()
    {
        return realParentSourceFileName;
    }

    public void setRealParentSourceFileName(String realParentSourceFileName)
    {
        this.realParentSourceFileName = realParentSourceFileName;
    }
}
