package instrument;

import utils.SpecialCharacter;
import utils.Utils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;
import tree.object.IFunctionNode;

import java.io.File;
import java.util.List;

/**
 * Instrument function for constructor, destructor, normal function (not include macro function)
 *
 * Extend the previous instrumentation function by adding extra information
 * (e.g., the line of statements) to markers. <br/>
 * Ex: int a = 0; ----instrument-----> mark("line 12:int a = 0"); int a = 0;
 * <p>
 * <br/>
 *
 * @author DucAnh
 */
public class FunctionInstrumentationForAllCoverages extends AbstractFunctionInstrumentation {

    public FunctionInstrumentationForAllCoverages(IASTFunctionDefinition astFunctionNode, IFunctionNode functionNode) {
        if (astFunctionNode != null && astFunctionNode.getFileLocation() != null) {
            this.astFunctionNode = Utils.disableMacroInFunction(astFunctionNode, functionNode);
        }
    }

    public static void main(String[] args) throws Exception {

    }

    @Override
    public String generateInstrumentedFunction() {
        try {
            String specifier = astFunctionNode.getDeclSpecifier().getRawSignature() + SpecialCharacter.SPACE;
            String declarator = astFunctionNode.getDeclarator().getRawSignature();
            String body = parseBlock((IASTCompoundStatement) astFunctionNode.getBody(), null, "");
            String memberInits = parseMemberInits(astFunctionNode);
            return specifier + declarator + memberInits + body;
        } catch (Exception e){
            e.printStackTrace();
            // return the original function without instrumentation
            return astFunctionNode.getRawSignature();
        }
    }

    @Override
    public IFunctionNode getFunctionNode()
    {
        return null;
    }

    @Override
    public void setFunctionNode(IFunctionNode functionNode)
    {

    }

    private String parseMemberInits(IASTFunctionDefinition astFunctionNode) {
        StringBuilder builder = new StringBuilder();

        if (astFunctionNode instanceof ICPPASTFunctionDefinition) {
            ICPPASTConstructorChainInitializer[] memberInits =
                    ((ICPPASTFunctionDefinition) astFunctionNode).getMemberInitializers();

            if (memberInits.length > 0) {
                builder.append(" : ");
                for (int i = 0; i < memberInits.length; i++) {
                    builder.append(memberInits[i].getRawSignature());
                    if (i != memberInits.length - 1)
                        builder.append(", ");
                    else
                        builder.append(SpecialCharacter.SPACE);
                }
            }
        }

        return builder.toString();
    }

    protected String addExtraCall(IASTStatement stm, String extra, String margin) {
        if (extra != null)
            extra = putInMark(extra, true);

        if (stm instanceof IASTCompoundStatement)
            return parseBlock((IASTCompoundStatement) stm, extra, margin);
        else {
            String inside = margin + SpecialCharacter.TAB;

            return SpecialCharacter.OPEN_BRACE + SpecialCharacter.LINE_BREAK + inside /*+ inside*/ +
                    parseStatement(stm, inside) + SpecialCharacter.LINE_BREAK + margin +
                    SpecialCharacter.CLOSE_BRACE;
        }
    }

    protected String parseBlock(IASTCompoundStatement block, String extra, String margin) {
        if (block == null)
            return "";
        StringBuilder b = new StringBuilder("{" + SpecialCharacter.LINE_BREAK);
        String inside = margin + SpecialCharacter.TAB;
//        if (extra != null)
//            b.append(inside);

        for (IASTStatement stm : block.getStatements()) {
            String content = stm.getRawSignature();
            if (stm instanceof IASTProblemHolder) {
                if (!content.equals(")")) {
                    // ignore
                    b.append("/* Cant instrument this following code */\n");
                    b.append(stm.getRawSignature());
                } else {
                    System.out.println();
                }
            } else {
                b.append(inside).append(parseStatement(stm, inside)).append(SpecialCharacter.LINE_BREAK);
                //.append(SpecialCharacter.LINE_BREAK);
            }
        }

        b.append(margin).append(SpecialCharacter.CLOSE_BRACE);

        if (block.getStatements().length > 0 && !b.toString().contains(SpecialCharacter.END_OF_STATEMENT)) {
            b.append(SpecialCharacter.END_OF_STATEMENT);
        }

        return b.toString();
    }

    private boolean isAssign(IASTNode astNode) {
        if (astNode instanceof IASTUnaryExpression && ((IASTUnaryExpression) astNode).getOperator() == IASTUnaryExpression.op_bracketedPrimary) {
            astNode = ((IASTUnaryExpression) astNode).getOperand();
        }

        if (astNode instanceof IASTBinaryExpression) {
            return ((IASTBinaryExpression) astNode).getOperator() == IASTBinaryExpression.op_assign;
        }

        return false;
    }

    protected String parseStatement(IASTStatement stm, String margin) {
        StringBuilder b = new StringBuilder();

        if (stm instanceof IASTCompoundStatement)
            b.append(parseBlock((IASTCompoundStatement) stm, null, margin));

        else if (stm instanceof IASTIfStatement) {
            IASTIfStatement astIf = (IASTIfStatement) stm;
            IASTStatement astElse = astIf.getElseClause();
            IASTNode cond = astIf.getConditionExpression();
            IASTNode decla = ((CPPASTIfStatement) stm).getConditionDeclaration();

            if (cond != null)
                b.append("if (")
                        .append(putInMark(addContentOfMarkFunction(cond, astFunctionNode, functionPath, true, false), false))
                        .append(" && (").append(createMarkForSubCondition(cond)).append(")) ");
            else if (decla != null) {
                b.append(putInMark(addContentOfMarkFunction(decla, astFunctionNode, functionPath, true, false), false)).append(";");
                b.append("if (").append(decla.getRawSignature()).append(")");
            }

            b.append(addExtraCall(astIf.getThenClause(), "", margin));

            if (astElse != null) {
                b.append(SpecialCharacter.LINE_BREAK).append(margin).append("else ");
                b.append(addExtraCall(astElse, "", margin));
            }

        } else if (stm instanceof IASTForStatement) {
            IASTForStatement astFor = (IASTForStatement) stm;

            // Add marker for initialization
            IASTStatement astInit = astFor.getInitializerStatement();
            if (!(astInit instanceof IASTNullStatement)) {
                b.append(putInMark(addContentOfMarkFunction(astInit, astFunctionNode, functionPath, false, false), true));
            }

            b.append("for (").append(getShortenContent(astInit));
            // Add marker for condition
            IASTExpression astCond = (IASTExpression) Utils.shortenAstNode(astFor.getConditionExpression());
            if (astCond != null) {
                //b.append(SpecialCharacter.LINE_BREAK).append("\t\t\t");
                b.append(putInMark(addContentOfMarkFunction(astCond, astFunctionNode, functionPath, true, false), false)).append(" && ")
                        .append(createMarkForSubCondition(astCond)).append(";");
            } else
                b.append(";");

            // Add marker for increment
            IASTExpression astIter = astFor.getIterationExpression();
            if (astIter != null) {
                //b.append(SpecialCharacter.LINE_BREAK).append("\t\t\t");
                b.append("({" + putInMark(addContentOfMarkFunction(astIter, astFunctionNode, functionPath, false, false), false)).append(";").
                        append(getShortenContent(astIter)).append(";})");
            }
            b.append(") ");

            // For loop: no condition
            if (astCond == null)
                b.append(parseStatement(astFor.getBody(), margin));
            else
                b.append(addExtraCall(astFor.getBody(), "", margin));

        } else if (stm instanceof IASTWhileStatement) {
            IASTWhileStatement astWhile = (IASTWhileStatement) stm;
            IASTNode cond = astWhile.getCondition();

            b.append("while (")
                    .append(putInMark(addContentOfMarkFunction(cond, astFunctionNode, functionPath, true, false), false))
                    .append(" && (").append(createMarkForSubCondition(cond)).append(")) ");

            b.append(addExtraCall(astWhile.getBody(), "", margin));

        } else if (stm instanceof IASTDoStatement) {
            IASTDoStatement astDo = (IASTDoStatement) stm;
            IASTNode cond = astDo.getCondition();

            boolean isAssign = isAssign(cond);

            b.append("do ").append(addExtraCall(astDo.getBody(), "", margin)).append(SpecialCharacter.LINE_BREAK)
                    .append(margin).append("while (");

            if (!isAssign) {
                b.append(putInMark(addContentOfMarkFunction(cond, astFunctionNode, functionPath, true, false), false))
                        .append(" && (");
            }

            b.append(createMarkForSubCondition(cond));

            if (!isAssign) {
                b.append(")");
            }

            b.append(");");

        } else if (stm instanceof ICPPASTTryBlockStatement) {
            ICPPASTTryBlockStatement astTry = (ICPPASTTryBlockStatement) stm;

            b.append(DriverConstant.MARK + "(\"start try;\");");

            b.append(SpecialCharacter.LINE_BREAK).append(margin).append("try ");
            b.append(addExtraCall(astTry.getTryBody(), null, margin));

            for (ICPPASTCatchHandler catcher : astTry.getCatchHandlers()) {
                b.append(SpecialCharacter.LINE_BREAK).append(margin).append("catch (");

                String exception = catcher.isCatchAll() ? "..." : getShortenContent(catcher.getDeclaration());
                b.append(exception).append(") ");

                b.append(addExtraCall(catcher.getCatchBody(), exception, margin));
            }

            b.append(SpecialCharacter.LINE_BREAK).append(margin).append(DriverConstant.MARK + "(\"end catch;\");");

        } else if (stm instanceof IASTBreakStatement || stm instanceof IASTContinueStatement) {
            b.append(putInMark(addContentOfMarkFunction(stm, astFunctionNode, functionPath, false, false), true));
            b.append(getShortenContent(stm));

        } else if (stm instanceof IASTReturnStatement) {
            b.append(putInMark(addContentOfMarkFunction(stm, astFunctionNode, functionPath, false, false), true));
            b.append(getShortenContent(stm));

        } else {
            String raw = getShortenContent(stm);
            b.append(putInMark(addContentOfMarkFunction(stm, astFunctionNode, functionPath, false, false), true));// add markers
            b.append(raw);
        }

        return b.toString();
    }

    private String createMarkForSubCondition(IASTNode astCon) {
        StringBuilder tempStr = new StringBuilder();
        astCon = Utils.shortenAstNode(astCon);
        if (isCondition(astCon)) {
            if (astCon instanceof IASTBinaryExpression) {
                int operator = ((IASTBinaryExpression) astCon).getOperator();

                switch (operator) {
                    case IASTBinaryExpression.op_greaterEqual:
                    case IASTBinaryExpression.op_greaterThan:
                    case IASTBinaryExpression.op_lessEqual:
                    case IASTBinaryExpression.op_lessThan:
                        tempStr.append("	(").append(astCon)
                                .append("&&").append(Utils.shortenAstNode(astCon).getRawSignature()).append(")");
                        break;

                    case IASTBinaryExpression.op_logicalAnd:
                    case IASTBinaryExpression.op_logicalOr:
                        IASTExpression operand1 = ((IASTBinaryExpression) astCon).getOperand1();
                        IASTExpression operand2 = ((IASTBinaryExpression) astCon).getOperand2();

                        tempStr.append("(").append(createMarkForSubCondition(operand1)).append(")")
                                .append(operator == IASTBinaryExpression.op_logicalAnd ? "	&&" : "	||").append("(").append(createMarkForSubCondition(operand2)).append(")");
                        break;
                }
            } else {
                // unary expression
                tempStr.append(DriverConstant.MARK + "(\"")
                        .append(addContentOfMarkFunction(astCon, astFunctionNode, functionPath, false, true)).
                        append("\")&&").
                        append(astCon.getRawSignature());
            }
        } else {
            if (isAssign(astCon)) {
                IASTBinaryExpression astBin = (IASTBinaryExpression) astCon;
                tempStr.append(astBin.getOperand1())
                        .append("=")
                        .append("(")
                        .append(astBin.getOperand2())
                        .append(") && ")
                        .append(DriverConstant.MARK + "(\"")
                        .append(addContentOfMarkFunction(astCon, astFunctionNode, functionPath, false, true)).
                        append("\")");

            } else {
                tempStr.append(DriverConstant.MARK + "(\"")
                        .append(addContentOfMarkFunction(astCon, astFunctionNode, functionPath, false, true)).
                        append("\")&&").
                        append(astCon.getRawSignature());
            }
        }

        return tempStr.toString();
    }
}
