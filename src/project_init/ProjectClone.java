package project_init;
import instrument.AbstractFunctionInstrumentation;
import instrument.DriverConstant;
import instrument.FunctionInstrumentationForAllCoverages;
import org.eclipse.cdt.core.dom.ast.*;
import parser.projectparser.ICommonFunctionNode;
import tree.object.*;
import utils.PathUtils;
import utils.SpecialCharacter;
import utils.Utils;
import utils.search.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectClone {
    public static final String CLONED_FILE_EXTENSION = ".uetignore";
    public static final String MAIN_REFACTOR_NAME = "UET_MAIN";

    private static List<String> sLibraries;

    public static void cloneASourceCodeFile(INode sourceCode){
        if (!(sourceCode instanceof SourcecodeFileNode))
            return;
        ProjectClone clone = new ProjectClone();

        try {
            String newContent = clone.generateFileContent(sourceCode);
            Utils.writeContentToFile(newContent, getClonedFilePath(sourceCode.getAbsolutePath()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private final Map<String, String> refactors = new HashMap<>();

    List<String> globalDeclarations = new ArrayList<>();

    public String generateFileContent(INode sourceCode) throws InterruptedException {
        String oldContent = Utils.readFileContent(sourceCode.getAbsolutePath());

        List<SearchCondition> conditions = new ArrayList<>();
        conditions.add(new IncludeHeaderNodeCondition());
        conditions.add(new AbstractFunctionNodeCondition());
        conditions.add(new DefinitionFunctionNodeCondition());

        List<INode> redefines = Search.searchNodes(sourceCode, conditions);

        int size = redefines.size();

        ExecutorService es = Executors.newFixedThreadPool(5);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (INode child : redefines) {
            Callable<Void> c = () -> {

                int index = redefines.indexOf(child);

                if (child instanceof IFunctionNode) {
                    IFunctionNode function = (IFunctionNode) child;
                    refactorFunction(function);
                }

                return null;
            };
            tasks.add(c);
        }

        es.invokeAll(tasks);

        for (Map.Entry<String, String> entry : refactors.entrySet()) {
            String prev = entry.getKey();
            String newC = entry.getValue();

            oldContent = oldContent.replace(prev, newC);
        }

        for (String globalDeclaration : globalDeclarations)
            oldContent = oldContent.replace("#endif\n\n" + globalDeclaration, "#endif");

        String defineSourceCodeName = IGTestConstant.SRC_PREFIX + sourceCode.getAbsolutePath().toUpperCase()
                .replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE);

        return wrapInIncludeGuard(defineSourceCodeName, oldContent);
    }

    private void refactorFunction(IFunctionNode function) {
        if (function instanceof AbstractFunctionNode) {
            IASTNode functionAST = null;
            if (function.getName().contains("ArrayCmp"))
                System.out.println();
            if (function instanceof AbstractFunctionNode) {
                functionAST = ((AbstractFunctionNode) function).getAST();
            }

            String oldFunctionCode = functionAST.getRawSignature();

            // generate instrumented function content
            String newFunctionCode = "";
            if (function instanceof IFunctionNode)
                newFunctionCode = generateInstrumentedFunction((IFunctionNode) function);

            // change main function to AKA_MAIN
            if (function.getSingleSimpleName().equals("main")) {
                newFunctionCode = refactorMain(function);
            }

            refactors.put(oldFunctionCode, newFunctionCode);
        }
    }

    /**
     * Ex: int main() -> int UET_MAIN()
     *
     * @param main       function node
     * @return new source code file
     */
    private String refactorMain(IFunctionNode main) {
        String oldMain = "";

        if (main instanceof DefinitionFunctionNode) {
            oldMain = ((DefinitionFunctionNode) main).getAST().getRawSignature();
        } else if (main instanceof FunctionNode) {
            oldMain = ((FunctionNode) main).getAST().getRawSignature();
        }

        return oldMain.replaceAll("\\bmain\\b", MAIN_REFACTOR_NAME);
    }

    public static int getStartLineNumber(ICommonFunctionNode functionNode) {
        INode sourceNode = Utils.getSourcecodeFile(functionNode);

        String clonedFilePath = getClonedFilePath(sourceNode.getAbsolutePath());

        String clonedFile = Utils.readFileContent(clonedFilePath);

        if (functionNode instanceof IFunctionNode) {
            IASTFunctionDefinition functionDefinition = ((IFunctionNode) functionNode).getAST();

            String rawSource = functionDefinition.getRawSignature();

            int openIndex = rawSource.indexOf('{') + 1;

            String declaration = rawSource.substring(0, openIndex)
                    .replaceAll("\\bmain\\b", "AKA_MAIN")
                    .replaceAll("\\s+\\{","{");

            int offsetInClonedFile = clonedFile.indexOf(declaration);

            return (int) clonedFile
                    .substring(0, offsetInClonedFile)
                    .chars()
                    .filter(c -> c == '\n')
                    .count();
        }  else if (functionNode instanceof DefinitionFunctionNode) {
            IASTNode ast = ((DefinitionFunctionNode) functionNode).getAST();
            return ast.getFileLocation().getStartingLineNumber();

        }

        return 0;
    }
    public static String getClonedFilePath(String origin) {
        String originName = new File(origin).getName();

        int lastDotPos = originName.lastIndexOf(SpecialCharacter.DOT);

        String clonedName = originName.substring(0, lastDotPos) + CLONED_FILE_EXTENSION + originName.substring(lastDotPos);

        return origin.replace(originName, clonedName);
    }
    private String guardIncludeHeader(INode child) {
        if (child instanceof IncludeHeaderNode) {
            String oldIncludeHeader = ((IncludeHeaderNode) child).getAST().getRawSignature();

            String header = child.getName().replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE).toUpperCase();

            return wrapInIncludeGuard(IGTestConstant.INCLUDE_PREFIX + header, oldIncludeHeader);
        }

        return null;
    }

    private void guardGlobalDeclaration(INode child) {
        IASTNodeLocation[] tempAstLocations = ((ExternalVariableNode) child).getASTType().getNodeLocations();
        if (tempAstLocations.length > 0) {
            IASTNodeLocation astNodeLocation = tempAstLocations[0];
            if (astNodeLocation instanceof IASTCopyLocation) {
                IASTNode declaration = ((IASTCopyLocation) astNodeLocation).getOriginalNode().getParent();
                if (declaration instanceof IASTDeclaration) {
                    String originDeclaration = declaration.getRawSignature();

                    if (!globalDeclarations.contains(originDeclaration))
                        globalDeclarations.add(originDeclaration);

                    String oldDeclaration = ((ExternalVariableNode) child).getASTType().getRawSignature() + " "
                            + ((ExternalVariableNode) child).getASTDecName().getRawSignature() + ";";

                    String header = child.getAbsolutePath();

                    if (header.startsWith(File.separator))
                        header = header.substring(1);

                    header = header.replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE).toUpperCase();

                    String newDeclaration = wrapInIncludeGuard(IGTestConstant.GLOBAL_PREFIX + header, oldDeclaration);

                    refactors.put(originDeclaration, newDeclaration + "\n" + originDeclaration);
                }
            }
        }
    }

    public static String wrapInIncludeGuard(String name, String content) {
        return String.format("/** Guard statement to avoid multiple declaration */\n" +
                "#ifndef %s\n#define %s\n%s\n#endif\n", name, name, content);
    }

    /**
     * Change all private and protected labels in source code to public
     */
    private String refactorWhiteBox(String oldContent) {
        oldContent = oldContent.replaceAll("\\bprivate\\b", "public");
        oldContent = oldContent.replaceAll("\\bprotected\\b", "public");
        oldContent = oldContent.replaceAll("\\bstatic \\b", SpecialCharacter.EMPTY);

        return oldContent;
    }

    /**
     * Perform on instrumentation on the original function
     */
    private String generateInstrumentedFunction(IFunctionNode functionNode) {
        final String success = String.format("/** Instrumented function %s */\n", functionNode.getName());
        final String fail = String.format("/** Can not instrument function %s */\n", functionNode.getName());

        String instrumentedSourceCode;

        IASTFunctionDefinition astInstrumentedFunction = functionNode.getAST();
        AbstractFunctionInstrumentation fnInstrumentation = new FunctionInstrumentationForAllCoverages(
                astInstrumentedFunction, functionNode);

        fnInstrumentation.setFunctionPath(functionNode.getAbsolutePath());

        String instrument = fnInstrumentation.generateInstrumentedFunction();
        if (instrument == null || instrument.length() == 0){
            // can not instrument
            instrumentedSourceCode = fail + functionNode.getAST().getRawSignature();
        } else {
            instrumentedSourceCode = success + instrument;
            int bodyIdx = instrumentedSourceCode.indexOf(SpecialCharacter.OPEN_BRACE) + 1;

            instrumentedSourceCode = instrumentedSourceCode.substring(0, bodyIdx)
                    + generateCallingMark(functionNode.getAbsolutePath()) // insert mark start function
                    + instrumentedSourceCode.substring(bodyIdx);
        }


        return instrumentedSourceCode;
    }


    public static String generateCallingMark(String content) {
        content = PathUtils.toRelative(content);
        content = Utils.doubleNormalizePath(content);
        return String.format(DriverConstant.MARK + "(\"Calling: %s\");" + IGTestConstant.INCREASE_FCALLS, content);
    }

}
