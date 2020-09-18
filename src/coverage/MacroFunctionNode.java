package coverage;

import config.FunctionConfig;
import config.IFunctionConfig;
import org.eclipse.cdt.core.dom.ast.*;
import parser.projectparser.ICommonFunctionNode;
import parser.projectparser.SourcecodeFileParser;
import tree.object.CustomASTNode;
import tree.object.IFunctionNode;
import tree.object.IVariableNode;
import utils.Utils;

import java.util.List;

/**
 * Instrument a function-like macro
 */
public class MacroFunctionNode extends CustomASTNode<IASTPreprocessorFunctionStyleMacroDefinition> implements ICommonFunctionNode
{
    private IFunctionNode correspondingFunctionNode; // a fake function node
    private FunctionConfig functionConfig;
    private int nAddtionalOffsetInBody = 0;

    public static void main(String[] args) {

    }

    private String rewriteName(IASTPreprocessorFunctionStyleMacroDefinition originalMacroNode) {
        String macroContent = originalMacroNode.getRawSignature().replace("\\\n", " \n").replace("\\\r", " \n");
        String newName = macroContent.substring(0, macroContent.indexOf("(")).replaceFirst("#define", "");
        newName = "void" + newName + "(";
        IASTFunctionStyleMacroParameter[] parameters = originalMacroNode.getParameters();
        for (IASTFunctionStyleMacroParameter parameter : parameters) {
            newName += "auto " + parameter.getParameter() + ",";
        }
        newName = newName.substring(0, newName.lastIndexOf(","));
        newName += ")";
        return newName;
    }

    private String rewriteBodyOfMacroFunction(String macroContent) {
        String body = macroContent.substring(macroContent.indexOf(")") + 1);
        String tmpBody = body.trim();
        if (!tmpBody.startsWith("{")) {
            body = "{" + body;
            nAddtionalOffsetInBody++;
        }
        if (!tmpBody.endsWith("}"))
            if (tmpBody.endsWith(";"))
                body += "}";
            else
                body += ";}";
        return body;
    }
    public IASTFunctionDefinition convertMacroFunctionToRealFunction(IASTPreprocessorFunctionStyleMacroDefinition originalMacroNode) {
        String macroContent = originalMacroNode.getRawSignature().replace(LINE_BREAK_IN_MACRO, " ");
        String originalName = macroContent.substring(0, macroContent.indexOf(")") + 1);

        // rewrite the name of macro
        // Ex: "#define my_macro(a)" --> "void my_macro(auto a)"
        String newName = rewriteName(originalMacroNode);

        // rewrite body
        String newBody = rewriteBodyOfMacroFunction(macroContent);

        // merge new name with new body
        String newContent = newName + newBody;
//        System.out.println("new content: " + newContent);

        // generate new AST of new content
        int nAdditionalOffsetInName = newName.length() - originalName.length() ;
        int startOffset = originalMacroNode.getFileLocation().getNodeOffset() - (nAdditionalOffsetInName + nAddtionalOffsetInBody);
        newContent = Utils.insertSpaceToFunctionContent(originalMacroNode.getFileLocation().getStartingLineNumber(),
                startOffset, newContent);
        IASTTranslationUnit newAST = null;
        try {
            newAST = new SourcecodeFileParser().getIASTTranslationUnit(newContent.toCharArray());
            if (newAST.getChildren()[0] instanceof IASTFunctionDefinition) {
                IASTFunctionDefinition newFunctionAST = (IASTFunctionDefinition) newAST.getChildren()[0];

                final boolean[] foundProblem = {false};
                ASTVisitor visitor = new ASTVisitor() {

                    @Override
                    public int visit(IASTProblem name) {
                        foundProblem[0] = true;
                        return ASTVisitor.PROCESS_ABORT;
                    }
                };
                visitor.shouldVisitProblems = true;
                newFunctionAST.accept(visitor);

                if (foundProblem[0]) {
                    throw new Exception("Problem in AST. So ignore this macro function.");
                }

                return newFunctionAST;

            } else
                throw new Exception("Problem in AST. So ignore this macro function.");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final String MACRO_UNDEFINE_TYPE = "__MACRO_UNDEFINE_TYPE__";
    public static final String LINE_BREAK_IN_MACRO = "\\";

    @Override
    public String getNameOfFunctionConfigTab()
    {
        return null;
    }

    @Override
    public IFunctionConfig getFunctionConfig()
    {
        return null;
    }

    @Override
    public void setFunctionConfig(FunctionConfig functionConfig)
    {

    }

    @Override
    public List<IVariableNode> getArguments()
    {
        return null;
    }

    @Override
    public List<IVariableNode> getArgumentsAndGlobalVariables()
    {
        return null;
    }

    @Override
    public String getReturnType()
    {
        return null;
    }

    @Override
    public String getSimpleName()
    {
        return null;
    }

    @Override
    public String getSingleSimpleName()
    {
        return null;
    }

    @Override
    public boolean isTemplate()
    {
        return false;
    }

    @Override
    public int getVisibility()
    {
        return 0;
    }

    @Override
    public String getNameOfFunctionConfigJson()
    {
        return null;
    }

    @Override
    public String getTemplateFilePath()
    {
        return null;
    }

    @Override
    public List<IVariableNode> getExternalVariables()
    {
        return null;
    }

    @Override
    public boolean hasVoidPointerArgument()
    {
        return false;
    }

    @Override
    public boolean hasFunctionPointerArgument()
    {
        return false;
    }
}
