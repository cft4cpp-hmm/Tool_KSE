package testcasescript.object;

import utils.PathUtils;


public class TestUnitNode extends AbstractTestcaseNode {
    private String name; // absolute path

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString() + ": name = " + getName();
    }

    @Override
    public String exportToFile() {
        StringBuilder output = new StringBuilder();
        output.append(TEST_UNIT + " ").append(PathUtils.toRelative(name)).append("\n");

        for (ITestcaseNode child : getChildren())
            output.append(child.exportToFile()).append("\n");

        return output.toString();
    }


}
