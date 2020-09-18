package compiler;

import compiler.message.ICompileMessage;
import tree.object.INode;

import java.util.List;

public interface ICompiler {
    ICompileMessage compile(INode root);

    ICompileMessage compile(String filePath);

    ICompileMessage link(String executableFilePath, String... outputPaths);

    String generateCompileCommand(String filePath);

    String generatePreprocessCommand(String filePath);

    String preprocess(String inPath, String outPath);

    // the project path will contain the executable file
    String generateLinkCommand(String executablePath, String... outputPaths);

    String getCompileCommand();

    String getPreprocessCommand();

    String getLinkCommand();

    String getDebugCommand();

    List<String> getIncludePaths();

    List<String> getDefines();

    String getName();

    String getDefineFlag();

    String getIncludeFlag();

    String getOutputExtension();

    String getOutputFlag();

    String getDebugFlag();

    String INCLUDE_FILE_FLAG = "-include";
}
