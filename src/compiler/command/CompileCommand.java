package compiler.command;

import compiler.Compiler;
import compiler.ICompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompileCommand extends compiler.command.CompilerCommand
{

    private String[] includeDirectories;

    private String[] defines;

    private String[] includeFiles;

    private String path;

    @Override
    public String[] toScript(Compiler compiler) {
        List<String> output = new ArrayList<>(Arrays.asList(compiler.getCompileCommand().split(SPACE_REGEX)));

        output.add(isWrapInDoubleQuote(path) ? path.substring(0, path.length() - 1) : path);

        for (String includeDir : includeDirectories) {
            output.add(compiler.getIncludeFlag());
            output.add(isWrapInDoubleQuote(includeDir) ? includeDir.substring(0, includeDir.length() - 1) : includeDir);
        }

        for (String define : defines) {
            output.add(compiler.getDefineFlag());
            output.add(define);
        }

        for (String includeFile : includeFiles) {
            output.add(ICompiler.INCLUDE_FILE_FLAG);
            output.add(isWrapInDoubleQuote(includeFile) ? includeFile.substring(0, includeFile.length() - 1) : includeFile);
        }

        output.add(compiler.getOutputFlag());
        output.add(isWrapInDoubleQuote(outputPath) ? outputPath.substring(0, outputPath.length() - 1) : outputPath);

        int i = 0;
        while (i < output.size()) {
            String current = output.get(i);
            if (current.equals(compiler.getOutputFlag())) {
                break;
            } else if (current.startsWith(LIB_FLAG)) {
                output.add(output.remove(i));
            } else {
                i++;
            }
        }

        return output.toArray(new String[0]);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String[] getIncludeDirectories() {
        return includeDirectories;
    }

    public void setIncludeDirectories(String[] includeDirectories) {
        this.includeDirectories = includeDirectories;
    }

    public void setIncludeDirectories(List<String> includeDirectories) {
        this.includeDirectories = includeDirectories.toArray(new String[0]);
    }

    public String[] getDefines() {
        return defines;
    }

    public void setDefines(String[] defines) {
        this.defines = defines;
    }

    public void setDefines(List<String> defines) {
        this.defines = defines.toArray(new String[0]);
    }

    public String[] getIncludeFiles() {
        return includeFiles;
    }

    public void setIncludeFiles(String[] includeFiles) {
        this.includeFiles = includeFiles;
    }

    public void setIncludeFiles(List<String> includeFiles) {
        this.includeFiles = includeFiles.toArray(new String[0]);
    }
}
