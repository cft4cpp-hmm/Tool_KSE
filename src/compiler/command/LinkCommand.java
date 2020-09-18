package compiler.command;

import compiler.Compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinkCommand extends CompilerCommand {

    private String[] binFiles;

    public String[] getBinFiles() {
        return binFiles;
    }

    public void setBinFiles(List<String> binFiles) {
        this.binFiles = binFiles.toArray(new String[0]);
    }

    public void setBinFiles(String[] binFiles) {
        this.binFiles = binFiles;
    }

    @Override
    public String[] toScript(Compiler compiler) {
        List<String> output = new ArrayList<>(Arrays.asList(compiler.getCompileCommand().split(SPACE_REGEX)));

        for (String binFile : binFiles) {
            output.add(isWrapInDoubleQuote(binFile) ? binFile.substring(0, binFile.length() - 1) : binFile);
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
}
