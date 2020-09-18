package testcase_manager.optimize;

import com.dse.coverage.TestPathUtils;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testcasescript.object.TestNewNode;
import com.dse.util.AkaLogger;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestCaseCleaner {

    public static final AkaLogger logger = AkaLogger.get(TestCaseCleaner.class);

    public enum Scope {
        FUNCTION,
        SOURCE
    }

    public static List<TestCase> optimize(List<TestCase> testCases, Scope scope) {
        List<TestCase> optimizes = new ArrayList<>(testCases);

        int index = 0;

        while (index < optimizes.size()) {
            TestCase testCase = optimizes.get(index);
            List<TestCase> others = optimizes.stream()
                    .filter(tc -> !tc.equals(testCase))
                    .collect(Collectors.toList());

            boolean stop = false;

            logger.debug("Comparing test case " + testCase.getName() + " with others");
            int comparison = TestPathUtils.compare(testCase, others, scope);

            switch (Math.abs(comparison)) {
                case TestPathUtils.EQUAL:
                    optimizes.removeAll(others);
                    stop = true;
                    break;
                case TestPathUtils.HAVENT_EXEC:
                case TestPathUtils.SEPARATED_GT:
                case TestPathUtils.COMMON_GT:
                    index++;
                    break;
                case TestPathUtils.CONTAIN_GT:
                    // test case contains all others
                    if (comparison > 0) {
                        optimizes.removeAll(others);
                        stop = true;
                    }
                    // others contains current test case
                    // then remove it from list
                    else
                        optimizes.remove(testCase);

                    break;
                case TestPathUtils.ERR_COMPARE:
                    stop = true;
                    logger.error("Something wrong between " + testCase.getName() + " and the others");
                    break;
            }

            if (stop)
                break;
        }

        return optimizes;
    }

    // Grouping test case by subprogram under test
    private static Map<ICommonFunctionNode, List<TestCase>> groupTestCaseByFunction(List<ITestCase> testCases) {
        Map<ICommonFunctionNode, List<TestCase>> map = new HashMap<>();

        for (ITestCase testCase : testCases) {
            if (testCase instanceof TestCase) {
                ICommonFunctionNode sut = ((TestCase) testCase).getFunctionNode();

                List<TestCase> list = map.get(sut);
                if (list == null)
                    list = new ArrayList<>();

                if (!list.contains(testCase))
                    list.add((TestCase) testCase);

                map.put(sut, list);
            }
        }

        return map;
    }

    public static void clean(List<ITestCase> testCases, Scope scope) {
        logger.debug("Grouping test case by subprogram");
        Map<ICommonFunctionNode, List<TestCase>> group = groupTestCaseByFunction(testCases);

        TreeItem<ITestcaseNode> tiRoot = TestCasesNavigatorController
                .getInstance().getTestCasesNavigator().getRoot();
        ITestcaseNode tcRoot = tiRoot.getValue();

        for (Map.Entry<ICommonFunctionNode, List<TestCase>> entry : group.entrySet()) {
            List<TestCase> list = entry.getValue();
            List<TestCase> optimizes = optimize(list, scope);
            List<TestCase> unnecessary = list.stream()
                    .filter(tc -> !optimizes.contains(tc))
                    .collect(Collectors.toList());

            for (TestCase testCase : unnecessary) {
                String name = testCase.getName();

                TestNewNode node = TestcaseSearch.getFirstTestNewNodeByName(tcRoot, name);

                if (node != null) {
                    TestCasesTreeItem treeItem = TestcaseSearch
                            .searchTestCaseTreeItem((TestCasesTreeItem) tiRoot, node);

                    TestCasesNavigatorController.getInstance().deleteTestCase(node, treeItem);
                }
            }
        }
    }

    public static List<ITestCase> backgroundClean(List<ITestCase> testCases) {
        Map<ICommonFunctionNode, List<TestCase>> group = groupTestCaseByFunction(testCases);

        TreeItem<ITestcaseNode> tiRoot = TestCasesNavigatorController
                .getInstance().getTestCasesNavigator().getRoot();
        ITestcaseNode tcRoot = tiRoot.getValue();

        List<ITestCase> origin = new ArrayList<>(testCases);

        for (Map.Entry<ICommonFunctionNode, List<TestCase>> entry : group.entrySet()) {
            List<TestCase> list = entry.getValue();
            List<TestCase> optimizes = optimize(list, Scope.SOURCE);
            List<TestCase> unnecessary = list.stream()
                    .filter(tc -> !optimizes.contains(tc))
                    .collect(Collectors.toList());

            for (TestCase testCase : unnecessary) {
                origin.remove(testCase);
                testCase.deleteOldData();

                String name = testCase.getName();

                TestNewNode node = TestcaseSearch.getFirstTestNewNodeByName(tcRoot, name);
                TestCasesTreeItem treeItem = TestcaseSearch
                        .searchTestCaseTreeItem((TestCasesTreeItem) tiRoot, node);

                assert node != null;
                TestCasesNavigatorController.getInstance().deleteTestCase(node, treeItem);
            }
        }

        return origin;
    }
}
