package HybridAutoTestGen;

import cfg.CFG;
import cfg.CFGGenerationforSubConditionCoverage;
import cfg.ICFG;
import cfg.object.AbstractConditionLoopCfgNode;
import cfg.object.ConditionCfgNode;
import cfg.object.EndFlagCfgNode;
import cfg.object.ICfgNode;
import cfg.testpath.*;
import config.*;
import constraints.checker.RelatedConstraintsChecker;
import coverage.FunctionCoverageComputation;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Pair;
import normalizer.FunctionNormalizer;
import org.apache.log4j.Logger;
import parser.projectparser.ProjectParser;
import testdatagen.se.ISymbolicExecution;
import testdatagen.se.Parameter;
import testdatagen.se.PathConstraint;
import testdatagen.se.SymbolicExecution;
import testdatagen.se.solver.ISmtLibGeneration;
import testdatagen.se.solver.RunZ3OnCMD;
import testdatagen.se.solver.SmtLibGeneration;
import testdatagen.se.solver.Z3SolutionParser;
import tree.object.IFunctionNode;
import tree.object.IProjectNode;
import tree.object.IVariableNode;
import utils.SpecialCharacter;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class HybridAutoTestGen extends Application
{

    public static final String CONSTRAINTS_FILE = Settingv2.getValue(ISettingv2.SMT_LIB_FILE_PATH);
    public static final String Z3 = Settingv2.getValue(ISettingv2.SOLVER_Z3_PATH);
    final static Logger logger = Logger.getLogger(FullBoundedTestGen.class);
    /**
     * Represent control flow graph
     */
    private ICFG cfg;
    private int maxIterationsforEachLoop = ITestpathGeneration.DEFAULT_MAX_ITERATIONS_FOR_EACH_LOOP;
    private FullTestpaths possibleTestpaths = new FullTestpaths();
    public List<TestData> testCases;
    public IFunctionNode function;
    public IProjectNode projectNode;
    private int maxloop = 0;
    private String functionName;
    private String sourceFolder;
    private List<IVariableNode> variables;
    private float boundStep = 1;
    private boolean solvePathWhenGenBoundaryTestData = false;

    float durationTotal = 0;
    WeightedGraph graph = null;

    protected FunctionCoverageComputation functionCoverageComputation;

    public void setSolvePathWhenGenBoundaryTestData(boolean value)
    {
        solvePathWhenGenBoundaryTestData = value;
    }

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {

    }

    public HybridAutoTestGen(int _maxloop, String _functionName, String _sourceFolder, float _boundStep)
    {
        maxloop = _maxloop;
        functionName = _functionName;
        sourceFolder = _sourceFolder;
        boundStep = _boundStep;
    }

    //public void generateTestData(int maxloop, String functionName, String sourceFolder) throws Exception
    public void generateTestData(float boundStep) throws Exception
    {
        ProjectParser parser = new ProjectParser(new File(sourceFolder));

        projectNode = parser.getRootTree();

        function = (IFunctionNode) Search.searchNodes(projectNode, new FunctionNodeCondition(), functionName).get(0);

        //Sinh dữ liệu test theo biên, cần phải sinh CFG theo sub condition thì mới có được các điều kiện
        FunctionConfig functionConfig = new FunctionConfig();
        functionConfig.setSolvingStrategy(ISettingv2.SUPPORT_SOLVING_STRATEGIES[0]);
        ((IFunctionNode) function).setFunctionConfig(functionConfig);
        FunctionNormalizer fnNorm = ((IFunctionNode) function).normalizedAST();
        String normalizedCoverage = fnNorm.getNormalizedSourcecode();
        ((IFunctionNode) function).setAST(fnNorm.getNormalizedAST());
        IFunctionNode clone = (IFunctionNode) function.clone();
        clone.setAST(Utils.getFunctionsinAST(normalizedCoverage.toCharArray()).get(0));
        CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(clone);
        //CFGGenerationforBranchvsStatementCoverage cfgGen = new CFGGenerationforBranchvsStatementCoverage(function);

        cfg = (CFG) cfgGen.generateCFG();
        cfg.setFunctionNode(clone);

        this.cfg.resetVisitedStateOfNodes();
        this.cfg.setIdforAllNodes();
        this.testCases = new ArrayList<TestData>();
        this.maxIterationsforEachLoop = maxloop;
        this.variables = function.getArguments();

        LocalDateTime before1 = LocalDateTime.now();
        List<TestData> boundTestDataList = this.generateTestpathsForBoundaryTestGen();

        testCases.addAll(boundTestDataList);

        LocalDateTime after1 = LocalDateTime.now();

        Duration duration = Duration.between(before1, after1);

        float diff1 = Math.abs((float) duration.toMillis() / 1000);


        //Sinh dữ liệu test theo CFG có trọng số
        CFGGenerationforSubConditionCoverage cfgGen2 = new CFGGenerationforSubConditionCoverage(clone);

        cfg = (CFG) cfgGen2.generateCFG();

        function.normalizedAST();
        FunctionConfig config = new FunctionConfig();
        config.setCharacterBound(new ParameterBound(32, 100));
        config.setIntegerBound(new ParameterBound(0, 100));
        config.setSizeOfArray(20);

        function.setFunctionConfig(config);

        cfg = function.generateCFG();

        cfg.setFunctionNode(function);
        this.cfg.resetVisitedStateOfNodes();
        this.cfg.setIdforAllNodes();
        this.maxIterationsforEachLoop = maxloop;
        this.variables = function.getArguments();

        LocalDateTime before = LocalDateTime.now();
        this.generateTestpaths();

        //create weighted test paths
        graph = new WeightedGraph(before, cfg, this.getPossibleTestpaths(),
                this.function, sourceFolder);

        //Generate test data
        for (int i = 0; i < this.getPossibleTestpaths().size(); i++)
        {
            FullTestpath testpath = (FullTestpath) this.getPossibleTestpaths().get(i);
            FullTestpath tpclone = (FullTestpath) testpath.clone();
            tpclone.setTestCase(this.solveTestpath(function, testpath));

            String testcase = tpclone.getTestCase().replaceAll(";;", ";");

            if (!testcase.equals(IStaticSolutionGeneration.NO_SOLUTION))
            {
                TestData testData = TestData.parseString(testcase);

                if (!testCases.contains(testData) && Utils.isSolutionValid(this.variables, testcase))
                {
                    testCases.add(testData);
                }
            }

            if (!tpclone.getTestCase().equals(IStaticSolutionGeneration.NO_SOLUTION))
            {
                graph.updateWeightForPath(i, 1);
                graph.getFullWeightedTestPaths().get(i).setTestCase(tpclone.getTestCase());
            }
        }

        LocalDateTime after = LocalDateTime.now();

        Duration duration2 = Duration.between(before, after);

        float diff2 = Math.abs((float) duration2.toMillis() / 1000);


        durationTotal = diff1 + diff2;

//        graph.computeBranchCoverNew();
//        graph.computeStatementCovNew();

    }


    public void ExportReport() throws Exception
    {
        FileWriter csvWriter = new FileWriter(AbstractSetting.getValue("TEST_REPORT") + ".html", false);
        String valueString = "<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "\r\n" +
                "<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"hmm_report.css\">\r\n" +
                "\r\n" +
                "</head>\r\n" +
                "\r\n" +
                "<body>\r\n" +
                "    <h2>HYBRID: TEST REPORT</h2>\r\n" +

                "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>PathNumber</th>\r\n" +
                "                    <th style=\"width: 800px\">Test path</th>\r\n" +
                "                    <th>CFG generated test data</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";
        for (WeightedTestPath testPath : graph.getFullWeightedTestPaths())
        {
            valueString += testPath.toStringForCFT4Cpp();
        }
        valueString += "            </tbody></table> </div><br/>";
        valueString += "<div  class=\"table-wrapper\">" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>All test data = CFG generated test data + boundary value test data</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";
        for (TestData testcase : testCases)
        {
            valueString += "<tr><td>" + testcase.toString() + "</td></tr>";
        }
        valueString += "            </tbody>";
        valueString += "            </table>";
        valueString += "</div> <br/>";

        valueString += "<div  class=\"table-wrapper\">" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Coverage information</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";
        String loopString = "";

        valueString += loopString;
        float stateCov = ((float) getFunctionCoverageComputation().getNumberOfVisitedInstructions()) / ((float) getFunctionCoverageComputation().getNumberOfInstructions());
        float branchCov = ((float) getFunctionCoverageComputation().getNumberOfVisitedBranches()) / ((float) getFunctionCoverageComputation().getNumberOfBranches());

        String coverInfo = "";
        try
        {
            coverInfo =
                    "   <tr><td> Statement coverage " + stateCov + "</td></tr>" +
                            "        <tr><td>Branch coverage " + branchCov + "</td></tr>" +
                            "        <tr><td>Number of test data: " + testCases.size() + "; Time: " + durationTotal + "s</td></tr>";
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        valueString += coverInfo;
        valueString += "   </tbody>\r\n" +
                "        </table></div>\r\n" +
                "<div  class=\"table-wrapper\">" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Function raw signature</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>" +
                "<tr><td><pre>" + this.function.getAST().getRawSignature().toString() +

                "</pre></tr></td>" +
                "            </tbody></table></div>" +

                "</body></html>";
        csvWriter.append(valueString);
        csvWriter.close();
    }

    public void generateTestpaths()
    {
        FullTestpaths testpaths_ = new FullTestpaths();

        ICfgNode beginNode = cfg.getBeginNode();
        FullTestpath initialTestpath = new FullTestpath();
        initialTestpath.setFunctionNode(cfg.getFunctionNode());
        try
        {
            traverseCFG(beginNode, initialTestpath, testpaths_);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        for (ITestpathInCFG tp : testpaths_)
        {
            tp.setFunctionNode(cfg.getFunctionNode());
        }

        possibleTestpaths = testpaths_;
    }

    public List<TestData> generateTestpathsForBoundaryTestGen() throws Exception
    {
        List<ICfgNode> list = cfg.getAllNodes();

        Map<String, List<String>> paramValueList = new HashMap<>();

        Map<String, List<String>> paramMidValueList = new HashMap<>();

        for (ICfgNode node : list)
        {
            if (node instanceof ConditionCfgNode && !(node instanceof AbstractConditionLoopCfgNode))
            {
                ConditionCfgNode stm1 = (ConditionCfgNode) node.clone();

                String Content = stm1.getContent();
                Content = Content.replaceAll("<=|>=|<|>|!=", "=");

                String[] varOrVal = Content.split("=");

                if (varOrVal.length != 2)
                {
                    continue;
                }
                String paramName = "";
                String value = "";

                if (Utils.contains(this.variables,varOrVal[0].trim()))
                {
                    paramName = varOrVal[0].trim();
                    value = varOrVal[1].trim();
                }
                else if (Utils.contains(this.variables,varOrVal[1].trim()))
                {
                    paramName = varOrVal[1].trim();
                    value = varOrVal[0].trim();
                }
                else
                {
                    continue;
                }

                double val = 0, val1 = 0, val2 = 0;
                try
                {
                    val = Double.parseDouble(value);
//                    val1 = Double.parseDouble(value) - boundStep;
//                    val2 = Double.parseDouble(value) + boundStep;
                }
                catch (Exception ex)
                {
                    continue;
                }

                if (paramValueList.containsKey(paramName))
                {
                    if (!paramValueList.get(paramName).contains(Double.toString(val)))
                    {
                        paramValueList.get(paramName).add(Double.toString(val));
//                        paramValueList.get(paramName).add(Double.toString(val1));
//                        paramValueList.get(paramName).add(Double.toString(val2));
                    }
                }
                else
                {
                    List<String> valueList = new ArrayList<>();
                    valueList.add(Double.toString(val));
//                    valueList.add(Double.toString(val1));
//                    valueList.add(Double.toString(val2));

                    paramValueList.put(paramName, valueList);
                }
            }
        }

        for(IVariableNode variableNode: this.variables)
        {
            String max = "", min ="";
            switch (variableNode.getRealType())
            {
                case "int":
                    max = Integer.toString(Integer.MAX_VALUE);
                    min = Integer.toString(Integer.MIN_VALUE);
                    break;
                case "long":
                    max = Long.toString(Long.MAX_VALUE);
                    min = Long.toString(Long.MIN_VALUE);
                    break;
                case "float":
                    max = Float.toString(Float.MAX_VALUE);
                    min = Float.toString(Float.MIN_VALUE);
                    break;
                case "double":
                    max = Double.toString(Double.MAX_VALUE);
                    min = Double.toString(Double.MIN_VALUE);
                    break;
                case "short":
                    max = Short.toString(Short.MAX_VALUE);
                    min = Short.toString(Short.MIN_VALUE);
                    break;
            }

            String paramName = variableNode.getName();

            if (paramValueList.containsKey(paramName))
            {
                if (!paramValueList.get(paramName).contains(max))
                {
                    paramValueList.get(paramName).add(max);
                }
                if (!paramValueList.get(paramName).contains(min))
                {
                    paramValueList.get(paramName).add(min);
                }
            }
            else
            {
                List<String> valueList = new ArrayList<>();
                if (!"".equals(max))
                {
                    valueList.add(max);
                }
                if (!"".equals(min))
                {
                    valueList.add(min);
                }

                paramValueList.put(paramName, valueList);
            }

            Utils.sortValueList(paramValueList.get(paramName));
            List<String> midValueList = Utils.createMidValueList(paramValueList.get(paramName), variableNode.getRealType());
            paramMidValueList.put(paramName, midValueList);
        }

        List<TestData> testDataList = new ArrayList<>();

        for (IVariableNode variableNode: this.variables)
        {
            //get list of mid value

            Map<String, List<String>> otherVarMidValueList = new HashMap<>();

            for (IVariableNode midValueNode: this.variables)
            {
                if (!midValueNode.getName().equals(variableNode.getName()))
                {
                    otherVarMidValueList.put(midValueNode.getName(), paramMidValueList.get(midValueNode.getName()));
                }
            }

            List<String> listValue = paramValueList.get(variableNode.getName());

            List<TestData> testDataListforBoundValue = new ArrayList<>();

            for (String value: listValue)
            {
                TestData testData = new TestData();

                testData.add(new Pair<>(variableNode.getName(), value));

                testDataListforBoundValue.add(testData);
            }

            for (Map.Entry entry : otherVarMidValueList.entrySet()) {
                List<TestData> newList = new ArrayList<>();

                newList = CombineTestDataList(testDataListforBoundValue, entry.getKey().toString(), Utils.CloneStringList ((List<String>)entry.getValue()));

                testDataListforBoundValue = newList;
            }

            testDataList.addAll(testDataListforBoundValue);
        }

        return testDataList;
    }

    public List<TestData> CombineTestDataList(List<TestData> testDataList, String paramName, List<String> valueList)
    {
        List<TestData> returnList = new ArrayList<>();

        for (TestData testData: testDataList)
        {
            List<TestData> newList = new ArrayList<>();

            for (String value: valueList)
            {
                TestData testData1 = testData.clone();

                testData1.add(new Pair<>(paramName, value));

                newList.add(testData1);
            }

            returnList.addAll(newList);
        }

        return returnList;
    }


//    public void generateTestpathsForBoundaryTestGen() throws Exception
//    {
//        List<ICfgNode> list = cfg.getAllNodes();
//
//        for (ICfgNode node : list)
//        {
//            if (node instanceof ConditionCfgNode && !(node instanceof AbstractConditionLoopCfgNode))
//            {
//                ICfgNode trueNode = node.getTrueNode();
//
//                FullTestpath tp11 = new FullTestpath();
//
//                if (solvePathWhenGenBoundaryTestData == true)
//                {
//                    Random rand = new Random();
//                    for (float i = -boundStep; i <= boundStep; i += boundStep)
//                    {
//                        ConditionCfgNode stm1 = (ConditionCfgNode) node.clone();
//
//                        stm1.setContent(stm1.getContent().replaceAll("<=|>=|<|>|!=", "=="));
//                        stm1.setAst(ASTUtils.convertToIAST(stm1.getContent() + "+" + i));
//                        tp11.add(stm1);
//
//                        String result = this.getSolution(tp11, true);
//
//                        for (IVariableNode variable : this.variables)
//                        {
//                            if (!result.contains(variable.toString()) && !result.equals(IStaticSolutionGeneration.NO_SOLUTION))
//                            {
//                                result += variable.toString() + "=" + rand.nextInt(100) + ";";
//                            }
//                        }
//                        result = result.replaceAll(";;", ";");
//
//                        if (!result.equals(IStaticSolutionGeneration.NO_SOLUTION))
//                        {
//                            TestData testData = TestData.parseString(result);
//
//                            if (!testCases.contains(testData))
//                            {
//                                testCases.add(testData);
//                            }
//                        }
//                    }
//                }
//                else
//                {
//                    Random rand = new Random();
//
//                    ConditionCfgNode stm1 = (ConditionCfgNode) node.clone();
//
//                    String Content = stm1.getContent();
//                    Content = Content.replaceAll("<=|>=|<|>|!=", "==");
//
//                    stm1.setContent(Content);
//                    stm1.setAst(ASTUtils.convertToIAST(stm1.getContent()));
//                    tp11.add(stm1);
////                        tp11.add(trueNode);
//
//                    String result = this.getSolution(tp11, true);
//
//                    List<IVariableNode> listVarInResult = new ArrayList<>();
//
//                    List<TestData> resultList = new ArrayList<>();
//
//                    if (!result.trim().equals(IStaticSolutionGeneration.NO_SOLUTION))
//                    {
//                        String[] solutionList = result.split(";");
//
//                        //solutionList is in form of: [x=3][y=4]
//
//                        resultList = solutionListAnalysis(solutionList);
//
//                        for (TestData testData : resultList)
//                        {
//                            for (IVariableNode variable : this.variables)
//                            {
//                                if (!testData.isExist(variable.toString()))
//                                {
//                                    testData.add(new Pair<>(variable.toString(),rand.nextInt(100)));
//                                }
//                            }
//
//                            if (!testCases.contains(testData))
//                            {
//                                testCases.add(testData);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    /*
    solutionList is in form of [x=3][y=4]...
    result is a list of TestData with boundary value:
    [<x,3><y,3>][<x,2><y,3>][<x,4><y,3>]...


     */
    private List<TestData> solutionListAnalysis(String[] solutionList)
    {
        List<List<Pair<String, Object>>> list = new ArrayList<>();
        List<TestData> newRet = new ArrayList<>();

        for (String solution : solutionList)
        {
            if (solution.contains("="))
            {
                String param = solution.split("=")[0];
                String value = solution.split("=")[1];

                List<Pair<String, Object>> newList = new ArrayList<>();

                Random rand = new Random();

                for (float i = -boundStep; i <= boundStep; i += boundStep)
                {
                    double val = 0;
                    try
                    {
                        val = Double.parseDouble(value) + i;
                    }
                    catch (Exception ex)
                    {
                        val = rand.nextInt(100);
                    }

                    Pair<String, Object> newResult = new Pair<>(param, val);

                    newList.add(newResult);
                }

                list.add(newList);
            }
        }

        List<Pair<String, Object>> firstItem = list.get(0);
        List<TestData> list0 = new ArrayList<>();

        for (Pair<String, Object> item2 : firstItem)
        {
            TestData testData = new TestData(item2.getKey(), item2.getValue());
            list0.add(testData);
        }

        newRet = CombineList(list0, list.subList(1, list.size()));

        return newRet;
    }

    /*
    Combine list1 with the first item of list2
    Tinh to hop giua test data trong list1 voi test data trong phan tu dau tien cua list2

    Ket qua là mot danh sach TestData, moi testData là một đối tượng chứa danh sách các cặp
    <param, value>

     */
    private List<TestData> CombineList(List<TestData> list1, List<List<Pair<String, Object>>> list2)
    {
        if (list2.size() == 0)
        {
            return list1;
        }
        else
        {
            List<Pair<String, Object>> list20 = list2.get(0);

            List<TestData> ret = new ArrayList<>();

            for (int i = 0; i < list1.size(); i++)
            {
                for (int j = 0; j < list20.size(); j++)
                {
                    //tạo TestData mới để chứa dữ liệu của list1[0] và list20[0]
                    TestData testData = new TestData();
                    testData.add(list1.get(i).getTestData());
                    testData.add(list20.get(j));
                    ret.add(testData);
                }
            }

            List<TestData> newRet = CombineList(ret, list2.subList(1, list2.size()));

            return newRet;
        }
    }

    private void traverseCFG(ICfgNode stm, FullTestpath tp, FullTestpaths testpaths) throws Exception
    {
        tp.add(stm);
        if (stm instanceof EndFlagCfgNode)
        {
            testpaths.add((FullTestpath) tp.clone());
            tp.remove(tp.size() - 1);
        }
        else
        {
            ICfgNode trueNode = stm.getTrueNode();
            ICfgNode falseNode = stm.getFalseNode();

            if (stm instanceof ConditionCfgNode)
            {

                if (stm instanceof AbstractConditionLoopCfgNode)
                {
                    int currentIterations = tp.count(trueNode);
                    if (currentIterations < maxIterationsforEachLoop)
                    {
                        traverseCFG(falseNode, tp, testpaths);
                        traverseCFG(trueNode, tp, testpaths);
                    }
                    else
                    {
                        traverseCFG(falseNode, tp, testpaths);
                    }
                }
                else
                {
                    traverseCFG(falseNode, tp, testpaths);
                    traverseCFG(trueNode, tp, testpaths);
                }
            }
            else
            {
                traverseCFG(trueNode, tp, testpaths);
            }

            tp.remove(tp.size() - 1);
        }
    }

//    private void traverseCFGForBoundaryTestGen(ICfgNode stm, FullTestpath tp, FullTestpaths testpaths) throws Exception
//    {
//        tp.add(stm);
//        if (stm instanceof EndFlagCfgNode)
//        {
//            testpaths.add((FullTestpath) tp.clone());
//            tp.remove(tp.size() - 1);
//        }
//        else
//        {
//            ICfgNode trueNode = stm.getTrueNode();
//            ICfgNode falseNode = stm.getFalseNode();
//
//            if (stm instanceof ConditionCfgNode)
//            {
//
//                if (stm instanceof AbstractConditionLoopCfgNode)
//                {
//                    int currentIterations = tp.count(trueNode);
//                    if (currentIterations < maxIterationsforEachLoop)
//                    {
//                        traverseCFGForBoundaryTestGen(falseNode, tp, testpaths);
//                        traverseCFGForBoundaryTestGen(trueNode, tp, testpaths);
//                    }
//                    else
//                    {
//                        traverseCFGForBoundaryTestGen(falseNode, tp, testpaths);
//                    }
//                }
//                else
//                {
//                    if (solvePathWhenGenBoundaryTestData == true)
//                    {
//                        Random rand = new Random();
//                        for (float i = -boundStep; i <= boundStep; i += boundStep)
//                        {
//                            FullTestpath tp11 = (FullTestpath) tp.clone();
//                            ConditionCfgNode stm1 = (ConditionCfgNode) stm.clone();
//
//                            tp11.remove(tp.lastIndexOf(stm));
//                            stm1.setContent(stm1.getContent().replaceAll("<=|>=|<|>|!=", "=="));
//                            stm1.setAst(ASTUtils.convertToIAST(stm1.getContent() + "+" + i));
//                            tp11.add(stm1);
//                            tp11.add(trueNode);
//
//                            String result = this.getSolution(tp11, true);
//                            for (IVariableNode variable : this.variables)
//                            {
//                                if (!result.contains(variable.toString()) && !result.equals(IStaticSolutionGeneration.NO_SOLUTION))
//                                {
//                                    result += variable.toString() + "=" + rand.nextInt(100) + ";";
//                                }
//                            }
//                            result = result.replaceAll(";;", ";");
//                            if (!testCases.contains(result) && !result.equals(IStaticSolutionGeneration.NO_SOLUTION))
//                            {
//                                testCases.add(result);
//                            }
//                        }
//                    }
//                    else
//                    {
//                        Random rand = new Random();
//
//                        for (float i = -boundStep; i <= boundStep; i += boundStep)
//                        {
//
//                            FullTestpath tp11 = new FullTestpath();
//
//                            ConditionCfgNode stm1 = (ConditionCfgNode) stm.clone();
//
//                            String Content = stm1.getContent();
//                            Content = Content.replaceAll("<=|>=|<|>|!=", "==");
//
//                            stm1.setContent(stm1.getContent().replaceAll("<=|>=|<|>|!=", "=="));
//                            stm1.setAst(ASTUtils.convertToIAST(stm1.getContent() + "+" + i));
//                            tp11.add(stm1);
//                            tp11.add(trueNode);
//
//                            String result = this.getSolution(tp11, true);
//                            for (IVariableNode variable : this.variables)
//                            {
//                                if (!result.contains(variable.toString()) && !result.equals(IStaticSolutionGeneration.NO_SOLUTION))
//                                {
//                                    result += variable.toString() + "=" + rand.nextInt(100) + ";";
//                                }
//                            }
//                            result = result.replaceAll(";;", ";");
//                            if (!testCases.contains(result) && !result.equals(IStaticSolutionGeneration.NO_SOLUTION))
//                            {
//                                testCases.add(result);
//                            }
//                        }
////                        if (Content.contains("=="))
////                        {
////
//////                            result += Content;
//////                            result = result.replace("==", "=") + ";";
//////
//////                            String param = Content.split("==")[0].toLowerCase();
//////                            String value = Content.split("==")[1].toLowerCase();
//////
//////                            if ("false".equals(value.toLowerCase()) || "true".equals(value.toLowerCase()))
//////                            {
//////                                //do nothing
//////                            }
//////                            else
//////                            {
//////                                double val = 0;
//////                                try
//////                                {
//////                                    val = Double.parseDouble(value);
//////                                }
//////                                catch (Exception ex)
//////                                {
//////                                    val = rand.nextInt(100);
//////
//////                                    result = param + " = " + rand.nextInt(100);
//////
//////                                    if (contains(this.variables,value))
//////                                    {
//////                                        result3 = value + " = " + rand.nextInt(100);
//////                                    }
//////                                }
//////
//////                                result1 += param + " = " + (val - boundStep) + ";";
//////
//////                                result2 += param + " = " + (val + boundStep) + ";";
//////
//////                                for (IVariableNode variable : this.variables)
//////                                {
//////                                    if (!param.equals(variable.toString()))
//////                                    {
//////                                        result += variable.toString() + "=" + rand.nextInt(100) + ";";
//////                                        result1 += variable.toString() + "=" + rand.nextInt(100) + ";";
//////                                        result2 += variable.toString() + "=" + rand.nextInt(100) + ";";
//////                                    }
//////
//////                                    if (contains(this.variables,value) && !value.equals(variable.toString()))
//////                                    {
//////                                        result3 += variable.toString() + "=" + rand.nextInt(100) + ";";
//////                                    }
//////                                }
//////
//////                                if (!testCases.contains(result))
//////                                {
//////                                    testCases.add(result);
//////                                }
//////                                if (!testCases.contains(result1))
//////                                {
//////                                    testCases.add(result1);
//////                                }
//////                                if (!testCases.contains(result2))
//////                                {
//////                                    testCases.add(result2);
//////                                }
//////
//////                                if (contains(this.variables,value) && !testCases.contains(result3))
//////                                {
//////                                    testCases.add(result3);
//////                                }
//////                            }
////                        }
////                        else
////                        {
////                            //condition is in the form of if (abc) ==> content = abc, in C or C++, this is equivalence
////                            //to abc == 0 or abc != 0
////
//////                            Content = Content.toLowerCase();
//////
//////                            if (contains(this.variables,Content))
//////                            {
//////                                result = Content + " = 0;";
//////                                result1 = Content + " = " + -boundStep + ";";
//////                                result2 = Content + " = " + boundStep + ";";
//////
//////                                for (IVariableNode variable : this.variables)
//////                                {
//////                                    if (!Content.equals(variable.toString().toLowerCase()))
//////                                    {
//////                                        result += variable.toString() + "=" + rand.nextInt(100) + ";";
//////                                        result1 += variable.toString() + "=" + rand.nextInt(100) + ";";
//////                                        result2 += variable.toString() + "=" + rand.nextInt(100) + ";";
//////                                    }
//////                                }
//////
//////                                if (!testCases.contains(result))
//////                                {
//////                                    testCases.add(result);
//////                                }
//////                                if (!testCases.contains(result1))
//////                                {
//////                                    testCases.add(result1);
//////                                }
//////                                if (!testCases.contains(result2))
//////                                {
//////                                    testCases.add(result2);
//////                                }
//////                            }
//////                            else
//////                            {
//////                                result = Content + " = " + rand.nextInt(100) + ";";
//////
//////                                for (IVariableNode variable : this.variables)
//////                                {
//////                                    if (!Content.toLowerCase().equals(variable.toString().toLowerCase()))
//////                                    {
//////                                        result += variable.toString() + "=" + rand.nextInt(100) + ";";
//////                                    }
//////                                }
//////
//////                                if (!testCases.contains(result))
//////                                {
//////                                    testCases.add(result);
//////                                }
//////                            }
////                        }
////                        }
//                    }
//
//                    traverseCFGForBoundaryTestGen(falseNode, tp, testpaths);
//                    traverseCFGForBoundaryTestGen(trueNode, tp, testpaths);
//                }
//            }
//            else
//            {
//                traverseCFGForBoundaryTestGen(trueNode, tp, testpaths);
//            }
//
//            tp.remove(tp.size() - 1);
//        }
//    }

    private boolean contains(List<IVariableNode> variables, String param)
    {
        for (IVariableNode variable : this.variables)
        {
            if (!param.toLowerCase().equals(variable.toString().toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean haveSolution(FullTestpath tp, boolean finalConditionType) throws Exception
    {
        IPartialTestpath tp1 = createPartialTestpath(tp, finalConditionType);

        String solution = solveTestpath(cfg.getFunctionNode(), tp1);

        if (!solution.equals(IStaticSolutionGeneration.NO_SOLUTION))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    protected String getSolution(FullTestpath tp, boolean finalConditionType) throws Exception
    {
        IPartialTestpath tp1 = createPartialTestpath(tp, finalConditionType);

        String solution = solveTestpath(cfg.getFunctionNode(), tp1);
        return solution;
    }

    protected IPartialTestpath createPartialTestpath(FullTestpath fullTp, boolean finalConditionType)
    {
        IPartialTestpath partialTp = new PartialTestpath();
        for (ICfgNode node : fullTp.getAllCfgNodes())
        {
            partialTp.getAllCfgNodes().add(node);
        }

        partialTp.setFinalConditionType(finalConditionType);
        return partialTp;
    }

    protected String solveTestpath(IFunctionNode function, ITestpathInCFG testpath) throws Exception
    {
        /*
         * Get the passing variables of the given function
         */
        Parameter paramaters = new Parameter();
        for (IVariableNode n : function.getArguments())
        {
            paramaters.add(n);
        }
        for (IVariableNode n : function.getReducedExternalVariables())
        {
            paramaters.add(n);
        }

        /*
         * Get the corresponding path constraints of the test path
         */
        ISymbolicExecution se = new SymbolicExecution(testpath, paramaters, function);

        // fast checking
        RelatedConstraintsChecker relatedConstraintsChecker = new RelatedConstraintsChecker(
                se.getConstraints().getNormalConstraints(), function);
        boolean isRelated = relatedConstraintsChecker.check();
        //
        if (isRelated)
        {
            if (se.getConstraints().getNormalConstraints().size()
                    + se.getConstraints().getNullorNotNullConstraints().size() > 0)
            {
                /*
                 * Solve the path constraints
                 */
                ISmtLibGeneration smtLibGen = new SmtLibGeneration(function.getPassingVariables(),
                        se.getConstraints().getNormalConstraints());
                smtLibGen.generate();

                Utils.writeContentToFile(smtLibGen.getSmtLibContent(), CONSTRAINTS_FILE);

                RunZ3OnCMD z3 = new RunZ3OnCMD(Z3, CONSTRAINTS_FILE);
                z3.execute();
                logger.debug("solving done");
                String staticSolution = new Z3SolutionParser().getSolution(z3.getSolution());

                if (staticSolution.equals(IStaticSolutionGeneration.NO_SOLUTION))
                {
                    return IStaticSolutionGeneration.NO_SOLUTION;
                }
                else
                {
                    if (se.getConstraints().getNullorNotNullConstraints().size() > 0)
                    {
                        for (PathConstraint nullConstraint : se.getConstraints().getNullorNotNullConstraints())
                        {
                            staticSolution += nullConstraint + SpecialCharacter.END_OF_STATEMENT;
                        }
                    }

                    if (se.getConstraints().getNullorNotNullConstraints().size() > 0)
                    {
                        return staticSolution + "; " + se.getConstraints().getNullorNotNullConstraints();
                    }
                    else
                    {
                        return staticSolution + ";";
                    }
                }
            }
            else
            {
                return IStaticSolutionGeneration.NO_SOLUTION;
            }
        }
        else
        {
            return IStaticSolutionGeneration.EVERY_SOLUTION;
        }
    }


    public ICFG getCfg()
    {
        return cfg;
    }


    public void setCfg(ICFG cfg)
    {
        this.cfg = cfg;
    }


    public int getMaxIterationsforEachLoop()
    {
        return maxIterationsforEachLoop;
    }


    public void setMaxIterationsforEachLoop(int maxIterationsforEachLoop)
    {
        this.maxIterationsforEachLoop = maxIterationsforEachLoop;
    }


    public FullTestpaths getPossibleTestpaths()
    {
        return possibleTestpaths;
    }

    public List<TestData> getTestCases()
    {
        return this.testCases;
    }

    public FunctionCoverageComputation getFunctionCoverageComputation()
    {
        return functionCoverageComputation;
    }

    public void setFunctionCoverageComputation(FunctionCoverageComputation functionCoverageComputation)
    {
        this.functionCoverageComputation = functionCoverageComputation;
    }
}
