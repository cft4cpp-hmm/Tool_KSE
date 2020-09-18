package testcase_manager;

import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.user_code.objects.TestCaseUserCode;
import com.dse.util.*;
import com.google.gson.*;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

public class TestCaseManager {
    private final static AkaLogger logger = AkaLogger.get(TestCaseManager.class);
    public static Map<String, TestCase> nameToBasicTestCaseMap = new HashMap<>();
    private static Map<String, CompoundTestCase> nameToCompoundTestCaseMap = new HashMap<>();
    private static Map<ICommonFunctionNode, List<String>> functionToTestCasesMap = new HashMap<>();

    public static void main(String[] args) {
//        TestCase testCase = TestCaseManager.getTestCaseByName("mergeTwoArray.57777", "local/hoan_wd/HOHO/testcases");

    }

    public static void clearMaps() {
        nameToBasicTestCaseMap.clear();
        nameToCompoundTestCaseMap.clear();
        functionToTestCasesMap.clear();
    }

    public static void initializeMaps() {
        TestcaseRootNode rootNode = Environment.getInstance().getTestcaseScriptRootNode();
        if (rootNode != null) {
            // initialize nameToBasicTestCaseMap
            List<ITestcaseNode> nodes = TestcaseSearch.searchNode(Environment.getInstance().getTestcaseScriptRootNode(), new TestNormalSubprogramNode());
            try {
                for (ITestcaseNode node : nodes) {
                    String path = ((TestNormalSubprogramNode) node).getName();
                    ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(path);
                    List<ITestcaseNode> testNewNodes = TestcaseSearch.searchNode(node, new TestNewNode());
                    List<String> testCaseNames = new ArrayList<>();
                    for (ITestcaseNode testNewNode : testNewNodes) {
                        List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
                        if (names.size() == 1) {
                            String name = ((TestNameNode) names.get(0)).getName();
                            nameToBasicTestCaseMap.put(name, null);
                            testCaseNames.add(name);
                        }
                    }

                    functionToTestCasesMap.put(functionNode, testCaseNames);
                }
            } catch (FunctionNodeNotFoundException fe) {
                logger.error("Function node not found");
            }

            // initialize nameToCompoundTestCaseMap
            nodes = TestcaseSearch.searchNode(Environment.getInstance().getTestcaseScriptRootNode(), new TestCompoundSubprogramNode());
            List<ITestcaseNode> testNewNodes = TestcaseSearch.searchNode(nodes.get(0), new TestNewNode());

            for (ITestcaseNode testNewNode : testNewNodes) {
                List<ITestcaseNode> names = TestcaseSearch.searchNode(testNewNode, new TestNameNode());
                if (names.size() == 1) {
                    String name = ((TestNameNode) names.get(0)).getName();
                    nameToCompoundTestCaseMap.put(name, null);
                }
            }
        } else {
            logger.error("The test case script root node is null when initialize maps for TestcaseManager.");
        }
    }

    public static TestCase createTestCase(String name, ICommonFunctionNode functionNode) {
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
            } else {
                logger.error("Can not find list testcase names correspond to functionNode: " + functionNode.getAbsolutePath() + "when create test case");
            }

            // init parameter expected outputs
            logger.debug("initParameterExpectedOuputs");
            testCase.initParameterExpectedOuputs(testCase.getRootDataNode());

            logger.debug("initGlobalInputExpOutputMap");
            testCase.initGlobalInputExpOutputMap(testCase.getRootDataNode(), testCase.getGlobalInputExpOutputMap());

            exportBasicTestCaseToFile(testCase);
            exportBreakpointsToFile(testCase);
            return testCase;
        } else {
            return null;
        }
    }

    public static TestCase createMinMidMaxTestCase(String type, IFunctionNode functionNode) {
        String testcaseName;
        switch (type) {
            case "MIN":
                testcaseName = AbstractTestCase.removeSpecialCharacter(functionNode.getSimpleName() + ".MIN");
                return createTestCase(testcaseName, functionNode);
            case "MID":
                testcaseName = AbstractTestCase.removeSpecialCharacter(functionNode.getSimpleName() + ".MID");
                return createTestCase(testcaseName, functionNode);
            case "MAX":
                testcaseName = AbstractTestCase.removeSpecialCharacter(functionNode.getSimpleName() + ".MAX");
                return createTestCase(testcaseName, functionNode);
            default:
                logger.error(type + "is not in {MIN, MID, MAX}");
                return null;
        }
    }

    public static TestCase createTestCase(ICommonFunctionNode functionNode, String nameTestcase) {
        TestCase testCase;
        String testCaseName;
        if (nameTestcase != null || nameTestcase.length() > 0)
            testCaseName = AbstractTestCase.removeSpecialCharacter(nameTestcase);
        else {
            testCaseName = TestCaseManager.generateContinuousNameOfTestcase(functionNode.getSimpleName());
        }
        testCase = createTestCase(testCaseName, functionNode);
        return testCase;
    }

    public static TestCase createTestCase(ICommonFunctionNode functionNode) {
        if (functionNode == null)
            return null;
        String testCaseName = TestCaseManager.
                generateContinuousNameOfTestcase(functionNode.getSimpleName() + ITestCase.POSTFIX_TESTCASE_BY_USER);
        TestCase testCase = createTestCase(testCaseName, functionNode);
        return testCase;
    }

    public static CompoundTestCase createCompoundTestCase() {
        String testCaseName = TestCaseManager.generateContinuousNameOfTestcase(ITestCase.COMPOUND_SIGNAL);
        CompoundTestCase compoundTestCase = new CompoundTestCase(testCaseName);
        compoundTestCase.setCreationDateTime(LocalDateTime.now());

        nameToCompoundTestCaseMap.put(testCaseName, compoundTestCase);
        exportCompoundTestCaseToFile(compoundTestCase);

        return compoundTestCase;
    }

    public static ITestCase getTestCaseByName(String name) {
        ITestCase testCase = getBasicTestCaseByName(name);

        if (testCase == null)
            testCase = getCompoundTestCaseByName(name);

        if (testCase == null)
            logger.error(String.format("Test case %s not found.", name));

        return testCase;
    }

    public static ITestCase getTestCaseByNameWithoutData(String name) {
        ITestCase testCase = getBasicTestCaseByNameWithoutData(name);

        if (testCase == null)
            testCase = getCompoundTestCaseByName(name);

        if (testCase == null)
            logger.error(String.format("Test case %s not found.", name));

        return testCase;
    }

    public static TestCase getBasicTestCaseByNameWithoutData(String name) {
        // find in the map first
        if (nameToBasicTestCaseMap.containsKey(name)) {
            TestCase testCaseInMap = nameToBasicTestCaseMap.get(name);
            if (testCaseInMap != null) {
                return testCaseInMap;

            } else { // haven't loaded yet
                String testCaseDirectory = new WorkspaceConfig().fromJson().getTestcaseDirectory();
                // find and load from disk
                TestCase testCase = getBasicTestCaseByName(name, testCaseDirectory, false);

                if (testCase != null) {
                    nameToBasicTestCaseMap.replace(name, testCase);
                } else {
                    logger.error(String.format("Failed to load test case %s", name));
                }

                return testCase;
            }
        } else if (!name.startsWith(CompoundTestCase.COMPOUND_SIGNAL)) {
            logger.error(String.format("basic test case %s does not exist.", name));
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
                    String testCaseDirectory = new WorkspaceConfig().fromJson().getTestcaseDirectory();
                    TestCase testCase = getBasicTestCaseByName(name, testCaseDirectory, true);

                    if (testCase != null)
                        nameToBasicTestCaseMap.replace(name, testCase);

                    return testCase;
                } else
                    return testCaseInMap;
            } else { // haven't loaded yet
                String testCaseDirectory = new WorkspaceConfig().fromJson().getTestcaseDirectory();
                // find and load from disk
                TestCase testCase = getBasicTestCaseByName(name, testCaseDirectory, true);

                if (testCase != null) {
                    nameToBasicTestCaseMap.replace(name, testCase);
                } else {
                    logger.error(String.format("Failed to load test case %s", name));
                }

                return testCase;
            }
        } else if (!name.startsWith(CompoundTestCase.COMPOUND_SIGNAL)) {
            logger.error(String.format("basic test case %s does not exist.", name));
        }

        return null;
    }

    public static CompoundTestCase getCompoundTestCaseByName(String name) {
        if (name == null)
            return null;

        if (nameToCompoundTestCaseMap.containsKey(name)) {
            CompoundTestCase testCaseInMap = nameToCompoundTestCaseMap.get(name);
            if (testCaseInMap != null) {
                return testCaseInMap;
            } else { // haven't loaded yet
                String compoundTestCaseDirectory = new WorkspaceConfig().fromJson().getCompoundTestcaseDirectory();
                File directory = new File(compoundTestCaseDirectory);
                if (directory.exists() && directory.isDirectory()) {
                    for (File compoundTCFile : Objects.requireNonNull(directory.listFiles())) {
                        if (compoundTCFile.getName().equals(name + ".json")) { // testcase.001.json
                            CompoundTestCaseImporter importer = new CompoundTestCaseImporter();
                            CompoundTestCase compoundTestCase = importer.importCompoundTestCase(compoundTCFile);
                            compoundTestCase.validateSlots();
                            TestCaseManager.exportCompoundTestCaseToFile(compoundTestCase);

                            nameToCompoundTestCaseMap.replace(name, compoundTestCase);

                            return compoundTestCase;
                        }
                    }
                }
                logger.error(String.format("Compound test case %s not found.", name));
                return null;
            }
        } else {
            logger.error(String.format("Compound test case %s does not exist.", name));
            return null;
        }
    }

    private static void exportBreakpointsToFile(TestCase testCase) {
        if (testCase == null)
            return;
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        json.add(testCase.getRootDataNode().getFunctionNode().getParent().getAbsolutePath(), array);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(json);
        Utils.writeContentToFile(jsonString, testCase.getBreakpointPath());
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

        ICommonFunctionNode functionNode = null;
        if (jsonObject.get("rootDataNode") != null) {
            JsonObject rootDataNodeJsonObject = jsonObject.get("rootDataNode").getAsJsonObject();
            String functionPath = rootDataNodeJsonObject.get("functionNode").getAsString();
            functionPath = PathUtils.toAbsolute(functionPath);

            try {
                functionNode = UIController.searchFunctionNodeByPath(functionPath);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (functionNode == null)
                return null;

        } else
            return null;

        TestCase testCase = new TestCase();
        testCase.setName(name);
        testCase.setStatus(status);

        extractRelateInfo(jsonObject, testCase);

        // todo: nead to validate status
        testCase.setFunctionNode(functionNode);
        return testCase;
    }

    public static void extractRelateInfo(JsonObject jsonObject, ITestCase testCase) {
        if (jsonObject.get("path") != null)
            testCase.setPath(PathUtils.toAbsolute(jsonObject.get("path").getAsString()));

        if (jsonObject.get("sourceCode") != null)
            testCase.setSourceCodeFile(PathUtils.toAbsolute(jsonObject.get("sourceCode").getAsString()));

        if (jsonObject.get("testPath") != null)
            testCase.setTestPathFile(PathUtils.toAbsolute(jsonObject.get("testPath").getAsString()));

        if (jsonObject.get("executable") != null)
            testCase.setExecutableFile(PathUtils.toAbsolute(jsonObject.get("executable").getAsString()));

        if (jsonObject.get("commandConfig") != null)
            testCase.setCommandConfigFile(PathUtils.toAbsolute(jsonObject.get("commandConfig").getAsString()));

        if (jsonObject.get("commandDebug") != null)
            testCase.setCommandDebugFile(PathUtils.toAbsolute(jsonObject.get("commandDebug").getAsString()));

        if (jsonObject.get("breakPoint") != null)
            testCase.setBreakpointPath(PathUtils.toAbsolute(jsonObject.get("breakPoint").getAsString()));

        if (jsonObject.get("debugExecutable") != null)
            testCase.setDebugExecutableFile(PathUtils.toAbsolute(jsonObject.get("debugExecutable").getAsString()));

        if (jsonObject.get("executionResult") != null)
            testCase.setExecutionResultFile(PathUtils.toAbsolute(jsonObject.get("executionResult").getAsString()));

        if (jsonObject.get("creationDate") != null) {
            LocalDateTime dt = DateTimeUtils.parse(jsonObject.get("creationDate").getAsString());
            testCase.setCreationDateTime(dt);
        }
    }

    public static TestCase parseJsonToTestCase(JsonObject jsonObject) {
        String name = jsonObject.get("name").getAsString();

        String status = "N/A";
        if (jsonObject.get("status") != null)
            status = jsonObject.get("status").getAsString();

        AssertionResult result = new AssertionResult();
        if (jsonObject.get("result") != null) {
            String[] tempList = jsonObject.get("result").getAsString().split("/");

            result.setPass(Integer.parseInt(tempList[0]));
            result.setTotal(Integer.parseInt(tempList[1]));
        }

        TestCase testCase = new TestCase();
        testCase.setName(name);
        testCase.setStatus(status);
        testCase.setExecutionResult(result);

        TestCaseUserCode testCaseUserCode = readUserCode(jsonObject);
        if (testCaseUserCode != null)
            testCase.setTestCaseUserCode(testCaseUserCode);

        DataNode rootDataNode;
        if (jsonObject.get("rootDataNode") != null) {
            JsonObject rootDataNodeJsonObject = jsonObject.get("rootDataNode").getAsJsonObject();
            TestCaseDataImporter importer = new TestCaseDataImporter();
            importer.setTestCase(testCase);
            rootDataNode = importer.importRootDataNode(rootDataNodeJsonObject);

            if (rootDataNode == null) {
                logger.debug("Failed to import root data node of " + name);
                return null;
            }
        } else
            return null;

        extractRelateInfo(jsonObject, testCase);

        // todo: nead to validate status
        testCase.setRootDataNode((RootDataNode) rootDataNode);
        testCase.setFunctionNode(((RootDataNode) rootDataNode).getFunctionNode());
        testCase.updateGlobalInputExpOutputAfterInportFromFile();
        return testCase;
    }

    static TestCaseUserCode readUserCode(JsonObject jsonObject) {
        TestCaseUserCode testCaseUserCode = null;
        if (jsonObject.get("testCaseUserCode") != null) {
            JsonObject tcUc = (JsonObject) jsonObject.get("testCaseUserCode");
            testCaseUserCode = new TestCaseUserCode();
            String setUpContent = tcUc.get("setUpContent").getAsString();
            String tearDownContent = tcUc.get("tearDownContent").getAsString();
            testCaseUserCode.setSetUpContent(setUpContent);
            testCaseUserCode.setTearDownContent(tearDownContent);

            JsonArray includePaths = tcUc.get("includePaths").getAsJsonArray();
            for (JsonElement element : includePaths) {
                testCaseUserCode.getIncludePaths().add(element.getAsString());
            }
        }
        return testCaseUserCode;
    }

    public static String getStatusTestCaseByName(String name) {
        ITestCase testCase = getTestCaseByNameWithoutData(name);
        if (testCase == null)
            return ITestCase.STATUS_EMPTY;
        else
            return testCase.getStatus();
    }

    public static TestCase getBasicTestCaseByName(String name, String testCaseDirectory, boolean parseData) {
        String fullPathOfTestcase = new File(testCaseDirectory).getAbsolutePath() + File.separator + name + ".json";
        if (new File(fullPathOfTestcase).exists()) {
            String json = Utils.readFileContent(fullPathOfTestcase);
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

            if (parseData)
                return parseJsonToTestCase(jsonObject);
            else
                return parseJsonToTestCaseWithoutData(jsonObject);
        }
        logger.error(String.format("Basic test case %s not found.", name));
        return null;
    }

    public static TestCase importBasicTestCaseByFile(File testcaseFile) {
        if (testcaseFile != null) {
            String json = Utils.readFileContent(testcaseFile);
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            TestCase testCase = parseJsonToTestCase(jsonObject);

            if (testCase != null) {
                testCase.setName(testCase.getName());

                ICommonFunctionNode functionNode = testCase.getFunctionNode();
                if (!functionToTestCasesMap.containsKey(functionNode)) {
                    logger.debug("Failed to import basic testcase from file " + testcaseFile.getAbsolutePath());
                    return null;
                } else {
                    if (!nameToBasicTestCaseMap.containsKey(testCase.getName())) {
                        nameToBasicTestCaseMap.put(testCase.getName(), testCase);
                        functionToTestCasesMap.get(functionNode).add(testCase.getName());
                        // save to tst file
                        Platform.runLater(testCase::updateToTestCasesNavigatorTree);
                    }

                    TestCaseManager.exportTestCaseToFile(testCase);
                    return testCase;
                }
            }
        }

        return null;
    }

    public static void importTestCasesFromDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            String[] extensions = new String[]{"json"};
            // false -> non recursive
            List<File> files = (List<File>) FileUtils.listFiles(directory, extensions, false);
            for (File testcaseFile : files) {
                if (importBasicTestCaseByFile(testcaseFile) == null) {
                    logger.error("Failed to import test case from file: " + testcaseFile.getAbsolutePath());
                } else {
                    logger.debug("Import testcase successfully: " + testcaseFile.getAbsolutePath());
                }
            }
        }
    }

    public static void exportCompoundTestCaseToFile(CompoundTestCase compoundTestCase) {
        Environment.getInstance().saveTestcasesScriptToFile();
        logger.debug("Export test compound to " + compoundTestCase.getPath());
        CompoundTestCaseExporter exporter = new CompoundTestCaseExporter();
        File file = new File(compoundTestCase.getPath());
        exporter.export(file, compoundTestCase);
    }

    public static void exportTestCaseToFile(ITestCase testCase) {
        if (testCase instanceof TestCase)
            exportBasicTestCaseToFile((TestCase) testCase);
        else if (testCase instanceof CompoundTestCase)
            exportCompoundTestCaseToFile((CompoundTestCase) testCase);
    }

    public static void exportBasicTestCaseToFile(TestCase testCase) {
        Environment.getInstance().saveTestcasesScriptToFile();

        JsonObject json = new JsonObject();
        json.addProperty("name", testCase.getName());

        String status = testCase.getStatus();
        if (status != null) {
            json.addProperty("status", testCase.getStatus());
        } else {
            json.addProperty("status", "null");
        }

        // export test case user code
        compressRelateInfo(json, testCase);

        TestCaseDataExporter exporter = new TestCaseDataExporter();
        JsonElement rootDataNode = exporter.exportToJsonElement(testCase);
        json.add("rootDataNode", rootDataNode);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(json);
        Utils.writeContentToFile(jsonString, testCase.getPath());

        // save the real type of template function
        if (testCase.getFunctionNode() instanceof IFunctionNode && testCase.getFunctionNode().isTemplate()) {
            String realTypeFile = testCase.getFunctionNode().getTemplateFilePath();
            PrototypeOfFunction obj = new PrototypeOfFunction();

            if (new File(realTypeFile).exists()) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson customGson = gsonBuilder.create();
                String jsonStr = Utils.readFileContent(realTypeFile);
                obj = customGson.fromJson(jsonStr, PrototypeOfFunction.class);
            }

            if (obj != null) {
                // remove deleted prototypes
                for (int i = obj.getPrototypes().size() - 1; i >= 0; i--)
                    if (!(new File(obj.getPrototypes().get(i)).exists()))
                        obj.getPrototypes().remove(i);

                // save
                if (!obj.getPrototypes().contains(testCase.getPath()))
                    obj.getPrototypes().add(testCase.getPath());

                GsonBuilder builder = new GsonBuilder();
                gson = builder.setPrettyPrinting().create();
                String jsonStr = gson.toJson(obj, PrototypeOfFunction.class);
                Utils.writeContentToFile(jsonStr, realTypeFile);
            }
        }
    }

    public static void compressRelateInfo(JsonObject json, ITestCase testCase) {
        JsonObject jsonObject = new JsonObject();

        // USER CODE
        TestCaseUserCode testCaseUserCode = testCase.getTestCaseUserCode();

        JsonArray includePaths = new JsonArray();
        for (String string : testCaseUserCode.getIncludePaths())
            includePaths.add(string);
        jsonObject.add("includePaths", includePaths);

        jsonObject.addProperty("setUpContent", testCaseUserCode.getSetUpContent());
        jsonObject.addProperty("tearDownContent", testCaseUserCode.getTearDownContent());

        json.add("testCaseUserCode", jsonObject);

        // TEST RESULT
        AssertionResult executionResult = testCase.getExecutionResult();
        if (executionResult != null)
            json.addProperty("result", String.format("%d/%d",
                    executionResult.getPass(),
                    executionResult.getTotal()
            ));

        String path = testCase.getPath();
        json.addProperty("path", PathUtils.toRelative(path));

        String sourceCodeFile = testCase.getSourceCodeFile();
        if (sourceCodeFile != null)
            json.addProperty("sourceCode", PathUtils.toRelative(sourceCodeFile));

        String testPathFile = testCase.getTestPathFile();
        if (testPathFile != null)
            json.addProperty("testPath", PathUtils.toRelative(testPathFile));

        String executableFile = testCase.getExecutableFile();
        if (executableFile != null)
            json.addProperty("executable", PathUtils.toRelative(executableFile));

        String commandConfigFile = testCase.getCommandConfigFile();
        if (commandConfigFile != null)
            json.addProperty("commandConfig", PathUtils.toRelative(commandConfigFile));

        String commandDebugFile = testCase.getCommandDebugFile();
        if (commandDebugFile != null)
            json.addProperty("commandDebug", PathUtils.toRelative(commandDebugFile));

        String breakpointPath = testCase.getBreakpointPath();
        if (breakpointPath != null)
            json.addProperty("breakPoint", PathUtils.toRelative(breakpointPath));

        String debugExecutableFile = testCase.getDebugExecutableFile();
        if (debugExecutableFile != null)
            json.addProperty("debugExecutable", PathUtils.toRelative(debugExecutableFile));

        String executionResultFile = testCase.getExecutionResultFile();
        if (executionResultFile != null)
            json.addProperty("executionResult", PathUtils.toRelative(executionResultFile));

        LocalDateTime creationDateTime = testCase.getCreationDateTime();
        if (creationDateTime != null)
            json.addProperty("creationDate", PathUtils.toRelative(creationDateTime.toString()));
    }

    public static void removeBasicTestCase(String name) {
        TestCase testCase = getBasicTestCaseByNameWithoutData(name);
        if (testCase != null) {
            testCase.deleteOldData();
            nameToBasicTestCaseMap.remove(name);

            ICommonFunctionNode functionNode = testCase.getFunctionNode();
            functionToTestCasesMap.get(functionNode).remove(name);

        } else {
            logger.error("Test case not found. Name: " + name);
        }
    }

    public static void removeCompoundTestCase(String name) {
        CompoundTestCase compoundTestCase = getCompoundTestCaseByName(name);
        if (compoundTestCase != null) {
            compoundTestCase.deleteOldData();
            nameToCompoundTestCaseMap.remove(name);
        }
    }

    public static TestCase duplicateBasicTestCase(String name) {
        // load from disk
        String testCaseDirectory = new WorkspaceConfig().fromJson().getTestcaseDirectory();
        TestCase clone = getBasicTestCaseByName(name, testCaseDirectory, true);
        if (clone != null) {
            ICommonFunctionNode functionNode = clone.getRootDataNode().getFunctionNode();
            if (functionNode != null) {
                String testCaseName = generateContinuousNameOfTestcase(functionNode.getSimpleName());
                clone.setName(testCaseName);
                exportBasicTestCaseToFile(clone);

                nameToBasicTestCaseMap.put(testCaseName, clone);
                List<String> testcaseNames = functionToTestCasesMap.get(functionNode);
                if (testcaseNames != null) {
                    testcaseNames.add(testCaseName);
                } else {
                    logger.error("Can not find list testcase names correspond to functionNode: "
                            + functionNode.getAbsolutePath() + "when duplicate test case " + name);
                }

                return clone;
            } else {
                logger.error("The functionNode of test case is null");
            }
        }
        return null;
    }

    public static CompoundTestCase duplicateCompoundTestCase(String name) {
        // load from disk
        CompoundTestCase clone = getCompoundTestCaseByName(name);
        if (clone != null) {
            String[] strings = clone.getName().split("\\.");
            String str = strings[strings.length - 1];
            String newExtension = SpecialCharacter.DOT_IN_STRUCT + new Random().nextInt(100000);
            String compoundTestCaseName = clone.getName().replace(SpecialCharacter.DOT + str, newExtension);
            clone.setName(compoundTestCaseName);
            exportCompoundTestCaseToFile(clone);
            nameToCompoundTestCaseMap.put(compoundTestCaseName, clone);
            return clone;
        }
        return null;
    }

    /**
     * This method to check if the testcase file (or compound testcase) that has the name
     * exists or not
     *
     * @param name: name of the testcase (or compound testcase)
     * @return true if find out a testcase or a compound testcase in directories
     */
    public static boolean checkTestCaseExisted(String name) {
        optimizeNameToBasicTestCaseMap(name);
        return nameToBasicTestCaseMap.containsKey(name);
    }

    public static void optimizeNameToBasicTestCaseMap(String name) {
        TestCase tc = nameToBasicTestCaseMap.get(name);
        if (tc == null)
            return;
        String path = tc.getPath();
        if (path == null || !new File(path).exists()) {
            nameToBasicTestCaseMap.remove(name);
        }
    }

    public static List<TestCase> getTestCasesByFunction(ICommonFunctionNode functionNode) {
        List<TestCase> testCases = new ArrayList<>();
        List<String> names = functionToTestCasesMap.get(functionNode);
        for (String name : names) {
            TestCase tc = getBasicTestCaseByName(name);
            if (tc != null && new File(tc.getPath()).exists())
                testCases.add(tc);
        }
        return testCases;
    }

    public static Map<ICommonFunctionNode, List<String>> getFunctionToTestCasesMap() {
        return functionToTestCasesMap;
    }

    public static Map<String, CompoundTestCase> getNameToCompoundTestCaseMap() {
        return nameToCompoundTestCaseMap;
    }

    public static Map<String, TestCase> getNameToBasicTestCaseMap() {
        return nameToBasicTestCaseMap;
    }

//    public static List<String> generatedTestcaseNames = new ArrayList<>();

    public synchronized static String generateContinuousNameOfTestcase(String testCaseNamePrefix){
        logger.debug("Generate name of test case name");

        for (int i = 0; i < RANDOM_BOUND; i++){
            String candidateName = testCaseNamePrefix + ITestCase.AKA_SIGNAL + i;

            // to create unique name temporarily
            candidateName = AbstractTestCase.removeSpecialCharacter(candidateName); // to create unique name temporarily

//            if (generatedTestcaseNames.contains(candidateName))
//                continue;

            WorkspaceConfig workspaceConfig = new WorkspaceConfig().fromJson();
            String candidatePath;

            if (testCaseNamePrefix.startsWith(ITestCase.COMPOUND_SIGNAL))
                candidatePath = workspaceConfig.getCompoundTestcaseDirectory() + File.separator + candidateName + ".json";
            else
                candidatePath = workspaceConfig.getTestcaseDirectory() + File.separator + candidateName + ".json";
            if (new File(candidatePath).exists())
                continue;
            else {
//                generatedTestcaseNames.add(candidateName);
                return candidateName;
            }
        }
        return new Random().nextInt(9999999) + "";
    }

    private static final int RANDOM_BOUND = 9999999;
}
