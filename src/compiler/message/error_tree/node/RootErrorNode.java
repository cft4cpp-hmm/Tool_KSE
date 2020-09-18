package com.dse.compiler.message.error_tree.node;

import com.dse.parser.object.SourcecodeFileNode;

public class RootErrorNode extends ErrorNode {
    private SourcecodeFileNode<?> source;

    public SourcecodeFileNode<?> getSource() {
        return source;
    }

    public void setSource(SourcecodeFileNode<?> source) {
        this.source = source;
    }
}
