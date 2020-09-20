package project_init;


import org.eclipse.cdt.core.dom.ast.IASTNode;
import tree.object.AbstractFunctionNode;

public interface IProjectCloneMap {
    IASTNode getClonedASTNode(IASTNode origin);

    int getLineInFunction(IASTNode origin);

    int getLineInFunction(AbstractFunctionNode functionNode, int line);
}

