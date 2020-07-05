package HybridAutoTestGen;

import cfg.object.ICfgNode;
import cfg.testpath.IStaticSolutionGeneration;
import com.ibm.icu.text.DecimalFormat;
import testdatagen.se.IPathConstraints;
import testdatagen.se.PathConstraint;
import testdatagen.se.PathConstraints;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TestPath
{

    private List<Edge> edges;
    private int pathNumber;
    private boolean isGenerated;
    private int visitedNumber ;
    private String realString;
    private IPathConstraints constraints;
    private String testCase;
    private String toString ;
    private static DecimalFormat df2 = new DecimalFormat("#.####");

    public TestPath(int pathNumber) {
        this.pathNumber=pathNumber;
        this.edges = new ArrayList<Edge>();
        this.visitedNumber=0;
        this.isGenerated=false;
        this.testCase= IStaticSolutionGeneration.NO_SOLUTION;
        this.realString="";
        toString="";
    }

    public List<ICfgNode> getFullCfgNode(){
        List<ICfgNode> fullCfgNode = new ArrayList<ICfgNode>();
        fullCfgNode.add(this.edges.get(0).getNode());
        for(int i=0;i<this.edges.size();i++){
            fullCfgNode.add(this.edges.get(i).getNextNode());
        }
        return fullCfgNode;
    }
    public boolean compare(List<ICfgNode> cfgNodes) {
        cfgNodes  = new ArrayList<ICfgNode>(cfgNodes);
        for(int i=0;i<cfgNodes.size();i++) {
            if(cfgNodes.get(i).toString().contains("{")||cfgNodes.get(i).toString().contains("}")||cfgNodes.get(i).toString().indexOf("[")==0){
                cfgNodes.remove(i);
                i-=1;
            }
        }

        for(ICfgNode node: this.getFullCfgNode()) {
            if(cfgNodes.indexOf(node)!=this.getFullCfgNode().indexOf(node)) {
                return false;
            }
        }
        if(this.getFullCfgNode().size()!=cfgNodes.size()) {
            return false;
        }


        return true;
    }

    public Edge searchEdge(ICfgNode node, ICfgNode nextNode) {
        for(Edge edge : this.edges) {
            if(edge.getNode()==node && edge.getNextNode()==nextNode) {
                return edge;
            }
        }
        return null;
    }


    public int getWeight() {
        int prob=1;
        for(Edge edge: this.edges) {
            prob*=edge.getWeight();
        }
        return prob;
    }
    public IPathConstraints getConstraints() {
        return this.constraints;
    }
    public void setConstraints(IPathConstraints iPathConstraints) {
        this.constraints=iPathConstraints;
    }

    public boolean isGenerated() {
        return isGenerated;
    }
    public void setGenerated(boolean isGenerated) {
        this.isGenerated = isGenerated;
    }
    public String getTestCase() {
        return testCase;
    }
    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }
    public int getVisitedNumber() {
        return visitedNumber;
    }
    public void setVisitedNumber(int visitedNumber) {
        this.visitedNumber += visitedNumber;
    }
    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }
    public List<Edge> getEdge(){
        return this.edges;
    }
    public String getRealString() {
        return realString;
    }
    public void setRealString(String realString) {
        this.realString = realString;
    }
    public void setToString(String toString) {
        this.toString=toString;
    }

    public String toString() {
        if(!this.toString.equals("")) {
            this.toString = this.toString.substring(4,this.toString.length());
            this.toString=this.toString.replace("{", "");
            this.toString=this.toString.replace("}", "");
            this.toString=this.toString.replace("  ", "");

            String[] listStrings = this.toString.split("=>|=> =>");

            String newString ="<tr><td>"+pathNumber+"</td><td>";

            List<String> newLiStrings = new ArrayList<String>();

            for(int i=0;i<listStrings.length;i++) {
                if(!listStrings[i].equals(" ")&&!listStrings[i].equals("  ")&&!listStrings[i].contains("[")) {
                    newLiStrings.add(listStrings[i]);
                }
            }
            newString+=newLiStrings.get(newLiStrings.size()-1);
            newString+="</td>"+"<td>"+this.getTestCase()+"</td></tr>";
            return newString;
        }

        List<PathConstraint> constraints = new ArrayList<PathConstraint>();
        Pattern pattern = Pattern.compile("=|<|>");
        try {
            for(PathConstraint c: (PathConstraints) this.getConstraints()) {
                if(c.getCfgNode().toString().matches(".*\\b(|<|>)\\b.*") || c.getCfgNode().toString().contains("==")){
                    constraints.add(c);
                }

            }
        }catch (Exception e) {
            // TODO: handle exception
        }

        this.toString = "<tr><td>"+pathNumber+"</td><td>";
        int temp = 0;
        for(int i=0;i<this.getFullCfgNode().size()-1;i++) {
            ICfgNode node = this.getFullCfgNode().get(i);
            if(node.toString().contains("{")||node.toString().contains("}")) {
                continue;
            }
        }

        this.toString+=this.getFullCfgNode().get(this.getFullCfgNode().size()-1);
        this.toString+="</td>"+"<td>"+this.getTestCase()+"</td></tr>";
        return this.toString;

    }
}
