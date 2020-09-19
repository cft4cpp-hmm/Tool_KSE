package coverage;


import coverage.function_call.FunctionCall;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestPathUtils {

    public static final String CALLING_TAG = "Calling: ";
    public static final String SKIP_TAG = "SKIP ";
    public static final String BEGIN_TAG = "BEGIN OF ";
    public static final String END_TAG = "END OF ";
    public static final String RETURN_TAG = "Return from: ";
    private static final String PRE_CALLING_TAG = "<<PRE-CALLING>>";
    private static final String DELIMITER = "|";

    public static List<FunctionCall> traceFunctionCall(String filePath) {
        List<FunctionCall> calledFunctions = new ArrayList<>();
//        String[] lines = Utils.readFileContent(filePath).split("\\R");
//        for (int i = 0; i < lines.length; i++) {
//            if (lines[i].startsWith(CALLING_TAG)) {
//                String functionPath = lines[i].substring(CALLING_TAG.length());
//                functionPath = Utils.normalizePath(functionPath);
//                functionPath = PathUtils.toAbsolute(functionPath);
//
//                FunctionCall call;
//
//                if (functionPath.contains(DELIMITER)) {
//                    String[] paths = functionPath.split("\\Q" + DELIMITER + "\\E");
//                    call = new ConstructorCall();
//                    call.setAbsolutePath(paths[0]);
//                    ((ConstructorCall) call).setParameterPath(paths[1]);
//                } else {
//                    call = new FunctionCall();
//                    call.setAbsolutePath(functionPath);
//                }
//
//                if (i > 0 && lines[i - 1].startsWith(PRE_CALLING_TAG))
//                    call.setCategory(Event.Position.FIRST);
//                else
//                    call.setCategory(Event.Position.MIDDLE);
//
//                call.setIndex(calledFunctions.size());
//
//                calledFunctions.add(call);
//
//                int iterator = (int) calledFunctions.stream()
//                        .filter(c -> c.getAbsolutePath().equals(call.getAbsolutePath()))
//                        .count();
//
//                call.setIterator(iterator);
//
//            } else if (lines[i].startsWith(RETURN_TAG)) {
//                String functionPath = lines[i].substring(RETURN_TAG.length());
//
//                FunctionCall call;
//
//                if (functionPath.contains(DELIMITER)) {
//                    String[] paths = functionPath.split("\\Q" + DELIMITER + "\\E");
//                    call = new ConstructorCall();
//                    call.setAbsolutePath(PathUtils.toAbsolute(paths[0]));
//                    ((ConstructorCall) call).setParameterPath(paths[1]);
//                } else {
//                    call = new FunctionCall();
//                    functionPath = PathUtils.toAbsolute(functionPath);
//                    call.setAbsolutePath(functionPath);
//                }
//
//                call.setCategory(Event.Position.LAST);
//                call.setIndex(calledFunctions.size());
//                calledFunctions.add(call);
//            }
//        }

        return calledFunctions;
    }

//    public static List<ICFG> getAllCFG(TestCase testCase) {
//        File testPath = new File(testCase.getTestPathFile());
//
//        ICommonFunctionNode sut = testCase.getRootDataNode().getFunctionNode();
//        INode sourceNode = Utils.getSourcecodeFile(sut);
//
//        List<ICFG> cfgList = new ArrayList<>();
//
//        Search.searchNodes(sourceNode, new AbstractFunctionNodeCondition())
//                .stream()
//                .map(f -> (IFunctionNode) f)
//                .forEach(function -> {
//                    try {
//                        ICFG cfg = getCFG(function, testPath);
//                        if (cfg != null)
//                            cfgList.add(cfg);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//
//        return cfgList;
//    }
//
//    private static ICFG getCFG(IFunctionNode function, File testPath) throws Exception {
//        String content = Utils.readFileContent(testPath);
//        String[] lines = removeRedundantLineBreak(content).split("\\R");
//
//        ICFG cfg = Utils.createCFG(function, Environment.getInstance().getTypeofCoverage());
//
//        // Update the cfg
//        TestpathString_Marker marker = new TestpathString_Marker();
//        marker.setEncodedTestpath(lines);
//
//        CFGUpdaterv2 updater = new CFGUpdaterv2(marker, cfg);
//        updater.updateVisitedNodes();
//
//        return cfg;
//    }
//
//    public static ICFG getCFG(TestCase testCase) {
//        ICommonFunctionNode sut = testCase.getRootDataNode().getFunctionNode();
//
//        if (!(sut instanceof IFunctionNode))
//            return null;
//
//        IFunctionNode function = (IFunctionNode) sut;
//
//        File testPath = new File(testCase.getTestPathFile());
//
//        if (testPath.exists()) {
//            try {
//                return getCFG(function, testPath);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//
//        return null;
//    }
//
//    public static List<Object> getVisited(TestCase testCase, TestCaseCleaner.Scope scope) {
//        List<Object> visited = new ArrayList<>();
//
//        File testPath = new File(testCase.getTestPathFile());
//        if (testPath.exists()) {
//            List<ICFG> cfgList = null;
//            if (scope == TestCaseCleaner.Scope.FUNCTION) {
//                ICFG cfg = getCFG(testCase);
//                if (cfg != null)
//                    cfgList = Collections.singletonList(cfg);
//            } else
//                cfgList = getAllCFG(testCase);
//
//            if (cfgList == null)
//                return null;
//
//            for (ICFG cfg : cfgList) {
//                List<Object> list = cfg.getVisitedStatements().stream()
//                        .map(n -> (Object) n)
//                        .collect(Collectors.toList());
//
//                visited.addAll(list);
//            }
//        }
//
//        return visited;
//    }
//
//    public static List<Object> getVisitedOfCurrentType(TestCase testCase, TestCaseCleaner.Scope scope) {
//        return getVisited(testCase, scope);
//    }

//    /**
//     * Compare visited statement/branch between 2 test cases
//     *
//     * @return integer corresponding comparision
//     *      [0] EQUAL           - equal
//     *      [1] SEPARATED_GT    - tc1 and tc2 doesn't have common part (tc1 >= tc2)
//     *      [2] COMMON_GT       - tc1 and tc2 have common part (tc1 >= tc2)
//     *      [3] CONTAIN_GT      - tc1 contain tc2
//     *      ~ negative value mean tc2 > tc1
//     */
//    public static int compare(TestCase tc1, TestCase tc2) {
//        if (!isExecutable(tc1) || !isExecutable(tc2)) {
//            if (!isExecutable(tc1))
//                return HAVENT_EXEC;
//            else
//                return HAVENT_EXEC * -1;
//        }
//
//        if (!tc1.getFunctionNode().equals(tc2.getFunctionNode()))
//            return ERR_COMPARE;
//
//        List<Object> visited1 = getVisitedOfCurrentType(tc1);
//        List<Object> visited2 = getVisitedOfCurrentType(tc2);
//
//        return compare(visited1, visited2);
//    }

//    public static int compare(TestCase testCase, List<TestCase> other, TestCaseCleaner.Scope scope) {
//        ICommonFunctionNode sut = testCase.getFunctionNode();
//
//        if (!isExecutable(testCase))
//            return HAVENT_EXEC;
//
//        List<Object> visited1 = getVisitedOfCurrentType(testCase, scope);
//        List<Object> visited2 = new ArrayList<>();
//
//        for (TestCase tc : other) {
//            TestCaseCleaner.logger.debug("Comparing test case " + testCase.getName() + " with " + tc.getName());
//            if (!tc.getFunctionNode().equals(sut))
//                return ERR_COMPARE;
//
//            if (isExecutable(tc)) {
//                List<Object> visited = getVisitedOfCurrentType(tc, scope);
//                if (visited != null) {
//                    for (Object node : visited) {
//                        if (!visited2.contains(node))
//                            visited2.add(node);
//                    }
//                }
//            }
//        }
//
//        if (visited2.isEmpty())
//            return HAVENT_EXEC * -1;
//
//        return compare(visited1, visited2);
//    }

    public static int compare(List<Object> visited1, List<Object> visited2) {
        int result;

        if (visited1 == null || visited2 == null)
            return ERR_COMPARE;
        else if (visited1.size() >= visited2.size())
            result = 1;
        else
            result = -1;

        List<Object> commonPart = new ArrayList<>();

        for (Object node1 : visited1) {
            for (Object node2 : visited2) {
                if (node1.equals(node2)) {
                    commonPart.add(node1);
                }
            }
        }

        if (commonPart.isEmpty()) {
            // do nothing
        } else if (commonPart.size() == visited1.size() && commonPart.size() == visited2.size())
            result *= EQUAL;
        else if (commonPart.size() == visited1.size() || commonPart.size() == visited2.size())
            result *= CONTAIN_GT;
        else
            result *= COMMON_GT;

        return result;

    }

//    private static boolean isExecutable(ITestCase testCase) {
//        return testCase.getStatus().equals(ITestCase.STATUS_RUNTIME_ERR)
//                || testCase.getStatus().equals(ITestCase.STATUS_SUCCESS);
//    }

    public static final int EQUAL = 0;
    public static final int SEPARATED_GT = 1;
    public static final int COMMON_GT = 2;
    public static final int CONTAIN_GT = 3;
    public static final int ERR_COMPARE = 4;
    public static final int HAVENT_EXEC = 5;
}
