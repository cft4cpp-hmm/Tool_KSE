package coverage;

import tree.object.INode;
import utils.search.SearchCondition;

public class MacroFunctionNodeCondition extends SearchCondition
{

    @Override
    public boolean isSatisfiable(INode n) {
        return n instanceof MacroFunctionNode;
    }
}
