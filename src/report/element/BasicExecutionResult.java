package report.element;

import com.dse.coverage.TestPathUtils;
import com.dse.coverage.function_call.ConstructorCall;
import com.dse.coverage.function_call.FunctionCall;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_execution.result_trace.ResultTrace;
import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.INode;
import com.dse.search.Search2;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.SubprogramNode;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.List;

public class BasicExecutionResult extends AbstractExecutionResult {

    private static final int MAX_DUPLICATE = 3;
    private static final boolean SKIP_DUPLICATE = false;

    public BasicExecutionResult(ITestCase testCase, Execution execution) {
        super(testCase, execution);
    }

    @Override
    protected void generate(ITestCase tc, Execution execution) {
        TestCase testCase = (TestCase) tc;

        Line sectionTitle = new Line(testCase.getName() + " Execution Result", COLOR.DARK);
        title.add(sectionTitle);

        String testPath = testCase.getTestPathFile();

        if (testPath == null || !new File(testPath).exists()) {
            body.add(new Line("Test case haven't executed yet", COLOR.LIGHT));

        } else
            generateCPP(testCase, execution);

        body.add(new BlankLine());
    }

    private static final String EMPTY_COVERAGE = "<pre>\n...\n...\n...\n...\n</pre>\n";

    private void generateCoverageHighlight(TestCase testCase) {
        body.add(new Section.CenteredLine("Coverage Highlight", COLOR.MEDIUM));

        String typeOfCoverage = Environment.getInstance().getTypeofCoverage();
        switch (typeOfCoverage){
            case EnviroCoverageTypeNode.STATEMENT:
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.BASIS_PATH:
            case EnviroCoverageTypeNode.MCDC:{
                String coverageHighlight = Utils.readFileContent(testCase.getHighlightedFunctionPath(typeOfCoverage));
                if (coverageHighlight.equals(EMPTY_COVERAGE))
                    body.add(new Section.CenteredLine("Cannot instrument this function", COLOR.WHITE));
                else
                    body.add(new CodeView(coverageHighlight));

                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH:{
                String stmCoverageHighlight = Utils.readFileContent(testCase.getHighlightedFunctionPath(EnviroCoverageTypeNode.STATEMENT));
                String branchCoverageHighlight = Utils.readFileContent(testCase.getHighlightedFunctionPath(EnviroCoverageTypeNode.BRANCH));

                if (stmCoverageHighlight.equals(EMPTY_COVERAGE) && branchCoverageHighlight.equals(EMPTY_COVERAGE))
                    body.add(new Section.CenteredLine("Cannot instrument this function", COLOR.WHITE));
                else {
                    body.add(new CodeView(stmCoverageHighlight));
                    body.add(new CodeView(branchCoverageHighlight));
                }

                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC:{
                String stmCoverageHighlight = Utils.readFileContent(testCase.getHighlightedFunctionPath(EnviroCoverageTypeNode.STATEMENT));
                String mcdcCoverageHighlight = Utils.readFileContent(testCase.getHighlightedFunctionPath(EnviroCoverageTypeNode.MCDC));

                if (stmCoverageHighlight.equals(EMPTY_COVERAGE) && mcdcCoverageHighlight.equals(EMPTY_COVERAGE))
                    body.add(new Section.CenteredLine("Cannot instrument this function", COLOR.WHITE));
                else {
                    body.add(new CodeView(stmCoverageHighlight));
                    body.add(new CodeView(mcdcCoverageHighlight));
                }

                break;
            }
        }
    }

    private void generateCPP(TestCase testCase, Execution execution) {
        generateCoverageHighlight(testCase);
        boolean pass = generateEventsSection(testCase);

        File testPath = new File(testCase.getTestPathFile());

        String result = Execution.RUNTIME_ERROR;

        double executedTime = testCase.getExecutedTime();

        if (execution != null) {
            String testCaseName = testCase.getName();
            executedTime = execution.getTestCaseByName(testCaseName).getTime();
        }

        if (testPath.exists()) {
            String testPathContent = Utils.readFileContent(testPath);
            if (testPathContent.contains(TestPathUtils.END_TAG)) {
                result = pass ? Execution.PASSED : Execution.FAILED;
            }
        }

        Table overall = generateOverallResultsTable(testCase, result, executedTime);
        body.add(0, overall);
    }

    private boolean generateEventsSection(TestCase testCase) {
        List<IResultTrace> failures = ResultTrace.load(testCase);

        List<FunctionCall> calledFunctions = TestPathUtils
                .traceFunctionCall(testCase.getTestPathFile());

        List<IDataNode> subprograms = Search2
                .searchNodes(testCase.getRootDataNode(), new SubprogramNode());

        int skip = 0;
        String firstLine = Utils.readFileContent(testCase.getTestPathFile()).split("\\R")[0];
        if (firstLine.startsWith(TestPathUtils.SKIP_TAG))
            skip = Integer.parseInt(firstLine.substring(TestPathUtils.SKIP_TAG.length()));

        /*
         * Result PASS/ALL
         */
        AssertionResult results = new AssertionResult();

        int duplicate = 0;
        FunctionCall prev = null;

        Event.Position position;

//        for (int i = 0; i < calledFunctions.size() && position != Event.Position.LAST; i++) {
        for (int i = 0; i < calledFunctions.size(); i++) {
            FunctionCall current = calledFunctions.get(i);
            SubprogramNode subprogram = findSubprogram(subprograms, current);
            position = current.getCategory();

            if (prev != null && prev.equals(current))
                duplicate++;
            else
                duplicate = 0;

            // skip current event if access the same subprogram as previous
            if (SKIP_DUPLICATE && duplicate >= MAX_DUPLICATE)
                continue;

            int index = i + skip + 1;

            if (subprogram != null && Utils.getSourcecodeFile(subprogram.getFunctionNode()) != null) {
                Event event;

                if (position == Event.Position.MIDDLE && current.getIterator() > 1)
                    event = new Event(subprogram, failures, index, position, current.getIterator());
                else
                    event = new Event(subprogram, failures, index, position);

                results.append(event.getResults());

                body.add(event);
            }

            prev = calledFunctions.get(i);
        }

        testCase.setExecutionResult(results);

        String textColor = results.isAllPass() ? "green" : "red";
        double passPercent = results.getPercent();

        Table resultsSummary = new Table();
        resultsSummary.getRows().add(
            new Table.Row(
                new Text(String.format("Expected Results matched %.2f%%", passPercent)),
                new Text(String.format("(%d/%d) PASS", results.getPass(), results.getTotal()), TEXT_STYLE.BOLD, textColor)
            )
        );

        body.add(resultsSummary);

        return results.isAllPass();
    }

    private SubprogramNode findSubprogram(List<IDataNode> subprograms, FunctionCall call) {
        for (IDataNode subprogram : subprograms) {
            if (subprogram instanceof SubprogramNode) {
                INode functionNode = ((SubprogramNode) subprogram).getFunctionNode();

                if (call instanceof ConstructorCall) {
                    if (functionNode.getAbsolutePath().equals(call.getAbsolutePath())
                            && subprogram.getPathFromRoot().equals(((ConstructorCall) call).getParameterPath()))
                        return (SubprogramNode) subprogram;
                } else {
                    if (functionNode.getAbsolutePath().equals(call.getAbsolutePath()))
                        return (SubprogramNode) subprogram;
                }
            }
        }

        try {
            INode called = UIController.searchFunctionNodeByPath(call.getAbsolutePath());
            return new SubprogramNode(called);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private Table generateOverallResultsTable(TestCase testCase, String result, double executedTime) {
        Text resultText = new Text(result, TEXT_STYLE.BOLD, result.equals(Execution.PASSED) ? "green" : "red");

        String coveragePercentage = SpecialCharacter.EMPTY;
        String typeOfCoverage = Environment.getInstance().getTypeofCoverage();
        switch (typeOfCoverage) {
            case EnviroCoverageTypeNode.BRANCH:
            case EnviroCoverageTypeNode.STATEMENT:
            case EnviroCoverageTypeNode.MCDC:
            case EnviroCoverageTypeNode.BASIS_PATH: {
                coveragePercentage = getCoveragePercentage(testCase, typeOfCoverage);
                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
                String statementCoveragePercent = getCoveragePercentage(testCase, EnviroCoverageTypeNode.STATEMENT)
                        + " (" + EnviroCoverageTypeNode.STATEMENT + "); ";

                String branchCoveragePercent = getCoveragePercentage(testCase, EnviroCoverageTypeNode.BRANCH)
                        + " (" + EnviroCoverageTypeNode.BRANCH + ");";

                coveragePercentage = statementCoveragePercent + branchCoveragePercent;

                break;
            }

            case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
                String statementCoveragePercent = getCoveragePercentage(testCase, EnviroCoverageTypeNode.STATEMENT)
                        + " (" + EnviroCoverageTypeNode.STATEMENT + "); ";

                String mcdcCoveragePercent = getCoveragePercentage(testCase, EnviroCoverageTypeNode.MCDC)
                        + " (" + EnviroCoverageTypeNode.MCDC + ");";

                coveragePercentage = statementCoveragePercent + mcdcCoveragePercent;

                break;
            }
        }

        Table overall = new Table();

        overall.getRows().add(new Table.Row(new Table.Cell<Text>("Result:"), new Table.Cell<>(resultText)));
        overall.getRows().add(new Table.Row("Coverage:", String.format("%s", coveragePercentage)));

        if (executedTime >= 0)
                overall.getRows()
                        .add(new Table.Row("Executed Time:", String.format("%.3fs", executedTime)));

        return overall;
    }

    private String getCoveragePercentage(TestCase testCase, String coverageType) {
        String coveragePercentage = null;

        String path = testCase.getProgressCoveragePath(coverageType);
        String content = Utils.readFileContent(path);
        JsonElement json = new JsonParser().parse(content);

        if (json != null && !(json instanceof JsonNull)) {
            double percentage = json.getAsJsonObject().get(coverageType).getAsDouble();
            coveragePercentage = Utils.round(percentage * 100, 2) + "%";
        }

        return coveragePercentage;
    }
}
