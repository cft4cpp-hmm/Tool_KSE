package com.dse.coverage;

import auto_testcase_generation.cfg.ICFG;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.environment.Environment;
import com.dse.coverage.highlight.SourcecodeHighlighterForCoverage;
import com.dse.parser.ProjectParser;
import com.dse.parser.object.*;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.util.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to compute coverage of function level
 * <p>
 * The type of coverage is only STATEMENT, BRANCH, and MCDC.
 * <p>
 * For STATEMENT+BRANCH, STATEMENT+MCDC, these kinds of coverage include two coverage types.
 */
public class FunctionCoverageComputation extends AbstractCoverageComputation {

    protected ICommonFunctionNode functionNode;

    public static void main(String[] args) {
        // parse project
        ProjectParser projectParser = new ProjectParser(new File("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/macro"));

        projectParser.setCpptoHeaderDependencyGeneration_enabled(true);
        projectParser.setExpandTreeuptoMethodLevel_enabled(true);
        projectParser.setExtendedDependencyGeneration_enabled(true);

        ProjectNode projectRoot = projectParser.getRootTree();
        Environment.getInstance().setProjectNode(projectRoot);

        ISourcecodeFileNode consideredSourcecodeNode = (ISourcecodeFileNode) Search
                .searchNodes(projectParser.getRootTree(), new SourcecodeFileNodeCondition(), "ex3.cpp").get(0);
        System.out.println(consideredSourcecodeNode.getAbsolutePath());

        //
        String tpFile = "/Users/ducanhnguyen/Documents/akautauto/local/working-directory/nnnn/test-paths/test1.97817.tp";
        FunctionCoverageComputation computator = new FunctionCoverageComputation();
        computator.setTestpathContent(Utils.readFileContent(tpFile));
        computator.setConsideredSourcecodeNode(consideredSourcecodeNode);
        computator.setCoverage(EnviroCoverageTypeNode.STATEMENT);
        computator.compute();
        System.out.println("number of instructions = " + computator.getNumberOfInstructions());
        System.out.println("number of visited instructions = " + computator.getNumberOfVisitedInstructions());

        // highlighter
        SourcecodeHighlighterForCoverage highlighter = new SourcecodeHighlighterForCoverage();
        highlighter.setSourcecode(consideredSourcecodeNode.getAST().getRawSignature());
        highlighter.setTestpathContent(Utils.readFileContent(tpFile));
        highlighter.setSourcecodePath("/Users/ducanhnguyen/Documents/akautauto/datatest/duc-anh/macro/ex3.cpp");
        highlighter.setAllCFG(computator.getAllCFG());
        highlighter.setTypeOfCoverage(computator.getCoverage());
        highlighter.highlight();
        Utils.writeContentToFile(highlighter.getFullHighlightedSourcecode(), "/Users/ducanhnguyen/Desktop/x.html");
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

    protected int getNumberofMcdcs(INode functionNode) {
        int nMcdcs = 0;
        if (functionNode instanceof AbstractFunctionNode) {
            try {
                ICFG cfg = Utils.createCFG((IFunctionNode) functionNode, EnviroCoverageTypeNode.MCDC);
                if (cfg != null) {
                    nMcdcs += cfg.getVisitedBranches().size() + cfg.getUnvisitedBranches().size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (functionNode instanceof MacroFunctionNode) {
            try {
                ICFG cfg = Utils.createCFG(((MacroFunctionNode) functionNode).getCorrespondingFunctionNode(), EnviroCoverageTypeNode.MCDC);
                if (cfg != null) {
                    nMcdcs += cfg.getVisitedBranches().size() + cfg.getUnvisitedBranches().size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return nMcdcs;
    }

    protected int getNumberofBranches(INode functionNode) {
        int nBranches = 0;
        if (functionNode instanceof AbstractFunctionNode) {
            try {
                ICFG cfg = Utils.createCFG((IFunctionNode) functionNode, EnviroCoverageTypeNode.BRANCH);
                if (cfg != null) {
                    nBranches += cfg.getVisitedBranches().size() + cfg.getUnvisitedBranches().size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (functionNode instanceof MacroFunctionNode) {
            try {
                ICFG cfg = Utils.createCFG(((MacroFunctionNode) functionNode).getCorrespondingFunctionNode(), EnviroCoverageTypeNode.BRANCH);
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
                ICFG cfg = Utils.createCFG((IFunctionNode) functionNode, EnviroCoverageTypeNode.STATEMENT);
                if (cfg != null) {
                    nStatements += cfg.getVisitedStatements().size() + cfg.getUnvisitedStatements().size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (functionNode instanceof MacroFunctionNode) {
            try {
                ICFG cfg = Utils.createCFG(((MacroFunctionNode) functionNode).getCorrespondingFunctionNode(), EnviroCoverageTypeNode.STATEMENT);
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
