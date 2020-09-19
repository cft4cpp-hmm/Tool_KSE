package coverage;

import cfg.CFGGenerationforSubConditionCoverage;
import cfg.ICFG;
import parser.projectparser.ICommonFunctionNode;
import tree.object.AbstractFunctionNode;
import tree.object.IFunctionNode;
import tree.object.INode;
import utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to compute coverage of function level
 * <p>
 * The type of coverage is only STATEMENT, BRANCH
 * <p>
 */
public class FunctionCoverageComputation extends AbstractCoverageComputation {

    protected ICommonFunctionNode functionNode;

    public static void main(String[] args) {
    }

    public void compute() {
        if (functionNode == null
                || !(functionNode instanceof IFunctionNode)
                || testpathContent == null || testpathContent.length() == 0) {
            this.numberOfInstructions = getNumberOfInstructions(functionNode, coverage);
            return;
        }
        if (coverage.equals(EnviroCoverageTypeNode.STATEMENT_AND_BRANCH) ||
                coverage.equals(EnviroCoverageTypeNode.STATEMENT_AND_MCDC))
            return;

        Map<String, TestpathsOfAFunction> affectedFunctions = categoryTestpathByFunctionPath(testpathContent.split("\n"), coverage);
        affectedFunctions = removeRedundantTestpath(affectedFunctions);

        int nInstructions = getNumberOfInstructions(functionNode, coverage);

        int nVisitedInstructions;
        nVisitedInstructions = getNumberOfVisitedInstructions(affectedFunctions, coverage, consideredSourcecodeNode, allCFG);

        this.numberOfInstructions = nInstructions;
        this.numberOfVisitedInstructions = nVisitedInstructions;
    }

    protected Map<String, TestpathsOfAFunction> removeRedundantTestpath(Map<String, TestpathsOfAFunction> affectedFunctions){
        Map<String, TestpathsOfAFunction> output = new HashMap<>();
        String path = functionNode.getAbsolutePath();
        output.put(path, affectedFunctions.get(path));
        return output;
    }

    protected int getNumberofBranches(INode functionNode) {
        int nBranches = 0;
        if (functionNode instanceof AbstractFunctionNode) {
            try {
                IFunctionNode clone = (IFunctionNode) functionNode.clone();
                CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(clone);

                ICFG cfg = cfgGen.generateCFG();
                if (cfg != null) {
                    nBranches += cfg.getVisitedBranches().size() + cfg.getUnvisitedBranches().size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return nBranches;
    }

    protected int getNumberofStatements(INode functionNode) {
        int nStatements = 0;
        if (functionNode instanceof AbstractFunctionNode) {
            try {
                IFunctionNode clone = (IFunctionNode) functionNode.clone();
                CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(clone);
                ICFG cfg = cfgGen.generateCFG();
                if (cfg != null) {
                    nStatements += cfg.getVisitedStatements().size() + cfg.getUnvisitedStatements().size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return nStatements;
    }


    public void setFunctionNode(ICommonFunctionNode functionNode) {
        this.functionNode = functionNode;
    }

    public ICommonFunctionNode getFunctionNode() {
        return functionNode;
    }
}
