package coverage;


import tree.object.INode;
import tree.object.SourcecodeFileNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Compute the number of statements/branches
 */
public class InstructionComputator {
    public static final int BRANCH_COV_INDEX = 0;
    public static final int STATEMENT_COV_INDEX = 1;
    public static final int MCDC_COV_INDEX = 2;

    private static final String LOG_FORMAT = "[%d/%d] calculate num of visited %s in %s";
    private static final String ERROR_FORMAT = "Error with %s: %s";

    private final int[] result = new int[3];

    private int count, size;

    public static void compute(INode projectNode) {
//        List<INode> sourcecodeNodes = Search.searchNodes(projectNode, new SourcecodeFileNodeCondition());
//        InstructionMapping nStatements = new InstructionMapping();
//        InstructionMapping nBranches = new InstructionMapping();
//        InstructionMapping nMcdcs = new InstructionMapping();
//
//        List<String> computedSourcecodes = new ArrayList<>();
//        for (INode sourcecodeNode : sourcecodeNodes)
//            if (sourcecodeNode instanceof SourcecodeFileNode &&
//                    // to avoid duplicating instruction computation
//                    !computedSourcecodes.contains(sourcecodeNode.getAbsolutePath())) {
//                computedSourcecodes.add(sourcecodeNode.getAbsolutePath());
//
//                try {
//                    InstructionComputator instructionComputator = new InstructionComputator();
//                    int[] instructionComputation = instructionComputator.getNumberOfBranchesAndStatements(sourcecodeNode);
//
//                    int nStatement = instructionComputation[InstructionComputator.STATEMENT_COV_INDEX];
//                    //nStatements.put(PathUtils.toRelative(sourcecodeNode.getAbsolutePath()), nStatement);
//
//                    int nBranch = instructionComputation[InstructionComputator.BRANCH_COV_INDEX];
//                    //nBranches.put(PathUtils.toRelative(sourcecodeNode.getAbsolutePath()), nBranch);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }

//        Environment.getInstance().setStatementsMapping(nStatements);
//        Environment.getInstance().setBranchesMapping(nBranches);

//        // save the number of statements to file
//        {
//            String nStmFilePath = new WorkspaceConfig().fromJson().getnStatementPath();
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            String nStatementsJson = gson.toJson(nStatements);
//            Utils.writeContentToFile(nStatementsJson, nStmFilePath);
//        }
//
//        // save the number of branches to file
//        {
//            String nBranchFilePath = new WorkspaceConfig().fromJson().getnBranchPath();
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            String nBranchesJson = gson.toJson(nBranches);
//            Utils.writeContentToFile(nBranchesJson, nBranchFilePath);
//        }

    }


    public int[] getNumberOfBranchesAndStatements(INode consideredSourcecodeNode) {

        // constructor, destructor, normal function
//        List<INode> functionNodes = Search.searchNodes(consideredSourcecodeNode, new AbstractFunctionNodeCondition());
//
//        // macro function
//        List<INode> macroFunctionNodes = Search.searchNodes(consideredSourcecodeNode, new MacroFunctionNodeCondition());
//
//        size = (functionNodes.size() + macroFunctionNodes.size()) * 3;
//
//        for (INode node : functionNodes) {
//            if (node instanceof IFunctionNode) {
//                IFunctionNode functionNode = (IFunctionNode) node;
//                computeNode(functionNode);
//            }
//        }
//
//        for (INode node : macroFunctionNodes)
//            if (node instanceof MacroFunctionNode) {
//                MacroFunctionNode macroFunctionNode = (MacroFunctionNode) node;
//                IFunctionNode correspondingNode = macroFunctionNode.getCorrespondingFunctionNode();
//                computeNode(correspondingNode);
//            }

        return result;
    }

//    private void computeNode(IFunctionNode functionNode) {
//        String functionName = functionNode.getName();
//
//        logger.debug(String.format(LOG_FORMAT, ++count, size, EnviroCoverageTypeNode.STATEMENT, functionName));
//        result[STATEMENT_COV_INDEX] += computeNodeByType(functionNode, EnviroCoverageTypeNode.STATEMENT);
//
//        logger.debug(String.format(LOG_FORMAT, ++count, size, EnviroCoverageTypeNode.BRANCH, functionName));
//        result[BRANCH_COV_INDEX] += computeNodeByType(functionNode, EnviroCoverageTypeNode.BRANCH);
//
//        logger.debug(String.format(LOG_FORMAT, ++count, size, EnviroCoverageTypeNode.MCDC, functionName));
//        result[MCDC_COV_INDEX] += computeNodeByType(functionNode, EnviroCoverageTypeNode.MCDC);
//    }
//
//    private int computeNodeByType(IFunctionNode functionNode, String coverageType) {
//        try {
//            ICFG cfg = Utils.createCFG(functionNode, coverageType);
//
//            switch (coverageType) {
//                case EnviroCoverageTypeNode.BRANCH:
//                case EnviroCoverageTypeNode.MCDC:
//                    return cfg.getVisitedBranches().size() + cfg.getUnvisitedBranches().size();
//
//                case EnviroCoverageTypeNode.STATEMENT:
//                    return cfg.getVisitedStatements().size() + cfg.getUnvisitedStatements().size();
//
//                default:
//                    return 0;
//            }
//
//        } catch (Exception ex) {
//            logger.debug(String.format(ERROR_FORMAT, functionNode.getName(), ex.getMessage()));
//            return 0;
//        }
//    }
}
