package testcase_manager;

//
//import com.dse.testcasescript.TestcaseSearch;

import com.google.gson.*;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import parser.projectparser.ICommonFunctionNode;
import parser.projectparser.ProjectParser;
import testdata.object.RootDataNode;
import tree.object.IFunctionNode;
import tree.object.IProjectNode;
import utils.DateTimeUtils;
import utils.PathUtils;
import utils.Utils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

public class TestCaseManager {
    public static Map<String, TestCase> nameToBasicTestCaseMap = new HashMap<>();
    private static Map<ICommonFunctionNode, List<String>> functionToTestCasesMap = new HashMap<>();

    public static void main(String[] args) {
//        TestCase testCase = TestCaseManager.getTestCaseByName("mergeTwoArray.57777", "local/hoan_wd/HOHO/testcases");

    }

    public static void clearMaps() {
        nameToBasicTestCaseMap.clear();
        functionToTestCasesMap.clear();
    }

    public static void initializeMaps() {

//        ProjectParser parser = new ProjectParser(new File("F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2"));
//
//        IProjectNode projectNode;
//        projectNode = parser.getRootTree();
//
//        TestcaseRootNode rootNode =
//
//        if (rootNode != null) {
//            // initialize nameToBasicTestCaseMap
//            List<ITestcaseNode> nodes = TestcaseSearch.searchNode(Environment.getInstance().getTestcaseScriptRootNode(), new TestNormalSubprogramNode());
//            try {
//                for (ITestcaseNode node : nodes) {
//                    String path = ((TestNormalSubprogramNode) node).getName();
//                    ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(path);
//                    List<ITestcaseNode> testNewNodes = TestcaseSearch.searchNode(node, new TestNewNode());
//                    List<String> testCaseNames = new ArrayList<>();
//                    for (ITestcaseNode testNewNode : testNewNodes) {
//                        List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
//                        if (names.size() == 1) {
//                            String name = ((TestNameNode) names.get(0)).getName();
//                            nameToBasicTestCaseMap.put(name, null);
//                            testCaseNames.add(name);
//                        }
//                    }
//
//                    functionToTestCasesMap.put(functionNode, testCaseNames);
//                }
//            } catch (Exception fe) {
//            }
//
//            // initialize nameToCompoundTestCaseMap
//            nodes = TestcaseSearch.searchNode(Environment.getInstance().getTestcaseScriptRootNode(), new TestCompoundSubprogramNode());
//            List<ITestcaseNode> testNewNodes = TestcaseSearch.searchNode(nodes.get(0), new TestNewNode());
//
//            for (ITestcaseNode testNewNode : testNewNodes) {
//                List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
//                if (names.size() == 1) {
//                    String name = ((TestNameNode) names.get(0)).getName();
//                    nameToCompoundTestCaseMap.put(name, null);
//                }
//            }
//        }
    }

    public static TestCase createTestCase(String name, IFunctionNode functionNode) {
        if (name == null || functionNode == null)
            return null;

        if (!TestCaseManager.checkTestCaseExisted(name)) {
            TestCase testCase = new TestCase(functionNode, name);
            if (testCase == null)
                return null;

            testCase.setCreationDateTime(LocalDateTime.now());

            // need to validate name of testcase
            List<String> testcaseNames = functionToTestCasesMap.get(functionNode);
            if (testcaseNames != null) {
                testcaseNames.add(name);
                nameToBasicTestCaseMap.put(name, testCase);
            }
            return testCase;
        } else {
            return null;
        }
    }

    public static TestCase createTestCase(IFunctionNode functionNode, String nameTestcase) {
        TestCase testCase;
        String testCaseName = "TestCase1";
        testCase = createTestCase(testCaseName, functionNode);
        return testCase;
    }

    public static TestCase createTestCase(IFunctionNode functionNode) {
        if (functionNode == null)
            return null;
        String testCaseName = "TestCase1";
        TestCase testCase = createTestCase(testCaseName, functionNode);
        return testCase;
    }

    public static CompoundTestCase createCompoundTestCase() {
        String testCaseName = "TestCase1";
        CompoundTestCase compoundTestCase = new CompoundTestCase(testCaseName);
        compoundTestCase.setCreationDateTime(LocalDateTime.now());

        return compoundTestCase;
    }


    public static CompoundTestCase getCompoundTestCaseByName(String name) {

        return null;
    }
    public static ITestCase getTestCaseByName(String name) {
        ITestCase testCase = getBasicTestCaseByName(name);

        if (testCase == null)
            testCase = getCompoundTestCaseByName(name);

//        if (testCase == null)
//            logger.error(String.format("Test case %s not found.", name));

        return testCase;
    }

    public static ITestCase getTestCaseByNameWithoutData(String name) {
        ITestCase testCase = getBasicTestCaseByNameWithoutData(name);

        if (testCase == null)
            testCase = getCompoundTestCaseByName(name);

//        if (testCase == null)
//            logger.error(String.format("Test case %s not found.", name));

        return testCase;
    }

    public static TestCase getBasicTestCaseByNameWithoutData(String name) {
        // find in the map first
        if (nameToBasicTestCaseMap.containsKey(name)) {
            TestCase testCaseInMap = nameToBasicTestCaseMap.get(name);
            if (testCaseInMap != null) {
                return testCaseInMap;

            } else { // haven't loaded yet
                String testCaseDirectory = "F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2";
                // find and load from disk
                TestCase testCase = getBasicTestCaseByName(name, testCaseDirectory, false);

                if (testCase != null) {
                    nameToBasicTestCaseMap.replace(name, testCase);
                } else {
                }

                return testCase;
            }
        }

        return null;
    }

    public static TestCase getBasicTestCaseByName(String name) {
        if (name == null)
            return null;

        // find in the map first
        if (nameToBasicTestCaseMap.containsKey(name)) {
            optimizeNameToBasicTestCaseMap(name);
            TestCase testCaseInMap = nameToBasicTestCaseMap.get(name);
            if (testCaseInMap != null) {
                if (testCaseInMap.getRootDataNode() == null) {
                    String testCaseDirectory = "F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2";
                    TestCase testCase = getBasicTestCaseByName(name, testCaseDirectory, true);

                    if (testCase != null)
                        nameToBasicTestCaseMap.replace(name, testCase);

                    return testCase;
                } else
                    return testCaseInMap;
            } else { // haven't loaded yet
                String testCaseDirectory = "F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2";
                // find and load from disk
                TestCase testCase = getBasicTestCaseByName(name, testCaseDirectory, true);

                if (testCase != null) {
                    nameToBasicTestCaseMap.replace(name, testCase);
                } else {
                }

                return testCase;
            }
        }

        return null;
    }


    private static TestCase parseJsonToTestCaseWithoutData(JsonObject jsonObject) {
        if (jsonObject == null)
            return null;

        String name = "N/A";
        if (jsonObject.get("name") != null)
            name = jsonObject.get("name").getAsString();

        String status = "N/A";
        if (jsonObject.get("status") != null)
            status = jsonObject.get("status").getAsString();

        IFunctionNode functionNode = null;
        if (jsonObject.get("rootDataNode") != null) {
            JsonObject rootDataNodeJsonObject = jsonObject.get("rootDataNode").getAsJsonObject();
            String functionPath = rootDataNodeJsonObject.get("functionNode").getAsString();
            functionPath = PathUtils.toAbsolute(functionPath);

//            try {
//                functionNode = UIController.searchFunctionNodeByPath(functionPath);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }

            if (functionNode == null)
                return null;

        } else
            return null;

        TestCase testCase = new TestCase();
        testCase.setName(name);
        //testCase.setStatus(status);

        //extractRelateInfo(jsonObject, testCase);

        // todo: nead to validate status
        testCase.setFunctionNode(functionNode);
        return testCase;
    }

    public static TestCase getBasicTestCaseByName(String name, String testCaseDirectory, boolean parseData) {
        String fullPathOfTestcase = new File(testCaseDirectory).getAbsolutePath() + File.separator + name + ".json";
        if (new File(fullPathOfTestcase).exists()) {
            String json = Utils.readFileContent(fullPathOfTestcase);
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
                return parseJsonToTestCaseWithoutData(jsonObject);
        }
        return null;
    }


    public static boolean checkTestCaseExisted(String name) {
        optimizeNameToBasicTestCaseMap(name);
        return nameToBasicTestCaseMap.containsKey(name);
    }

    public static void optimizeNameToBasicTestCaseMap(String name) {
        TestCase tc = nameToBasicTestCaseMap.get(name);
        if (tc == null)
            return;

    }

    private static final int RANDOM_BOUND = 9999999;
}
