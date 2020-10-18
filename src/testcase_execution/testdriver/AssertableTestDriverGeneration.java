package testcase_execution.testdriver;

import Common.TestConfig;
import project_init.IGTestConstant;
import testcase_manager.TestCase;
import testdata.object.RootDataNode;
import testdatagen.module.Search2;
import tree.object.ConstructorNode;
import utils.SpecialCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AssertableTestDriverGeneration{ //extends TestDriverGeneration {

//    protected String generateBodyScript(TestCase testCase) throws Exception {
//        // STEP 1: assign aka test case name
//        String testCaseNameAssign = String.format("%s=\"%s\";", TestConfig.UET_TEST_CASE_NAME, testCase.getName());
//
//        // STEP 2: Generate initialization of variables
//        String initialization = generateInitialization(testCase);
//
//        // STEP 3: Generate full function call
//        String functionCall = generateFunctionCall(testCase);
//
//        // STEP 4: FCALLS++ - Returned from UUT
//        String increaseFcall;
//        if (testCase.getFunctionNode() instanceof ConstructorNode)
//            increaseFcall = SpecialCharacter.EMPTY;
//        else
//            increaseFcall = IGTestConstant.INCREASE_FCALLS + generateReturnMark(testCase);
//
//        // STEP 5: Generation assertion actual & expected values
//
//        // STEP 6: Repeat iterator
//        String singleScript = String.format(
//                    "{\n" +
//                        "%s\n" +
//                        "%s\n" +
//                        "%s\n" +
//                        "%s\n" +
//                        //"%s\n" +
//                        //"%s\n" +
//                        //"%s\n" +
//                    "}",
//                testCaseNameAssign,
//                //testCase.getTestCaseUserCode().getSetUpContent(),
//                initialization,
//                functionCall,
//                increaseFcall
//                //testCase.getTestCaseUserCode().getTearDownContent()
//                );
//
////        StringBuilder script = new StringBuilder();
////        for (int i = 0; i < iterator; i++)
////            script.append(singleScript).append(SpecialCharacter.LINE_BREAK);
//
//        // STEP 7: mark beginning and end of test case
////        script = new StringBuilder(wrapScriptInMark(testCase, script.toString()));
////        script = new StringBuilder(wrapScriptInTryCatch(script.toString()));
////
////        return script.toString();
//        singleScript = wrapScriptInTryCatch(singleScript);
//
//        return singleScript;
//    }


}
