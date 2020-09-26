package testcase_manager;

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

    public TestCase(IFunctionNode functionNode, String name) {
        setName(name);
        setFunctionNode(functionNode);
    }

    public TestCase() {
    }

    @Override
    public void setPathDefault() {
        String testcasePath = "F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2";
        setPath(testcasePath);
    }

    @Override
    public void setTestPathFileDefault()
    {

    }

    @Override
    public String getExecutionResultTrace()
    {
        return null;
    }

    @Override
    public void setExecutableFileDefault()
    {

    }

    @Override
    public void setCommandConfigFileDefault()
    {

    }

    @Override
    public void setCommandDebugFileDefault()
    {

    }

    @Override
    public void setBreakpointPathDefault()
    {

    }

    @Override
    public void setDebugExecutableFileDefault()
    {

    }

    @Override
    public void setExecutionResultFileDefault()
    {

    }

    @Override
    public void setCurrentCoverageDefault()
    {

    }

    @Override
    public void setCurrentProgressDefault()
    {

    }

    @Override
    public void deleteOldData()
    {

    }

    @Override
    public void deleteOldDataExceptValue()
    {

    }

    @Override
    public void setSourcecodeFileDefault()
    {

    }

    @Override
    public List<String> getAdditionalIncludes()
    {
        return null;
    }


    public RootDataNode getRootDataNode() {
        return rootDataNode;
    }

    public void setRootDataNode(RootDataNode rootDataNode) {
        this.rootDataNode = rootDataNode;
    }

    public void setName(String name) {
        super.setName(name);
        setPathDefault();
        setBreakpointPathDefault();
        setCurrentCoverageDefault();
        setCurrentProgressDefault();
    }

    @Override
    protected String generateDefinitionCompileCmd() {
        String defineName = getName().toUpperCase()
                .replace(SpecialCharacter.DOT, SpecialCharacter.UNDERSCORE_CHAR);

        return String.format("-DAKA_TC_%s", defineName);
    }
    public void setFunctionNode(IFunctionNode functionNode) {
        this.functionNode = functionNode;
    }


    public IFunctionNode getFunctionNode() {
        return this.functionNode;
    }
}
