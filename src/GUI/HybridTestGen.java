package GUI;


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
import coverage.IEnvironmentNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import parser.projectparser.ProjectParser;
import tree.object.INode;
import tree.object.IProjectNode;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\TEST_REPORT.html";

        File htmlFile = new File(path);
        Desktop.getDesktop().browse(htmlFile.toURI());

        Compiler c = getCompiler();

        //for (INode currentSrcFile : Search.searchNodes(Environment.getInstance().getProjectNode(), new SourcecodeFileNodeCondition())) {
//                    UILogger.getUiLogger().log("Compiling " + currentSrcFile.getAbsolutePath());
            //ICompileMessage message = c.compile(currentSrcFile);

//            if (message.getType() == ICompileMessage.MessageType.ERROR) {
//                String error = "Source code file: " + currentSrcFile.getAbsolutePath()
//                        + "\nMESSSAGE:\n" + message.getMessage() + "\n----------------\n";
//                UIController.showDetailDialog(Alert.AlertType.ERROR, "Compilation message", "Compile message", error);
//                return;
//            }
        //}
//        UIController.showSuccessDialog("Compile all source code files successfully"
//                , "Compilation message", "Compile message");


    }
    public Compiler getCompiler() {
//        if (compiler == null) {
        Compiler compiler = new Compiler();

        Compiler compiler = createTemporaryCompiler(cbCompilers.getValue());
        preprocessCmd.setText(compiler.getPreprocessCommand());
        compileCmd.setText(compiler.getCompileCommand());
        tfDefineFlag.setText(compiler.getDefineFlag());
        tfIncludeFlag.setText(compiler.getIncludeFlag());
        tfOutfileFlag.setText(compiler.getOutputFlag());
        tfOutfileExtension.setText(compiler.getOutputExtension());
        tfLinkCommand.setText(compiler.getLinkCommand());
        tfDebugCommand.setText(compiler.getDebugCommand());

        compiler.setCompileCommand(envNode.getCompileCmd());
        compiler.setPreprocessCommand(envNode.getPreprocessCmd());
        compiler.setLinkCommand(envNode.getLinkCmd());
        compiler.setDebugCommand(envNode.getDebugCmd());
        compiler.setIncludeFlag(envNode.getIncludeFlag());
        compiler.setDefineFlag(envNode.getDefineFlag());
        compiler.setOutputFlag(envNode.getOutputFlag());
        compiler.setDebugFlag(envNode.getDebugFlag());
        compiler.setOutputExtension(envNode.getOutputExt());

        return compiler;
    }

    private Compiler createTemporaryCompiler(String opt) {
        if (opt != null) {
            for (Class<?> c : AvailableCompiler.class.getClasses()) {
                try {
                    String name = c.getField("NAME").get(null).toString();

                    if (name.equals(opt))
                        return new Compiler(c);
                } catch (Exception ex) {
                }
            }
        }

        return null;
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

            console.exportToHtml(new File(AbstractSetting.getValue(Settingv2.TEST_REPORT)+".html"), value);

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

        bGen.generateTestData();

        JOptionPane.showMessageDialog(null, "Finish generating data. Click on [View report] " +
                "for the result.", DSEConstants.PRODUCT_NAME, JOptionPane.INFORMATION_MESSAGE);
    }
}