package Khamd;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.lang.System.Logger;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.print.attribute.HashAttributeSet;
import javax.swing.plaf.basic.BasicScrollPaneUI.VSBChangeListener;

import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.xmlbeans.impl.regex.REUtil;
import org.eclipse.cdt.core.parser.util.IUnaryPredicate;
import org.eclipse.cdt.core.settings.model.COutputEntry;

import com.alee.log.Log;
import com.ibm.icu.text.UFieldPosition;

import HMM.HMMGraph;
import HMM.Node;
import cfg.ICFG;
import cfg.object.AbstractConditionLoopCfgNode;
import cfg.object.BranchInCFG;
import cfg.object.ICfgNode;
import cfg.testpath.FullTestpath;
import cfg.testpath.FullTestpaths;
import cfg.testpath.IFullTestpath;
import cfg.testpath.ITestpathInCFG;
import config.AbstractSetting;
import testdata.object.TestpathString_Marker;
import testdatagen.coverage.CFGUpdater_Mark;
import testdatagen.se.ISymbolicExecution;
import testdatagen.se.Parameter;
import testdatagen.se.PathConstraint;
import testdatagen.se.PathConstraints;
import testdatagen.se.SymbolicExecution;
import tree.object.FunctionNode;
import tree.object.IFunctionNode;
import tree.object.INode;

public class Graph {
	
	private List<IFullTestpath> fullPossibleTestpaths;
	private List<ProbTestPath> fullProbTestPaths;
	private IFunctionNode functionNode;
	private String pathToFile;
	private ICFG cfg;
	private LocalDateTime createdDate;
	private int epoches;
	private String loopSolution;
	private int k;
	private int RealLoppiterations;
	private float statementCover;
	private float branchCover;
	private int realFor2loop;
	private String _2LoopSolution;
	private int loopCover;
	private ITestpathInCFG pathFor2Loop;
	private ITestpathInCFG pathForKLoop;
	private float duration;
	public int getRealLoppiterations() {
		return RealLoppiterations;
	}

	public void setRealLoppiterations(int realLoppiterations) {
		RealLoppiterations = realLoppiterations;
	}

	public Graph(LocalDateTime createdDate,ICFG cfg,List<IFullTestpath> fullPossibleIFullTestpaths, IFunctionNode functionNode, String pathtoFile, int version) {
		
		List<IFullTestpath> fullTestpaths = fullPossibleIFullTestpaths;
		this.fullPossibleTestpaths=fullPossibleIFullTestpaths;
		this.functionNode=functionNode;
		this.pathToFile=pathtoFile;
		this.createdDate = createdDate;
//		System.out.println(this.createdDate.getSecond());
		this.fullProbTestPaths = new ArrayList<ProbTestPath>();
		this.cfg=cfg;
		this.epoches = 1;
		this._2LoopSolution= null;
		this.loopSolution = null;
		
		for(int pathNumber = 0; pathNumber< this.fullPossibleTestpaths.size(); pathNumber++) {
			List<ICfgNode> fullCfgNodes = (ArrayList<ICfgNode>)this.fullPossibleTestpaths.get(pathNumber).getAllCfgNodes();
			fullCfgNodes= new ArrayList<ICfgNode>(fullCfgNodes);
			ProbTestPath myTestPath = new ProbTestPath(pathNumber);
			fullCfgNodes.remove(0);
			fullCfgNodes.remove(fullCfgNodes.size()-1);
			
			for(int i=0;i<fullCfgNodes.size();i++) {
				if(fullCfgNodes.get(i).toString().contains("{")||fullCfgNodes.get(i).toString().contains("}")||fullCfgNodes.get(i).toString().indexOf("[")==0) {
					fullCfgNodes.remove(i);
					i=i-1;
				}
			}
			for(int i=0;i<fullCfgNodes.size()-1;i++) {
				Edge edge = new Edge(fullCfgNodes.get(i), fullCfgNodes.get(i+1), pathNumber,version);
				myTestPath.addEdge(edge);
			}
			this.fullProbTestPaths.add(myTestPath); 
		}
	}
	
	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public int getIntersection2Path(ProbTestPath path1, ProbTestPath path2) {
		int numOfNode=0;
		for(ICfgNode node1_i : path1.getFullCfgNode()) {
			for(ICfgNode node2_i : path2.getFullCfgNode()) {
				if(node1_i==node2_i) {
					numOfNode++;
				}
			}
		}
		return numOfNode-1;
	}
	
	public void updateGraph(int pathNumber, int weight, HMMGraph hmmGraph, int version) {
		ProbTestPath testPath = this.fullProbTestPaths.get(pathNumber);
		testPath.setGenerated(true);
		for(ICfgNode cfgNode : testPath.getFullCfgNode()) {
			cfgNode.setVisit(true);
		}
		for(Edge edge: testPath.getEdge()) {
			edge.setIsVisited();
//			System.out.println(edge.isVisited());
			for(ProbTestPath testPath1 : this.getFullProbTestPaths()) {
				for(Edge edge1 : testPath1.getEdge()) {
					if(testPath1!=testPath&&edge.getNode()==edge1.getNode()&&edge.getNextNode()==edge1.getNextNode()) {
						edge1.setIsVisited();
					}
				}
			}
			
		}
		
		for(Edge edge : testPath.getEdge()) {
			Node node = hmmGraph.getNode(edge.getNode());
			node.updateProbability(edge.getNextNode(),version);
		}
		

		for(ProbTestPath testPath2: this.getFullProbTestPaths()) {
			for(Edge edge: testPath2.getEdge()) {
				Node node = hmmGraph.getNode(edge.getNode());
				edge.setWeight(node.getProbability(edge.getNextNode()));
			}
		}	
		
	}
	
	public float computeBranchCover() throws Exception {
		Set<Edge> setEdges = new HashSet<Edge>();
		Set<Edge> visitedEdges = new HashSet<Edge>();
		for(ProbTestPath testPath: this.getFullProbTestPaths()) {
			for(Edge edge: testPath.getEdge()) {
				if(edge.isVisited()) {
					visitedEdges.add(edge);
				}
				setEdges.add(edge);
			}
		}
		
//		return (float)visitedEdges.size()/setEdges.size();
		if(visitedEdges.size()!=0) {
			this.branchCover = (float)visitedEdges.size()/setEdges.size();
		}
		return this.branchCover;
	}
	public float computeBranchCover(List<String> testDatas) throws Exception {
//		List<String> testDatas = new ArrayList<String>();
		for(ProbTestPath path: this.getFullProbTestPaths()) {
			testDatas.add(path.getTestCase());
		}
//		CFGUpdater_Mark updater = 
		ProbFunctionExection probFunction = new ProbFunctionExection(this, Main.pathToZ3, Main.pathToMingw32, Main.pathToGCC, Main.pathToGPlus);
		TestpathString_Marker testpath;
		for(String testData: testDatas) {
			testpath = probFunction.getEncodedPath(testData.replace(";;", ";"));
			CFGUpdater_Mark updater = new CFGUpdater_Mark(testpath, this.getCfg());
		}
		
		probFunction.deleteClone();
		return this.getCfg().computeBranchCoverage();
	}
	
	public float computeStatementCov() {
		int visitedNode = 0;
		for(ICfgNode cfgNode : this.cfg.getAllNodes()) {
			if(cfgNode.isVisited()) {
				visitedNode++;
			}
		}
//		System.out.println(visitedNode);
//		System.out.println(this.cfg.getAllNodes().size());
		
		if(visitedNode!=0){
			this.statementCover = (float)visitedNode/this.getAllCFGNode();
			
		}
		return this.statementCover;
		
	}
	public int getAllCFGNode() {
		Set<ICfgNode> nodes = new HashSet();
		for(ProbTestPath testPath : this.getFullProbTestPaths()) {
			for(ICfgNode node: testPath.getFullCfgNode()) {
				nodes.add(node);
			}
		}
		return nodes.size();
	}
	public void computeProbabilityForAllPath(int version) {
		Node node;
		Node nextNode;
		HMMGraph hmmGraph = new HMMGraph(version);
		for(ProbTestPath probTestPath : this.getFullProbTestPaths()) {
			for(Edge edge : probTestPath.getEdge()) {
				node = new Node(edge.getNode());
				nextNode = new Node(edge.getNextNode());
			}
		}
	}
	
	public void setVisitedPath(int pathNumber) {
		this.fullProbTestPaths.get(pathNumber).setGenerated(true);
	}
	
	public int countVisitedNode() {
		int count =0;
		for(ProbTestPath testPath: this.fullProbTestPaths) {
			if(testPath.isGenerated()) {
				count++;
			}
		}
		return count;
	}
	
	public float getCoverage() {
		return (float)this.countVisitedNode()/(this.getFullProbTestPaths().size());
	}
	
	public int getNewPath() {
		int weight=-1;
		int index =-1;
		for(ProbTestPath testPath: this.fullProbTestPaths) {
			if(testPath.isGenerated()==false && testPath.getVisitedNumber() < this.epoches) {
				if(testPath.getWeight()>=weight) {
					weight=testPath.getWeight();
					index = this.fullProbTestPaths.indexOf(testPath);
					break;
				}
			}
		}
		if(index!=-1) {
			this.fullProbTestPaths.get(index).setVisitedNumber(1);
		}
		
		return index;
	}
	
	public void addConstraint() throws Exception {
		Parameter paramaters = new Parameter();
		for (INode n : ((FunctionNode) this.functionNode).getArguments())
			paramaters.add(n);
		for (INode n : ((FunctionNode) this.functionNode).getReducedExternalVariables())
			paramaters.add(n);
		for(IFullTestpath fullTestpath:this.getFullPossibleFullTestpaths()) {
			ISymbolicExecution se = new SymbolicExecution(fullTestpath, paramaters, this.functionNode);
			int path = this.getFullPossibleFullTestpaths().indexOf(fullTestpath);
			this.getFullProbTestPaths().get(path).setConstraints(se.getConstraints());
		}

	}
	
	public float getDuration() {
		return this.duration;
	}
	public void toHtml(LocalDateTime diff1, int coverage, float timeForLoop, String toolName) throws Exception {
		
		Duration duration = Duration.between(this.createdDate,diff1);
		
		
		float diff = Math.abs((float)duration.toMillis()/1000);
		this.duration = diff;
//		diff-=diff1;
		FileWriter csvWriter = new FileWriter(AbstractSetting.getValue("TEST_REPORT")+".html",false);
		String valueString = "<!DOCTYPE html>\r\n" + 
				"<html>\r\n" + 
				"\r\n" + 
				"<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"hmm_report.css\">\r\n" + 
				"\r\n" + 
				"</head>\r\n" + 
				"\r\n" + 
				"<body>\r\n" + 
				"    <h2>TEST REPORT</h2>\r\n" + 
				
				"    <div class=\"table-wrapper\">\r\n" + 
				"        <table class=\"fl-table\">\r\n" + 
				"            <thead>\r\n" + 
				"                <tr>\r\n" + 
				"                    <th>PathNumber</th>\r\n" + 
				"                    <th style=\"width: 800px\">Test path</th>\r\n" + 
				"                    <th>Test Data</th>\r\n" + 
				"                </tr>\r\n" + 
				"            </thead>\r\n" + 
				"            <tbody>";
		for(ProbTestPath testPath: this.getFullProbTestPaths()) {
			if(toolName=="WCFT4Cpp") {
				valueString+=testPath.toString();
			}
			else valueString+=testPath.toStringForCFT4Cpp();
			}
			
		String loopString = "";
		
	
		valueString+=loopString;
		float stateCov = this.getCfg().computeStatementCoverage();
		float branchCov = this.getCfg().computeBranchCoverage();
		
//		if(this.getCoverage()==1 && (stateCov==0 || branchCov==0)) {
//			stateCov =1;
//			branchCov = 1;
//		}
//		String coverInfoC1vsC2 = "  <div>Statement Cover: "+this.computeStatementCov()+"</div>\r\n"+
//		        "        <div>Branch Cover "+this.computeBranchCover()+"</div></div>\r\n";
		
		String coverInfo = "";
		try {
			coverInfo = 
					        "        <div>C2 Coverage "+this.computeStatementCov()+"</div></div>\r\n";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		valueString+="   <tbody>\r\n" + 
				"        </table></div>\r\n" + 
				"<div class=\"conlusion\">\n"+ 
		        "<pre>"+this.functionNode.getAST().getRawSignature().toString()+
		        
		        "</pre>"+
		       
		        "        <div>Time For "+(coverage==0?"C2 :":"C3: ") + diff+"s</div>\r\n" + 
		        
		        coverInfo+
//		        "        <div>Cover: "+this.computeBranchCover()+"</div>\r\n"+
//		        "        <div> "+1+"</div></div>\r\n"+
//				"    </div>\r\n" + 
				"</body></html>";
		csvWriter.append(valueString);
		csvWriter.close();
	}
	public void createProbabilityForTestPath(HMMGraph hmmGraph) {
		List<Float> proList = new ArrayList<Float>();
		for(ProbTestPath testPath: this.getFullProbTestPaths()) {
			proList = new ArrayList<Float>();
			for(int i=0;i<testPath.getFullCfgNode().size()-1;i++) {
				Node node = hmmGraph.getNode(testPath.getFullCfgNode().get(i));
				ICfgNode nextCfgNode = testPath.getFullCfgNode().get(i+1);
				proList.add(node.getProbability(nextCfgNode));
			}
			testPath.setProList(proList);
		}
	}
	public boolean hasLoop() {
		for(ICfgNode cfgNode: this.cfg.getAllNodes()) {
			if(cfgNode instanceof AbstractConditionLoopCfgNode) {
				return true;
				
			}
		}
		return false;
	}
	public AbstractConditionLoopCfgNode getLastConditionNode(List<AbstractConditionLoopCfgNode> listCondition) {
		ICfgNode node = null;
//		int count = 0;
		for(ICfgNode cfgNode: this.cfg.getAllNodes()) {
			if(cfgNode instanceof AbstractConditionLoopCfgNode && !listCondition.contains(cfgNode)) {
				node = cfgNode;
//				count++;
			}
		}
//		System.out.println("count: "+count);
		return (AbstractConditionLoopCfgNode)node;
	}
	
	public ICFG getCfg() {
		return cfg;
	}
	public void setCfg(ICFG cfg) {
		this.cfg = cfg;
	}
	public String getPathToFile() {
		return pathToFile;
	}
	public void setPathToFile(String pathToFile) {
		this.pathToFile = pathToFile;
	}
	public IFunctionNode getFunctionNode() {
		return functionNode;
	}
	public void setFunctionNode(IFunctionNode functionNode) {
		this.functionNode = functionNode;
	}
	public List<IFullTestpath> getFullPossibleFullTestpaths(){
		return this.fullPossibleTestpaths;
	}
	
	public List<ProbTestPath> getFullProbTestPaths(){
		return this.fullProbTestPaths;
	}

	public int getEpoches() {
		return epoches;
	}

	public void setEpoches(int epoches) {
		this.epoches = epoches;
	}

	public String getLoopSolution() {
		return loopSolution;
	}

	public void setLoopSolution(String loopSolution) {
//		System.out.println(loopSolution);
		this.loopSolution = loopSolution;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}
	public String get_2LoopSolution() {
		return _2LoopSolution;
	}

	public void set_2LoopSolution(String _2LoopSolution) {
		this._2LoopSolution = _2LoopSolution;
	}

	public int getRealFor2loop() {
		return realFor2loop;
	}

	public void setRealFor2loop(int realFor2loop) {
		this.realFor2loop = realFor2loop;
	}

	public int getLoopCover() {
		return loopCover;
	}

	public void setLoopCover(int loopCover) {
		this.loopCover = loopCover;
	}

	public ITestpathInCFG getPathFor2Loop() {
		return pathFor2Loop;
	}

	public void setPathFor2Loop(ITestpathInCFG pathFor2Loop) {
		this.pathFor2Loop = pathFor2Loop;
	}

	public ITestpathInCFG getPathForKLoop() {
		return pathForKLoop;
	}

	public void setPathForKLoop(ITestpathInCFG pathForKLoop) {
		this.pathForKLoop = pathForKLoop;
	}
	
	
}
