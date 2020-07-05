package GUI;

import Common.DSEConstants;
import HMM.HMMGraph;
import HMM.Node;
import HybridAutoTestGen.CFT4CPP;
import HybridAutoTestGen.Edge;
import HybridAutoTestGen.Graph;
import HybridAutoTestGen.ProbTestPath;
import cfg.ICFG;
import cfg.object.AbstractConditionLoopCfgNode;
import cfg.object.ConditionCfgNode;
import cfg.object.EndFlagCfgNode;
import cfg.object.ICfgNode;
import cfg.testpath.*;
import config.FunctionConfig;
import config.ISettingv2;
import config.ParameterBound;
import config.Settingv2;
import constraints.checker.RelatedConstraintsChecker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
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
import tree.object.INode;
import tree.object.IProjectNode;
import tree.object.IVariableNode;
import utils.SpecialCharacter;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HybridTestGen extends Component
{
    public static final String CONSTRAINTS_FILE = Settingv2.getValue(ISettingv2.SMT_LIB_FILE_PATH);
    public static final String Z3 = Settingv2.getValue(ISettingv2.SOLVER_Z3_PATH);
    final static Logger logger = Logger.getLogger(CFT4CPP.class);
    public ComboBox cboSelectedFunction;
    public Button btnGetFunctionList;
    /**
     * Represent control flow graph
     */
    private ICFG cfg;
    private int maxIterationsforEachLoop = ITestpathGeneration.DEFAULT_MAX_ITERATIONS_FOR_EACH_LOOP;
    private FullTestpaths possibleTestpaths = new FullTestpaths();
    public List<String> testCases;
    public IFunctionNode function;
    public IProjectNode projectNode;

    @FXML
    public Button btnBrowseInput;
    @FXML
    public TextField txtMaxLoop;
    @FXML
    public Button btnGenerateTestData;
    @FXML
    public TextField txtSourceFolder;

    @FXML
    protected void btnGetFunctionList_Clicked(ActionEvent event) throws Exception
    {
        ProjectParser parser = new ProjectParser(new File(txtSourceFolder.getText()));

        projectNode = parser.getRootTree();

        List<INode> functionList = Search.getAllNodes(projectNode, new FunctionNodeCondition());

        if (functionList.size() > 0)
        {
            for (INode function : functionList)
            {
                cboSelectedFunction.getItems().add(new FunctionComboItem(function.getName(), function.getName()));
            }

            cboSelectedFunction.getSelectionModel().select(0);
        }
    }

    @FXML
    protected void btnViewReport_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnViewReport_Clicked started");
//        CFT4CPP tpGen = new CFT4CPP(null, 1, "sum(int,int)");
//        tpGen.run();
    }

    @FXML
    protected void btnBrowseInput_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnBrowseInput_Clicked started");
        JFileChooser _fileChooser = new JFileChooser();
        _fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\Sample_for_R1_2";

        _fileChooser.setSelectedFile(new File(path));
        if (_fileChooser.showDialog(this, "Choose folder") == JFileChooser.APPROVE_OPTION)
        {
            String selectedPath = _fileChooser.getSelectedFile().getAbsolutePath();

            txtSourceFolder.setText(selectedPath);
        }
    }

    public void initialize()
    {
        // initialization code here...
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\Sample_for_R1_2";

        txtSourceFolder.setText(path);
    }

    @FXML
    protected void btnGenerateTestData_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnTest_Clicked started");
        int maxloop = 1;
        try
        {
            maxloop = Integer.parseInt(txtMaxLoop.getText());
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, "Maxloop is invalid", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR);
        }
        Object item = cboSelectedFunction.getSelectionModel();
        String value = cboSelectedFunction.getValue().toString();

        generateTestData(maxloop, value, txtSourceFolder.getText());
    }

    public void generateTestData(int maxloop, String functionName, String sourceFolder) throws Exception
    {
        function = (IFunctionNode) Search.searchNodes(projectNode, new FunctionNodeCondition(), functionName).get(0);
        FunctionNormalizer fnNorm = ((IFunctionNode) function).normalizedAST();

        int ij = 0;
        if (ij == 0)
        {
            return;
        }

        FunctionConfig funcConfig = new FunctionConfig();
        funcConfig.setCharacterBound(new ParameterBound(32, 100));
        funcConfig.setIntegerBound(new ParameterBound(0, 100));
        funcConfig.setSizeOfArray(20);
        ((IFunctionNode) function).setFunctionConfig(funcConfig);

        ICFG cfg = ((IFunctionNode) function).generateCFG();

//			CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(function);

//			CFGGenerationforBranchvsStatementCoverage cfgGen = new CFGGenerationforBranchvsStatementCoverage(function);
        int maxIterations = 0;
//			ICFG cfg = cfgGen.generateCFG();
        cfg.setFunctionNode(function);
        cfg.setIdforAllNodes();
        cfg.resetVisitedStateOfNodes();
        cfg.generateAllPossibleTestpaths(maxIterations);
        this.cfg = cfg;
        this.function = function;
        this.cfg.resetVisitedStateOfNodes();
        this.cfg.setIdforAllNodes();
        this.testCases = new ArrayList<String>();
        this.maxIterationsforEachLoop = maxloop;

        LocalDateTime before = LocalDateTime.now();
        this.generateTestpaths(this.function);
//		LocalDateTime after = LocalDateTime.now();
//		Duration duration = Duration.between(before,after);


        Graph graph = new Graph(before, cfg, this.getPossibleTestpaths(), this.function, sourceFolder, 1);
        HMMGraph hmmGraph = new HMMGraph(1);
        Node node;
        Node nextNode;
        String solution;

        for (ProbTestPath testPath : graph.getFullProbTestPaths())
        {
            for (Edge edge : testPath.getEdge())
            {
                node = new Node(edge.getNode());
                nextNode = new Node(edge.getNextNode());
                hmmGraph.addNode(node, nextNode, (float) edge.getWeight());
            }
        }

        for (int i = 0; i < this.getPossibleTestpaths().size(); i++)
        {
            FullTestpath testpath = (FullTestpath) this.getPossibleTestpaths().get(i);
            if (!testpath.getTestCase().equals(IStaticSolutionGeneration.NO_SOLUTION))
            {
                graph.updateGraph(i, 1, hmmGraph, 1);
                graph.getFullProbTestPaths().get(i).setTestCase(testpath.getTestCase());
            }
        }
        graph.toHtml(LocalDateTime.now(), 0, 1, "CFT4Cpp");

    }

    public void generateTestpaths(IFunctionNode function)
    {
        // Date startTime = Calendar.getInstance().getTime();
        FullTestpaths testpaths_ = new FullTestpaths();

        ICfgNode beginNode = cfg.getBeginNode();
        FullTestpath initialTestpath = new FullTestpath();
        initialTestpath.setFunctionNode(cfg.getFunctionNode());
        try
        {
            traverseCFG(beginNode, initialTestpath, testpaths_, function);
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

        // Calculate the running time
        // Date end = Calendar.getInstance().getTime();
        // totalRunningTime = end.getTime() - startTime.getTime();
        // logger.debug("Total running time: " + totalRunningTime + " ms");
        // logger.debug("Solving time: " + solvingTime + " ms");
        // logger.debug("Number of solving calls: " + numberOfSolvingCalls + "
        // ms");
        // logger.debug(
        // "Number of solving calls that does not have solution: " +
        // numberOfSolvingCallsThatNoSolution + " ms");
    }

    private void traverseCFG(ICfgNode stm, FullTestpath tp, FullTestpaths testpaths, IFunctionNode function) throws Exception
    {

        tp.add(stm);
        FullTestpath tp1 = (FullTestpath) tp.clone();
        FullTestpath tp2 = (FullTestpath) tp.clone();
//		System.out.println(this.haveSolution(tp, finalConditionType)+tp.getFullPath());
//		System.out.println(stm.toString());
        if (stm instanceof EndFlagCfgNode)
        {
            FullTestpath tpclone = (FullTestpath) tp.clone();
            tpclone.setTestCase(this.solveTestpath(function, tp));
            testpaths.add(tpclone);
            testCases.add(tpclone.getTestCase());

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
                        tp1.add(falseNode);
                        if (this.haveSolution(tp1, false))
                        {
                            traverseCFG(falseNode, tp, testpaths, function);
                        }
                        tp2.add(trueNode);
                        if (this.haveSolution(tp2, true))
                        {
                            traverseCFG(trueNode, tp, testpaths, function);
                        }

//						traverseCFG(trueNode, tp, testpaths,function);
                    }
                    else
                    {
                        tp1.add(falseNode);
                        if (this.haveSolution(tp1, false))
                        {
                            traverseCFG(falseNode, tp, testpaths, function);
                        }

                    }
                }
                else
                {
                    tp1.add(falseNode);

                    if (this.haveSolution(tp1, false))
                    {
                        traverseCFG(falseNode, tp, testpaths, function);
                    }

                    tp2.add(trueNode);
                    if (this.haveSolution(tp2, true))
                    {
                        traverseCFG(trueNode, tp, testpaths, function);
                    }
                }
            }
            else
            {
                traverseCFG(trueNode, tp, testpaths, function);
            }

            tp.remove(tp.size() - 1);
        }
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

    public List<String> getTestCases()
    {
        return this.testCases;
    }
}

class FunctionComboItem
{
    private String key;
    private String value;

    public FunctionComboItem(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return key;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }
}