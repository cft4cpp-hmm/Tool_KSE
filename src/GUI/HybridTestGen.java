package GUI;

import Common.DSEConstants;
import HybridAutoTestGen.CFT4CPP;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HybridTestGen extends Component
{
    @FXML
    public Button btnBrowseInput;
    @FXML
    public TextField txtMaxLoop;
    @FXML
    public Button btnGenerateTestData;
    @FXML
    public TextField txtSourceFolder;

    @FXML
    protected void btnGenerateTestData_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnTest_Clicked started");
        int maxloop = 1;
        try
        {
            maxloop = Integer.parseInt(txtMaxLoop.getText());
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(null, "Maxloop is invalid", DSEConstants.PRODUCT_NAME, JOptionPane.ERROR);
        }

        CFT4CPP tpGen = new CFT4CPP(null, maxloop, txtSourceFolder.getText(), "sum(int,int)");
        tpGen.run();
    }
    @FXML
    protected void btnViewReport_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnViewReport_Clicked started");
//        CFT4CPP tpGen = new CFT4CPP(null, 1, "sum(int,int)");
//        tpGen.run();
    }

    @FXML
    protected void btnBrowseInput_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnBrowseInput_Clicked started");
        JFileChooser _fileChooser = new JFileChooser();
        _fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);

        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\Sample_for_R1_2";

        _fileChooser.setSelectedFile(new File(path));
        if (_fileChooser.showDialog(this, "Choose folder") == JFileChooser.APPROVE_OPTION)
        {
            String selectedPath = _fileChooser.getSelectedFile().getAbsolutePath();

            txtSourceFolder.setText(selectedPath);
        }
    }
}
