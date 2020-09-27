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
    }

    // for development
    public void setNameAndPath(String name, String path) {
        super.setName(name);
    }



    @Override
    protected String generateDefinitionCompileCmd() {
        StringBuilder output = new StringBuilder();

        return output.toString();
    }


    public List<String> getAdditionalIncludes() {
        List<String> list = new ArrayList<>();
        return list;
    }
}
