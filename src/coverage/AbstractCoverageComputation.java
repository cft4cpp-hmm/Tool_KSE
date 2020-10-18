package coverage;

import Common.TestConfig;
import cfg.CFGGenerationforBranchvsStatementCoverage;
import cfg.CFGGenerationforSubConditionCoverage;
import cfg.ICFG;
import instrument.IFunctionInstrumentationGeneration;
import testcase_manager.TestCase;
import testdata.object.TestpathString_Marker;
import testdatagen.coverage.ICoverageComputation;
import tree.object.AbstractFunctionNode;
import tree.object.IFunctionNode;
import tree.object.INode;
import utils.PathUtils;
import utils.Utils;
import utils.search.AbstractFunctionNodeCondition;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import java.util.*;

public abstract class AbstractCoverageComputation implements ICoverageComputation
{

    protected String testpathContent; // may visit may source code files
    protected INode consideredSourcecodeNode; // the source code file which we need to compute coverage at file level
    protected String coverage;
    protected int numberOfVisitedInstructions; // depend on coverage, instruction is statement, condition, or sub-condition
    protected int numberOfInstructions;
    protected int numberOfVisitedBranches; // depend on coverage, instruction is statement, condition, or sub-condition
    protected int numberOfBranches;
    protected List<ICFG> allCFG = new ArrayList<>();

    protected abstract Map<String, TestpathsOfAFunction> removeRedundantTestpath(Map<String, TestpathsOfAFunction> affectedFunctions);

    public int getNumberOfInstructions(INode consideredSourcecodeNode, String coverage)
    {
        int nInstructions = 0;
        switch (coverage)
        {
            case EnviroCoverageTypeNode.STATEMENT:
            {
                nInstructions = getNumberofStatements(consideredSourcecodeNode);
                break;
            }
            case EnviroCoverageTypeNode.BRANCH:
            {
                nInstructions = getNumberofBranches(consideredSourcecodeNode);
                break;
            }
        }
        return nInstructions;
    }

    /**
     * @return the number of visited instructions (>=0), return -1 if there is error happening
     */
    protected int getNumberOfVisitedInstructions(Map<String, TestpathsOfAFunction> affectedFunctions,
                                                 String coverage, INode consideredSourcecodeNode,
                                                 List<ICFG> allCFG)
    {
        final int ERROR = -1;
        int nVisitedInstructions = 0;
        for (String functionPath : affectedFunctions.keySet())
        // only consider functions in a specified source code file
        {
            if (functionPath.contains(consideredSourcecodeNode.getAbsolutePath()))
            {
                // Find the function node
                TestpathsOfAFunction testpathsOfAFunction = affectedFunctions.get(functionPath);

                String functionName = functionPath.substring(functionPath.lastIndexOf("\\") + 1);

                List<INode> functionNodes = Search.searchNodes(consideredSourcecodeNode, new AbstractFunctionNodeCondition(), PathUtils.toAbsolute(functionPath));
                if (functionNodes.size() == 0)
                {
                    functionNodes = Search.searchNodes(consideredSourcecodeNode, new FunctionNodeCondition(), functionName);
                }

                if (functionNodes.size() != 1)
                {
                    return ERROR;
                }

                // generate cfg of the function
                INode functionNode = functionNodes.get(0);
                try
                {
                    ICFG cfg = null;
                    if (functionNode instanceof AbstractFunctionNode)
                    {

                        IFunctionNode clone = (IFunctionNode) functionNode.clone();
                        //CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(clone);
                        CFGGenerationforBranchvsStatementCoverage cfgGen = new CFGGenerationforBranchvsStatementCoverage(clone);

                        cfg = cfgGen.generateCFG();
                        allCFG.add(cfg);
                        cfg.setFunctionNode((IFunctionNode) functionNode);
                        cfg.resetVisitedStateOfNodes();
                    }

                    if (cfg == null)
                    {
                        return ERROR;
                    }

                    // compute coverage of a cfg
                    TestpathString_Marker testpath = new TestpathString_Marker();

                    if (testpathsOfAFunction != null && testpathsOfAFunction.getTestpathsInArray() != null)
                    {
                        testpath.setEncodedTestpath(testpathsOfAFunction.getTestpathsInArray());
                    }
                    else
                    {
                        testpath.setEncodedTestpath(new String[]{});
                    }

                    CFGUpdaterv2 cfgUpdaterv2 = new CFGUpdaterv2(testpath, cfg);
                    cfgUpdaterv2.updateVisitedNodes();
                    switch (coverage)
                    {
                        case EnviroCoverageTypeNode.STATEMENT:
                        {
                            nVisitedInstructions += cfg.getVisitedStatements().size();
                            break;
                        }
                        case EnviroCoverageTypeNode.BRANCH:
                        {
                            nVisitedInstructions += cfg.countVisitedBranches();
                            break;
                        }
//                        case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
//                            nVisitedInstructions += cfg.getVisitedStatements().size();
//                            nVisitedInstructions += cfg.getVisitedBranches().size();
//                            break;
//                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return nVisitedInstructions;
    }


    public int getNumberOfVisitedInstructions(IFunctionNode functionNode,
                                              String coverage, List<TestCase> testCaseList)
    {
        final int ERROR = -1;
        int nVisitedInstructions = 0;
        // only consider functions in a specified source code file

        // generate cfg of the function
        try
        {
            ICFG cfg = null;
            if (functionNode instanceof AbstractFunctionNode)
            {
                IFunctionNode clone = (IFunctionNode) functionNode.clone();
                CFGGenerationforBranchvsStatementCoverage cfgGen = new CFGGenerationforBranchvsStatementCoverage(clone);

                cfg = cfgGen.generateCFG();
                allCFG.add(cfg);
                cfg.setFunctionNode((IFunctionNode) functionNode);
                cfg.resetVisitedStateOfNodes();
            }

            if (cfg == null)
            {
                return ERROR;
            }

            List<String> testPathList = new ArrayList<>();

            for(TestCase testCase: testCaseList)
            {
                String testPathFile = TestConfig.TESTPATH_FILE + "\\" + testCase.getName() + TestConfig.TESTPATH_EXTENTION;

                String tpContent = Utils.readFileContent(testPathFile);

                String[] test = tpContent.split("\n");

                testPathList.addAll(Arrays.asList(test));
            }

            TestpathsOfAFunction testpathsOfAFunction = readTestpathByFunctionPath(testPathList, EnviroCoverageTypeNode.BRANCH);

            // compute coverage of a cfg
            TestpathString_Marker testpath = new TestpathString_Marker();

            if (testpathsOfAFunction != null && testpathsOfAFunction.getTestpathsInArray() != null)
            {
                testpath.setEncodedTestpath(testpathsOfAFunction.getTestpathsInArray());
            }
            else
            {
                testpath.setEncodedTestpath(new String[]{});
            }

            CFGUpdaterv2 cfgUpdaterv2 = new CFGUpdaterv2(testpath, cfg);
            cfgUpdaterv2.updateVisitedNodes();
            switch (coverage)
            {
                case EnviroCoverageTypeNode.STATEMENT:
                {
                    nVisitedInstructions += cfg.getVisitedStatements().size();
                    break;
                }
                case EnviroCoverageTypeNode.BRANCH:
                {
                    nVisitedInstructions += cfg.countVisitedBranches();
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return nVisitedInstructions;
    }

    /**
     * @return a hash map, where key is the path of function, value is a list.
     * Each item in list is corresponding to a test path.
     */
    protected Map<String, TestpathsOfAFunction> categoryTestpathByFunctionPath(String[] testpaths, String coverage)
    {
        Map<String, TestpathsOfAFunction> tps = new HashMap<>();

        for (String testpath : testpaths)
        {
            String functionAddress = getValue(testpath, IFunctionInstrumentationGeneration.FUNCTION_ADDRESS);

            if (functionAddress != null && functionAddress.length() > 0)
            {
                functionAddress = PathUtils.toAbsolute(functionAddress);
                functionAddress = Utils.normalizePath(functionAddress);

                switch (coverage)
                {
                    case EnviroCoverageTypeNode.BRANCH:
                    case EnviroCoverageTypeNode.STATEMENT:
                    {
//                        if (AbstractHighlighterForSourcecodeLevel.isSubCondition(testpath))
//                            // ignore the test path which goes through subcondition
//                            continue;
                        //else
                        break;
                    }
                }

                if (!tps.containsKey(functionAddress))
                {
                    tps.put(functionAddress, new TestpathsOfAFunction());
                }

                Offset offset = new Offset();
                offset.startingOffsetInFunction = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.START_OFFSET_IN_FUNCTION));
                offset.endOffsetInFunction = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.END_OFFSET_IN_FUNCTION));
                offset.startingOffsetInSourcecodeFile = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.START_OFFSET_IN_SOURCE_CODE_FILE));
                offset.endOffsetInSourcecodeFile = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.END_OFFSET_IN_SOURCE_CODE_FILE));
                offset.testpath = testpath;
                tps.get(functionAddress).testpaths.add(offset);
            }
        }
        return tps;
    }

    public TestpathsOfAFunction readTestpathByFunctionPath(List<String> testpaths, String coverage)
    {
        TestpathsOfAFunction testpathsOfAFunction = new TestpathsOfAFunction();

        for (String testpath : testpaths)
        {
            String functionAddress = getValue(testpath, IFunctionInstrumentationGeneration.FUNCTION_ADDRESS);

            if (functionAddress != null && functionAddress.length() > 0)
            {
                switch (coverage)
                {
                    case EnviroCoverageTypeNode.BRANCH:
                    case EnviroCoverageTypeNode.STATEMENT:
                    {
//                        if (AbstractHighlighterForSourcecodeLevel.isSubCondition(testpath))
//                            // ignore the test path which goes through subcondition
//                            continue;
                        //else
                        break;
                    }
                }

                Offset offset = new Offset();
                offset.startingOffsetInFunction = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.START_OFFSET_IN_FUNCTION));
                offset.endOffsetInFunction = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.END_OFFSET_IN_FUNCTION));
                offset.startingOffsetInSourcecodeFile = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.START_OFFSET_IN_SOURCE_CODE_FILE));
                offset.endOffsetInSourcecodeFile = Utils.toInt(getValue(testpath, IFunctionInstrumentationGeneration.END_OFFSET_IN_SOURCE_CODE_FILE));
                offset.testpath = testpath;
                if (!testpathsOfAFunction.testpaths.contains(offset))
                {
                    testpathsOfAFunction.testpaths.add(offset);
                }
            }
        }
        return testpathsOfAFunction;
    }

    protected String getValue(String line, String property)
    {
        if (line.contains(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTIES))
        {
            String[] tokens = line.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTIES);
            for (String token : tokens)
            {
                if (token.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE)[0].equals(property))
                {
                    return token.split(IFunctionInstrumentationGeneration.DELIMITER_BETWEEN_PROPERTY_AND_VALUE)[1].trim();
                }
            }
        }
        return null;
    }

    protected abstract int getNumberofBranches(INode consideredSourcecodeNode);

    protected abstract int getNumberofStatements(INode consideredSourcecodeNode);


    public void setTestpathContent(String testpathContent)
    {
        this.testpathContent = testpathContent;
    }

    public String getTestpathContent()
    {
        return testpathContent;
    }

    public void setConsideredSourcecodeNode(INode consideredSourcecodeNode)
    {
        this.consideredSourcecodeNode = consideredSourcecodeNode;
    }

    public INode getConsideredSourcecodeNode()
    {
        return consideredSourcecodeNode;
    }

    public String getCoverage()
    {
        return coverage;
    }

    public void setCoverage(String coverage)
    {
        this.coverage = coverage;
    }

    public int getNumberOfInstructions()
    {
        return numberOfInstructions;
    }

    public void setNumberOfInstructions(int numberOfInstructions)
    {
        this.numberOfInstructions = numberOfInstructions;
    }

    public int getNumberOfVisitedInstructions()
    {
        return numberOfVisitedInstructions;
    }

    public void setNumberOfVisitedInstructions(int numberOfVisitedInstructions)
    {
        this.numberOfVisitedInstructions = numberOfVisitedInstructions;
    }

    public List<ICFG> getAllCFG()
    {
        return allCFG;
    }

    public void setAllCFG(List<ICFG> allCFG)
    {
        this.allCFG = allCFG;
    }

    public int getNumberOfVisitedBranches()
    {
        return numberOfVisitedBranches;
    }

    public void setNumberOfVisitedBranches(int numberOfVisitedBranches)
    {
        this.numberOfVisitedBranches = numberOfVisitedBranches;
    }

    public int getNumberOfBranches()
    {
        return numberOfBranches;
    }

    public void setNumberOfBranches(int numberOfBranches)
    {
        this.numberOfBranches = numberOfBranches;
    }

    static class TestpathsOfAFunction
    {
        ArrayList<Offset> testpaths = new ArrayList<>();

        @Override
        public String toString()
        {
            return testpaths.toString();
        }

        String[] getTestpathsInArray()
        {
            String[] tpInArray = new String[testpaths.size()];
            int count = 0;
            for (Offset offset : testpaths)
            {
                tpInArray[count] = offset.testpath;
                count++;
            }
            return tpInArray;
        }
    }

    static class Offset
    {
        int startingOffsetInSourcecodeFile;
        int endOffsetInSourcecodeFile;
        int startingOffsetInFunction;
        int endOffsetInFunction;
        String testpath;

        @Override
        public String toString()
        {
            return "offset in source code file = " + startingOffsetInSourcecodeFile + ":" + endOffsetInSourcecodeFile + ", offset in function = " + startingOffsetInFunction + ":" + endOffsetInFunction + "\n";
        }
    }
}
