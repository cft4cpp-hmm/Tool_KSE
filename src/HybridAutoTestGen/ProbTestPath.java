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

public class ProbTestPath
{
    private List<Edge> edges;
    private int pathNumber;
    private boolean isGenerated;
    private int visitedNumber;
    private String realString;
    private IPathConstraints constraints;
    private String testCase;
    private String toString;
    private List<Float> proList;
    private static DecimalFormat df2 = new DecimalFormat("#.####");

    public ProbTestPath(int pathNumber)
    {
        this.pathNumber = pathNumber;
        this.edges = new ArrayList<Edge>();
        this.visitedNumber = 0;
        this.isGenerated = false;
        this.testCase = IStaticSolutionGeneration.NO_SOLUTION;
        this.realString = "";
        toString = "";
    }

    public List<ICfgNode> getFullCfgNode()
    {
        List<ICfgNode> fullCfgNode = new ArrayList<ICfgNode>();
        fullCfgNode.add(this.edges.get(0).getNode());
        for (int i = 0; i < this.edges.size(); i++)
        {
            fullCfgNode.add(this.edges.get(i).getNextNode());
        }
        return fullCfgNode;
    }

    public boolean compare(List<ICfgNode> cfgNodes)
    {
        cfgNodes = new ArrayList<ICfgNode>(cfgNodes);
        for (int i = 0; i < cfgNodes.size(); i++)
        {
            if (cfgNodes.get(i).toString().contains("{") || cfgNodes.get(i).toString().contains("}") || cfgNodes.get(i).toString().indexOf("[") == 0)
            {
                cfgNodes.remove(i);
                i -= 1;
            }
        }

        for (ICfgNode node : this.getFullCfgNode())
        {
            if (cfgNodes.indexOf(node) != this.getFullCfgNode().indexOf(node))
            {
                return false;
            }
        }
        if (this.getFullCfgNode().size() != cfgNodes.size())
        {
            return false;
        }


        return true;
    }

    public String toString()
    {
        String returnString = "";

        if ("".equals(this.getTestCase()))
        {
            return "";
        }

        List<PathConstraint> constraints = new ArrayList<PathConstraint>();
        Pattern pattern = Pattern.compile("=|<|>");
        try
        {
            for (PathConstraint c : (PathConstraints) this.getConstraints())
            {
                if (c.getCfgNode().toString().matches(".*\\b(|<|>)\\b.*") || c.getCfgNode().toString().contains("=="))
                {
                    constraints.add(c);
                }

            }
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }

        returnString = "<tr><td>" + pathNumber + "</td><td>";
        int temp = 0;
        for (int i = 0; i < this.getFullCfgNode().size() - 1; i++)
        {
            ICfgNode node = this.getFullCfgNode().get(i);
            if (node.toString().contains("{") || node.toString().contains("}"))
            {
                continue;
            }

            if (this.proList !=null && this.proList.size() > 0 )
            {
                if (node.toString().contains("<") || node.toString().contains(">") || node.toString().contains("=="))
                {
                    if (constraints.size() > 0 && constraints.get(0).toString().replace(" ", "").indexOf("!") == 0)
                    {
                        if (this.proList.get(0) == 1 && temp == 1)
                        {
                            returnString += "<font class = \"redColor\">!( " + node.toString() + ")</font> <font>" + df2.format(this.proList.get(0)) + "</font>";
                        }
                        else
                        {
                            returnString += "!( " + node.toString() + ") <font>" + df2.format(this.proList.get(0)) + "</font>";
                        }
                        temp = Integer.parseInt(df2.format(this.proList.get(0)).toString().replace(".", ""));
                        constraints.remove(0);
                        this.proList.remove(0);
                    }
                    else if (constraints.size() > 0)
                    {
                        if (this.proList.get(0) == 1 && temp == 1)
                        {
                            returnString += "<font class = \"redColor\">!( " + node.toString() + ")</font> <font>" + df2.format(this.proList.get(0)) + "</font>";
                        }
                        else
                        {
                            returnString += " (" + node.toString() + ") <font>" + df2.format(this.proList.get(0)) + "</font>";
                        }
                        temp = Integer.parseInt(df2.format(this.proList.get(0)).toString().replace(".", ""));
                        constraints.remove(0);
                        this.proList.remove(0);
                    }
                }
                else
                {
                    if (this.proList.get(0) == 1 && temp == 1)
                    {
                        returnString += "<font class = \"redColor\">( " + node.toString() + ")</font> <font>" + df2.format(this.proList.get(0)) + "</font>";
                    }
                    else
                    {
                        returnString += "( " + node.toString() + ") <font>" + df2.format(this.proList.get(0)) + "</font>";
                    }
                    temp = Integer.parseInt(df2.format(this.proList.get(0)).toString().replace(".", ""));
                    this.proList.remove(0);
                }
            }
        }

        returnString += this.getFullCfgNode().get(this.getFullCfgNode().size() - 1);
        returnString += "</td>" + "<td>" + this.getTestCase() + "</td></tr>";
        return returnString;

    }


    //	Get String value for CFT4Cpp
// dominhkha
    public String toStringForCFT4Cpp()
    {
        if (!this.toString.equals(""))
        {
            this.toString = this.toString.substring(4, this.toString.length());
            this.toString = this.toString.replace("{", "");
            this.toString = this.toString.replace("}", "");
            this.toString = this.toString.replace("  ", "");

            String[] listStrings = this.toString.split("=>|=> =>");

            String newString = "<tr><td>" + pathNumber + "</td><td>";

            List<String> newLiStrings = new ArrayList<String>();

            for (int i = 0; i < listStrings.length; i++)
            {
                if (!listStrings[i].equals(" ") && !listStrings[i].equals("  ") && !listStrings[i].contains("["))
                {
                    newLiStrings.add(listStrings[i]);
                }
            }
            for (int i = 0; i < newLiStrings.size() - 1; i++)
            {
				if (newLiStrings.get(i).contains("["))
				{
					continue;
				}

                newString += newLiStrings.get(i) + " " + "<font> => </font> ";

            }
            newString += newLiStrings.get(newLiStrings.size() - 1);

//            if (!"".equals(this.getTestCase()))
//            {
                newString += "</td>" + "<td>" + this.getTestCase() + "</td></tr>";
//            }
//            else
//            {
//                newString += "</td>" + "<td>" +  "not have test data" + "</td></tr>";
//            }
            return newString;
        }

        List<PathConstraint> constraints = new ArrayList<PathConstraint>();
        Pattern pattern = Pattern.compile("=|<|>");
        try
        {
            for (PathConstraint c : (PathConstraints) this.getConstraints())
            {
                if (c.getCfgNode().toString().matches(".*\\b(|<|>)\\b.*") || c.getCfgNode().toString().contains("=="))
                {
                    constraints.add(c);
                }

            }
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }

        this.toString = "<tr><td>" + pathNumber + "</td><td>";
        int temp = 0;
        for (int i = 0; i < this.getFullCfgNode().size() - 1; i++)
        {
//			System.out.println(temp);
            ICfgNode node = this.getFullCfgNode().get(i);
            if (node.toString().contains("{") || node.toString().contains("}"))
            {
                continue;
            }
            if (node.toString().contains("<") || node.toString().contains(">") || node.toString().contains("=="))
            {
                if (constraints.size() > 0 && constraints.get(0).toString().replace(" ", "").indexOf("!") == 0)
                {
                    toString += "!( " + node.toString() + ") <font> => </font>";
                    temp = Integer.parseInt(df2.format(this.proList.get(0)).toString().replace(".", ""));
                    constraints.remove(0);

                }
                else if (constraints.size() > 0)
                {


                    toString += " (" + node.toString() + ") <font> => </font>";
                    temp = Integer.parseInt(df2.format(this.proList.get(0)).toString().replace(".", ""));
                    constraints.remove(0);

                }
            }

            else
            {

                toString += "( " + node.toString() + ") <font> => </font>";

            }

        }

        this.toString += this.getFullCfgNode().get(this.getFullCfgNode().size() - 1);
        this.toString += "</td>" + "<td>" + this.getTestCase() + "</td></tr>";
        return this.toString;
    }


    public Edge searchEdge(ICfgNode node, ICfgNode nextNode)
    {
        for (Edge edge : this.edges)
        {
            if (edge.getNode() == node && edge.getNextNode() == nextNode)
            {
                return edge;
            }
        }
        return null;
    }


    public int getWeight()
    {
        int prob = 1;
        for (Edge edge : this.edges)
        {
            prob *= edge.getWeight();
        }
        return prob;
    }

    public IPathConstraints getConstraints()
    {
        return this.constraints;
    }

    public void setConstraints(IPathConstraints iPathConstraints)
    {
        this.constraints = iPathConstraints;
    }

    public boolean isGenerated()
    {
        return isGenerated;
    }

    public void setGenerated(boolean isGenerated)
    {
        this.isGenerated = isGenerated;
    }

    public String getTestCase()
    {
        return testCase;
    }

    public void setTestCase(String testCase)
    {
        this.testCase = testCase.replaceAll(";;", ";");
    }

    public int getVisitedNumber()
    {
        return visitedNumber;
    }

    public void setVisitedNumber(int visitedNumber)
    {
        this.visitedNumber += visitedNumber;
    }

    public void addEdge(Edge edge)
    {
        this.edges.add(edge);
    }

    public List<Edge> getEdge()
    {
        return this.edges;
    }

    public String getRealString()
    {
        return realString;
    }

    public void setRealString(String realString)
    {
        this.realString = realString;
    }

    public void setToString(String toString)
    {
        this.toString = toString;
    }

    public List<Float> getProList()
    {
        return proList;
    }

    public void setProList(List<Float> proList)
    {
        this.proList = proList;
    }


}
