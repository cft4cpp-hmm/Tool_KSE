package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Common.TestConfig;
import HybridAutoTestGen.TestData;
import cfg.ICFG;
import cfg.object.AbstractConditionLoopCfgNode;
import cfg.object.ConditionCfgNode;
import cfg.object.ICfgNode;
import compiler.AvailableCompiler;
import compiler.Compiler;
import coverage.FunctionCoverageComputation;
import javafx.util.Pair;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTProblemDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;

import config.AbstractSetting;
import config.ISettingv2;
import parser.makefile.CompilerFolderParser;
import parser.projectparser.ProjectLoader;
import parser.projectparser.ProjectParser;
import parser.projectparser.SourcecodeFileParser;
import project_init.ProjectClone;
import testcase_execution.TestcaseExecution;
import testcase_manager.TestCase;
import testdatagen.se.ExpressionRewriterUtils;
import testdatagen.se.memory.ISymbolicVariable;
import testdatagen.se.memory.IVariableNodeTable;
import testdatagen.testdatainit.VariableTypes;
import tree.object.*;
import utils.search.FunctionNodeCondition;
import utils.search.Search;
import utils.search.SourcecodeFileNodeCondition;

public class Utils implements IRegex
{
    public static final int UNDEFINED_TO_INT = -9999;
    public static final float UNDEFINED_TO_DOUBLE = -9999;
    final static Logger logger = Logger.getLogger(Utils.class);
    public static boolean containFunction = false;
    static boolean containBlock = false;
    /**
     *
     */
    static IASTNode output = null;

    /**
     * Add quote to the content (e.g., abs--->"abc")
     *
     * @param content
     * @return
     */
    public static String toQuote(String content)
    {
        return "\"" + content + "\"";
    }

    public static boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

    public static boolean isMac()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }

    public static boolean isUnix()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("nix") >= 0
                || System.getProperty("os.name").toLowerCase().indexOf("nux") >= 0
                || System.getProperty("os.name").toLowerCase().indexOf("aix") >= 0
                || System.getProperty("os.name").toLowerCase().indexOf("centos") >= 0;
    }

    public static boolean isSolaris()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("sunos") >= 0;
    }

    public static String normalizePath(String path)
    {
        return path.replace("\\", File.separator).replace("/", File.separator);
    }

    /**
     * Check whether the function contains do..while, while..do, for...
     *
     * @param fn
     * @return
     */
    public static boolean containsLoopBlock(IFunctionNode fn)
    {
        IASTFunctionDefinition fnAst = fn.getAST();
        Utils.containBlock = false;

        ASTVisitor visitor = new ASTVisitor()
        {

            @Override
            public int visit(IASTStatement statement)
            {
                if (statement instanceof IASTWhileStatement || statement instanceof IASTDoStatement
                        || statement instanceof IASTForStatement)
                {
                    Utils.containBlock = true;
                    return ASTVisitor.PROCESS_ABORT;
                }
                else
                {
                    return ASTVisitor.PROCESS_CONTINUE;
                }
            }
        };

        visitor.shouldVisitStatements = true;

        fnAst.accept(visitor);

        return Utils.containBlock;
    }

    /**
     * Check whether the function contains do..while, while..do, for...
     *
     * @param fn
     * @return
     */
    public static boolean containsIfBlock(IFunctionNode fn)
    {
        IASTFunctionDefinition fnAst = fn.getAST();
        Utils.containBlock = false;

        ASTVisitor visitor = new ASTVisitor()
        {

            @Override
            public int visit(IASTStatement statement)
            {
                if (statement instanceof IASTIfStatement)
                {
                    Utils.containBlock = true;
                    return ASTVisitor.PROCESS_ABORT;
                }
                else
                {
                    return ASTVisitor.PROCESS_CONTINUE;
                }
            }
        };

        visitor.shouldVisitStatements = true;

        fnAst.accept(visitor);

        return Utils.containBlock;
    }

    public static String getCastedValue(String value, ISymbolicVariable var)
    {
        String castedValue = var.getType();

        if (VariableTypes.isNum(var.getType()) && VariableTypes.isNumFloat(var.getType()))
        {
            castedValue = "to_int(" + value + ")";
        }
        return castedValue;
    }

    /**
     * Get real type of variable.
     * <p>
     * Ex: "typedex XXX int; XXX a" -------------> type of a is "int", not "XXX"
     * <p>
     * Note:
     * <p>
     * <p>
     *
     * <pre>
     * typedef int int_t; // declares int_t to be an alias for the type int
     * typedef char char_t, *char_p, (*fp)(void); // declares char_t to be an
     * alias for char // char_p to be an alias for char* // fp to be an alias
     * for char(*)(void)
     *
     * <pre>
     *
     * @param type
     *            Ex1: XXX, Ex2: const int&
     * @param function
     *            the function contains the using type
     * @return
     */
    public static String getRealType(String type, INode function)
    {
        String realType = "";

        /*
         * Delete unnecessary character
         *
         * Ex: "const BigData&" ----------> ""BigData
         */
        type = VariableTypes.deleteStorageClasses(type);
        type = VariableTypes.deleteStructKeywork(type);
        type = VariableTypes.deleteUnionKeywork(type);

        type = type.replace("&", "");

        /*
         * Create temporary variable
         */
        VariableNode var = new VariableNode();

        String remaining = "";
        String reducedType = type;
        if (VariableTypes.isOneDimension(type) || VariableTypes.isTwoDimension(type))
        {
            int index = type.indexOf("[");

            remaining = type.substring(index);
            reducedType = type.substring(0, index);

        }
        else if (VariableTypes.isOneLevel(type) || VariableTypes.isTwoLevel(type))
        {
            int index = type.indexOf("*");
            remaining = type.substring(index);
            reducedType = type.substring(0, index);
        }

        var.setRawType(reducedType);
        var.setCoreType(reducedType);
        var.setReducedRawType(reducedType);
        var.setParent(function);

        INode nodeType = var.resolveCoreType();

        if (nodeType != null)
        {
            if (nodeType instanceof AvailableTypeNode)
            {
                realType = ((AvailableTypeNode) nodeType).getType();
            }
            else if (nodeType instanceof VariableNode)
            {
                realType = ((IVariableNode) nodeType).getReducedRawType();
            }
            else if (nodeType instanceof StructureNode)
            {
                realType = ((StructureNode) nodeType).getNewType();
            }
        }
        else
        {
            realType = type;
        }

        realType += remaining;

        return realType;
    }

    /**
     * Get the reduce index of array item
     * <p>
     * Ex: a[1+2][3] --------> [3][3]
     *
     * @param arrayItem
     * @param table
     * @return
     * @throws Exception
     */
    public static String getReducedIndex(String arrayItem, IVariableNodeTable table) throws Exception
    {
        String index = "";
        List<String> indexes = Utils.getIndexOfArray(arrayItem);

        for (String indexItem : indexes)
        {
            indexItem = ExpressionRewriterUtils.rewrite(table, indexItem);
            index += Utils.asIndex(indexItem);
        }
        return index;
    }

    /**
     * Shorten ast node. <br/>
     * Ex:"(a)" -----> "a" <br/>
     * Ex: "(!a)" --------> "!a"
     *
     * @param ast
     * @return
     */
    public static IASTNode shortenAstNode(IASTNode ast)
    {
        IASTNode tmp = ast;
        /*
         * Ex:"(a)" -----> "a"
         *
         * Ex: "(!a)" --------> !a
         */
        while ((tmp instanceof CPPASTExpressionStatement || tmp instanceof ICPPASTUnaryExpression
                && tmp.getRawSignature().startsWith("(") && tmp.getRawSignature().endsWith(")"))
                && tmp.getChildren().length == 1 && !tmp.getRawSignature().startsWith("!"))
        {
            tmp = tmp.getChildren()[0];
        }

        return tmp;
    }

    /**
     * Get all unary expression
     * <p>
     * Ex: "x=(a++) +1+ (--b)" -------> unary expression: {"a++", "--b}
     *
     * @param ast
     * @return
     */
    public static List<ICPPASTUnaryExpression> getUnaryExpressions(IASTNode ast)
    {
        List<ICPPASTUnaryExpression> unaryExpressions = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor()
        {

            @Override
            public int visit(IASTExpression name)
            {
                if (name instanceof ICPPASTUnaryExpression)
                {
                    unaryExpressions.add((ICPPASTUnaryExpression) name);
                    return ASTVisitor.PROCESS_CONTINUE;
                }
                else
                {
                    return ASTVisitor.PROCESS_CONTINUE;
                }
            }
        };

        visitor.shouldVisitExpressions = true;

        ast.accept(visitor);
        return unaryExpressions;
    }

    public static boolean isCondition(String content)
    {
        /*
         * Get type of the statement
         */
        boolean isCondition = false;

        // special case: content= "a"
        if (content.matches(IRegex.NAME_REGEX))
        {
            isCondition = true;
        }

        else
            /*
             * Ex: char c = static_cast<char>(x)
             *
             * Ex: char c = static_cast<char>(x);
             *
             * Ex: cout << "A";
             */
            if (content.endsWith(SpecialCharacter.END_OF_STATEMENT) || content.contains(" = ")
                    || Utils.containRegex(content, "\\b=\\b") || content.startsWith("cout ") || content.startsWith("cout<<")
                    || content.startsWith("std::"))
            {
                isCondition = false;
            }
            else
            {
                final String[] CONDITION_SIGNALS = new String[]{"!=", "<=", ">=", "==", ">", "<", "!"};

                for (String conditionSignal : CONDITION_SIGNALS)
                {
                    if (content.contains(conditionSignal))
                    {
                        isCondition = true;
                    }
                }
            }
        return isCondition;
    }

    /**
     * Check whether a string contain regex or not
     *
     * @param src
     * @param regex
     * @return
     */
    public static boolean containRegex(String src, String regex)
    {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(src);

        return m.find();
    }

    /**
     * Get all expression in the assignment. Ex: "x=y=z+1"---->{x, y, z+1} in
     * order of left side to right side
     *
     * @param binaryAST
     */
    public static List<String> getAllExpressionsInBinaryExpression(IASTBinaryExpression binaryAST)
    {
        List<String> expression = new ArrayList<>();
        IASTNode tmpAST = binaryAST;

        while (tmpAST instanceof IASTBinaryExpression)
        {
            IASTNode firstChild = tmpAST.getChildren()[0];
            expression.add(firstChild.getRawSignature());

            IASTNode secondChild = tmpAST.getChildren()[1];
            tmpAST = secondChild;
        }
        expression.add(tmpAST.getRawSignature());

        return expression;
    }

    /**
     * Convert a string into regex
     *
     * @param str
     * @return
     */
    public static String toRegex(String str)
    {
        str = str.replace("[", "\\[").replace("]", "\\]").replace("(", "\\(").replace(")", "\\)").replace(".", "\\.")
                .replace("*", "\\*").replace(" ", "\\s*").replace("_", "\\_");

        /*
         * Add bound of word at the beginning
         */
        if (str.toCharArray()[0] >= 'A' && str.toCharArray()[0] <= 'Z'
                || str.toCharArray()[0] >= 'a' && str.toCharArray()[0] <= 'z')
        {
            str = "\\b" + str;
        }

        /*
         * Add bound of word at the end
         */
        int last = str.toCharArray().length - 1;
        if (str.toCharArray()[last] >= 'A' && str.toCharArray()[last] <= 'Z'
                || str.toCharArray()[last] >= 'a' && str.toCharArray()[last] <= 'z')
        {
            str += "\\b";
        }
        return str;
    }

    public static String asIndex(int i)
    {
        return "[" + i + "]";
    }

    public static String asIndex(String str)
    {
        return "[" + str + "]";
    }

    public static void initializeEnvironment() throws Exception
    {
        Utils.getMethodName(2);

        CompilerFolderParser compiler = new CompilerFolderParser(new File("C:/Dev-Cpp/"));
        compiler.parse();
        if (new File(compiler.getMakePath()).exists() && new File(compiler.getGccPath()).exists()
                && new File(compiler.getgPlusPlusPath()).exists())
        {
            AbstractSetting.setValue(ISettingv2.GNU_MAKE_PATH, compiler.getMakePath());
            AbstractSetting.setValue(ISettingv2.GNU_GCC_PATH, compiler.getGccPath());
            AbstractSetting.setValue(ISettingv2.GNU_GPlusPlus_PATH, compiler.getgPlusPlusPath());
        }
        else
        {
            throw new Exception("Ä�Æ°á»�ng dáº«n biÃªn dá»‹ch sai");
        }
        /**
         *
         */
        String z3SolverPath = "C:/z3/bin/z3.exe";

        // edit here

        z3SolverPath = "D/cft4cpp-core/loca/z3/bin/z3.exe";
        if (new File(z3SolverPath).exists())
        {
            AbstractSetting.setValue(ISettingv2.SOLVER_Z3_PATH, z3SolverPath);
        }
        else
        {
            throw new Exception("Ä�Æ°á»�ng dáº«n Z3 sai");
        }
    }

    /**
     * Bá»• sung thÃªm ná»™i dung vÃ o tá»‡p
     *
     * @param path
     * @param content
     */
    public static void appendContentToFile(String content, String path)
    {
        File f = new File(path);
        if (!f.exists())
        {
            Utils.writeContentToFile(content, path);
        }
        else
        {
            String currentContent = Utils.readFileContent(path);
            Utils.writeContentToFile(currentContent + content, path);
        }
    }

    /**
     * Kiá»ƒm tra trong biá»ƒu thá»©c cÃ³ lá»�i gá»�i hÃ m khÃ´ng
     *
     * @param ast
     * @return
     */
    public static boolean containFunctionCall(IASTNode ast)
    {

        ASTVisitor visitor = new ASTVisitor()
        {

            @Override
            public int visit(IASTExpression expression)
            {
                if (expression instanceof IASTFunctionCallExpression)
                {
                    Utils.containFunction = true;
                    return ASTVisitor.PROCESS_ABORT;
                }
                else
                {
                    return ASTVisitor.PROCESS_CONTINUE;
                }
            }
        };
        visitor.shouldVisitStatements = true;
        visitor.shouldVisitExpressions = true;
        ast.accept(visitor);
        return Utils.containFunction;
    }

    /**
     * Convert List<String> into String[]
     *
     * @param list
     * @return
     */
    public static String[] convertToArray(List<String> list)
    {
        String[] strarray = list.toArray(new String[list.size()]);
        return strarray;
    }

    public static File copy(String originalFolder) throws IOException
    {
        originalFolder = Utils.normalizePath(originalFolder);

        String copyFolder = originalFolder;
        if (originalFolder.endsWith(File.separator))
        {
            copyFolder = originalFolder.substring(0, originalFolder.length() - 1);
        }

        copyFolder += "_copy";
        while (new File(copyFolder).exists())
        {
            copyFolder += "1";
        }
        Utils.copyFolder(new File(originalFolder), new File(copyFolder));
        return new File(copyFolder);
    }

    public static void copyFolder(File src, File dest) throws IOException
    {

        if (src.isDirectory())
        {

            // if directory not exists, create it
            if (!dest.exists())
            {
                dest.mkdir();
            }

            // list all the directory contents
            String files[] = src.list();

            for (String file : files)
            {
                // construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // recursive copy
                Utils.copyFolder(srcFile, destFile);
            }

        }
        else
        {
            // if file, then copy it
            // Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            // copy the file content in bytes
            while ((length = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }

    /**
     * Tao folder
     *
     * @param path
     */
    public static void createFolder(String path)
    {
        File destDir = new File(path);
        if (!destDir.exists())
        {
            destDir.mkdirs();
        }
    }

    public static void deleteFileOrFolder(File path)
    {
        try
        {
            FileDeleteStrategy.FORCE.delete(path);
            if (!path.exists())
            {
                return;
            }
        }
        catch (IOException e)
        {
            try
            {
                Thread.sleep(30);
                Utils.deleteFileOrFolder(path);
            }
            catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
        }
    }


    public static void deleteFilesInFolder(File folderPath)
    {
        try
        {
            if (!folderPath.exists())
            {
                return;
            }

            String files[] = folderPath.list();

            for (String file : files)
            {
                FileDeleteStrategy.FORCE.delete(new File(folderPath + "\\" + file));
            }
        }
        catch (IOException e)
        {
        }
    }

    /**
     * Delete all main() functions in project and update the project.
     *
     * @param root
     */
    public static void deleteMain(INode root)
    {
        List<INode> mainFunctionsType1 = Search.searchNodes(root, new FunctionNodeCondition(), "main()");
        List<INode> mainFunctionsType2 = Search.searchNodes(root, new FunctionNodeCondition(), "main(int,char**)");
        mainFunctionsType1.addAll(mainFunctionsType2);

        for (INode functionMain : mainFunctionsType1)
        {
            IASTFunctionDefinition ast = ((IFunctionNode) functionMain).getAST();
            String content = Utils.readFileContent(functionMain.getParent());
            content = content.replace(ast.getRawSignature(), "");
            Utils.writeContentToFile(content.toString(), functionMain.getParent().getAbsolutePath());
        }
    }

    /**
     * Ba trÆ°á»�ng há»£p cáº§n xá»­ lÃ½ gá»“m:
     * <p>
     * - HÃ m cáº§n kiá»ƒm thá»­ khÃ´ng thuá»™c class, struct nÃ o háº¿t
     * <p>
     * - HÃ m cáº§n kiá»ƒm thá»­ khai bÃ¡o trong class nhÆ°ng Ä‘á»‹nh nghÄ©a
     * ngoÃ i class Ä‘Ã³
     * <p>
     * - HÃ m cáº§n kiá»ƒm thá»­ khai bÃ¡o vÃ  Ä‘á»‹nh nghÄ©a trong class
     *
     * @param testedFunction
     */
    public static String deleteOrRenameTestedFunction(IFunctionNode testedFunction)
    {
        String parentPath = Utils.getSourcecodeFile(testedFunction).getAbsolutePath();
        String oldContent = Utils.readFileContent(parentPath);

        String newContent = "";
        if (testedFunction.getSimpleName().contains("::"))
        {
            String function = ((FunctionNode) testedFunction).getAST().getRawSignature();
            newContent = oldContent.replace(function, ";");
        }
        else
        {
            String function = ((FunctionNode) testedFunction).getAST().getRawSignature();
            String functionBody = function.substring(function.indexOf("{"));
            newContent = oldContent.replace(functionBody, ";");
        }
        return newContent;
    }

    public static <T extends Object> String displayQueue(Queue<T> objectsQueue)
    {
        String output = "";
        for (T item : objectsQueue)
        {
            output += item.toString() + " # ";
        }
        return output;
    }

    public static <T extends Object> String displayStack(Stack<T> objectsStack)
    {
        String output = "";
        for (T item : objectsStack)
        {
            output += item.toString() + " # ";
        }
        return output;
    }

    public static String findFileExeMapWithNodeCurrent(String pathProject, INode nodeCurrent) throws IOException
    {
        /**
         * Find list file exe
         */
        File dir = new File(pathProject);
        File[] listEXE = dir.listFiles((FilenameFilter) (dir1, name) -> name.endsWith(".exe"));

        /**
         * Map Node with file exe
         */
        INode nodeProject = nodeCurrent;
        while (nodeProject != null)
        {
            File folder = new File(nodeProject.getAbsolutePath());
            if (folder.isDirectory())
            {
                String nameFolder = folder.getName();
                for (File temp : listEXE)
                {
                    if (temp.getName().equals(nameFolder))
                    {
                        return temp.getCanonicalPath();
                    }
                }
            }
            nodeProject = nodeProject.getParent();
        }

        return null;
    }

    public static IASTNode findFirstASTByName(String name, IASTNode ast)
    {

        ASTVisitor visitor = new ASTVisitor()
        {
            @Override
            public int visit(IASTStatement statement)
            {
                if (statement.getRawSignature().equals(name))
                {
                    Utils.output = statement;
                    return ASTVisitor.PROCESS_ABORT;
                }
                else
                {
                    return ASTVisitor.PROCESS_CONTINUE;
                }
            }
        };
        visitor.shouldVisitStatements = true;
        visitor.shouldVisitExpressions = true;
        ast.accept(visitor);

        return Utils.output;
    }

    /**
     * Find ast by the content of condition
     *
     * @param name the content of condition
     * @param ast  the ast of source code containing the condition
     * @return
     */
    public static IASTNode findFirstConditionByName(String name, IASTNode ast)
    {
        ASTVisitor visitor = new ASTVisitor()
        {
            @Override
            public int visit(IASTStatement statement)
            {
                if (statement instanceof IASTIfStatement)
                {
                    IASTNode con = ((IASTIfStatement) statement).getConditionExpression();

                    if (con.getRawSignature().equals(name))
                    {
                        Utils.output = con;
                        return ASTVisitor.PROCESS_ABORT;
                    }
                }
                else if (statement instanceof IASTWhileStatement)
                {
                    IASTNode con = ((IASTWhileStatement) statement).getCondition();
                    if (con.getRawSignature().equals(name))
                    {
                        Utils.output = con;
                        return ASTVisitor.PROCESS_ABORT;
                    }
                }
                else if (statement instanceof IASTDoStatement)
                {
                    IASTNode con = ((IASTDoStatement) statement).getCondition();
                    if (con.getRawSignature().equals(name))
                    {
                        Utils.output = con;
                        return ASTVisitor.PROCESS_ABORT;
                    }
                }
                else if (statement instanceof IASTSwitchStatement)
                {
                    // TODO: xu ly
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };
        visitor.shouldVisitStatements = true;
        visitor.shouldVisitExpressions = true;
        ast.accept(visitor);

        return Utils.output;
    }

    /**
     * Láº¥y mÃ£ ASCII cá»§a kÃ­ tá»±
     *
     * @param ch
     * @return
     */
    public static int getASCII(char ch)
    {
        return ch;
    }

    public static INode getClassvsStructvsNamesapceNodeParent(INode n)
    {
        if (n == null)
        {
            return null;
        }
        else if (n instanceof ClassNode || n instanceof StructNode || n instanceof NamespaceNode)
        {
            return n;
        }
        else
        {
            return Utils.getClassvsStructvsNamesapceNodeParent(n.getParent());
        }
    }

    public static INode getTopLevelClassvsStructvsNamesapceNodeParent(INode n)
    {
        if (n == null)
        {
            return null;
        }
        else if (n instanceof ClassNode || n instanceof StructNode || n instanceof NamespaceNode)
        {
            if (n.getParent() != null && n.getParent() instanceof SourcecodeFileNode)
            {
                return n;
            }
            else
            {
                return Utils.getTopLevelClassvsStructvsNamesapceNodeParent(n.getParent());
            }
        }
        else if (n instanceof IFunctionNode)
        {
            return Utils.getTopLevelClassvsStructvsNamesapceNodeParent(((IFunctionNode) n).getRealParent());
        }
        else
        {
            return Utils.getTopLevelClassvsStructvsNamesapceNodeParent(n.getParent());
        }
    }

    /**
     * Láº¥y danh sÃ¡ch CPPASTName trong má»™t node AST
     *
     * @param ast
     * @return
     */
    public static List<CPPASTName> getCPPASTNames(IASTNode ast)
    {
        List<CPPASTName> cppASTNames = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor()
        {

            @Override
            public int visit(IASTName name)
            {
                cppASTNames.add((CPPASTName) name);
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };

        visitor.shouldVisitNames = true;

        ast.accept(visitor);
        return cppASTNames;
    }

    public static List<ICPPASTFieldReference> getFieldReferences(IASTNode ast)
    {
        List<ICPPASTFieldReference> binaryASTs = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor()
        {

            @Override
            public int visit(IASTExpression name)
            {
                if (name instanceof ICPPASTFieldReference)
                {
                    binaryASTs.add((ICPPASTFieldReference) name);
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };

        visitor.shouldVisitExpressions = true;

        ast.accept(visitor);
        return binaryASTs;
    }

    /**
     * Get all binary expressions
     *
     * @param ast
     * @return
     */
    public static List<ICPPASTBinaryExpression> getBinaryExpressions(IASTNode ast)
    {
        List<ICPPASTBinaryExpression> binaryASTs = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor()
        {

            @Override
            public int visit(IASTExpression name)
            {
                if (name instanceof ICPPASTBinaryExpression)
                {
                    binaryASTs.add((ICPPASTBinaryExpression) name);
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };

        visitor.shouldVisitExpressions = true;

        ast.accept(visitor);
        return binaryASTs;
    }

    /**
     * Get all declarations in the given ast
     *
     * @param ast
     * @return
     */
    public static List<IASTSimpleDeclaration> getSimpleDeclarations(IASTNode ast)
    {
        List<IASTSimpleDeclaration> declarationASTs = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor()
        {

            @Override
            public int visit(IASTDeclaration name)
            {
                if (name instanceof IASTSimpleDeclaration)
                {
                    declarationASTs.add((IASTSimpleDeclaration) name);
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };

        visitor.shouldVisitDeclarations = true;

        ast.accept(visitor);
        return declarationASTs;
    }

    public static List<ICPPASTLiteralExpression> getLiteralExpressions(IASTNode ast)
    {
        List<ICPPASTLiteralExpression> literalASTs = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor()
        {

            @Override
            public int visit(IASTExpression name)
            {
                if (name instanceof ICPPASTLiteralExpression)
                {
                    literalASTs.add((ICPPASTLiteralExpression) name);
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };

        visitor.shouldVisitExpressions = true;

        ast.accept(visitor);
        return literalASTs;
    }

    public static String getFileExtension(String path)
    {
        String[] pathSegments = path.split(File.separator);
        String fileName = pathSegments[pathSegments.length - 1];

        int i = fileName.lastIndexOf('.');
        if (i == -1)
        {
            return "";
        }
        else
        {
            return fileName.substring(i + 1);
        }
    }

    /**
     * Láº¥y node cha lÃ  function
     *
     * @param n
     * @return
     */
    public static INode getFunctionNodeParent(INode n)
    {
        if (n == null)
        {
            return null;
        }
        else if (n instanceof FunctionNode)
        {
            return n;
        }
        else
        {
            return Utils.getFunctionNodeParent(n.getParent());
        }

    }

    public static List<ICPPASTFunctionDefinition> getFunctionsinAST(char[] sourcecode)
    {
        List<ICPPASTFunctionDefinition> output = new ArrayList<>();

        try
        {
            IASTTranslationUnit unit = Utils.getIASTTranslationUnitforCpp(sourcecode);

            if (unit.getChildren()[0] instanceof CPPASTProblemDeclaration)
            {
                unit = Utils.getIASTTranslationUnitforC(sourcecode);
            }

            ASTVisitor visitor = new ASTVisitor()
            {
                @Override
                public int visit(IASTDeclaration declaration)
                {
                    if (declaration instanceof ICPPASTFunctionDefinition)
                    {
                        output.add((ICPPASTFunctionDefinition) declaration);
                        return ASTVisitor.PROCESS_SKIP;
                    }
                    return ASTVisitor.PROCESS_CONTINUE;
                }
            };

            visitor.shouldVisitDeclarations = true;

            unit.accept(visitor);
        }
        catch (Exception e)
        {

        }
        return output;
    }

    public static IASTTranslationUnit getIASTTranslationUnitforCpp(char[] code) throws Exception
    {
        File filePath = new File("");
        FileContent fc = FileContent.create(filePath.getAbsolutePath(), code);
        Map<String, String> macroDefinitions = new HashMap<>();
        String[] includeSearchPaths = new String[0];
        IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
        IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
        IIndex idx = null;
        int options = ILanguage.OPTION_IS_SOURCE_UNIT;
        IParserLogService log = new DefaultLogService();
        return GPPLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);
    }

    public static IASTTranslationUnit getIASTTranslationUnitforC(char[] code) throws Exception
    {
        File filePath = new File("");
        FileContent fc = FileContent.create(filePath.getAbsolutePath(), code);
        Map<String, String> macroDefinitions = new HashMap<>();
        String[] includeSearchPaths = new String[0];
        IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
        IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
        IIndex idx = null;
        int options = ILanguage.OPTION_IS_SOURCE_UNIT;
        IParserLogService log = new DefaultLogService();
        return GCCLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);
    }

    /**
     * Láº¥y danh sÃ¡ch id trong má»™t node AST
     *
     * @param ast
     * @return
     */
    public static List<CPPASTIdExpression> getIds(IASTNode ast)
    {
        List<CPPASTIdExpression> ids = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor()
        {
            @Override
            public int visit(IASTExpression expression)
            {
                if (expression instanceof CPPASTIdExpression)
                {
                    ids.add((CPPASTIdExpression) expression);
                }
                return ASTVisitor.PROCESS_CONTINUE;
            }
        };

        visitor.shouldVisitExpressions = true;

        ast.accept(visitor);
        return ids;
    }

    public static List<ICPPASTArraySubscriptExpression> getArraySubscriptExpression(IASTNode ast)
    {
        List<ICPPASTArraySubscriptExpression> ids = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor()
        {
            @Override
            public int visit(IASTExpression expression)
            {
                if (expression instanceof ICPPASTArraySubscriptExpression)
                {
                    ids.add((ICPPASTArraySubscriptExpression) expression);

                    if (expression.getChildren()[0] instanceof ICPPASTArraySubscriptExpression)
                    {
                        return ASTVisitor.PROCESS_SKIP;
                    }
                    else
                    {
                        return ASTVisitor.PROCESS_CONTINUE;
                    }
                }
                else
                {
                    return ASTVisitor.PROCESS_CONTINUE;
                }
            }
        };

        visitor.shouldVisitExpressions = true;

        ast.accept(visitor);
        return ids;
    }

    /**
     * Láº¥y danh sÃ¡ch chá»‰ sá»‘ máº£ng
     *
     * @param constraint VD: a[3][2]
     * @return VD: 3,2
     * @problem ChÆ°a xá»­ lÃ½ chá»‰ sá»‘ máº£ng chá»©a chá»‰ sá»‘ máº£ng khÃ¡c.
     * VD: a[1+b[2]]
     */
    public static List<String> getIndexOfArray(String constraint)
    {
        List<String> output = new ArrayList<>();

        Pattern p = Pattern.compile(IRegex.ARRAY_INDEX);
        Matcher m = p.matcher(constraint);

        while (m.find())
        {
            String str = m.group(1);
            output.add(str);
        }
        return output;
    }

    /**
     * Get the method name for a depth in call stack. <br />
     * Utility function
     *
     * @param depth depth in the call stack (0 means current method, 1 means call
     *              method, ...)
     * @return method name
     */
    public static String getMethodName(final int depth)
    {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[ste.length - 1 - depth].getMethodName();
    }

    /**
     * Láº¥y tÃªn tá»‡p .exe
     *
     * @param makefilepath Ä�Æ°á»�ng dáº«n tá»‡p make
     * @return
     */
    public static String getNameOfExeInDevCppMakefile(String makefilepath)
    {
        String makefileContent = Utils.readFileContent(Utils.normalizePath(makefilepath));

        // TH 1. Tá»‡p exe Ä‘á»‹nh nghÄ©a tÆ°á»�ng minh
        // Ex: g++ StackLinkedList.o main.o -o "main.exe"
        Pattern p = Pattern.compile("\\s+-o\\s+\"([a-zA-Z0-9\\.]+)\\.exe\"");
        Matcher m = p.matcher(makefileContent);
        if (m.find())
        {
            logger.debug("Exe name: " + m.group(1));
            return m.group(1) + ".exe";
        }

        // TH 2. Tá»‡p exe Ä‘á»‹nh nghÄ©a tÆ°á»�ng minh
        // Ex: g++ StackLinkedList.o main.o -o main.exe
        p = Pattern.compile("\\s+-o\\s+([a-zA-Z0-9\\.]+)\\.exe");
        m = p.matcher(makefileContent);
        if (m.find())
        {
            logger.debug("Exe name: " + m.group(1));
            return m.group(1) + ".exe";
        }

        // TH 3. Tá»‡p exe Ä‘á»‹nh nghÄ©a khÃ´ng tÆ°á»�ng
        // minh
        p = Pattern.compile("BIN\\s+=\\s+([^\\.]+)\\.exe");
        m = p.matcher(makefileContent);
        if (m.find())
        {
            logger.debug("Exe name: " + m.group(1));
            return m.group(1) + ".exe";
        }
        return "";
    }

    /**
     * Get name of variable Ex: "a[2]" ----->"a" Ex: "a.b[2]" ----->"a.b"
     *
     * @param variableName
     * @return
     */
    public static String getNameVariable(String variableName)
    {
        if (variableName.endsWith("]") && variableName.contains("["))
        {
            return variableName.substring(0, variableName.lastIndexOf("["));
        }
        else
        {
            return variableName;
        }
    }

    public static INode getRoot(INode n)
    {
        if (n == null)
        {
            return null;
        }
        else if (n.getParent() == null)
        {
            return n;
        }
        else
        {
            return Utils.getRoot(n.getParent());
        }

    }

    /**
     * Láº¥y danh sÃ¡ch CPPASTSimpleDeclSpecifier trong má»™t node AST
     *
     * @param ast
     * @return
     */
    public static List<CPPASTSimpleDeclSpecifier> getSimpleDeclSpecifiers(IASTNode ast)
    {
        List<CPPASTSimpleDeclSpecifier> cppSimpleDeclSpecifiers = new ArrayList<>();

        ASTVisitor visitor = new ASTVisitor()
        {
            @Override
            public int visit(IASTDeclSpecifier declSpec)
            {
                if (declSpec instanceof CPPASTSimpleDeclSpecifier)
                {
                    cppSimpleDeclSpecifiers.add((CPPASTSimpleDeclSpecifier) declSpec);
                }
                return super.visit(declSpec);
            }
        };

        visitor.shouldVisitDeclSpecifiers = true;

        ast.accept(visitor);
        return cppSimpleDeclSpecifiers;
    }

    /**
     * Get the source code file containing a specified node
     *
     * @param n
     * @return
     */
    public static ISourcecodeFileNode getSourcecodeFile(INode n)
    {
        if (n == null)
        {
            return null;
        }
        else if (n instanceof ISourcecodeFileNode)
        {
            return (ISourcecodeFileNode) n;
        }
        else
        {
            return Utils.getSourcecodeFile(n.getParent());
        }

    }

    public static INode getProjectNode(INode n)
    {
        if (n == null)
        {
            return null;
        }
        else if (n instanceof IProjectNode)
        {
            return n;
        }
        else
        {
            return Utils.getProjectNode(n.getParent());
        }
    }

    /**
     * Get the file node contain the given node
     *
     * @param n
     * @return
     */
    public static INode getFileNode(INode n)
    {
        if (n == null)
        {
            return null;
        }
        else if (new File(n.getAbsolutePath()).exists())
        {
            return n;
        }
        else
        {
            return Utils.getSourcecodeFile(n.getParent());
        }

    }

    /**
     * Lay node cha la class | struct
     */
    public static INode getStructureParent(INode n)
    {
        if (n == null)
        {
            return null;
        }
        else if (n instanceof ClassNode || n instanceof StructNode)
        {
            return n;
        }
        else
        {
            return Utils.getStructureParent(n.getParent());
        }

    }

    public static <T extends Object> boolean isAvailable(List<T> l)
    {
        if (l == null || l.size() == 0)
        {
            return false;
        }
        return true;
    }

    public static boolean isAvailable(String s)
    {
        if (s == null || s.length() == 0)
        {
            return false;
        }
        return true;
    }

    public static boolean isSpecialChInVisibleRange(int ASCII)
    {
        return ASCII == 34 /* nhay kep */ || ASCII == 92 /* gach cheo */
                || ASCII == 39 /* nhay don */;
    }

    /**
     * Check whether the character corresponding to ASCII can be shown in screen
     * or not
     *
     * @param ASCII
     * @return
     */
    public static boolean isVisibleCh(int ASCII)
    {
        return ASCII >= 32 && ASCII <= 126;
    }

    public static String putInSingleQuote(Character c)
    {
        return "'" + c + "'";
    }

    public static String putInString(String str)
    {
        return "\"" + str + "\"";
    }

    public static String readFileContent(File file)
    {
        return Utils.readFileContent(file.getAbsolutePath());
    }

    public static String readFileContent(INode n)
    {
        return Utils.readFileContent(n.getAbsolutePath());
    }

    /**
     * Doc noi dung file
     *
     * @param filePath duong dan tuyet doi file
     * @return noi dung file
     */
    public static String readFileContent(String filePath)
    {
        StringBuilder fileData = new StringBuilder(3000);
        try
        {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(filePath));
            char[] buf = new char[10];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1)
            {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            return fileData.toString();
        }
    }

    /**
     * Cusom splitting
     *
     * @param str
     * @param delimiter
     * @return
     */
    public static List<String> split(String str, String delimiter)
    {
        List<String> output = new ArrayList<>();

        /**
         * In case we need split a path into tokens
         */
        if (delimiter.equals("\\") || delimiter.equals("/"))
        {
            str = str.replace("\\", "/");
            delimiter = "/";
        }
        if (str.contains(delimiter))
        {

            String[] elements = str.split(delimiter);
            for (String element : elements)
            /**
             * Nhá»¯ng xÃ¢u cÃ³ Ä‘á»™ dÃ i báº±ng 0 thÃ¬ bá»� qua
             */

            {
                if (element.length() > 0)
                {
                    output.add(element);
                }
            }

        }
        else
        {
            output.add(str);
        }
        return output;
    }

    public static int toInt(String str)
    {
        /**
         * Remove bracket from negative number. Ex: Convert (-2) into -2
         */
        str = str.replaceAll("\\((" + IRegex.NUMBER_REGEX + ")\\)", "$1");

        /**
         *
         */
        boolean isNegative = false;
        if (str.startsWith("-"))
        {
            str = str.substring(1);
            isNegative = true;
        }
        else if (str.startsWith("+"))
        {
            str = str.substring(1);
        }
        /**
         *
         */
        int n;
        try
        {
            n = Integer.parseInt(str);
            if (isNegative)
            {
                n = -n;
            }
        }
        catch (Exception e)
        {
            n = Utils.UNDEFINED_TO_INT;
        }
        return n;
    }

    public static double toDouble(String str)
    {
        /**
         * Remove bracket from negative number. Ex: Convert (-2) into -2
         */
        str = str.replaceAll("\\((" + IRegex.NUMBER_REGEX + ")\\)", "$1");

        /**
         *
         */
        boolean isNegative = false;
        if (str.startsWith("-"))
        {
            str = str.substring(1);
            isNegative = true;
        }
        else if (str.startsWith("+"))
        {
            str = str.substring(1);
        }
        /**
         *
         */
        double n;
        try
        {
            n = Double.parseDouble(str);
            if (isNegative)
            {
                n = -n;
            }
        }
        catch (Exception e)
        {
            n = Utils.UNDEFINED_TO_DOUBLE;
        }
        return n;
    }

    public static String toUpperFirstCharacter(String str)
    {
        String output = "";
        char[] c = str.toCharArray();

        output = (c[0] + "").toUpperCase();
        for (int i = 1; i < c.length; i++)
        {
            output += c[i];
        }

        return output;
    }

    /**
     * Specify type of project that belong to which IDE (e.g., Eclipse, Dev-Cpp,
     * Code block, Visual studio)
     *
     * @param projectPath : path of Project
     * @return
     */
    public static int getTypeOfProject(String projectPath)
    {
        File dir = new File(projectPath);

        /**
         * Project is created without using IDE or not. It only has a makefile.
         */
        final String[] MAKEFILE_PROJECT_SIGNAL = new String[]{"Makefile"};
        for (String signal : MAKEFILE_PROJECT_SIGNAL)
        {
            if (new File(projectPath + File.separator + signal).exists())
            {
                logger.debug("Is custom makefile project");
                return ISettingv2.PROJECT_CUSTOMMAKEFILE;
            }
        }

        /**
         * Project is created by using IDE Dev-Cpp
         */
        final String[] DEV_CPP_PROJECT_SIGNAL = new String[]{".win"};
        for (String signal : DEV_CPP_PROJECT_SIGNAL)
        {
            if (dir.listFiles((FilenameFilter) (dir1, name) -> name.endsWith(signal)).length > 0)
            {
                logger.debug("Is DevCpp project");
                return ISettingv2.PROJECT_DEV_CPP;
            }
        }

        /**
         * Project is created by using IDE Code block
         */
        final String[] CODE_BLOCK_PROJECT_SIGNAL = new String[]{".cbp"};
        for (String signal : CODE_BLOCK_PROJECT_SIGNAL)
        {
            if (dir.listFiles((FilenameFilter) (dir1, name) -> name.endsWith(signal)).length > 0)
            {
                logger.debug("Is code block project");
                return ISettingv2.PROJECT_CODEBLOCK;
            }
        }

        /**
         * Project is created by using IDE Visual Studio
         */
        final String[] CODE_VISUAL_STUDIO_PROJECT_SIGNAL = new String[]{".vcxproj", ".sln"};
        for (String signal : CODE_VISUAL_STUDIO_PROJECT_SIGNAL)
        {
            if (dir.listFiles((FilenameFilter) (dir1, name) -> name.endsWith(signal)).length > 0)
            {
                logger.debug("Is Visual studio project");
                return ISettingv2.PROJECT_VISUALSTUDIO;
            }
        }

        /**
         * Project is created by using IDE Eclipse
         */
        final String[] ECLIPSE_STUDIO_PROJECT_SIGNAL = new String[]{".cproject", ".project"};
        for (String signal : ECLIPSE_STUDIO_PROJECT_SIGNAL)
        {
            if (dir.listFiles((FilenameFilter) (dir1, name) -> name.endsWith(signal)).length > 0)
            {
                logger.debug("Is Eclipse project");
                return ISettingv2.PROJECT_ECLIPSE;
            }
        }

        return ISettingv2.PROJECT_UNKNOWN_TYPE;
    }

    public static boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException
    {
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);

        do
        {
            try
            {
                return true;
            }
            catch (IllegalThreadStateException ex)
            {
                if (rem > 0)
                {
                    Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
                }
            }
            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        }
        while (rem > 0);
        return false;
    }

    public static void writeContentToFile(String content, INode n)
    {
        Utils.writeContentToFile(content, n.getAbsolutePath());
    }

    public static void writeContentToFile(String content, String filePath)
    {
        try
        {
            String folder = new File(filePath).getParent();
            Utils.createFolder(folder);
            PrintWriter out = new PrintWriter(filePath);
            out.println(content);
            out.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public static INode findRootProject(INode currentNode)
    {
        if (currentNode.getParent() == null)
        {
            return currentNode;
        }
        else if (Utils.isProjectNode(currentNode))
        {
            return currentNode;
        }
        else
        {
            return Utils.findRootProject(currentNode.getParent());
        }
    }

    public static boolean isProjectNode(INode node)
    {
        for (INode nodeChild : node.getChildren())
        {
            if (nodeChild.getParent() == null || nodeChild.getNewType().endsWith(".vcxproj")
                    || nodeChild.getNewType().endsWith("win"))
            {
                return true;
            }
        }
        return false;
    }

    public static List<String> findHeaderFiles(String pathProject)
    {
        List<String> pathSourceCodeFiles = new ArrayList<>();
        IProjectNode root = new ProjectLoader().load(new File(pathProject));

        List<INode> nodeSourceCodeFiles = Search.searchNodes(root, new SourcecodeFileNodeCondition());

        for (INode temp : nodeSourceCodeFiles)
        {
            if (temp.getAbsolutePath().endsWith(HeaderNode.HEADER_SIGNALS))
            {
                pathSourceCodeFiles.add(temp.getAbsolutePath().replace("\\", "/"));
            }
        }

        return pathSourceCodeFiles;
    }

    public static List<String> findSourcecodeFiles(String pathProject)
    {
        List<String> pathSourceCodeFiles = new ArrayList<>();
        IProjectNode root = new ProjectLoader().load(new File(pathProject));

        List<INode> nodeSourceCodeFiles = Search.searchNodes(root, new SourcecodeFileNodeCondition());

        for (INode temp : nodeSourceCodeFiles)
        {
            if (temp.getAbsolutePath().endsWith(".cpp") || temp.getAbsolutePath().endsWith(".c"))
            {
                pathSourceCodeFiles.add(temp.getAbsolutePath().replace("\\", "/"));
            }
        }

        return pathSourceCodeFiles;
    }

    /**
     * @param expression
     * @return
     * @see #{CustomJevalTest.java}
     */
    public static String transformFloatNegativeE(String expression)
    {
        Matcher m = Pattern.compile("\\d+E-\\d+").matcher(expression);
        while (m.find())
        {
            String beforeE = expression.substring(0, expression.indexOf("E-"));
            String afterE = expression.substring(expression.indexOf("E-") + 2);

            String newValue = "";

            if (Utils.toInt(afterE) != Utils.UNDEFINED_TO_INT)
            {
                int numDemicalPoint = Utils.toInt(afterE);

                if (numDemicalPoint == 0)
                {
                    newValue = beforeE;

                }
                else if (beforeE.length() > numDemicalPoint)
                {
                    for (int i = 0; i < beforeE.length() - numDemicalPoint; i++)
                    {
                        newValue += beforeE.toCharArray()[i];
                    }
                    newValue += ".";

                    for (int i = beforeE.length() - numDemicalPoint; i < beforeE.length(); i++)
                    {
                        newValue += beforeE.toCharArray()[i];
                    }
                }
                else
                {
                    newValue += "0.";
                    for (int i = 0; i <= numDemicalPoint - 1 - beforeE.length(); i++)
                    {
                        newValue = newValue + "0";
                    }
                    newValue = newValue + beforeE;
                }
            }

            expression = expression.replace(m.group(0), newValue);
        }
        return expression;
    }

    public static String transformFloatPositiveE(String expression)
    {
        Matcher m = Pattern.compile("\\d+E\\+\\d+").matcher(expression);
        while (m.find())
        {
            String beforeE = expression.substring(0, expression.indexOf("E+"));
            String afterE = expression.substring(expression.indexOf("E+") + 2);

            String newValue = "";

            if (Utils.toInt(afterE) != Utils.UNDEFINED_TO_INT)
            {
                int numDemicalPoint = Utils.toInt(afterE);

                if (numDemicalPoint == 0)
                {
                    newValue = beforeE;

                }
                else
                {
                    newValue = beforeE;
                    for (int i = 0; i < numDemicalPoint; i++)
                    {
                        newValue += "0";
                    }
                }
            }

            expression = expression.replace(m.group(0), newValue);
        }
        return expression;
    }


    public static String readResourceContent(String cTestDriverPath)
    {
        InputStream in = Utils.class.getResourceAsStream(cTestDriverPath);

        StringBuilder template = new StringBuilder();
        String line;

        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while ((line = reader.readLine()) != null)
            {
                template.append(line).append(SpecialCharacter.LINE_BREAK);
            }
        }
        catch (IOException ex)
        {
            logger.error("Cant read resource content from " + "");
        }

        return template.toString();
    }

    public static String insertSpaceToFunctionContent(int expectedStartLine, int expectedNodeOffset, String oldFunction)
    {
        {
            String addition = "";
            for (int i = 0; i < expectedStartLine - 2; i++)
            {
                addition += "\n";
            }
            int additionalOffset = expectedNodeOffset - addition.length() - 1;
            for (int i = 0; i < additionalOffset; i++)
            {
                addition += " ";
            }
            if (expectedStartLine > 1)
            {
                addition += "\n";
            }
            return addition + oldFunction;
        }
    }


    public static String doubleNormalizePath(String path)
    {
        String singleBackSlash = "\\";
        String doubleBackSlash = singleBackSlash + singleBackSlash;
        String singleSlash = "/";

        String result = normalizePath(path);

        if (!File.separator.equals(singleSlash))
        {
            result = result.replace(File.separator, doubleBackSlash);
        }

        return result;
    }

    public static IASTFunctionDefinition disableMacroInFunction(IASTFunctionDefinition astFunctionNode, IFunctionNode functionNode)
    {
        if (astFunctionNode.getFileLocation() == null)
        {
            return astFunctionNode;
        }

        IASTFunctionDefinition output = null;
        // insert spaces to ensure that the location of new function and of the old function are the same
        try
        {
            int startLine = astFunctionNode.getFileLocation().getStartingLineNumber();
            int startOffset = astFunctionNode.getFileLocation().getNodeOffset();
            String content = astFunctionNode.getRawSignature();
            if (functionNode != null && (functionNode instanceof ConstructorNode || functionNode instanceof DestructorNode)
                    && functionNode.getParent() instanceof ClassNode)
            {
                // put the function in a class to void error when constructing ast
                String className = functionNode.getParent().getName();
                content = "class " + className + "{" + content + "};";
                String newContent = Utils.insertSpaceToFunctionContent(startLine,
                        startOffset - new String("class " + className + "{").length(), content);
                IASTTranslationUnit unit = new SourcecodeFileParser().getIASTTranslationUnit(newContent.toCharArray());
                //                logger.debug("Reconstructed tree: ");
//                ASTUtils.printTreeFromAstNode(unit, "\t");
                output = (IASTFunctionDefinition) unit.getChildren()[0].
                        getChildren()[0].getChildren()[1];

            }
            else
            {
                String newContent = Utils.insertSpaceToFunctionContent(startLine, startOffset, content);
                IASTTranslationUnit unit = new SourcecodeFileParser().getIASTTranslationUnit(newContent.toCharArray());
                output = (IASTFunctionDefinition) unit.getChildren()[0];
            }
        }
        catch (Exception e)
        {
//			e.printStackTrace();
            output = astFunctionNode;
        }
        finally
        {
            // log
            if (output == null || output instanceof CPPASTProblemDeclaration || output.getFileLocation() == null ||
                    output.getFileLocation().getStartingLineNumber() != astFunctionNode.getFileLocation().getStartingLineNumber()
                            && output.getFileLocation().getNodeOffset() != astFunctionNode.getFileLocation().getNodeOffset())
            {
                logger.error("Fail to instrument [" + astFunctionNode.getClass() + "] content = " + astFunctionNode.getRawSignature());
            }
            return output;
        }
    }

    public static Compiler getCompiler()
    {
        Compiler compiler = createTemporaryCompiler("[GNU Native] C++ 11");

        compiler.setCompileCommand(AvailableCompiler.CPP_11_GNU_NATIVE.COMPILE_CMD);
        compiler.setPreprocessCommand(AvailableCompiler.CPP_11_GNU_NATIVE.PRE_PRECESS_CMD);
        compiler.setLinkCommand(AvailableCompiler.CPP_11_GNU_NATIVE.LINK_CMD);
        compiler.setDebugCommand(AvailableCompiler.CPP_11_GNU_NATIVE.DEBUG_CMD);
        compiler.setIncludeFlag(AvailableCompiler.CPP_11_GNU_NATIVE.INCLUDE_FLAG);
        compiler.setDefineFlag(AvailableCompiler.CPP_11_GNU_NATIVE.DEFINE_FLAG);
        compiler.setOutputFlag(AvailableCompiler.CPP_11_GNU_NATIVE.OUTPUT_FLAG);
        compiler.setDebugFlag(AvailableCompiler.CPP_11_GNU_NATIVE.DEBUG_FLAG);
        compiler.setOutputExtension(AvailableCompiler.CPP_11_GNU_NATIVE.OUTPUT_EXTENSION);

        return compiler;
    }

    public static boolean isSolutionValid(List<IVariableNode> paramList, String solution)
    {
        solution = solution.replace(";;", ";");
        String[] paramNValue = solution.split(";");

        List<String> paramSolNameList = new ArrayList<>();

        for (String temp : paramNValue)
        {
            String paramName = temp.substring(0, temp.indexOf("="));

            if (paramName.indexOf("[") > 0)
            {
                paramName = paramName.substring(0, paramName.indexOf("["));
            }

            if (!paramSolNameList.contains(paramName))
            {
                paramSolNameList.add(paramName);
            }
        }
        boolean isValid = true;

        for (IVariableNode variableNode: paramList)
        {
            if (!paramSolNameList.contains(variableNode.getName()))
            {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public static String getClonedFilePath(String origin)
    {
        String originName = new File(origin).getName();

        int lastDotPos = originName.lastIndexOf(SpecialCharacter.DOT);

        String clonedName = originName.substring(0, lastDotPos) + ProjectClone.CLONED_FILE_EXTENSION + originName.substring(lastDotPos);

        return TestConfig.INSTRUMENTED_CODE + "\\" + clonedName;
    }


    public static FunctionCoverageComputation ExecuteTestCase(String sourceFolder, String functionName, List<TestData> testCases) throws Exception
    {
        IProjectNode projectNode;

        TestConfig.SetProjectPath(sourceFolder);

        ProjectParser parser = new ProjectParser(new File(sourceFolder));

        projectNode = parser.getRootTree();

        config.Paths.DATA_GEN_TEST = sourceFolder;

        Utils.deleteFilesInFolder(new File(TestConfig.TEST_DRIVER_PATH));
        Utils.deleteFilesInFolder(new File(TestConfig.EXE_PATH));

        List<INode> sources = Search.searchNodes(projectNode, new SourcecodeFileNodeCondition());

        for (INode sourceCode : sources)
        {
            ProjectClone clone = new ProjectClone();
            String uetignoreFilePath = Utils.getClonedFilePath(sourceCode.getAbsolutePath());

            try
            {
                String newContent = clone.generateFileContent(sourceCode);
                Utils.writeContentToFile(newContent, uetignoreFilePath);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        IFunctionNode function;

        function = (IFunctionNode) Search.searchNodes(projectNode, new FunctionNodeCondition(), functionName).get(0);

        TestcaseExecution executor = new TestcaseExecution();
        executor.setFunction(function);

        INode realParent = function.getRealParent();
        String sourceFile = realParent.getAbsolutePath();
        String sourceFileName = realParent.getName();

        executor.setMode(TestcaseExecution.IN_AUTOMATED_TESTDATA_GENERATION_MODE);

        List<TestCase> testCaseList = new ArrayList<>();

        int i = 0;

        for (TestData testData0 : testCases)
        {
            i += 1;
            TestCase testCase = new TestCase();
            testCase.setTestData(testData0);
            testCase.setName(TestConfig.TESTCASE_NAME + i);
            testCase.setFunctionNode(function);
            testCase.setSourcecodeFile(sourceFile);
            testCase.setRealParentSourceFileName(sourceFileName);

            testCaseList.add(testCase);
        }

        executor.execute(testCaseList);
        FunctionCoverageComputation functionCoverageComputation = executor.computeCoverage(function, testCaseList);

        return functionCoverageComputation;

    }

    private static Compiler createTemporaryCompiler(String opt)
    {
        if (opt != null)
        {
            for (Class<?> c : AvailableCompiler.class.getClasses())
            {
                try
                {
                    String name = c.getField("NAME").get(null).toString();

                    if (name.equals(opt))
                    {
                        return new Compiler(c);
                    }
                }
                catch (Exception ex)
                {
                }
            }
        }

        return null;
    }

    public static boolean isC()
    {
        return !getCompiler().getName().contains("C++");
    }

    public static boolean contains(List<IVariableNode> variableNodeList, String paramName)
    {
        boolean found = false;
        for (IVariableNode variableNode : variableNodeList)
        {
            if (variableNode.getName().equals(paramName))
            {
                found = true;
                break;
            }
        }
        return found;
    }

    public static String getVariableType(List<IVariableNode> variableNodeList, String paramName)
    {
        for (IVariableNode variableNode : variableNodeList)
        {
            if (variableNode.getName().equals(paramName))
            {
                return variableNode.getRealType();
            }
        }
        return "";
    }

    public static void sortValueList(List<String> list)
    {
        for (int i = 0; i < list.size(); i++)
        {
            for (int j = i + 1; j < list.size(); j++)
            {
                String siItem = list.get(i);
                String sjItem = list.get(j);
                double iItem = Double.parseDouble(siItem);
                double jItem = Double.parseDouble(sjItem);
                if (iItem > jItem)
                {
                    String temp = siItem;
                    list.remove(i);
                    list.add(i, sjItem);

                    list.remove(j);
                    list.add(j, temp);
                }
            }
        }
    }

    public static List<String> createMidValueList(List<String> list, String paramType)
    {
        List<String> midValueList = new ArrayList<>();
        for (int i = 0; i < list.size() - 1; i++)
        {
            String siItem = list.get(i);
            String siiItem = list.get(i + 1);
            double iItem = Double.parseDouble(siItem);
            double iiItem = Double.parseDouble(siiItem);


            switch (paramType)
            {
                case "int":
                    if (iItem + 1 >= iiItem)
                    {
                        continue;
                    }

                    int mid = (int) ((iItem + iiItem) / 2);

                    midValueList.add(Integer.toString(mid));
                    break;
                case "long":
                    if (iItem + 1 >= iiItem)
                    {
                        continue;
                    }

                    long midLong = (long) ((iItem + iiItem) / 2);

                    midValueList.add(Long.toString(midLong));
                    break;
                case "short":
                    if (iItem + 1 >= iiItem)
                    {
                        continue;
                    }

                    short midshort = (short) ((iItem + iiItem) / 2);

                    midValueList.add(Short.toString(midshort));
                    break;
                case "float":
                    float midfloat = (float) ((iItem + iiItem) / 2);
                    midValueList.add(Float.toString(midfloat));
                    break;
                case "double":
                    double middouble = (double) ((iItem + iiItem) / 2);
                    midValueList.add(Double.toString(middouble));
                    break;
            }
        }

        return midValueList;
    }

    public static List<String> CloneStringList(List<String> list)
    {
        List<String> returnList = new ArrayList<>();

        for (String str : list)
        {
            returnList.add(str);
        }

        return returnList;
    }

    public static List<TestData> generateTestpathsForBoundaryTestGen(ICFG cfg,
                                                                     List<IVariableNode> variables,
                                                                     float boundStep) throws Exception
    {
        List<ICfgNode> list = cfg.getAllNodes();

        //paramValueList chứa các giá trị biên của các biến đầu vào
        Map<String, List<String>> paramValueList = new HashMap<>();

        //paramMidValueList chứa các giá trị cận trên và dưới cho mỗi biên
        Map<String, List<String>> upNDownParamValueList = new HashMap<>();

        //paramMidValueList chứa các giá trị norm (trung bình) của các khoảng giá trị cho mỗi biến
        Map<String, List<String>> paramMidValueList = new HashMap<>();

        for (ICfgNode node : list)
        {
            if (node instanceof ConditionCfgNode && !(node instanceof AbstractConditionLoopCfgNode))
            {
                ConditionCfgNode stm1 = (ConditionCfgNode) node.clone();

                String Content = stm1.getContent();
                Content = Content.replaceAll("<=|>=|<|>|!=", "=");

                String[] varOrVal = Content.split("=");

                if (varOrVal.length != 2)
                {
                    continue;
                }
                String paramName = "";
                String value = "";

                if (Utils.contains(variables,varOrVal[0].trim()))
                {
                    paramName = varOrVal[0].trim();
                    value = varOrVal[1].trim();
                }
                else if (Utils.contains(variables,varOrVal[1].trim()))
                {
                    paramName = varOrVal[1].trim();
                    value = varOrVal[0].trim();
                }
                else
                {
                    continue;
                }

                double val = 0, val1 = 0, val2 = 0;
                try
                {
                    val = Double.parseDouble(value);
                    val1 = Double.parseDouble(value) - boundStep;
                    val2 = Double.parseDouble(value) + boundStep;
                }
                catch (Exception ex)
                {
                    continue;
                }

                if (paramValueList.containsKey(paramName))
                {
                    if (!paramValueList.get(paramName).contains(Double.toString(val)))
                    {
                        paramValueList.get(paramName).add(Double.toString(val));
                    }
                }
                else
                {
                    List<String> valueList = new ArrayList<>();
                    valueList.add(Double.toString(val));

                    paramValueList.put(paramName, valueList);
                }

                if (upNDownParamValueList.containsKey(paramName))
                {
                    if (!upNDownParamValueList.get(paramName).contains(Double.toString(val1)))
                    {
                        upNDownParamValueList.get(paramName).add(Double.toString(val1));
                    }
                    if (!upNDownParamValueList.get(paramName).contains(Double.toString(val2)))
                    {
                        upNDownParamValueList.get(paramName).add(Double.toString(val2));
                    }
                }
                else
                {
                    List<String> valueList = new ArrayList<>();
                    valueList.add(Double.toString(val1));
                    valueList.add(Double.toString(val2));

                    upNDownParamValueList.put(paramName, valueList);
                }
            }
        }

        for(IVariableNode variableNode: variables)
        {
            String max = "", min ="";
            switch (variableNode.getRealType())
            {
                case "int":
                    max = Integer.toString(Integer.MAX_VALUE);
                    min = Integer.toString(Integer.MIN_VALUE);
                    break;
                case "long":
                    max = Long.toString(Long.MAX_VALUE);
                    min = Long.toString(Long.MIN_VALUE);
                    break;
                case "float":
                    max = Float.toString(Float.MAX_VALUE);
                    min = Float.toString(Float.MIN_VALUE);
                    break;
                case "double":
                    max = Double.toString(Double.MAX_VALUE);
                    min = Double.toString(Double.MIN_VALUE);
                    break;
                case "short":
                    max = Short.toString(Short.MAX_VALUE);
                    min = Short.toString(Short.MIN_VALUE);
                    break;
            }

            String paramName = variableNode.getName();

            if (paramValueList.containsKey(paramName))
            {
                if (!paramValueList.get(paramName).contains(max))
                {
                    paramValueList.get(paramName).add(max);
                }
                if (!paramValueList.get(paramName).contains(min))
                {
                    paramValueList.get(paramName).add(min);
                }
            }
            else
            {
                List<String> valueList = new ArrayList<>();
                if (!"".equals(max))
                {
                    valueList.add(max);
                }
                if (!"".equals(min))
                {
                    valueList.add(min);
                }

                paramValueList.put(paramName, valueList);
            }

            Utils.sortValueList(paramValueList.get(paramName));
            List<String> midValueList = Utils.createMidValueList(paramValueList.get(paramName), variableNode.getRealType());
            paramMidValueList.put(paramName, midValueList);
        }

        //merge 2 danh sach nay lai: paramValueList, upNDownParamValueList

        for (Map.Entry entry : paramValueList.entrySet())
        {
            List<String> valueList = (List<String>)entry.getValue();
            List<String> upNDownValueList = upNDownParamValueList.get(entry.getKey());
            if (upNDownValueList != null)
            {
                valueList.addAll(upNDownValueList);
            }

            Utils.sortValueList((List<String>)entry.getValue());
        }

        List<TestData> testDataList = new ArrayList<>();

        for (IVariableNode variableNode: variables)
        {
            //get list of mid value

            Map<String, List<String>> otherVarMidValueList = new HashMap<>();

            for (IVariableNode midValueNode: variables)
            {
                if (!midValueNode.getName().equals(variableNode.getName()))
                {
                    otherVarMidValueList.put(midValueNode.getName(), paramMidValueList.get(midValueNode.getName()));
                }
            }

            List<String> listValue = paramValueList.get(variableNode.getName());

            List<TestData> testDataListforBoundValue = new ArrayList<>();

            for (String value: listValue)
            {
                TestData testData = new TestData();

                testData.add(new Pair<>(variableNode.getName(), value));

                testDataListforBoundValue.add(testData);
            }

            for (Map.Entry entry : otherVarMidValueList.entrySet()) {
                List<TestData> newList = new ArrayList<>();

                newList = CombineTestDataList(testDataListforBoundValue, entry.getKey().toString(), Utils.CloneStringList ((List<String>)entry.getValue()));

                testDataListforBoundValue = newList;
            }

            testDataList.addAll(testDataListforBoundValue);
        }

        return testDataList;
    }

    public static List<TestData> CombineTestDataList(List<TestData> testDataList, String paramName, List<String> valueList)
    {
        List<TestData> returnList = new ArrayList<>();

        for (TestData testData: testDataList)
        {
            List<TestData> newList = new ArrayList<>();

            for (String value: valueList)
            {
                TestData testData1 = testData.clone();

                testData1.add(new Pair<>(paramName, value));

                newList.add(testData1);
            }

            returnList.addAll(newList);
        }

        return returnList;
    }
}