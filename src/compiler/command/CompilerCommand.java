package compiler.command;

import compiler.Compiler;

public abstract class CompilerCommand {

    protected String outputPath;

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    protected boolean isWrapInDoubleQuote(String path) {
        return path.startsWith(DOUBLE_QUOTE) && path.endsWith(DOUBLE_QUOTE);
    }

    public abstract String[] toScript(Compiler compiler);

    protected static final String LIB_FLAG = "-l";
    protected static final String SPACE_REGEX = "\\s+";
    protected static final String DOUBLE_QUOTE = "\"";
}
