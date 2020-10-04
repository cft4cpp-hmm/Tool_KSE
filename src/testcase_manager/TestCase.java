package testcase_manager;

import HybridAutoTestGen.TestData;
import parser.projectparser.ICommonFunctionNode;
import testdata.object.RootDataNode;
import tree.object.IFunctionNode;
import tree.object.INode;
import utils.SpecialCharacter;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represent a single test case
 */
public class TestCase extends AbstractTestCase {
    private RootDataNode rootDataNode;
    private IFunctionNode functionNode;

    private TestData testData;

    public TestCase(IFunctionNode functionNode, String name) {
        setName(name);
        setFunctionNode(functionNode);
    }

    public TestCase() {
    }


    public RootDataNode getRootDataNode() {
        return rootDataNode;
    }

    public void setRootDataNode(RootDataNode rootDataNode) {
        this.rootDataNode = rootDataNode;
    }

    public void setName(String name) {
        super.setName(name);
    }

    @Override
    protected String generateDefinitionCompileCmd() {
        String defineName = getName().toUpperCase()
                .replace(SpecialCharacter.DOT, SpecialCharacter.UNDERSCORE_CHAR);

        return String.format("-UET_TC_%s", defineName);
    }
    public void setFunctionNode(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }


    public IFunctionNode getFunctionNode() {
        return this.functionNode;
    }

    public TestData getTestData()
    {
        return testData;
    }

    public void setTestData(TestData testData)
    {
        this.testData = testData;
    }
}
