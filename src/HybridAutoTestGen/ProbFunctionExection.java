package HybridAutoTestGen;

import java.io.File;
import java.io.IOException;

import cfg.ICFG;
import config.Paths;
import parser.projectparser.ProjectParser;
import testdata.object.TestpathString_Marker;
import testdatagen.FunctionExecution;
import tree.object.IFunctionNode;
import tree.object.INode;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

public class ProbFunctionExection extends FunctionExecution{
	
	private Graph graph;
	private File clone;
	public ProbFunctionExection(Graph graph, String pathToZ3, String pathToMingw32, String pathToGCC, String pathToGPlus) throws Exception {
		super(pathToZ3,pathToMingw32,pathToGCC,pathToGPlus);
		this.graph = graph;
		String testedProject = graph.getPathToFile();
		clone = Utils.copy(testedProject);
		Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH = clone.getAbsolutePath();
		ProjectParser parser = new ProjectParser(clone);
//		FunctionConfig config = new FunctionConfig();
//		config.setCharacterBound(new ParameterBound(32, 100));
//		config.setIntegerBound(new ParameterBound(0, 100));
		INode function = Search
				.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), graph.getFunctionNode().getName())
				.get(0);
//		FunctionNode testedFunction = (FunctionNode) Search
//				.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), graph.getFunctionNode().getName()).get(0);
		this.setTestedFunction((IFunctionNode) function);
//		this.setPreparedInput(preparedInput);
		this.setClonedProject(clone.getCanonicalPath());
		this.setCFG(this.graph.getCfg());
	}
	public ProbFunctionExection(String PathToFile, String functionName, ICFG cfg) throws IOException {
		super(WeightedCFGTestGEn.pathToZ3, WeightedCFGTestGEn.pathToMingw32, WeightedCFGTestGEn.pathToGCC, WeightedCFGTestGEn.pathToGPlus);
		String testedProject = PathToFile;
		clone = Utils.copy(testedProject);
		Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH = clone.getAbsolutePath();
		ProjectParser parser = new ProjectParser(clone);
//		FunctionConfig config = new FunctionConfig();
//		config.setCharacterBound(new ParameterBound(32, 100));
//		config.setIntegerBound(new ParameterBound(0, 100));
		INode function = Search
				.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), functionName)
				.get(0);
//		FunctionNode testedFunction = (FunctionNode) Search
//				.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), graph.getFunctionNode().getName()).get(0);
		this.setTestedFunction((IFunctionNode) function);
//		this.setPreparedInput(preparedInput);
		this.setClonedProject(clone.getCanonicalPath());
		this.setCFG(cfg);
	}
	
	public TestpathString_Marker getEncodedPath(String preparedInput) {
//		String testedProject = graph.getPathToFile();
		try {

			this.setPreparedInput(preparedInput);
			TestpathString_Marker testpathString_Marker= this.analyze(this.getTestedFunction(), this.getPreparedInput());
			
			return testpathString_Marker;
			
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
		
	}
	public void deleteClone() {
		Utils.deleteFileOrFolder(clone);
	}
	
}
