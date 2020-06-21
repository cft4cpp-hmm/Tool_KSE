package Khamd;

import java.util.List;

import org.eclipse.cdt.internal.core.dom.parser.c.GNUCASTGotoStatement;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTCompositeTypeSpecifier;
import org.fife.ui.rsyntaxtextarea.modes.GroovyTokenMaker;

import config.Paths;
import testdata.object.TestpathString_Marker;
import testdatagen.coverage.CFGUpdater_Mark;

public class Coverage {
//	public static ProbFunctionExection functionExection;
//	public static Graph graph;
//	
//	public static void main(String[] args) {
//		Main Prob = new Main();
//		try {
//			graph = Prob.createGraph(Paths.TSDV_R1_2, "whileTest(int)", 1);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void Cover(List<String> solutions, Graph graph,ProbFunctionExection functionExection) {
		TestpathString_Marker testpath ;
		CFGUpdater_Mark updater;
		System.out.println(solutions);
		testpath = new TestpathString_Marker();
		for(String solution: solutions) {
			
			testpath.setEncodedTestpath(functionExection.getEncodedPath(solution).getEncodedTestpath());
			updater = new CFGUpdater_Mark(testpath,graph.getCfg());
			
			updater.updateVisitedNodes();
//			System.out.println(graph.getCfg().computeBranchCoverage());
		}
//		System.out.println(graph.getCfg().);
		
	}
	

}
