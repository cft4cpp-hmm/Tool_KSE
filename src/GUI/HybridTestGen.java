package GUI;

import HybridAutoTestGen.TestData;
import Common.TestConfig;
import com.google.gson.JsonObject;
import compiler.AvailableCompiler;
import Common.DSEConstants;
import HybridAutoTestGen.CFT4CPP;
import HybridAutoTestGen.FullBoundedTestGen;
import HybridAutoTestGen.HybridAutoTestGen;
import HybridAutoTestGen.WeightedCFGTestGEn;
import compiler.AvailableCompiler;
import compiler.message.ICompileMessage;
import compiler.Compiler;
import config.AbstractSetting;
import config.Settingv2;
import console.Console;
import coverage.FunctionCoverageComputation;
import coverage.IEnvironmentNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import parser.projectparser.ICommonFunctionNode;
import parser.projectparser.ProjectParser;
import project_init.ProjectClone;
import testcase_execution.TestcaseExecution;
import testcase_manager.ITestCase;
import testcase_manager.TestCase;
import tree.object.IFunctionNode;
import tree.object.INode;
import tree.object.IProjectNode;
import utils.DateTimeUtils;
import utils.PathUtils;
import utils.SpecialCharacter;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;
import utils.search.SourcecodeFileNodeCondition;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HybridTestGen extends Component
{
    public ComboBox cboSelectedFunction;
    public Button btnGetFunctionList;
    public Button btnBVTG;
    public Button btnSTCFG;
    public Button btnWCFT;
    /**
     * Represent control flow graph
     */
    public IProjectNode projectNode;

    @FXML
    public Button btnBrowseInput;
    @FXML
    public TextField txtMaxLoop;
    @FXML
    public Button btnGenerateTestData;
    @FXML
    public TextField txtSourceFolder;

    @FXML
    public CheckBox chkSolvePathWhenGenBoundaryTestData;
    public Button btnRunTest;

    @FXML
    protected void btnGetFunctionList_Clicked(ActionEvent event) throws Exception
    {
        ProjectParser parser = new ProjectParser(new File(txtSourceFolder.getText()));

        projectNode = parser.getRootTree();

        List<INode> functionList = Search.getAllNodes(projectNode, new FunctionNodeCondition());

        if (functionList.size() > 0)
        {
            for (INode function : functionList)
            {
                cboSelectedFunction.getItems().add(new FunctionComboItem(function.getName(), function.getName()));
            }

            cboSelectedFunction.getSelectionModel().select(0);
        }
    }

    @FXML
    protected void btnViewReport_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnViewReport_Clicked started");
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\TEST_REPORT.html";

        File htmlFile = new File(path);
        Desktop.getDesktop().browse(htmlFile.toURI());
    }

    @FXML
    protected void btnRunTest_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnRunTest_Clicked started");

//        List<TestData> testDataList = new ArrayList<>();
//        TestData testData = new TestData();
//        testData.add(new Pair<>("averageGrade", 95));
//
//        testDataList.add(testData);
//
//
//        TestData testData1= new TestData();
//        testData1.add(new Pair<>("averageGrade", 63));
//
//        testDataList.add(testData1);

//        String file = "F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2\\test.cpp";
//
//        Compiler c = getCompiler();
//
//        ICompileMessage message = c.compile(file);
//
//        if (message.getType() == ICompileMessage.MessageType.ERROR)
//        {
//            String error = "Source code file: "
//                    + "\nMESSSAGE:\n" + message.getMessage() + "\n----------------\n";
//            JOptionPane.showMessageDialog(null, "Error: " + error, DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }

//        TestCase testCase = new TestCase();
//        testCase.setTestData(testData);
//        testCase.setName(TestConfig.TESTCASE_NAME + i);
//        testCase.setFunctionNode(function);
//        testCase.setSourcecodeFile(sourceFile);
//        testCase.setRealParentSourceFileName(sourceFileName);
//        executor.setTestCase(testCase);
//
//        executor.execute();
    }


    @FXML
    protected void btnBrowseInput_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnBrowseInput_Clicked started");
        JFileChooser _fileChooser = new JFileChooser();
        _fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\Sample_for_R1_2";

        _fileChooser.setSelectedFile(new File(path));
        if (_fileChooser.showDialog(this, "Choose folder") == JFileChooser.APPROVE_OPTION)
        {
            String selectedPath = _fileChooser.getSelectedFile().getAbsolutePath();

            txtSourceFolder.setText(selectedPath);
        }
    }

    @FXML
    protected void btnWCFT_Clicked(ActionEvent event) throws Exception
    {
        try
        {
            int maxloop = 1;
            try
            {
                maxloop = Integer.parseInt(txtMaxLoop.getText());
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(null, "Maxloop is invalid",
                        DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
            }
            String value = "";

            if (cboSelectedFunction.getValue() == null)
            {
                JOptionPane.showMessageDialog(null, "Please click on [Get function list] button, then choose a function to generate test data", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
                return;
            }
            value = cboSelectedFunction.getValue().toString();

            WeightedCFGTestGEn weightedCFGTestGEn = new WeightedCFGTestGEn(value, maxloop, txtSourceFolder.getText());
            weightedCFGTestGEn.run();



            FunctionCoverageComputation functionCoverageComputation = Utils.ExecuteTestCase(txtSourceFolder.getText(), value, weightedCFGTestGEn.getTestDataList());

            weightedCFGTestGEn.setFunctionCoverageComputation(functionCoverageComputation);

            weightedCFGTestGEn.ExportReport("WCFT4Cpp");

            JOptionPane.showMessageDialog(null, "Finish generating data. Click on [View report] " +
                    "for the result.", DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    @FXML
    protected void btnSTCFG_Clicked(ActionEvent event) throws Exception
    {
        try
        {
            int maxloop = 1;
            try
            {
                maxloop = Integer.parseInt(txtMaxLoop.getText());
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(null, "Maxloop is invalid",
                        DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
            }
            String value = "";

            if (cboSelectedFunction.getValue() == null)
            {
                JOptionPane.showMessageDialog(null, "Please click on [Get function list] button, then choose a function to generate test data", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
                return;
            }
            value = cboSelectedFunction.getValue().toString();

            CFT4CPP cft4cpp = new CFT4CPP(null, maxloop, txtSourceFolder.getText(), value);

            cft4cpp.run();

            JOptionPane.showMessageDialog(null, "Finish generating data. Click on [View report] " +
                    "for the result.", DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    @FXML
    protected void btnBVTG_Clicked(ActionEvent event) throws Exception
    {
        try
        {
            int maxloop = 1;
            try
            {
                maxloop = Integer.parseInt(txtMaxLoop.getText());
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(null, "Maxloop is invalid",
                        DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
            }
            String value = "";

            if (cboSelectedFunction.getValue() == null)
            {
                JOptionPane.showMessageDialog(null, "Please click on [Get function list] button, then choose a function to generate test data", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
                return;
            }
            value = cboSelectedFunction.getValue().toString();

            FullBoundedTestGen bGen = new FullBoundedTestGen(null, maxloop, value, txtSourceFolder.getText());

            bGen.boundaryValueTestGen();

            JOptionPane.showMessageDialog(null, "Finish generating data. Click on [View report] " +
                    "for the result.", DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    @FXML
    protected void btnConcolic_Clicked(ActionEvent event) throws Exception
    {
        try
        {
            int maxloop = 1;
            try
            {
                maxloop = Integer.parseInt(txtMaxLoop.getText());
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(null, "Maxloop is invalid",
                        DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
            }
            String value = "";

            if (cboSelectedFunction.getValue() == null)
            {
                JOptionPane.showMessageDialog(null, "Please click on [Get function list] button, then choose a function to generate test data", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
                return;
            }
            value = cboSelectedFunction.getValue().toString();

            Console console = new Console(value);

            console.exportToHtml(new File(AbstractSetting.getValue(Settingv2.TEST_REPORT) + ".html"), value);

            JOptionPane.showMessageDialog(null, "Finish generating data. Click on [View report] " +
                    "for the result.", DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void initialize()
    {
        // initialization code here...
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\Sample_for_R1_2";

        txtSourceFolder.setText(path);
    }

    @FXML
    protected void btnHybrid_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnGenerateTestData_Clicked started");
        int maxloop = 1;
        try
        {
            maxloop = Integer.parseInt(txtMaxLoop.getText());
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, "Maxloop is invalid", DSEConstants.PRODUCT_NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
        String value = "";

        if (cboSelectedFunction.getValue() == null)
        {
            JOptionPane.showMessageDialog(null, "Please click on [Get function list] button, then choose a function to generate test data", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR_MESSAGE);
            return;
        }
        value = cboSelectedFunction.getValue().toString();

        //generateTestData(maxloop, value, txtSourceFolder.getText());

        HybridAutoTestGen bGen = new HybridAutoTestGen(maxloop, value, txtSourceFolder.getText(), 1);

        boolean checked = chkSolvePathWhenGenBoundaryTestData.isSelected();

        bGen.setSolvePathWhenGenBoundaryTestData(checked);

        float boundStep = 1;

        bGen.generateTestData(boundStep);



        FunctionCoverageComputation functionCoverageComputation = Utils.ExecuteTestCase(txtSourceFolder.getText(), value, bGen.testCases);

        bGen.setFunctionCoverageComputation(functionCoverageComputation);

        bGen.ExportReport();

        JOptionPane.showMessageDialog(null, "Finish generating data. Click on [View report] " +
                "for the result.", DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
    }
}