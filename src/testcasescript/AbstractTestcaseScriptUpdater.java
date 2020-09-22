package testcasescript;


import testcasescript.object.ITestcaseNode;

public abstract class AbstractTestcaseScriptUpdater {
    /**
     * The root of test script
     */
    private ITestcaseNode rootTestScript;

    private String nameOfTestcase;

    public abstract void updateOnTestcaseScript() throws Exception;


    public ITestcaseNode getRootTestScript() {
        return rootTestScript;
    }

    public void setRootTestScript(ITestcaseNode rootTestScript) {
        this.rootTestScript = rootTestScript;
    }

    public String getNameOfTestcase() {
        return nameOfTestcase;
    }

    public void setNameOfTestcase(String nameOfTestcase) {
        this.nameOfTestcase = nameOfTestcase;
    }

    class MatchingPair {
        ITestcaseNode testcaseNode = null;

        public ITestcaseNode getTestcaseNode() {
            return testcaseNode;
        }

        public void setTestcaseNode(ITestcaseNode testcaseNode) {
            this.testcaseNode = testcaseNode;
        }
    }
}
