package GUI;

import HybridAutoTestGen.CFT4CPP;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class HybridTestGen
{
    @FXML
    Button btnTest;

    @FXML
    protected void btnGenerateTestData_Clicked(ActionEvent event) throws Exception
    {
        System.out.println("btnTest_Clicked started");
        CFT4CPP tpGen = new CFT4CPP(null, 1, "sum(int,int)");
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

    }
}
