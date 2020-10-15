package coverage;


import cfg.ICFG;
import cfg.object.ConditionCfgNode;
import cfg.object.ICfgNode;
import cfg.testpath.FullTestpath;
import cfg.testpath.ITestpathInCFG;
import instrument.FunctionInstrumentationForStatementvsBranch_Markerv2;
import testdata.object.StatementInTestpath_Mark;
import testdata.object.TestpathString_Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Update the visited state of CFG.
 *
 */
public class CFGUpdaterv2 implements ICFGUpdater {
    private TestpathString_Marker testpath;
    private ICFG cfg;

    public CFGUpdaterv2(TestpathString_Marker testpath, ICFG cfg) {
        this.testpath = testpath;
        this.cfg = cfg;
    }

    public static void main(String[] args) throws Exception {
        // find function
//        ProjectParser parser = new ProjectParser(new File(Paths.JOURNAL_TEST));
//        parser.setExpandTreeuptoMethodLevel_enabled(true);
//        IFunctionNode function = (IFunctionNode) Search
//                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), "Tritype(int,int,int)").get(0);
//        logger.debug(function.getAST().getRawSignature());
//
//        // generate cfg
//        CFGGenerationforBranchvsStatementvsBasispathCoverage cfgGen = new CFGGenerationforBranchvsStatementvsBasispathCoverage(function);
//        ICFG cfg = cfgGen.generateCFG();
//        cfg.setIdforAllNodes();
//        for (ICfgNode node : cfg.getAllNodes())
//            node.setVisit(false);
//
//        // get test paths from file
//        TestpathString_Marker testpath = new TestpathString_Marker();
//        String content = Utils.readFileContent(new File("/Users/ducanhnguyen/Documents/akautauto/local/working-directory/3/testpaths/Tritype@4898456.tp"));
//        content = content.replace("\r", "\n");
//        content = content.replace("\n\n", "\n");
//        String[] lines = content.split("\n");
//        testpath.setEncodedTestpath(lines);
//
//        // compute coverage
//        CFGUpdaterv2 updaterv2 = new CFGUpdaterv2(testpath, cfg);
//        cfg.setFunctionNode(function);
//        logger.debug(cfg);
//        updaterv2.updateVisitedNodes();
//        logger.debug("Visited statements = " + cfg.getVisitedStatements());
//        logger.debug("Statement coverage = " + cfg.computeStatementCoverage());
//
//        List<BranchInCFG> visitedBranches = cfg.getVisitedBranches();
//        logger.debug("Visited branches =  " + visitedBranches);
//        logger.debug("Branch coverage = " + cfg.computeBranchCoverage());


    }

    @Override
    public void updateVisitedNodes() {
        // find all nodes corresponding to statements or conditions
        Set<String> visitedOffsets = getAllVisitedNodesByItsOffset(testpath);

        // update the visited state of nodes
        updateStateOfVisitedNodeByItsOffset(cfg, visitedOffsets);

        // create a chain of visited statement in order
        updateVisitedStateOfBranches(testpath, cfg);

    }

    private void updateVisitedStateOfBranches(TestpathString_Marker testpath, ICFG cfg) {
        String visitedStatementInStr = " ";
        for (String offset : testpath.getStandardTestpathByProperty(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION))
            visitedStatementInStr += offset + " ";

        int offsetOfFunctionInSourcecodeFile = cfg.getFunctionNode().getAST().getFileLocation().getNodeOffset();
        for (ICfgNode visitedNode : cfg.getVisitedStatements()) {
            if (visitedNode instanceof ConditionCfgNode) { // condition
                int tmp = (visitedNode.getAstLocation().getNodeOffset() - offsetOfFunctionInSourcecodeFile);

                // analyze the true branch
                ICfgNode trueBranchNode = visitedNode.getTrueNode();
                boolean isUpdatedAsVisited = updateTheStateOfBranches(trueBranchNode, visitedNode, offsetOfFunctionInSourcecodeFile, visitedStatementInStr);
                if (isUpdatedAsVisited)
                    ((ConditionCfgNode) visitedNode).setVisitedTrueBranch(true);

                // analyze the false branch
                ICfgNode falseBranchNode = visitedNode.getFalseNode();
                isUpdatedAsVisited = updateTheStateOfBranches(falseBranchNode, visitedNode, offsetOfFunctionInSourcecodeFile, visitedStatementInStr);
                if (isUpdatedAsVisited)
                    ((ConditionCfgNode) visitedNode).setVisitedFalseBranch(true);
            }
        }
    }

    private Set<String> getAllVisitedNodesByItsOffset(TestpathString_Marker testpath) {
        Set<String> visitedOffsets = new HashSet<>();
        for (StatementInTestpath_Mark line : testpath.getStandardTestpathByAllProperties()) {
            if (line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION) != null &&
                    line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION) != null) {
                // is statement or condition
                visitedOffsets.add(line.getPropertyByName(FunctionInstrumentationForStatementvsBranch_Markerv2.START_OFFSET_IN_FUNCTION).getValue());
            }
        }
        return visitedOffsets;
    }

    private void updateStateOfVisitedNodeByItsOffset(ICFG cfg, Set<String> visitedOffsets) {
        List<ICfgNode> nodes = cfg.getAllNodes();
        for (ICfgNode node : nodes)
            if (node.getAstLocation() != null) {
                String offsetInCFG = node.getAstLocation().getNodeOffset() - cfg.getFunctionNode().getAST().getFileLocation().getNodeOffset() + "";
                //String offsetInCFG = node.getParent() - cfg.getFunctionNode().getAST().getFileLocation().getNodeOffset() + "";
                if (visitedOffsets.contains(offsetInCFG)) {
                    node.setVisit(true);
                }
            }
    }

    private boolean updateTheStateOfBranches(ICfgNode branchNode, ICfgNode visitedNode,
                                             int offsetOfFunctionInSourcecodeFile, String visitedStatementInStr) {
        if (visitedNode instanceof ConditionCfgNode && branchNode != null) {
            boolean isUpdated = false;
            List<ICfgNode> flagNodes = new ArrayList<>();

            int offsetVisitedNode = visitedNode.getAstLocation().getNodeOffset();

            // ignore nodes corresponding to flag
            while (branchNode != null && branchNode.isSpecialCfgNode()) {
                flagNodes.add(branchNode);
                branchNode = branchNode.getTrueNode();  // for these type of nodes, true node and false node are the same
            }

            if (branchNode == null){
                // the current node point to the end node of cfg
                for (ICfgNode flagNode : flagNodes)
                    flagNode.setVisit(true);
                return true;

            } else {
                // make a comparison to check whether the branch node is visited
                String comparison = " " + (offsetVisitedNode - offsetOfFunctionInSourcecodeFile) + " " +
                        (branchNode.getAstLocation().getNodeOffset() - offsetOfFunctionInSourcecodeFile) + " ";
                if (visitedStatementInStr.contains(comparison)) {
                    isUpdated = true;
                    branchNode.setVisit(true);
//                    logger.debug("updated the branch " + comparison);
                    // update the flag nodes between the condition node and the normal nodes in its checked branch
                    for (ICfgNode flagNode : flagNodes)
                        flagNode.setVisit(true);
                    return isUpdated;
                } else
                    return false;
            }
        } else
            return false;
    }

    @Override
    public String[] getTestpath() {
        return new String[0];
    }

    @Override
    public void setTestpath(String[] testpath) {
        // nothing to do
    }

    @Override
    public ICFG getCfg() {
        return cfg;
    }

    @Override
    public void setCfg(ICFG cfg) {
        this.cfg = cfg;
    }

    @Override
    public ITestpathInCFG getUpdatedCFGNodes() {
        ITestpathInCFG updatedCFGNodes = new FullTestpath();
        for (ICfgNode node : getCfg().getVisitedStatements()) {
            updatedCFGNodes.getAllCfgNodes().add(node);
        }
        return updatedCFGNodes;
    }

    @Override
    public void setUpdatedCFGNodes(ITestpathInCFG updatedCFGNodes) {
        // nothing to do
    }

    @Override
    public void unrollChangesOfTheLatestPath() {
        // nothing to do
    }
}
