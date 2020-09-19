package compiler;


import compiler.message.CompileMessage;
import compiler.message.ICompileMessage;
import tree.object.INode;
import utils.CompilerUtils;
import utils.PathUtils;
import utils.SpecialCharacter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class Compiler implements ICompiler {

    private String compileCommand = "compile";
    private String preprocessCommand = "pre-process";
    private String linkCommand = "link";
    private String debugCommand = "debug";

    private String includeFlag = "-I";
    private String defineFlag = "-D";
    private String outputFlag = "-o";
    private String debugFlag = "-ggdb";

    private String outputExtension = ".out";

    private List<String> includePaths = new ArrayList<>();
    private List<String> defines = new ArrayList<>();

    private String name;

    /*
     * TODO: other option
     */

    public Compiler() {

    }

    public Compiler(Class<?> c) throws IllegalAccessException, NoSuchFieldException {
        name = c.getField("NAME").get(null).toString();

        compileCommand = c.getField("COMPILE_CMD").get(null).toString();
        preprocessCommand = c.getField("PRE_PRECESS_CMD").get(null).toString();
        linkCommand = c.getField("LINK_CMD").get(null).toString();
        debugCommand = c.getField("DEBUG_CMD").get(null).toString();

        includeFlag = c.getField("INCLUDE_FLAG").get(null).toString();
        defineFlag = c.getField("DEFINE_FLAG").get(null).toString();
        outputFlag = c.getField("OUTPUT_FLAG").get(null).toString();
        debugFlag = c.getField("DEBUG_FLAG").get(null).toString();

        outputExtension = c.getField("OUTPUT_EXTENSION").get(null).toString();
    }

    @Override
    public ICompileMessage compile(INode file) {
        String filepath = file.getAbsolutePath();
        filepath = PathUtils.toRelative(filepath);

        return compile(filepath);
    }

    @Override
    public ICompileMessage compile(String filePath) {
        String script = generateCompileCommand(filePath);
        String workspace = "";
        //String directory = new File(workspace).getParentFile().getParentFile().getPath();

        ICompileMessage compileMessage = null;

        try {
            String[] command = CompilerUtils.prepareForTerminal(this, script);
            String message = new Terminal(command, "F:\\VietData\\GitLab\\bai10\\data-test\\Output").get();
            compileMessage = new CompileMessage(message, filePath);
            compileMessage.setCompilationCommand(script);

        } catch (Exception ex) {
        }

        return compileMessage;
    }

    @Override
    public ICompileMessage link(String executableFilePath, String... outputPaths) {
        executableFilePath = PathUtils.toRelative(executableFilePath);
        for (int i = 0; i < outputPaths.length; i++) {
            outputPaths[i] = PathUtils.toRelative(outputPaths[i]);
        }
        String script = generateLinkCommand(executableFilePath, outputPaths);
        String workspace = "";
        String directory = new File(workspace).getParentFile().getParentFile().getPath();

        ICompileMessage compileMessage;

        try {
            String[] command = CompilerUtils.prepareForTerminal(this, script);
            String message = new Terminal(command, directory).get();
            compileMessage = new CompileMessage(message, executableFilePath);
            compileMessage.setLinkingCommand(script);
        } catch (Exception ex) {
            compileMessage = null;
        }

        return compileMessage;
    }

    @Override
    public String generateCompileCommand(String filePath) {
        String outfilePath = CompilerUtils.getOutfilePath(filePath, outputExtension);

        StringBuilder builder = new StringBuilder();
        builder.append(compileCommand)
                .append(SpecialCharacter.SPACE)
                .append("\"" + filePath + "\"")
                .append(SpecialCharacter.SPACE);

        if (includePaths != null && includePaths.size() != 0) {
            for (String path : includePaths) {
                builder.append(includeFlag)
                        .append("\"" + path + "\"")
                        .append(SpecialCharacter.SPACE);
            }
        }

        List<String> userCodes = new ArrayList<>();
        //userCodes.add("F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2\\test.cpp");
        for (String userCode : userCodes) {
            builder.append(INCLUDE_FILE_FLAG)
                    .append("\"").append(userCode).append("\"")
                    .append(SpecialCharacter.SPACE);
        }

        if (defines != null && defines.size() != 0) {
            for (String variable : defines) {
                builder.append(defineFlag)
                        .append(variable)
                        .append(SpecialCharacter.SPACE);
            }
        }

        builder.append(outputFlag)
                .append("\"" + outfilePath + "\"");

        return builder.toString();
    }

    @Override
    public String generatePreprocessCommand(String filePath) {
        String fileName = CompilerUtils.getFileName(filePath);

        StringBuilder builder = new StringBuilder();
        builder.append(preprocessCommand)
                .append(SpecialCharacter.SPACE)
                .append("\"" + filePath + "\"")
                .append(SpecialCharacter.SPACE);

        if (includePaths != null && includePaths.size() != 0) {
            for (String path : includePaths) {
                builder.append(includeFlag)
                        .append("\"" + path + "\"")
                        .append(SpecialCharacter.SPACE);
            }
        }

        if (defines != null && defines.size() != 0) {
            for (String variable : defines) {
                builder.append(defineFlag)
                        .append(variable)
                        .append(SpecialCharacter.SPACE);
            }
        }

        return builder.toString();
    }

    @Override
    public String generateLinkCommand(String executableFilePath, String... outputPaths) {
        if (outputPaths == null || outputPaths.length == 0)
            return null;

        StringBuilder builder = new StringBuilder();

        builder.append(linkCommand)
                .append(SpecialCharacter.SPACE);

        for (String output : outputPaths)
            builder.append("\"" + output + "\"")
                    .append(SpecialCharacter.SPACE);

        builder.append(outputFlag)
                .append("\"" + executableFilePath + "\"");

        return builder.toString();
    }

    @Override
    public String preprocess(String inPath, String outPath) {
        String command = generatePreprocessCommand(inPath) + " " + outputFlag + "\"" + outPath + "\"";

        try {
            String[] script = CompilerUtils.prepareForTerminal(this, command);

            String stderr = new Terminal(script).getStderr();

            File outFile = new File(outPath);

            if (outFile.exists())
                return readFileContent(outPath);
            else
                return stderr;

        } catch (Exception ex) {
        }

        return null;
    }

    public static String readFileContent(INode n) {
        return readFileContent(n.getAbsolutePath());
    }
    public static String readFileContent(String filePath) {
        StringBuilder fileData = new StringBuilder(3000);
        try {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(filePath));
            char[] buf = new char[10];
            int numRead;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return fileData.toString();
        }
    }
    @Override
    public String getCompileCommand() {
        return compileCommand;
    }

    @Override
    public String getPreprocessCommand() {
        return preprocessCommand;
    }

    @Override
    public String getLinkCommand() {
        return linkCommand;
    }

    @Override
    public String getDebugCommand() {
        return debugCommand;
    }

    @Override
    public List<String> getIncludePaths() {
        return includePaths;
    }

    @Override
    public List<String> getDefines() {
        return defines;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDefineFlag() {
        return defineFlag;
    }

    @Override
    public String getIncludeFlag() {
        return includeFlag;
    }

    @Override
    public String getOutputExtension() {
        return outputExtension;
    }

    @Override
    public String getOutputFlag() {
        return outputFlag;
    }

    @Override
    public String getDebugFlag() {
        return debugFlag;
    }

    public void setCompileCommand(String compileCommand) {
        this.compileCommand = compileCommand;
    }

    public void setDebugCommand(String debugCommand) {
        this.debugCommand = debugCommand;
    }

    public void setLinkCommand(String linkCommand) {
        this.linkCommand = linkCommand;
    }

    public void setPreprocessCommand(String preprocessComand) {
        this.preprocessCommand = preprocessComand;
    }

    public void setIncludePaths(List<String> includePaths) {
        this.includePaths = includePaths;
    }

    public void setDefines(List<String> defines) {
        this.defines = defines;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefineFlag(String defineFlag) {
        this.defineFlag = defineFlag;
    }

    public void setIncludeFlag(String includeFlag) {
        this.includeFlag = includeFlag;
    }

    public void setOutputExtension(String outputExtension) {
        this.outputExtension = outputExtension;
    }

    public void setOutputFlag(String outputFlag) {
        this.outputFlag = outputFlag;
    }

    public void setDebugFlag(String debugFlag) {
        this.debugFlag = debugFlag;
    }

    public boolean isGccCommand(){
        return compileCommand.contains("gcc");
    }

    public boolean isGPlusPlusCommand(){
        return compileCommand.contains("g++");
    }
}
