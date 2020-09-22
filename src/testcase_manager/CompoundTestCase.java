package testcase_manager;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a compound test case
 */
public class CompoundTestCase extends AbstractTestCase {

    // this constructor is used for development
    public CompoundTestCase() {
    }

    public CompoundTestCase(String name) {
        setName(name);
    }

    public void setName(String name) {
        super.setName(name);
        if (getPath() == null)
            setPathDefault();
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


    // for development
    public void setNameAndPath(String name, String path) {
        super.setName(name);
        setPath(path);
    }



    @Override
    protected String generateDefinitionCompileCmd() {
        StringBuilder output = new StringBuilder();

        return output.toString();
    }


    @Override
    public List<String> getAdditionalIncludes() {
        List<String> list = new ArrayList<>();
        return list;
    }
}
