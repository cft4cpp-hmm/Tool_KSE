package HybridAutoTestGen;

import HMM.HMMGraph;
import HMM.Node;
import cfg.CFGGenerationforSubConditionCoverage;
import cfg.ICFG;
import cfg.object.ICfgNode;
import cfg.testpath.IStaticSolutionGeneration;
import cfg.testpath.ITestpathInCFG;
import cfg.testpath.PossibleTestpathGeneration;
import config.*;
import coverage.FunctionCoverageComputation;
import normalizer.FunctionNormalizer;
import parser.projectparser.ProjectParser;
import testdatagen.se.*;
import testdatagen.se.solver.RunZ3OnCMD;
import testdatagen.se.solver.SmtLibGeneration;
import testdatagen.se.solver.Z3SolutionParser;
import tree.object.FunctionNode;
import tree.object.IFunctionNode;
import tree.object.INode;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;

public class WeightedCFGTestGEn
{
    //	public static String pathToZ3 ="..\\Bai10\\local\\z3\\bin\\z3.exe";
//	public static String pathToMingw32 = "..\\Bai10\\bin\\mingw32-make.exe";
//	public static String pathToGCC = "..\\Bai10\\bin\\gcc.exe";
//	public static String pathToGPlus = "..\\Bai10\\bin\\g++.exe";
//	public static String pathToConstraint = "D:\\Bai10\\myConstraint.smt2";
    public static String pathToZ3 = AbstractSetting.getValue(Settingv2.SOLVER_Z3_PATH);
    public static String pathToMingw32 = AbstractSetting.getValue(Settingv2.GNU_MAKE_PATH);
    public static String pathToGCC = AbstractSetting.getValue(Settingv2.GNU_GCC_PATH);
    public static String pathToGPlus = AbstractSetting.getValue(Settingv2.GNU_GPlusPlus_PATH);
    public static String pathToConstraint = "myConstraint.smt2";

    public static SmtLibGeneration smt = new SmtLibGeneration();
    public static final int version = 1;    // 1 for weighted graph 2 for probability graph
    public static final int coverage = 0;   // 0 for C1,C2. 1 for C3
    public static final int max = 20;        // The K's biggest value
    public static final int min = 5;        // The K's smallest value
    public static final int maxLoopInGenerateTestcaseForLoopFunction = 4;
    private String functionName;
    private int interations;
    private String sourceFolder;
    private FunctionCoverageComputation functionCoverageComputation;

    private List<ProbTestPath> probTestPathList;
    private IFunctionNode functionNode;
    private float testDataGenerationTime = 0;
    private LocalDateTime startDateTime;

    //	public Main(String pathToZ3)
    public WeightedCFGTestGEn()
    {

    }

    public WeightedCFGTestGEn(String funcName, int iterations, String _sourceFolder)
    {
        this.functionName = funcName;
        this.interations = iterations;
        sourceFolder = _sourceFolder;
    }


    public void run() throws Exception
    {

        int times = 1;
        int j = 0;
        List<Float> timesList = new ArrayList<Float>();
        while (j < times)
        {
            WeightedCFGTestGEn Prob = new WeightedCFGTestGEn();
            int epoch = 1;
            List<String> listSolution = new ArrayList<String>();
            try
            {
                ProjectParser parser = new ProjectParser(new File(sourceFolder));

                functionNode = (IFunctionNode) Search.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), functionName).get(0);

                startDateTime = LocalDateTime.now();
                int randomTestPath = 1;
                int maxIterations = this.interations;
                String func_name = this.functionName;
                Graph graph = Prob.createGraph(sourceFolder, func_name, maxIterations, coverage);
                graph.setEpoches(epoch);
                graph.addConstraint();
                boolean isLoopFunction = graph.hasLoop();
                HMMGraph hmmGraph = new HMMGraph(version);
                Node node;
                Node nextNode;
                String solution;
                int pathNumber = graph.getNewPath();

                for (ProbTestPath testPath : graph.getFullProbTestPaths())
                {
                    for (Edge edge : testPath.getEdge())
                    {
                        node = new Node(edge.getNode());
                        nextNode = new Node(edge.getNextNode());
                        hmmGraph.addNode(node, nextNode, (float) edge.getWeight());
                    }
                }


                do
                {
                    solution = Prob.getSolutionInRandomPath(graph, pathNumber);
                    solution = solution.replace("(", "");
                    solution = solution.replace(")", "");
                    if (solution == "")
                    {
                        pathNumber = graph.getNewPath();
                        continue;
                    }

                    listSolution.add(solution);
                    List<String> list = new ArrayList<String>();
                    for (ICfgNode node1 : graph.getFullPossibleFullTestpaths().get(pathNumber).getAllCfgNodes())
                    {
                        list.add(node1.toString());
                    }

                    ProbTestPath trackedPath = graph.getFullProbTestPaths().get(pathNumber);
                    graph.updateGraph(pathNumber, 1, hmmGraph, version);
                    trackedPath.setTestCase(solution);
                    pathNumber = graph.getNewPath();

                }
                while (pathNumber != -1);

                LocalDateTime afterGenForC = LocalDateTime.now();
//                graph.computeBranchCoverNew();
//                graph.computeStatementCovNew();
//                Graph graphForLoop;

                graph.createProbabilityForTestPath(hmmGraph);

                Duration duration = Duration.between(startDateTime, afterGenForC);

                testDataGenerationTime = ((float) duration.toMillis()/1000);

                hmmGraph.recomputeProbability();

                probTestPathList = graph.getFullProbTestPaths();

                //graph.toHtml(afterGenForC, coverage, (float) duration.toMillis() / 1000, "WCFT4Cpp");
//                timesList.add(graph.getDuration());

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            j++;
        }
//		float sum = 0;
//		for(float time: timesList) {
//			sum+=time;
//			System.out.println(time);
//		}
    }

    public void ExportReport(String toolName) throws IOException
    {
        FileWriter csvWriter = new FileWriter(AbstractSetting.getValue("TEST_REPORT") + ".html", false);
        String valueString = "<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "\r\n" +
                "<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"hmm_report.css\">\r\n" +
                "\r\n" +
                "</head>\r\n" +
                "\r\n" +
                "<body>\r\n";

        if (toolName == "WCFT4Cpp")
        {
            valueString +=
                    "    <h2>WCFT: TEST REPORT</h2>\r\n";
        }
        else if (toolName == "CFT4Cpp")
        {
            valueString +=
                    "    <h2>WCFT: TEST REPORT</h2>\r\n";
        }
        else
        {
            valueString +=
                    "    <h2>Concolic: TEST REPORT</h2>\r\n";

        }

        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>PathNumber</th>\r\n" +
                "                    <th style=\"width: 800px\">Test path</th>\r\n" +
                "                    <th>Test Data</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";
        for (ProbTestPath testPath : this.probTestPathList)
        {
            if (toolName == "WCFT4Cpp")
            {
                valueString += testPath.toString();
            }
            else
            {
                valueString += testPath.toStringForCFT4Cpp();
            }
        }
        valueString += "</tbody></table></div>";

        String loopString = "";

        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Coverage information</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";

        float stateCov = ((float) functionCoverageComputation.getNumberOfVisitedInstructions()) / ((float) functionCoverageComputation.getNumberOfInstructions());
        float branchCov = ((float) functionCoverageComputation.getNumberOfVisitedBranches()) / ((float) functionCoverageComputation.getNumberOfBranches());


        String coverInfo = "";
        try
        {
            coverInfo =
                    "        <tr><td>stateCov: " + stateCov + "</td></tr>\r\n" +
                            "        <tr><td>branchCov: " + branchCov + "</td></tr>\r\n" +
                            "        <tr><td>Time: " + testDataGenerationTime + " s</td></tr>\r\n";
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        valueString += coverInfo;
        valueString += "   </tbody>\r\n" +
                "        </table></div>\r\n";


        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Function raw signature</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>" +
                "<tr><td><pre>" + this.functionNode.getAST().getRawSignature().toString() +

                "</pre></td></tr></tbody></table></div>" +

                "</body></html>";
        csvWriter.append(valueString);
        csvWriter.close();

    }

    public List<ProbTestPath> getTestPaths()
    {
        return this.probTestPathList;
    }

    public List<TestData> getTestDataList()
    {
        List<TestData> testDataList = new ArrayList<>();
        for (ProbTestPath testPath : probTestPathList)
        {
            String solution = testPath.getTestCase();
            if (!"".equals(solution) && Utils.isSolutionValid(this.functionNode.getPassingVariables(), solution))
            {
                TestData testData = TestData.parseString(testPath.getTestCase());
                testDataList.add(testData);
            }
        }

        return testDataList;
    }

    public void setFunctionCoverageComputation(FunctionCoverageComputation coverageComputation)
    {
        this.functionCoverageComputation = coverageComputation;
    }

    public FunctionCoverageComputation getFunctionCoverageComputation(FunctionCoverageComputation coverageComputation)
    {
        return this.functionCoverageComputation;
    }

    public static void main(String[] args)
    {
//		System.out.println("current: "+ Paths.CURRENT_PROJECT.ORIGINAL_PROJECT_PATH);
        int times = 1;
        int j = 0;
        List<Float> timesList = new ArrayList<Float>();
        while (j < times)
        {
            WeightedCFGTestGEn Prob = new WeightedCFGTestGEn();
            int epoch = 1;
            List<String> listSolution = new ArrayList<String>();
            try
            {
                int randomTestPath = 1;
                int maxIterations = 0;
                String func_name = null;
                String fileName = "testFunction.txt";
                File testedFile = new File(fileName);

                BufferedReader br = new BufferedReader(new FileReader(testedFile));
                while ((func_name = br.readLine()) != null)
                {
                    break;
                }

                Graph graph = null;
                try
                {
                    graph = Prob.createGraph(Paths.TSDV_R1_2, func_name, maxIterations, coverage);
                }
                catch (StackOverflowError e)
                {
                    System.out.println("Cannnot generate CFG");
                    // TODO: handle exception
                }

                graph.setEpoches(epoch);
                graph.addConstraint();
                boolean isLoopFunction = graph.hasLoop();
                HMMGraph hmmGraph = new HMMGraph(version);
                Node node;
                Node nextNode;
                String solution;
//			ProbFunctionExection functionExection = new ProbFunctionExection(graph,pathToZ3,pathToMingw32,pathToGCC,pathToGPlus);
                int pathNumber = graph.getNewPath();

                for (ProbTestPath testPath : graph.getFullProbTestPaths())
                {
                    for (Edge edge : testPath.getEdge())
                    {
                        node = new Node(edge.getNode());
                        nextNode = new Node(edge.getNextNode());
                        hmmGraph.addNode(node, nextNode, (float) edge.getWeight());
                    }
                }


                do
                {
                    solution = Prob.getSolutionInRandomPath(graph, pathNumber);
                    solution = solution.replace("(", "");
                    solution = solution.replace(")", "");
                    if (solution == "")
                    {
                        pathNumber = graph.getNewPath();
                        continue;
                    }

                    listSolution.add(solution);
                    List<String> list = new ArrayList<String>();
                    for (ICfgNode node1 : graph.getFullPossibleFullTestpaths().get(pathNumber).getAllCfgNodes())
                    {
                        list.add(node1.toString());
                    }

                    ProbTestPath trackedPath = graph.getFullProbTestPaths().get(pathNumber);
                    graph.updateGraph(pathNumber, 1, hmmGraph, version);
                    trackedPath.setTestCase(solution);
                    pathNumber = graph.getNewPath();

                }
                while (pathNumber != -1);

                LocalDateTime afterGenForC = LocalDateTime.now();
//			System.out.println(nowDateTime.getSecond());
                graph.computeBranchCover();
                graph.computeStatementCov();
                Graph graphForLoop;

//			if(coverage==0) {
//				graphForLoop = graph;
//			}
//			else {
//				graphForLoop = Prob.createGraph(Paths.TSDV_R1_2, func_name ,maxIterations,0);
//			}
//			
//			
//			Random random = new Random();
//			String loopSolution=IStaticSolutionGeneration.NO_SOLUTION;
//			PossibleTestpathGenerationForLoop tpForLoop = null;
//			int k =2 ;
//			int loopCover = 2;
//			boolean resultForCondition = false;
//			List<AbstractConditionLoopCfgNode> listCondition = new ArrayList<AbstractConditionLoopCfgNode>();
//			boolean hasSolution = false;
//			int temp=-1;
//			AbstractConditionLoopCfgNode tempConditionLoopCfgNode = null;
//			AbstractConditionLoopCfgNode tempCondition = null;
//			boolean usedNumbericCon = false;
//			boolean breakLoop = false;
//			System.out.println("done");
//			while(loopCover!=4&&!breakLoop) {
//				if(usedNumbericCon) {
//					break;
//				}
//				int count = 0;
//				AbstractConditionLoopCfgNode condition = graphForLoop.getLastConditionNode(listCondition);
//				
//				
//				if(!isNumbericCondition(condition).equals(IStaticSolutionGeneration.NO_SOLUTION) && !isNumbericCondition(condition).equals("-1")) {
//					temp=Integer.parseInt(isNumbericCondition(condition));
//					tempCondition = condition;
//					listCondition.add(condition);
//					continue;
//					
//				}
//				
//				if(condition==null&&temp==-1) {
//					break;
//				}
//				else if(condition==null){
//					condition = tempCondition;
//					usedNumbericCon=true;
//				}
//				
//				listCondition.add(condition);
//				if(condition instanceof ConditionDoCfgNode) {
//					PossibleTestpathGenerationForLoop.isDoWhileLoop = true;
//				}
//				
//				if(graph.get_2LoopSolution()==null&&usedNumbericCon==false) {
//					k=2;
//				}
//				else if (graph.getLoopSolution()==null) {
//					if(usedNumbericCon==true) {
//						k = temp;
//					}
//					else k = random.nextInt((max - min)+1)+min;
//				}
//				else break;
//				
//			do{
//				
//				try {
//					tpForLoop = new PossibleTestpathGenerationForLoop(graphForLoop.getCfg(), condition);
//					tpForLoop.setMaximumIterationsForOtherLoops(k);
//					tpForLoop.setIterationForUnboundedTestingLoop(k);
//					tpForLoop.setAddTheEndTestingCondition(true);
//					tpForLoop.generateTestpaths();
//				}catch (OutOfMemoryError e) {
//					// TODO: handle exception
//					breakLoop=true;
//					break;
//				}
//				
//				int i = 0;
//				loopSolution = solveTestpath(graphForLoop.getCfg(), tpForLoop.getPossibleTestpaths().get(i));
//					if(!loopSolution.contentEquals(IStaticSolutionGeneration.NO_SOLUTION)) {
//						i = (tpForLoop.getPossibleTestpaths().size())/2;
//						loopSolution = solveTestpath(graphForLoop.getCfg(), tpForLoop.getPossibleTestpaths().get(i));
//						if(!loopSolution.contentEquals(IStaticSolutionGeneration.NO_SOLUTION) && tpForLoop.getPossibleTestpaths().size()>1) {
//							i = (tpForLoop.getPossibleTestpaths().size())-1;
//							loopSolution = solveTestpath(graphForLoop.getCfg(), tpForLoop.getPossibleTestpaths().get(i));
//							
//						}
//					}
//					
//					if(k==2) {
//						graph.setPathFor2Loop(tpForLoop.getPossibleTestpaths().get(i));
//					}
//					else {
//						graph.setPathForKLoop(tpForLoop.getPossibleTestpaths().get(i));
//						graph.setK(k);
//					}
//					
//					if(!loopSolution.contentEquals(IStaticSolutionGeneration.NO_SOLUTION)) {
//						if(k==2) {
//							graph.set_2LoopSolution(loopSolution);
//							graph.setRealFor2loop(tpForLoop.getRealMaximumIterationForTestingLoop());
//							graph.setPathFor2Loop(tpForLoop.getPossibleTestpaths().get(i));
//							k = random.nextInt((max - min)+1)+min;
//							loopSolution = IStaticSolutionGeneration.NO_SOLUTION;
//							loopCover +=1;
//						}
//						
//						else {
//							
//							hasSolution = true;
//							graph.setK(k);
//							graph.setLoopSolution(loopSolution);
//							graph.setRealLoppiterations(tpForLoop.getRealMaximumIterationForTestingLoop());
//							graph.setPathForKLoop(tpForLoop.getPossibleTestpaths().get(i));
//							loopCover+=1;
//							break;
//							
//						}
//					
//					}
//
//				count ++;
//				
//				if(k==2) {
//					k = random.nextInt((max - min)+1)+min;
//				}
//				else k++;
//			}while(!hasSolution && count < maxLoopInGenerateTestcaseForLoopFunction);
//			
//		}

//			graph.setK(k);

                System.out.println("Finish Generating!");
                System.out.println("Computing Coverage");


                graph.createProbabilityForTestPath(hmmGraph);

                Duration duration = Duration.between(afterGenForC, LocalDateTime.now());

                float diff = (float) duration.toSeconds();
//			functionExection.deleteClone();

                hmmGraph.recomputeProbability();
//			graph.setLoopCover(loopCover);
                graph.toHtml(afterGenForC, coverage, (float) duration.toMillis() / 1000, "WCFT4Cpp");
                timesList.add(graph.getDuration());

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            System.out.println("Finish Generating!");


            j++;
        }
        float sum = 0;
        for (float time : timesList)
        {
            sum += time;
            System.out.println(time);
        }
        System.out.println("Average: " + (float) sum / timesList.size());
    }

    public String getSolutionInRandomPath(Graph graph, int pathNumber) throws Exception
    {
        IFunctionNode function = (IFunctionNode) graph.getFunctionNode();

        List<PathConstraint> constraints = new ArrayList<PathConstraint>();
        for (PathConstraint c : (PathConstraints) graph.getFullProbTestPaths().get(pathNumber).getConstraints())
        {
            constraints.add(c);
        }

        smt.setTestcases(function.getArguments());
        smt.setConstraints(constraints);
        smt.generate();

        BufferedWriter writer = new BufferedWriter(new FileWriter(pathToConstraint, false));
        writer.write(smt.getSmtLibContent());
        writer.close();
        RunZ3OnCMD run = new RunZ3OnCMD(pathToZ3, pathToConstraint);
        run.execute();
        return new Z3SolutionParser().getSolution(run.getSolution());
    }

    public static String solveTestpath(ICFG cfg, ITestpathInCFG testpath) throws Exception
    {
        IFunctionNode function = (IFunctionNode) cfg.getFunctionNode();
//		List<PathConstraint> constraints =new ArrayList<PathConstraint>();
        Parameter paramaters = new Parameter();
        for (INode n : ((FunctionNode) function).getArguments())
        {
            paramaters.add(n);
        }

        for (INode n : ((FunctionNode) function).getReducedExternalVariables())
        {
            paramaters.add(n);
        }

        ISymbolicExecution se = new SymbolicExecution(testpath, paramaters, function);
//		for(IFullTestpath fullTestpath:this.getFullPossibleFullTestpaths()) {
//			
//			int path = this.getFullPossibleFullTestpaths().indexOf(fullTestpath);
//			this.getFullProbTestPaths().get(path).setConstraints(se.getConstraints());
//		}
        List<PathConstraint> constraints = (List<PathConstraint>) se.getConstraints();


        smt.setTestcases(function.getArguments());
        smt.setConstraints(constraints);
        smt.generate();

        BufferedWriter writer = new BufferedWriter(new FileWriter(pathToConstraint, false));
        writer.write(smt.getSmtLibContent());
        writer.close();
        RunZ3OnCMD run = new RunZ3OnCMD(pathToZ3, pathToConstraint);
        run.execute();
        return new Z3SolutionParser().getSolution(run.getSolution());


    }

    public Graph createGraph(String pathtoFile, String functionName, int maxIteration, int coverage) throws Exception
    {
        ICFG cfg;
        ProjectParser parser = new ProjectParser(new File(pathtoFile));
        INode function;
        LocalDateTime createdTime = LocalDateTime.now();
        if (coverage == 0)
        {

            function = (IFunctionNode) Search.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), functionName).get(0);
            FunctionNormalizer fnNorm = ((IFunctionNode) function).normalizedAST();
//			ICFG cfg;
            FunctionConfig config = new FunctionConfig();
            config.setCharacterBound(new ParameterBound(32, 100));
            config.setIntegerBound(new ParameterBound(0, 100));
            config.setSizeOfArray(20);
            ((IFunctionNode) function).setFunctionConfig(config);
            createdTime = LocalDateTime.now();
            cfg = ((IFunctionNode) function).generateCFG();
            cfg.generateAllPossibleTestpaths(maxIteration);
//			System.out.println(dfg);

        }
        else
        {
            function = Search
                    .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), functionName)
                    .get(0);
            FunctionConfig functionConfig = new FunctionConfig();
            functionConfig.setSolvingStrategy(ISettingv2.SUPPORT_SOLVING_STRATEGIES[0]);
            ((IFunctionNode) function).setFunctionConfig(functionConfig);
            FunctionNormalizer fnNorm = ((IFunctionNode) function).normalizedAST();
            String normalizedCoverage = fnNorm.getNormalizedSourcecode();
            ((IFunctionNode) function).setAST(fnNorm.getNormalizedAST());
            IFunctionNode clone = (IFunctionNode) function.clone();
            clone.setAST(Utils.getFunctionsinAST(normalizedCoverage.toCharArray()).get(0));
            CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(clone);
            createdTime = LocalDateTime.now();
            cfg = cfgGen.generateCFG();
//			cfg.setIdforAllNodes();
            cfg.setFunctionNode(clone);


        }

        functionNode = (FunctionNode) function;

        PossibleTestpathGeneration tpGen = new PossibleTestpathGeneration(cfg, maxIteration);
        tpGen.generateTestpaths();

        Graph graph = new Graph(createdTime, cfg, tpGen.getPossibleTestpaths(), (IFunctionNode) function, pathtoFile, version);

        functionNode = (FunctionNode) function;

        return graph;

//		functionConfig.setCharacterBound(new ParameterBound(30, 120));
//		functionConfig.setIntegerBound(new ParameterBound(10, 200));
//		functionConfig.setSizeOfArray(5);
////		functionConfig.setMaximumInterationsForEachLoop(1);

//		


//		INode function = Search
//				.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), functionName)
//				.get(0);
//		FunctionConfig functionConfig = new FunctionConfig();
//		functionConfig.setSolvingStrategy(ISettingv2.SUPPORT_SOLVING_STRATEGIES[0]);
//		((IFunctionNode ) function).setFunctionConfig(functionConfig);
//		FunctionNormalizer fnNorm = ((IFunctionNode) function).normalizedAST();
//		String normalizedCoverage = fnNorm.getNormalizedSourcecode();
//		((IFunctionNode ) function).setAST(fnNorm.getNormalizedAST());
//		IFunctionNode clone = (IFunctionNode) function.clone();
//		clone.setAST(Utils.getFunctionsinAST(normalizedCoverage.toCharArray()).get(0));
//		CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(clone);
//		ICFG cfg = cfgGen.generateCFG();
//		cfg.setIdforAllNodes();
//		cfg.setFunctionNode(clone);


    }

    //	public static void genForKLoop(Graph graph, AbstractConditionLoopCfgNode condition, int k) {
//		String loopSolution = "";
//		PossibleTestpathGenerationForLoop tpForLoop = null;
//		tpForLoop = new PossibleTestpathGenerationForLoop(graph.getCfg(), condition);
//		tpForLoop.setMaximumIterationsForOtherLoops(k);
//		tpForLoop.setIterationForUnboundedTestingLoop(k);
//		tpForLoop.setAddTheEndTestingCondition(true);
//		tpForLoop.generateTestpaths();
//		int i = 0;
//		loopSolution = solveTestpath(graph.getCfg(), tpForLoop.getPossibleTestpaths().get(i));
//			if(!loopSolution.contentEquals(IStaticSolutionGeneration.NO_SOLUTION)) {
//				i = (tpForLoop.getPossibleTestpaths().size())/2;
//				loopSolution = solveTestpath(graph.getCfg(), tpForLoop.getPossibleTestpaths().get(i));
//				if(!loopSolution.contentEquals(IStaticSolutionGeneration.NO_SOLUTION) && tpForLoop.getPossibleTestpaths().size()>1) {
//					i = (tpForLoop.getPossibleTestpaths().size())-1;
//					loopSolution = solveTestpath(graph.getCfg(), tpForLoop.getPossibleTestpaths().get(i));
//				}
//			}
//			
//			if(k==2) {
//				graph.setPathFor2Loop(tpForLoop.getPossibleTestpaths().get(i));
//			}
//			else {
//				graph.setPathForKLoop(tpForLoop.getPossibleTestpaths().get(i));
//				graph.setK(k);
//			}
//			if(!loopSolution.contentEquals(IStaticSolutionGeneration.NO_SOLUTION)) {
//				if(k==2) {
//					graph.set_2LoopSolution(loopSolution);
//					graph.setRealFor2loop(tpForLoop.getRealMaximumIterationForTestingLoop());
//					graph.setPathFor2Loop(tpForLoop.getPossibleTestpaths().get(i));
//					k = random.nextInt((max - min)+1)+min;
//					loopSolution = IStaticSolutionGeneration.NO_SOLUTION;
//					loopCover +=1;
//				}
//				
//				else {
//					
//					hasSolution = true;
//					graph.setK(k);
//					graph.setLoopSolution(loopSolution);
//					graph.setRealLoppiterations(tpForLoop.getRealMaximumIterationForTestingLoop());
//					graph.setPathForKLoop(tpForLoop.getPossibleTestpaths().get(i));
//					loopCover+=1;
//					break;
//					
//				}
//			
//			}
//
//		count ++;
//		if(k==2) {
//			k = random.nextInt((max - min)+1)+min;
//		}
//		else k++;
//	}
//	
    public static String isNumbericCondition(ICfgNode node)
    {
        if (node == null)
        {
            return "-1";
        }

        String nodeString = String.valueOf(node.toString()).replaceAll("(|)", "").replace("||", "&&");
        String[] listCondition = nodeString.split("&&");
        for (String element : listCondition)
        {
            String leftSide = node.toString().split(">=|<=|==|!=|<|>")[0];
            String rightSide = node.toString().split(">=|<=|==|!=|<|>")[1];
            String number = "";
            if (leftSide.matches("[-+]?[0-9]*\\.?[0-9]+"))
            {
                number = leftSide;
                return number;
            }
            else if (rightSide.matches("[-+]?[0-9]*\\.?[0-9]+"))
            {
                number = rightSide;
                return number;
            }
        }

        return IStaticSolutionGeneration.NO_SOLUTION;
    }

    public List<ProbTestPath> getProbTestPathList()
    {
        return probTestPathList;
    }

    public void setProbTestPathList(List<ProbTestPath> probTestPathList)
    {
        this.probTestPathList = probTestPathList;
    }
}