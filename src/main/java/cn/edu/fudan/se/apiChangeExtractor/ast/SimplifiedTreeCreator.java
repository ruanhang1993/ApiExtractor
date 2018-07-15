package cn.edu.fudan.se.apiChangeExtractor.ast;

import japa.parser.ast.Node;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.comments.BlockComment;
import japa.parser.ast.comments.LineComment;
import japa.parser.ast.expr.*;
import japa.parser.ast.expr.BinaryExpr.Operator;
import japa.parser.ast.stmt.*;
import sav.common.core.utils.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class SimplifiedTreeCreator extends TreeConverter {
	private String classNameMap;
	private String typeCast;
    private CodeTree codeTree = new CodeTree();
    private TreeNode lastNode = codeTree.getRoot();
    private Map<String, String> class_variable = new HashMap<String, String>();
    private List<String> class_variable_list = new ArrayList<>();
    private Map<String, String> class_name_map = new HashMap<>();
    private List<String> completeClassList = new ArrayList<>();
    private Map<String, Boolean> castMap = new HashMap<>();
    private boolean parsedFlag = true;// this field is used to judge whether the method can be correctly parsed
    private String returnType = null;
    private List<String> starImportStringList = new ArrayList<>();
    private UserClassProcessing userClassProcessing;
    private boolean endFlag = true;
    private boolean holeFlag = false;
    // next node should be added
    // twice time (false and
    // true separately)
    private boolean elseIfFlag = false;//used to judge whether a node is a else if node

    public void setUserClassProcessing(UserClassProcessing userClassProcessing) {
        this.userClassProcessing = userClassProcessing;
    }

    public Map<String, String> getClass_variable() {
        return class_variable;
    }

    public List<String> getClass_variable_list() {
        return class_variable_list;
    }

    public Map<String, String> getClass_name_map() {
        return class_name_map;
    }

    public List<String> getStarImportStringList() {
        return starImportStringList;
    }

    public void setStarImportStringList(List<String> starImportStringList) {
        this.starImportStringList = starImportStringList;
    }

    public void setHoleFlag(boolean holeFlag) {
        this.holeFlag = holeFlag;
    }

    public void addClass_variable(String variableName, String type) {
        class_variable.put(variableName, type);
    }

    public void addClass_variable_list(String variable) {
        class_variable_list.add(variable);
    }

    public boolean getParsedFlag() {
        return parsedFlag;
    }

	public void addClass_name_map(String type) {
        if (class_name_map.get(type) == null) {
            class_name_map.put(type, type);
        }
    }

    public SimplifiedTreeCreator(String globalPath) {
    	classNameMap = globalPath +System.getProperty("user.dir")+ "/src/main/java/resources/class_name_map.config";
    	typeCast= globalPath +System.getProperty("user.dir")+ "/src/main/java/resources/type_cast.config";
        try {
            File fileClassNameMap = new File(classNameMap);
            FileInputStream fileInputStream = new FileInputStream(fileClassNameMap);
            Scanner scanner = new Scanner(fileInputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Scanner lineScanner = new Scanner(line);
                class_name_map.put(lineScanner.next(), lineScanner.next());
                lineScanner.close();
            }
            scanner.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SimplifiedTreeCreator(List<String> completeClassNameList, SimplifiedTreeCreator creator, String globalPath) {
    	classNameMap = globalPath +System.getProperty("user.dir")+ "/src/main/java/resources/class_name_map.config";
    	typeCast= globalPath +System.getProperty("user.dir")+ "/src/main/java/resources/type_cast.config";
        class_name_map = creator.getClass_name_map();
        class_variable_list = creator.getClass_variable_list();
        class_variable = creator.getClass_variable();
        completeClassList = completeClassNameList;
        for (int i = 0; i < completeClassNameList.size(); i++) {
            try {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(completeClassNameList.get(i));
                class_name_map.put(clazz.getSimpleName(), completeClassNameList.get(i));
            } catch (Exception e) {
                if (!(e instanceof ClassNotFoundException)) {
                    parsedFlag = false;
                }
                System.err.println(e.getMessage());
            } catch (Error e) {
                parsedFlag = false;
                System.err.println(e.getMessage());
            }

        }

        try {
            File fileTypeCast = new File(typeCast);
            FileInputStream fileInputStream = new FileInputStream(fileTypeCast);
            Scanner scanner = new Scanner(fileInputStream);
            while (scanner.hasNextLine()) {
                castMap.put(scanner.nextLine(), true);
            }

            File fileClassNameMap = new File(classNameMap);
            fileInputStream = new FileInputStream(fileClassNameMap);
            scanner = new Scanner(fileInputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Scanner lineScanner = new Scanner(line);
                class_name_map.put(lineScanner.next(), lineScanner.next());
                lineScanner.close();
            }
            scanner.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public CodeTree getCodeTree() {
        return codeTree;
    }

    public void setCodeTree(CodeTree codeTree) {
        this.codeTree = codeTree;
    }

    public CodeTree toCodeTree(Node node) {
        if (node != null && parsedFlag) {
            node.accept(this, null);
            return codeTree;
        }
        return codeTree;
    }

    @Override
    protected CodeTree convert(MethodDeclaration n) {
        toCodeTree(n.getBody());
        return codeTree;
    }

    @Override
    protected CodeTree convert(AssertStmt n) {
        return codeTree;
    }

    @Override
    protected CodeTree convert(BlockStmt n) {
        List<Node> stmts = n.getChildrenNodes();
        return convert(stmts);
    }

    @Override
    protected CodeTree convert(LineComment n) {
        if (holeFlag && codeTree.getHoleNode() == null) {
            String str = n.toString();
            str = str.replaceAll("\n", "");
            str = str.replaceAll(" ", "");
            if (str.equals("//hole")) {
                TreeNode node = new TreeNode();
                node.setCompleteMethodDeclaration("//hole");
                addNode(n.toString(),node,n.getBeginLine());
            }
        }
        return codeTree;
    }

    @Override
    protected CodeTree convert(BreakStmt n) {
        TreeNode node = new TreeNode();
        setNodeClassAndMethod(node, "break", "break", "", "");
        node.setAddMethodName(false);
        node.setExit(false);
        codeTree.addNode("",lastNode, node,-1);
        lastNode = node;
        return codeTree;
    }

    @Override
    protected CodeTree convert(ContinueStmt n) {
        TreeNode node = new TreeNode();
        setNodeClassAndMethod(node, "continue", "continue", "", "");
        node.setAddMethodName(false);
        node.setExit(false);
        codeTree.addNode("",lastNode, node,-1);

        lastNode = node;
        return codeTree;
    }

    @Override
    protected CodeTree convert(DoStmt n) {
        TreeNode node = new TreeNode();
        setNodeClassAndMethod(node, "doWhile", "doWhile", "", "");
        node.setControl(true);
        node.setExit(false);
        codeTree.addNode("",lastNode, node,-1);
        //add condition node
        lastNode = node;
        TreeNode conditionNode = new TreeNode();
        setNodeClassAndMethod(conditionNode, "condition", "condition", "", "");
        conditionNode.setAddMethodName(false);
        codeTree.addNode("",lastNode, conditionNode,-1);
        lastNode = conditionNode;
        dealCondition(n.getCondition(), conditionNode);
        //add end node to represent the end of condition
        lastNode = conditionNode;
        addEndNode();
        // set the current node to be the parent node for the next node
        lastNode = node;
        // deal with the body in do while
        if (n.getBody() != null && n.getBody().getChildrenNodes().size() != 0
                && !isAllAnnotationStmt(n.getBody().getChildrenNodes())) {
            toCodeTree(n.getBody());
        } else if (n.getBody() instanceof ContinueStmt || n.getBody() instanceof BreakStmt || n.getBody() instanceof ReturnStmt) {
            toCodeTree(n.getBody());
        } else {
            TreeNode emptyNode = new TreeNode();
            emptyNode.setAddMethodName(false);
            setNodeClassAndMethod(emptyNode, "EmptyStatement", "EmptyStatement", "", "");
            node.getChildNodes().add(emptyNode);
            lastNode = emptyNode;
        }
        // add end node
        if (endFlag) {
            addEndNode();
        } else {
            endFlag = true;
        }
        lastNode = node;
        return codeTree;
    }

    @Override
    protected CodeTree convert(EmptyStmt n) {
        //System.out.println(n);
        return codeTree;

    }

    @Override
    public CodeTree convert(ExpressionStmt n) {
        Expression expr = n.getExpression();
        if (expr != null) {
            if (expr instanceof VariableDeclarationExpr) {
                convert((VariableDeclarationExpr) expr);
            } else if (expr instanceof MethodCallExpr) {
                convert((MethodCallExpr) expr);
            } else if (expr instanceof AssignExpr) {
                convert((AssignExpr) expr);
            } else if (expr instanceof UnaryExpr) {
                convert((UnaryExpr) expr);
            } else if (expr instanceof FieldAccessExpr) {
                convert((FieldAccessExpr) expr);
            } else if (expr instanceof EnclosedExpr) {
                convert((EnclosedExpr) expr);
            } else {
                // to do deal with return variable
            }
        }
        return codeTree;
    }

    @Override
    protected CodeTree convert(ForeachStmt n) {
        ForStmt forStmt = ForeachConverter.toForStmt(n);
        if (forStmt != null) {
            if (n.getVariable().getVars().size() > 1) {
                parsedFlag = false;
                System.err.println(n.getVariable() + " " + "can not be parsed");
            } else {
                String temporaryVariable;
                //deal with for each
                TreeNode node = new TreeNode();
                setNodeClassAndMethod(node, "foreach", "foreach", "", "");
                node.setControl(true);
                node.setExit(false);
                codeTree.addNode("",lastNode, node,-1);
                //add condition node
                lastNode = node;
                TreeNode conditionNode = new TreeNode();
                setNodeClassAndMethod(conditionNode, "condition", "condition", "", "");
                conditionNode.setAddMethodName(false);
                codeTree.addNode("",lastNode, conditionNode,-1);
                lastNode = conditionNode;
                // deal with the condition in for each
            /*deal with variable declaration on the left of ":"*/
                TreeNode tempNode = lastNode;
                convert(n.getVariable());
                if (tempNode.equals(lastNode)) {
                    TreeNode temp = userClassProcessing.createVariableDeclarationNode();
                    addNode(n.toString(),temp,n.getBeginLine());
                }
                lastNode.setCondition(true);
                temporaryVariable = class_variable_list.get(class_variable_list.size() - 1);
            /*add ":" node*/
                TreeNode colonNode = new TreeNode();
                colonNode.setCondition(true);
                setNodeClassAndMethod(colonNode, ":", ":", "", "");
                colonNode.setAddMethodName(false);
                codeTree.addNode("",lastNode, colonNode,-1);
                lastNode = colonNode;
            /*deal with the variable on the right of ":"*/
                dealForEachAndSwitchIterable(n.getIterable());
                //add end node to represent the end of condition
                lastNode = conditionNode;
                addEndNode();
                //deal with the body of for each
                lastNode = node;
                if (forStmt.getBody() != null && forStmt.getBody().getChildrenNodes().size() != 1
                        && !isAllAnnotationStmt(forStmt.getBody().getChildrenNodes())) {
                    toCodeTree(forStmt.getBody().getChildrenNodes().get(1));
                } else {
                    TreeNode emptyNode = new TreeNode();
                    emptyNode.setAddMethodName(false);
                    setNodeClassAndMethod(emptyNode, "EmptyStatement", "EmptyStatement", "", "");
                    node.getChildNodes().add(emptyNode);
                    lastNode = emptyNode;
                }
                //add end node
                if (endFlag) {
                    addEndNode();
                } else {
                    endFlag = true;
                }
                lastNode = node;
                //remove the temporary variable
                String type = class_variable.get(temporaryVariable);
                class_variable.remove(temporaryVariable, type);
                class_variable_list.remove(class_variable_list.indexOf(temporaryVariable));
            }
        } else {
            parsedFlag = false;
            System.err.println(n + " can not be parsed");
        }
        return codeTree;
    }

    @Override
    protected CodeTree convert(ForStmt n) {
        TreeNode node = new TreeNode();
        setNodeClassAndMethod(node, "for", "for", "", "");
        node.setControl(true);
        node.setExit(false);
        codeTree.addNode("",lastNode, node,-1);

        // add condition node
        lastNode = node;
        TreeNode conditionNode = new TreeNode();
        setNodeClassAndMethod(conditionNode, "condition", "condition", "", "");
        conditionNode.setAddMethodName(false);
        codeTree.addNode("",lastNode, conditionNode,-1);
        lastNode = conditionNode;
        List<String> variableList = new ArrayList<>();
        //deal with condition in for
        /*deal with init*/
        dealForInitCondition(n, variableList);
       /*add first ";"*/
        TreeNode semicolonNode = new TreeNode();
        setNodeClassAndMethod(semicolonNode, ";", ";", "", "");
        semicolonNode.setCondition(true);
        semicolonNode.setAddMethodName(false);
        codeTree.addNode("",conditionNode, semicolonNode,-1);
        lastNode = semicolonNode;
        /*deal with compare*/
        if (n.getCompare() != null) {
            dealCondition(n.getCompare(), lastNode);
        } else {
            TreeNode emptyNode = new TreeNode();
            setNodeClassAndMethod(emptyNode, "EmptyCondition", "EmptyCondition", "", "");
            emptyNode.setAddMethodName(false);
            emptyNode.setCondition(true);
            codeTree.addNode("",lastNode, emptyNode,-1);
            lastNode = emptyNode;
        }
        /*add second ";"*/
        TreeNode secondSemicolonNode = new TreeNode();
        setNodeClassAndMethod(secondSemicolonNode, ";", ";", "", "");
        secondSemicolonNode.setCondition(true);
        secondSemicolonNode.setAddMethodName(false);
        codeTree.addNode("",conditionNode, secondSemicolonNode,-1);
        lastNode = secondSemicolonNode;
        /*deal with update*/
        if (n.getUpdate() != null) {
            for (int i = 0; i < n.getUpdate().size(); i++) {
                if (i > 0) {
                    //add ","
                    TreeNode commaNode = new TreeNode();
                    setNodeClassAndMethod(commaNode, ",", ",", "", "");
                    commaNode.setCondition(true);
                    commaNode.setAddMethodName(false);
                    codeTree.addNode("",lastNode, commaNode,-1);
                    lastNode = commaNode;
                }
                dealCondition(n.getUpdate().get(i), lastNode);
            }
        } else {
            TreeNode emptyNode = new TreeNode();
            setNodeClassAndMethod(emptyNode, "EmptyCondition", "EmptyCondition", "", "");
            emptyNode.setAddMethodName(false);
            emptyNode.setCondition(true);
            codeTree.addNode("",lastNode, emptyNode,-1);
            lastNode = emptyNode;
        }
        // add end node
        lastNode = conditionNode;
        addEndNode();
        // deal with the body in for
        lastNode = node;
        if (n.getBody() != null && n.getBody().getChildrenNodes().size() != 0
                && !isAllAnnotationStmt(n.getBody().getChildrenNodes())) {
            toCodeTree(n.getBody());
        } else if (n.getBody() instanceof ContinueStmt || n.getBody() instanceof BreakStmt || n.getBody() instanceof ReturnStmt) {
            toCodeTree(n.getBody());
        } else {
            TreeNode emptyNode = new TreeNode();
            emptyNode.setAddMethodName(false);
            setNodeClassAndMethod(emptyNode, "EmptyStatement", "EmptyStatement", "", "");
            node.getChildNodes().add(emptyNode);
            lastNode = emptyNode;
        }
        // add end node
        if (endFlag) {
            addEndNode();
        } else {
            endFlag = true;
        }
        lastNode = node;
        //remove temporary variales
        if (variableList.size() > 0) {
            for (int i = 0; i < variableList.size(); i++) {
                String type = class_variable.get(variableList.get(i));
                class_variable.remove(variableList.get(i), type);
                class_variable_list.remove(class_variable_list.indexOf(variableList.get(i)));
            }
        }
        return codeTree;
    }

    @Override
    protected CodeTree convert(IfStmt n) {
        TreeNode node = new TreeNode();
        node.setControl(true);
        if (lastNode == null || lastNode.getClassName() == null) {
            setNodeClassAndMethod(node, "if", "if", "", "");

        } else {
            if (lastNode.getClassName().contains("if") && lastNode.isControl() && elseIfFlag) {
                setNodeClassAndMethod(node, "elseif", "elseif", "", "");
            } else {
                setNodeClassAndMethod(node, "if", "if", "", "");
            }
        }
        node.setExit(false);
        codeTree.addNode("",lastNode, node,-1);
        //add condition node
        lastNode = node;
        TreeNode conditionNode = new TreeNode();
        setNodeClassAndMethod(conditionNode, "condition", "condition", "", "");
        conditionNode.setAddMethodName(false);
        codeTree.addNode("",lastNode, conditionNode,-1);
        lastNode = conditionNode;
        dealCondition(n.getCondition(), conditionNode);
        //add end node to represent the end of condition
        lastNode = conditionNode;
        addEndNode();
        // deal with ifthen body
        lastNode = node;
        elseIfFlag = false;
        if (n.getThenStmt() != null && n.getThenStmt().getChildrenNodes().size() != 0
                && !isAllAnnotationStmt(n.getThenStmt().getChildrenNodes())) {
            toCodeTree(n.getThenStmt());
        } else if (n.getThenStmt() instanceof ContinueStmt || n.getThenStmt() instanceof BreakStmt || n.getThenStmt() instanceof ReturnStmt) {
            toCodeTree(n.getThenStmt());
        } else {
            TreeNode emptyNode = new TreeNode();
            emptyNode.setAddMethodName(false);
            setNodeClassAndMethod(emptyNode, "EmptyStatement", "EmptyStatement", "", "");
            node.getChildNodes().add(emptyNode);
            lastNode = emptyNode;
        }

        // deal with elsethen body
        if (n.getElseStmt() != null) {
            lastNode = node;
            if (n.getElseStmt() instanceof IfStmt) {
                elseIfFlag = true;
                toCodeTree(n.getElseStmt());
                lastNode = node;
            } else {
                elseIfFlag = false;
                TreeNode elseNode = new TreeNode();
                setNodeClassAndMethod(elseNode, "else", "else", "", "");
                elseNode.setControl(true);
                node.getChildNodes().add(elseNode);
                lastNode = elseNode;
                if (n.getElseStmt() != null && n.getElseStmt().getChildrenNodes().size() != 0
                        && !isAllAnnotationStmt(n.getElseStmt().getChildrenNodes())) {
                    toCodeTree(n.getElseStmt());
                } else if (n.getElseStmt() instanceof ContinueStmt || n.getElseStmt() instanceof BreakStmt || n.getElseStmt() instanceof ReturnStmt) {
                    toCodeTree(n.getElseStmt());
                } else {
                    TreeNode emptyNode = new TreeNode();
                    emptyNode.setAddMethodName(false);
                    setNodeClassAndMethod(emptyNode, "EmptyStatement", "EmptyStatement", "", "");
                    elseNode.getChildNodes().add(emptyNode);
                    lastNode = emptyNode;
                }
                //add end node
                if (endFlag) {
                    addEndNode();
                } else {
                    endFlag = true;
                }
                //lastNode = elseNode;
                lastNode = node;
            }
        } else {
            //add end node
            if (endFlag) {
                addEndNode();
            } else {
                endFlag = true;
            }
            lastNode = node;
            elseIfFlag = false;
        }
        
        return codeTree;
    }

    @Override
    protected CodeTree convert(LabeledStmt n) {
        parsedFlag = false;
        System.err.println(n + " " + "can not be parsed");
        return codeTree;
    }

    @Override
    protected CodeTree convert(ReturnStmt n) {
        TreeNode node = new TreeNode();
        setNodeClassAndMethod(node, "return", "return", "", "");
        node.setAddMethodName(false);
        node.setExit(true);
        codeTree.addNode("",lastNode, node,-1);

        lastNode = node;
        return codeTree;
    }

    @Override
    protected CodeTree convert(SynchronizedStmt n) {
        parsedFlag = false;
        System.err.println(n + " " + "can not be parsed");
        return codeTree;
    }

    @Override
    protected CodeTree convert(TryStmt n) {
        //add try node
        TreeNode tryNode = new TreeNode();
        setNodeClassAndMethod(tryNode, "try", "try", "", "");
        tryNode.setControl(true);
        codeTree.addNode("",lastNode, tryNode,-1);
        //get the body of try node
        if (n.getResources().size() == 0) {
            if (n.getTryBlock().getChildrenNodes().size() == 0 || isAllAnnotationStmt(n.getTryBlock().getChildrenNodes())) {
                TreeNode tryStmtNode = new TreeNode();
                setNodeClassAndMethod(tryStmtNode, "EmptyStatement", "EmptyStatement", "", "");
                tryStmtNode.setAddMethodName(false);
                codeTree.addNode("",tryNode, tryStmtNode,-1);
                lastNode = tryStmtNode;
            } else {
                lastNode = tryNode;
                toCodeTree(n.getTryBlock());
            }
            // add catch node
            lastNode = tryNode;
            TreeNode catchNode = new TreeNode();
            setNodeClassAndMethod(catchNode, "catch", "catch", "", "");
            catchNode.setControl(true);
            TreeNode catchStmtNode = new TreeNode();
            setNodeClassAndMethod(catchStmtNode, "EmptyStatement", "EmptyStatement", "", "");
            catchStmtNode.setAddMethodName(false);
            codeTree.addNode("",catchNode, catchStmtNode,-1);
            codeTree.addNode("",lastNode, catchNode,-1);
            // add finally node if exits
            lastNode = catchNode;
            if (n.getFinallyBlock() != null) {
                TreeNode finallyNode = new TreeNode();
                setNodeClassAndMethod(finallyNode, "finally", "finally", "", "");
                finallyNode.setControl(true);
                codeTree.addNode("",lastNode, finallyNode,-1);
                lastNode = finallyNode;
                if (n.getFinallyBlock().getChildrenNodes().size() == 0 || isAllAnnotationStmt(n.getFinallyBlock().getChildrenNodes())) {
                    TreeNode finallyStmtNode = new TreeNode();
                    setNodeClassAndMethod(finallyStmtNode, "EmptyStatement", "EmptyStatement", "", "");
                    finallyStmtNode.setAddMethodName(false);
                    codeTree.addNode("",finallyNode, finallyStmtNode,-1);
                    lastNode = finallyStmtNode;
                } else {
                    toCodeTree(n.getFinallyBlock());
                }
                // add end node
                if (endFlag) {
                    addEndNode();
                } else {
                    endFlag = true;
                }
                lastNode = finallyNode;
            } else {
                // add end node
                lastNode = catchStmtNode;
                addEndNode();
                lastNode = catchNode;
            }
        } else {
            parsedFlag = false;
            System.err.println(n + " " + "can not be parsed");
        }
        return codeTree;
    }

    @Override
    protected CodeTree convert(TypeDeclarationStmt n) {
        return codeTree;
    }

    @Override
    protected CodeTree convert(WhileStmt n) {
        TreeNode node = new TreeNode();
        setNodeClassAndMethod(node, "while", "while", "", "");
        node.setControl(true);
        node.setExit(false);
        codeTree.addNode("",lastNode, node,-1);
        //add condition node
        lastNode = node;
        TreeNode conditionNode = new TreeNode();
        setNodeClassAndMethod(conditionNode, "condition", "condition", "", "");
        conditionNode.setAddMethodName(false);
        codeTree.addNode("",lastNode, conditionNode,-1);
        lastNode = conditionNode;
        dealCondition(n.getCondition(), conditionNode);
        //add end node to represent the end of condition
        lastNode = conditionNode;
        addEndNode();
        lastNode = node;
        if (n.getBody() != null && n.getBody().getChildrenNodes().size() != 0
                && !isAllAnnotationStmt(n.getBody().getChildrenNodes())) {
            toCodeTree(n.getBody());
        } else if (n.getBody() instanceof ContinueStmt || n.getBody() instanceof BreakStmt || n.getBody() instanceof ReturnStmt) {
            toCodeTree(n.getBody());
        } else {
            TreeNode emptyNode = new TreeNode();
            emptyNode.setAddMethodName(false);
            setNodeClassAndMethod(emptyNode, "EmptyStatement", "EmptyStatement", "", "");
            node.getChildNodes().add(emptyNode);
            lastNode = emptyNode;
        }

        //add end node
        if (endFlag) {
            addEndNode();
        } else {
            endFlag = true;
        }
        lastNode = node;
        //   contorlStatementFlag = true;
        //   controlNode = node;

        return codeTree;
    }

    @Override
    protected CodeTree convert(ExplicitConstructorInvocationStmt n) {
        parsedFlag = false;
        System.err.println(n + " " + "can not be parsed");
        return codeTree;
    }

    @Override
    protected CodeTree convert(SwitchStmt n) {
        TreeNode switchNode = new TreeNode();
        setNodeClassAndMethod(switchNode, "switch", "switch", "", "");
        switchNode.setControl(true);
        codeTree.addNode("",lastNode, switchNode,-1);
        //deal condition in switch
        /*add condition node*/
        lastNode = switchNode;
        TreeNode switchConditionNode = new TreeNode();
        setNodeClassAndMethod(switchConditionNode, "condition", "condition", "", "");
        switchConditionNode.setAddMethodName(false);
        codeTree.addNode("",lastNode, switchConditionNode,-1);
        lastNode = switchConditionNode;
        /*deal condition*/
        dealForEachAndSwitchIterable(n.getSelector());
        /*add end node*/
        lastNode = switchConditionNode;
        addEndNode();
        if(n.getEntries().size() > 9){
            parsedFlag = false;
        }
        //add case and default node
        for (SwitchEntryStmt entry : n.getEntries()) {
            if (entry.getLabel() != null) {
                TreeNode conditionNode = new TreeNode();
                setNodeClassAndMethod(conditionNode, "case", "case", "", "");
                conditionNode.setControl(true);
                switchNode.getChildNodes().add(conditionNode);
            } else {
                TreeNode conditionNode = new TreeNode();
                setNodeClassAndMethod(conditionNode, "default", "default", "", "");
                conditionNode.setControl(true);
                switchNode.getChildNodes().add(conditionNode);
            }
        }
        //deal case and default body
        for (int i = 1; i < switchNode.getChildNodes().size(); i++) {
            lastNode = switchNode.getChildNodes().get(i);
            if (n.getEntries().get(i - 1).getStmts() != null && n.getEntries().get(i - 1).getStmts().size() > 1) {
                parsedFlag = false;
                System.err.println(n.getEntries().get(i - 1).getStmts() + " " + "can not be parsed");
                break;
            }
            if (n.getEntries().get(i - 1).getStmts() != null && n.getEntries().get(i - 1).getStmts().get(0).getChildrenNodes().size() != 0
                    && !isAllAnnotationStmt(n.getEntries().get(i - 1).getStmts().get(0).getChildrenNodes())) {
                //convert(n.getEntries().get(i - 1).getStmts());
                convert(n.getEntries().get(i - 1).getChildrenNodes());
            } else {
                TreeNode emptyNode = new TreeNode();
                emptyNode.setAddMethodName(false);
                setNodeClassAndMethod(emptyNode, "EmptyStatement", "EmptyStatement", "", "");
                lastNode.getChildNodes().add(emptyNode);
                lastNode = emptyNode;
            }
            //add end node
            if (endFlag) {
                addEndNode();
            } else {
                endFlag = true;
            }
        }
        lastNode = switchNode;
        //      contorlStatementFlag = true;
        //      controlNode = switchNode;
        return codeTree;
    }

    @Override
    protected CodeTree convert(ThrowStmt n) {
        return codeTree;
    }

    @Override
    protected CodeTree newInstance(Node n) {
        return codeTree;
    }

    private CodeTree convert(List<Node> stmts) {
        for (Node stmt : CollectionUtils.nullToEmpty(stmts)) {
            String str = stmt.toString();
            str = str.replaceAll(" ", "");
            if (str.startsWith("//hole") && holeFlag) {
                if (codeTree.getHoleNode() == null) {
                    TreeNode node = new TreeNode();
                    node.setCompleteMethodDeclaration("//hole");
                    addNode(stmt.toString(),node,stmt.getBeginLine());
                }
            }
            toCodeTree(stmt);
        }
        return codeTree;
    }

    public CodeTree convert(VariableDeclarationExpr n) {
        for (int i = 0; i < n.getVars().size(); i++) {
            boolean verifyFlag = true;
            String type1 = n.getType().toString();
            String type2 = n.getType().toString();
            if (n.getType().toString().contains("<")) {
                int index = type1.indexOf("<");
                type1 = type1.substring(0, index);
                type2 = type1;
                if (n.getType().toString().contains("[")) {
                    parsedFlag = false;
                    System.err.println(n.toString() + " can not be parsed");
                }
            }
            if (n.getVars().get(i).getId().toString().contains("[")) {
                int index = n.getVars().get(i).getId().toString().indexOf("[");
                String str = n.getVars().get(i).getId().toString().substring(index, n.getVars().get(i).getId().toString().length());
                class_variable.put(n.getVars().get(i).getId().toString(), type1 + str);
                if (!class_variable_list.contains(n.getVars().get(i).getId().toString())) {
                    class_variable_list.add(n.getVars().get(i).getId().toString());
                }
                type1 += str;
            } else {
                class_variable.put(n.getVars().get(i).getId().toString(), type1);
                if (!class_variable_list.contains(n.getVars().get(i).getId().toString())) {
                    class_variable_list.add(n.getVars().get(i).getId().toString());
                }
                if (type2.contains("[")) {
                    int index = type2.indexOf("[");
                    type2 = type2.substring(0, index);
                }
            }
            if (class_name_map.get(n.getType().toString()) == null) {
                if (n.getType().toString().contains("<")) {
                    class_name_map.put(n.getType().toString(), class_name_map.get(type2));
                    String temp = n.getType().toString().replaceAll("\\<\\>", "");
                    if (class_name_map.get(temp) == null) {
                        userClassProcessing.addUserClass(n.getType().toString());
                        userClassProcessing.addUserClass(temp);
                    }
                } else if (n.getType().toString().contains("[")) {
                    class_name_map.put(n.getType().toString(), n.getType().toString());
                    String temp = n.getType().toString().replaceAll("\\[\\]", "");
                    if (class_name_map.get(temp) == null) {
                        userClassProcessing.addUserClass(n.getType().toString());
                        userClassProcessing.addUserClass(temp);
                    }

                } else {
                    class_name_map.put(n.getType().toString(), n.getType().toString());
                    userClassProcessing.addUserClass(n.getType().toString());
                }
            }
            TreeNode node = new TreeNode();
            node.setControl(false);
            node.setExit(false);
            setNodeClass(node, type1, class_name_map.get(type2));
            if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
                //addNode(userClassProcessing.createVariableDeclarationNode());
            } else {
                if (n.getVars().get(i).getInit() != null) {
                    if (!n.getVars().get(i).getInit().toString().equals("null")) {
                        String type = new String("");
                        if (type1.contains("[")) {
                            type += "Array";
                            //verifyFlag = false;
                        }
                        if (n.getVars().get(i).getInit() instanceof BinaryExpr) {
                            Expression left = n.getVars().get(i).getInit();
                            boolean flag = true;
                            TreeNode assignNew = new TreeNode();
                            assignNew.setAddMethodName(false);
                            setNodeClassAndMethod(assignNew,"AssignNew","AssignNew","","");
                            while(left != null){
                                Expression right;
                                if(left instanceof BinaryExpr){
                                    right = ((BinaryExpr) left).getRight();
                                    left = ((BinaryExpr) left).getLeft();
                                }else{
                                    right = left;
                                    left = null;
                                }
                                if(right instanceof MethodCallExpr){
                                    dealMethodExpr((MethodCallExpr)right,node);
                                    if(!node.getCompleteClassName().equals("userDefinedClass")){
                                        addNode("",assignNew,-1);
                                        addNode(n.toString(),node,n.getBeginLine());
                                        flag = false;
                                        break;
                                    }
                                }else if(right instanceof FieldAccessExpr){
                                    dealFieldAccessExpr((FieldAccessExpr)right,node);
                                    if(!node.getCompleteClassName().equals("userDefinedClass")){
                                        addNode("",assignNew,-1);
                                        addNode(n.toString(),node,n.getBeginLine());
                                        flag = false;
                                        break;
                                    }
                                }
                            }
                            if(flag){
                                String constant = handleConstant(n.getVars().get(i).getInit().toString());
                                type += constant + preserveSquareBracket(type1);
                                setNodeMethod(node, type, type);
                                addNode(n.toString(),node,n.getBeginLine());
                                verifyFlag = false;
                            }
                        } else if (n.getVars().get(i).getInit().getChildrenNodes().size() == 0 && !n.getVars().get(i).getInit().toString().contains("{")) {//this condition is equivalent the condition "n.getVars().get(0).getInit() instanceof IntegerExpr || DoubleExpr"
                            String constant = handleConstant(n.getVars().get(i).getInit().toString());
                            type += constant + preserveSquareBracket(type1);
                            setNodeMethod(node, type, type);
                            addNode(n.toString(),node,n.getBeginLine());
                            verifyFlag = false;
                        } else if (n.getVars().get(i).getInit() instanceof MethodCallExpr) {
                            MethodCallExpr expr = (MethodCallExpr) n.getVars().get(i).getInit();
                            TreeNode assignNewNode = new TreeNode();
                            assignNewNode.setAddMethodName(false);
                            assignNewNode.setCondition(false);
                            assignNewNode.setExit(false);
                            setNodeClassAndMethod(assignNewNode, "AssignNew", "AssignNew", "", "");
                            codeTree.addNode("",lastNode, assignNewNode,-1);
                            lastNode = assignNewNode;
                            TreeNode methodNode = new TreeNode();
                            dealMethodExpr(expr, methodNode);
                            if (methodNode.getCompleteClassName() != null && !methodNode.getCompleteClassName().equals("userDefinedClass")) {
                                returnType = getMethodReturnType(methodNode);
                            }
                            dealMethodReturnType(n.getVars().get(i).getId().toString(), returnType);
                            if (userClassProcessing.isUserClassProcessing(methodNode.getCompleteClassName())) {
                                setNodeClassAndMethod(node, node.getClassName(), node.getCompleteClassName(), "Declaration", "Declaration");
                                addNode(n.toString(),node,n.getBeginLine());
                            } else {
                                addNode(n.toString(),methodNode,n.getBeginLine());
                            }
                            //verifyFlag = false;
                        } else if (n.getVars().get(i).getInit() instanceof ObjectCreationExpr) {
                            ObjectCreationExpr expr = (ObjectCreationExpr) n.getVars().get(i).getInit();
                            setVariableType(n.getVars().get(i).getId().toString(), getVariableType(class_variable.get(n.getVars().get(i).getId().toString()), true), expr.getType().toString());
                            convert(expr);
                            //verifyFlag = false;
                        } else if (n.getVars().get(i).getInit() instanceof ArrayCreationExpr) {
                            ArrayCreationExpr expr = (ArrayCreationExpr) n.getVars().get(i).getInit();
                            setVariableType(n.getVars().get(i).getId().toString(), getVariableType(class_variable.get(n.getVars().get(i).getId().toString()), true), expr.getType().toString());
                            convert(expr);
                            //verifyFlag = false;
                        } else if (n.getVars().get(i).getInit() instanceof CastExpr) {
                            CastExpr expr = (CastExpr) n.getVars().get(i).getInit();
                            convert(expr);
                            setVariableType(n.getVars().get(i).getId().toString(), getVariableType(class_variable.get(n.getVars().get(i).getId().toString()), true), expr.getType().toString());
                            //verifyFlag = false;
                        } else if (n.getVars().get(i).getInit() instanceof ArrayAccessExpr) {
                            ArrayAccessExpr expr = (ArrayAccessExpr) n.getVars().get(i).getInit();
                            TreeNode arrayAccessNode = new TreeNode();
                            dealArrayAccessExprVariableType(expr, arrayAccessNode);
                            if (!userClassProcessing.isUserClassProcessing(arrayAccessNode.getCompleteClassName()) && !verifyMethodNameAndParameterOfSpecial(arrayAccessNode, arrayAccessNode.getClassName())) {
                                parsedFlag = false;
                                System.err.println(n.toString() + ": can not be parsed");
                                addNode(n.toString(),arrayAccessNode,n.getBeginLine());
                                return null;
                            } else if (!userClassProcessing.isUserClassProcessing(arrayAccessNode.getCompleteClassName()) && verifyMethodNameAndParameterOfSpecial(arrayAccessNode, arrayAccessNode.getClassName())) {
                                addNode(n.toString(),arrayAccessNode,n.getBeginLine());
                            } else {
                                setNodeClassAndMethod(node, node.getClassName(), node.getCompleteClassName(), "Declaration", "Declaration");
                                addNode(n.toString(),node,n.getBeginLine());
                                verifyFlag = false;
                            }
                            dealMethodReturnType(n.getVars().get(i).getId().toString(), returnType);
                        } else if (n.getVars().get(i).getInit() instanceof ArrayInitializerExpr) {
                            ArrayInitializerExpr expr = (ArrayInitializerExpr) n.getVars().get(i).getInit();
                            if (n.getType().toString().contains("[]")) {
                                convert(expr, n.getType().toString());
                            } else if (type1.contains("[]")) {
                                convert(expr, type1);
                            } else {
                                parsedFlag = false;
                                System.err.println(n.toString() + " can not be parsed");
                            }
                            //verifyFlag = false;
                        } else if (n.getVars().get(i).getInit() instanceof FieldAccessExpr) {
                            FieldAccessExpr expr = (FieldAccessExpr) n.getVars().get(i).getInit();
                            TreeNode assignNewNode = new TreeNode();
                            assignNewNode.setAddMethodName(false);
                            assignNewNode.setCondition(false);
                            assignNewNode.setExit(false);
                            setNodeClassAndMethod(assignNewNode, "AssignNew", "AssignNew", "", "");
                            codeTree.addNode("",lastNode, assignNewNode,-1);
                            lastNode = assignNewNode;
                            TreeNode fieldNode = new TreeNode();
                            dealFieldAccessExpr(expr, fieldNode);
                            if (fieldNode.getCompleteClassName() != null) {
                                if (!userClassProcessing.isUserClassProcessing(fieldNode.getCompleteClassName()) && !(fieldNode.getCompleteClassName().contains("[]"))) {
                                    returnType = getMethodReturnType(fieldNode);
                                    addNode(n.toString(),fieldNode,n.getBeginLine());
                                } else if (userClassProcessing.isUserClassProcessing(fieldNode.getCompleteClassName())) {
                                    returnType = "userDefinedClass";
                                    setNodeClassAndMethod(node, node.getClassName(), node.getCompleteClassName(), "Declaration", "Declaration");
                                    addNode(n.toString(),node,n.getBeginLine());
                                } else {
                                    returnType = "int";//represent the return type of String[].length,int[].length etc..
                                    addNode(n.toString(),fieldNode,n.getBeginLine());
                                }
                                dealMethodReturnType(n.getVars().get(i).getId().toString(), returnType);
                                //verifyFlag = false;
                            } else {
                                parsedFlag = false;
                                System.err.println(n + " can not be parsed");
                            }
                        }
                        else {
                            String constant = handleConstant(n.getVars().get(i).getInit().toString());
                            type += constant + preserveSquareBracket(type1);
                            setNodeMethod(node, type, type);
                            addNode(n.toString(),node,n.getBeginLine());
                            verifyFlag = false;
                        }
                        returnType = null;
                    }
                    else {
                        if (type1.contains("[")) {
                            setNodeMethod(node, "ArrayNull" + preserveSquareBracket(type1), "ArrayNull" + preserveSquareBracket(type1));
                        } else {
                            setNodeMethod(node, "Null", "Null");
                        }
                        addNode(n.toString(),node,n.getBeginLine());
                        verifyFlag = false;
                    }
                } else {
                    if (type1.contains("[")) {
                        setNodeMethod(node, "ArrayDeclaration" + preserveSquareBracket(type1), "ArrayDeclaration" + preserveSquareBracket(type1));
                    } else {
                        setNodeMethod(node, "Declaration", "Declaration");
                    }
                    addNode(n.toString(),node,n.getBeginLine());
                    verifyFlag = false;
                }
                if (!verifyFlag && !userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && !verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
                    parsedFlag = false;
                    System.err.println(n.toString() + ": can not be parsed");
                    return null;
                }
            }
//            addToJdkCall(n.toString(),lastNode);
        }
        
        return codeTree;
    }


    protected CodeTree convert(MethodCallExpr n) {
        TreeNode node = new TreeNode();
        dealMethodExpr(n, node);
        //addNode(node);
        if (node.getCompleteClassName() != null && !node.getCompleteClassName().equals("userDefinedClass")) {
            addNode(n.toString(),node,n.getBeginLine());
            returnType = getMethodReturnType(node);
        }
        return codeTree;

    }

    protected CodeTree convert(AssignExpr n) {
        TreeNode node = new TreeNode();
        node.setControl(false);
        node.setExit(false);
        dealAssignExpr(n, node);
        if (lastNode.getCompleteMethodDeclaration().equals("MinusAssign") || lastNode.getCompleteMethodDeclaration().equals("PlusAssign")
                || lastNode.getCompleteMethodDeclaration().equals("Assign") || lastNode.getCompleteMethodDeclaration().equals("StarAssign")
                || lastNode.getCompleteMethodDeclaration().equals("SlashAssign")) {
            lastNode = lastNode.getParentNode();
            lastNode.getChildNodes().remove(lastNode.getChildNodes().size() - 1);
        }
        return codeTree;
    }

    protected CodeTree convert(ObjectCreationExpr n) {
        TreeNode node = new TreeNode();
        String type = n.getType().getName();
        if (type.contains("<")) {
            int index = type.indexOf("<");
            type = type.substring(0, index);
        }
        if (userClassProcessing.isUserClassProcessing(class_name_map.get(type))) {
            //addNode(userClassProcessing.createObjectCreationExprNode());
        } else {
            setNodeClass(node, type, class_name_map.get(type));
            node.setControl(false);
            node.setExit(false);
            if (n.getArgs() != null) {
                List<Expression> args = n.getArgs();
                String arguments = new String("");
                node.setMethodName("new" + "(" + getArguments(args, arguments) + ")");
            } else {
                node.setMethodName("new" + "()");
            }
            // this fragment code is used to compare whether the node.toSting() is consistent with method declaration
            if (!verifyMethodNameAndParameter(node, n.getArgs())) {
                parsedFlag = false;
                System.err.println(n.toString() + ": can not be parsed");
                return null;
            }
            addNode(n.toString(),node,n.getBeginLine());
        }
        return codeTree;
    }

    protected CodeTree convert(ArrayCreationExpr n) {
        TreeNode node = new TreeNode();
        setNodeClass(node, n.getType().toString(), class_name_map.get(n.getType().toString()));
        node.setControl(false);
        node.setExit(false);
        if (userClassProcessing.isUserClassProcessing(class_name_map.get(n.getType().toString()))) {
            //addNode(userClassProcessing.createArrayCreationExprNode());
        } else {
            int squareBracketCount = 0;
            //List<Expression> args = n.getDimensions();
            for (int i = 0; i < n.toString().length(); i++) {
                if (n.toString().charAt(i) == '[') {
                    squareBracketCount++;
                }
            }
            String methodArguments = new String("");
            String completeMethodArguments = new String("");
            //for (int i = 0; i < argumentsList.length; i++) {
            for (int i = 0; i < squareBracketCount; i++) {
                methodArguments += "[]";
                completeMethodArguments += ("[]");
            }
            setNodeMethod(node, "new" + methodArguments, "new" + completeMethodArguments);
            if (!verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
                parsedFlag = false;
                System.err.println(n.toString() + ": can not be parsed");
                return null;
            }
            addNode(n.toString(),node,n.getBeginLine());
        }
        return codeTree;
    }

    protected CodeTree convert(ArrayAccessExpr n) {
        TreeNode node = new TreeNode();
        dealArrayAccessExprVariableType(n, node);
        if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && !verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
            parsedFlag = false;
            System.err.println(n.toString() + ": can not be parsed");
            return null;
        } else if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
            addNode(n.toString(),node,n.getBeginLine());
        } else if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
            // nothing to do
        }
        return codeTree;
    }

    protected CodeTree convert(ArrayInitializerExpr n, String type) {
        TreeNode node = new TreeNode();
        setNodeClassAndMethod(node, type, class_name_map.get(filterSquareBracket(type)), "ArrayInit" + preserveSquareBracket(type) + "{}", "ArrayInit" + preserveSquareBracket(type) + "{}");
        node.setControl(false);
        node.setExit(false);
        if (userClassProcessing.isUserClassProcessing(class_name_map.get(filterSquareBracket(type)))) {
            // addNode(userClassProcessing.createArrayInitExprNode());
        } else {
            if (!verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
                parsedFlag = false;
                System.err.println(n.toString() + ": can not be parsed");
                return null;
            }
            addNode(n.toString(),node,n.getBeginLine());
        }
        return codeTree;
    }

    protected CodeTree convert(CastExpr n) {
        if (n.getExpr() instanceof MethodCallExpr) {
            MethodCallExpr expr = (MethodCallExpr) n.getExpr();
            convert(expr);
        } else if (n.getExpr() instanceof ObjectCreationExpr) {
            ObjectCreationExpr expr = (ObjectCreationExpr) n.getExpr();
            convert(expr);
        } else if (n.getExpr() instanceof ArrayCreationExpr) {
            ArrayCreationExpr expr = (ArrayCreationExpr) n.getExpr();
            convert(expr);
        } else if (n.getExpr() instanceof ArrayAccessExpr) {
            ArrayAccessExpr expr = (ArrayAccessExpr) n.getExpr();
            convert(expr);
        } else if (n.getExpr() instanceof FieldAccessExpr) {
            FieldAccessExpr expr = (FieldAccessExpr) n.getExpr();
            convert(expr);
        } else {
            TreeNode node = new TreeNode();
            setNodeClassAndMethod(node, n.getType().toString(), class_name_map.get(filterSquareBracket(n.getType().toString())), "Cast", "Cast");
            node.setControl(false);
            node.setExit(false);
            if (userClassProcessing.isUserClassProcessing(class_name_map.get(filterSquareBracket(n.getType().toString())))) {
                //addNode(userClassProcessing.createCastExprNode());
            } else {
                if (!verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
                    parsedFlag = false;
                    System.err.println(n.toString() + ": can not be parsed");
                    return null;
                }
                addNode(n.toString(),node,n.getBeginLine());
            }
        }
        return codeTree;
    }

    protected CodeTree convert(FieldAccessExpr n) {
        TreeNode node = new TreeNode();
        dealFieldAccessExpr(n, node);
        //addNode(node);
        if (node.getCompleteClassName() != null) {
            if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && !(node.getCompleteClassName().contains("[]"))) {
                returnType = getMethodReturnType(node);
                addNode(n.toString(),node,n.getBeginLine());
            } else if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
                returnType = "userDefinedClass";
            } else {
                returnType = "int";//represent the return type of String[].length,int[].length etc..
                addNode(n.toString(),node,n.getBeginLine());
            }
        } else {
            parsedFlag = false;
            System.err.println(n + " can not be parsed");
        }
        return codeTree;
    }

    protected CodeTree convert(UnaryExpr n) {
        TreeNode node = new TreeNode();
        dealUnaryExpr(n, node);
        if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
            addNode(n.toString(),node,n.getBeginLine());
        }
        return codeTree;
    }

    protected CodeTree convert(EnclosedExpr n) {
        TreeNode node = new TreeNode();
        dealEnclosedExpr(n, node);
        return codeTree;
    }

    protected void dealClassNameMap(String type) {
        if (type != null && class_name_map.get(type) == null) {
            for (int i = 0; i < starImportStringList.size(); i++) {
                String className = starImportStringList.get(i).replace("*", type);
                try {
                    if (Thread.currentThread().getContextClassLoader().loadClass(className) != null) {
                        class_name_map.put(type, className);
                    }
                } catch (Exception e) {
                    if (!(e instanceof ClassNotFoundException)) {
                        parsedFlag = false;
                        System.err.println(e.getMessage());
                    }
                    // nothing to do
                } catch (Error e) {
                    parsedFlag = false;
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    protected void dealForInitCondition(ForStmt n, List<String> variableList) {
        if (n.getInit() != null) {
            if (n.getInit().size() > 1) {
                for (int i = 0; i < n.getInit().size(); i++) {
                    if (i > 0) {
                        //add ","
                        TreeNode commaNode = new TreeNode();
                        commaNode.setCondition(true);
                        setNodeClassAndMethod(commaNode, ",", ",", "", "");
                        commaNode.setAddMethodName(false);
                        codeTree.addNode("",lastNode, commaNode,-1);
                        lastNode = commaNode;
                    }
                    dealCondition(n.getInit().get(i), lastNode);
                }
            } else if (n.getInit().size() == 1 && !(n.getInit().get(0) instanceof VariableDeclarationExpr)) {
                dealCondition(n.getInit().get(0), lastNode);
            } else if (n.getInit().size() == 1 && n.getInit().get(0) instanceof VariableDeclarationExpr) {
                VariableDeclarationExpr expr = (VariableDeclarationExpr) n.getInit().get(0);
                if (expr.getVars().size() == 1) {
                    TreeNode node = lastNode;
                    convert(expr);
                    if (node.equals(lastNode)) {
                        TreeNode temp = userClassProcessing.createVariableDeclarationNode();
                        addNode(n.toString(),temp,n.getBeginLine());
                    }
                    lastNode.setCondition(true);
                    variableList.add(class_variable_list.get(class_variable_list.size() - 1));
                } else {
                    for (int i = 0; i < expr.getVars().size(); i++) {
                        if (i > 0) {
                            //add ","
                            TreeNode commaNode = new TreeNode();
                            commaNode.setCondition(true);
                            setNodeClassAndMethod(commaNode, ",", ",", "", "");
                            commaNode.setAddMethodName(false);
                            codeTree.addNode("",lastNode, commaNode,-1);
                            lastNode = commaNode;
                        }
                        VariableDeclarationExpr singleVariableDeclarationExpr = new VariableDeclarationExpr();
                        singleVariableDeclarationExpr.setType(expr.getType());
                        List list = new ArrayList();
                        list.add(expr.getVars().get(i));
                        singleVariableDeclarationExpr.setVars(list);
                        TreeNode node = lastNode;
                        convert(singleVariableDeclarationExpr);
                        if (node.equals(lastNode)) {
                            TreeNode temp = userClassProcessing.createVariableDeclarationNode();
                            addNode(n.toString(),temp,n.getBeginLine());
                        }
                        lastNode.setCondition(true);
                        variableList.add(class_variable_list.get(class_variable_list.size() - 1));
                    }
                }
            } else {
                parsedFlag = false;
                System.err.println(n + " " + "can not be parsed");
            }
        } else {
            TreeNode emptyNode = new TreeNode();
            setNodeClassAndMethod(emptyNode, "EmptyCondition", "EmptyCondition", "", "");
            emptyNode.setAddMethodName(false);
            emptyNode.setCondition(true);
            codeTree.addNode("",lastNode, emptyNode,-1);
            lastNode = emptyNode;
        }
    }

    protected void dealForEachAndSwitchIterable(Expression n) {
        if (n instanceof NameExpr) {
            dealNameCondition(n, lastNode);
        } else if (n instanceof MethodCallExpr) {
            convert((MethodCallExpr) n);
        } else if (n instanceof ArrayAccessExpr) {
            convert((ArrayAccessExpr) n);
        } else if (n instanceof FieldAccessExpr) {
            convert((FieldAccessExpr) n);
        } else {
            parsedFlag = false;
            System.err.println(n + " " + "can not be parsed");
        }
        lastNode.setCondition(true);
    }

    protected void dealCondition(Expression n, TreeNode node) {
        // deal condition in control structure
        if (n instanceof EnclosedExpr) {
            dealEnclosedCondition(n, node);
        } else if (n instanceof BinaryExpr) {
            if (((BinaryExpr) n).getOperator().toString().equals("and") || ((BinaryExpr) n).getOperator().toString().equals("or")) {
                //处理left expression
                Expression left = ((BinaryExpr) n).getLeft();
                dealCondition(left, lastNode);
                //处理&& , ||
                Operator operator = ((BinaryExpr) n).getOperator();
                TreeNode operatorNode = new TreeNode();
                operatorNode.setAddMethodName(false);
                operatorNode.setCondition(true);
                if (getOperator(operator).equals("&&")) {
                    setNodeClassAndMethod(operatorNode, "&&", "&&", "", "");
                } else if (getOperator(operator).equals("||")) {
                    setNodeClassAndMethod(operatorNode, "||", "||", "", "");
                }
                codeTree.addNode("",node, operatorNode,-1);
                lastNode = operatorNode;
                //处理right expression
                Expression right = ((BinaryExpr) n).getRight();
                dealCondition(right, lastNode);
            } else {
                dealBinaryCondition(n, node);
            }
        } else if (n instanceof MethodCallExpr) {
            dealMethodCallCondition(n, node);
        } else if (n instanceof BooleanLiteralExpr) {
            dealBooleanLiteralCondition(n, node);
        } else if (n instanceof UnaryExpr) {
            dealUnaryCondition(n, node);
        } else if (n instanceof NameExpr) {
            dealNameCondition(n, node);
        } else if (n instanceof ArrayAccessExpr) {
            dealArrayAccessCondition(n, node);
        } else if (n instanceof StringLiteralExpr || n instanceof IntegerLiteralExpr
                || n instanceof CharLiteralExpr || n instanceof DoubleLiteralExpr) {
            TreeNode currentNode = new TreeNode();
            currentNode.setAddMethodName(false);
            currentNode.setCondition(true);
            String constant = handleConstant(n.toString());
            setNodeClassAndMethod(currentNode, constant, constant, "", "");
            codeTree.addNode("",lastNode, currentNode,-1);
            lastNode = currentNode;
        } else if (n instanceof AssignExpr) {
            dealAssignCondition(n, node);
        } else if (n instanceof CastExpr) {
            dealCastCondition(n, node);
        } else if (n instanceof FieldAccessExpr) {
            node.setAddMethodName(true);
            node.setCondition(true);
            dealFieldAccessExpr((FieldAccessExpr) n, node);
        } else {
            parsedFlag = false;
            System.err.println(n + " " + "can not be parsed");
        }
    }

    protected void addBracketNode(TreeNode node, String bracket) {
        //增加括号节点
        TreeNode bracketNode = new TreeNode();
        bracketNode.setAddMethodName(false);
        bracketNode.setCondition(true);
        setNodeClassAndMethod(bracketNode, bracket, bracket, "", "");
        codeTree.addNode("",node, bracketNode,-1);
        lastNode = bracketNode;
    }

    protected void dealCastCondition(Expression n, TreeNode node) {
        CastExpr expr = (CastExpr) n;
        TreeNode castNode = new TreeNode();
        castNode.setCondition(true);
        String type;
        if (class_name_map.get(expr.getType().toString()) != null) {
            type = expr.getType().toString();
            if (userClassProcessing.isUserClassProcessing(type)) {
                setNodeClassAndMethod(castNode, "userDefinedClass", "userDefinedClass", "Cast", "Cast");
            } else {
                setNodeClassAndMethod(castNode, type, class_name_map.get(type), "Cast", "Cast");
            }
            codeTree.addNode(n.toString(),node, castNode,n.getBeginLine());
            lastNode = castNode;
        } else {
            parsedFlag = false;
            System.err.println(n + " " + "can not be parsed");
        }
        dealCondition(expr.getExpr(), castNode);
    }

    protected void dealAssignCondition(Expression n, TreeNode node) {
        AssignExpr expr = (AssignExpr) n;
        //处理被赋值的变量(target)
        dealCondition(expr.getTarget(), node);
        //增加"="节点
        String operator = "";
        if (expr.toString().contains("+=")) {
            operator = "+=";
        } else if (expr.toString().contains("-=")) {
            operator = "-=";
        } else {
            operator = "=";
        }
        TreeNode equalNode = new TreeNode();
        equalNode.setAddMethodName(false);
        equalNode.setCondition(true);
        setNodeClassAndMethod(equalNode, operator, operator, "", "");
        codeTree.addNode("",lastNode, equalNode,-1);
        lastNode = equalNode;
        //处理value
        dealCondition(expr.getValue(), lastNode);
    }

    protected void dealArrayAccessCondition(Expression n, TreeNode node) {
        TreeNode newNode = new TreeNode();
        dealArrayAccessExprVariableType((ArrayAccessExpr) n, newNode);
        newNode.setCondition(true);
        if (!userClassProcessing.isUserClassProcessing(newNode.getCompleteClassName()) && !verifyMethodNameAndParameterOfSpecial(newNode, newNode.getClassName())) {
            parsedFlag = false;
            System.err.println(n.toString() + ": can not be parsed");
        } else if (!userClassProcessing.isUserClassProcessing(newNode.getCompleteClassName()) && verifyMethodNameAndParameterOfSpecial(newNode, newNode.getClassName())) {
            addNode(n.toString(),newNode,n.getBeginLine());
        } else if (userClassProcessing.isUserClassProcessing(newNode.getCompleteClassName())) {
            TreeNode userNode = userClassProcessing.createArrayAccessExprNode();
            addNode(n.toString(),userNode,n.getBeginLine());
        }
    }

    protected void dealNameCondition(Expression n, TreeNode node) {
        String type;
        //get the type of variable
        if (class_variable.get(n.toString()) != null) {
            type = getVariableType(class_variable.get(n.toString()), true);
        } else if (class_variable.get(n.toString() + "[]") != null) {
            type = getVariableType(class_variable.get(n.toString() + "[]"), true);
        } else if (class_variable.get(n.toString() + "[][]") != null) {
            type = getVariableType(class_variable.get(n.toString() + "[][]"), true);
        } else {
            type = null;
        }
        //judge whether the type is exist
        if (type != null) {
            /*get the complete class type*/
            if (type.contains("[]")) {
                int index = type.indexOf('[');
                type = class_name_map.get(type.substring(0, index)) + type.substring(index, type.length());
            } else {
                type = class_name_map.get(type);
            }
            // add nameNode
            TreeNode nameNode = new TreeNode();
            nameNode.setAddMethodName(false);
            nameNode.setCondition(true);
            if (userClassProcessing.isUserClassProcessing(type)) {
                type = "userDefinedClass";
            }
            setNodeClassAndMethod(nameNode, type, type, "", "");
            codeTree.addNode("",node, nameNode,-1);
            lastNode = nameNode;
        } else {
            System.err.println(n + " " + "can not be parsed");
            parsedFlag = false;
        }
    }

    protected void dealUnaryCondition(Expression n, TreeNode node) {
        UnaryExpr expr = (UnaryExpr) n;
        TreeNode unaryNode = new TreeNode();
        unaryNode.setCondition(true);
        unaryNode.setAddMethodName(false);
        if (expr.getOperator().toString().equals("not")) {//!
            setNodeClassAndMethod(unaryNode, "!", "!", "", "");
            codeTree.addNode("",node, unaryNode,-1);
            lastNode = unaryNode;
            dealCondition(expr.getExpr(), unaryNode);
        } else if (expr.getOperator().toString().equals("posIncrement")) {//后++
            dealCondition(expr.getExpr(), node);
            setNodeClassAndMethod(unaryNode, "++", "++", "", "");
            codeTree.addNode("",lastNode, unaryNode,-1);
            lastNode = unaryNode;
        } else if (expr.getOperator().toString().equals("preIncrement")) {//前++
            setNodeClassAndMethod(unaryNode, "++", "++", "", "");
            codeTree.addNode("",node, unaryNode,-1);
            lastNode = unaryNode;
            dealCondition(expr.getExpr(), unaryNode);
        } else if (expr.getOperator().toString().equals("posDecrement")) {//后--
            dealCondition(expr.getExpr(), node);
            setNodeClassAndMethod(unaryNode, "--", "--", "", "");
            codeTree.addNode("",lastNode, unaryNode,-1);
            lastNode = unaryNode;
        } else if (expr.getOperator().toString().equals("preDecrement")) {//前--
            setNodeClassAndMethod(unaryNode, "--", "--", "", "");
            codeTree.addNode("",node, unaryNode,-1);
            lastNode = unaryNode;
            dealCondition(expr.getExpr(), unaryNode);
        } else if (expr.getOperator().toString().equals("positive")) {//+
            setNodeClassAndMethod(unaryNode, "+", "+", "", "");
            codeTree.addNode("",node, unaryNode,-1);
            lastNode = unaryNode;
            dealCondition(expr.getExpr(), unaryNode);
        } else if (expr.getOperator().toString().equals("negative")) {//-
            setNodeClassAndMethod(unaryNode, "-", "-", "", "");
            codeTree.addNode("",node, unaryNode,-1);
            lastNode = unaryNode;
            dealCondition(expr.getExpr(), unaryNode);
        } else {
            parsedFlag = false;
            System.err.println(n + " " + "can not be parsed");
        }
    }

    protected void dealEnclosedCondition(Expression n, TreeNode node) {
        Expression expr = ((EnclosedExpr) n).getInner();
        //增加左括号节点
        addBracketNode(node, "(");
        //处理括号中的条件
        dealCondition(expr, lastNode);
        //增加右括号节点
        addBracketNode(node, ")");
    }

    protected void dealBooleanLiteralCondition(Expression n, TreeNode node) {
        TreeNode booleanNode = new TreeNode();
        booleanNode.setAddMethodName(false);
        booleanNode.setCondition(true);
        if (!n.toString().equals("true") && !n.toString().equals("false")) {
            parsedFlag = false;
        }
        setNodeClassAndMethod(booleanNode, n.toString(), n.toString(), "", "");
        codeTree.addNode(n.toString(),node, booleanNode,n.getBeginLine());
        lastNode = booleanNode;
    }

    protected void dealMethodCallCondition(Expression n, TreeNode node) {
        MethodCallExpr expr = (MethodCallExpr) n;
        TreeNode conditionContentNode = new TreeNode();
        conditionContentNode.setCondition(true);
        dealMethodExpr(expr, conditionContentNode);
        codeTree.addNode(n.toString(),node, conditionContentNode,n.getBeginLine());
        lastNode = conditionContentNode;
    }

    protected void dealBinaryCondition(Expression n, TreeNode node) {
        BinaryExpr expr = (BinaryExpr) n;
        Expression left = expr.getLeft();
        Operator operator = expr.getOperator();
        Expression right = expr.getRight();
        TreeNode leftNode = new TreeNode();
        TreeNode operatorNode = new TreeNode();
        TreeNode rightNode = new TreeNode();
        // add left node
        leftNode.setCondition(true);
        if (left instanceof BinaryExpr) {
            dealBinaryCondition(left, lastNode);
        } else if (left instanceof EnclosedExpr) {
            dealEnclosedCondition(left, lastNode);
        } else {
            dealAtomLeftAndRightBinaryCondition(left, lastNode, leftNode);
        }
        // add operator node
        String ope = getOperator(operator);
        setNodeClassAndMethod(operatorNode, ope, ope, "", "");
        operatorNode.setAddMethodName(false);
        operatorNode.setCondition(true);
        codeTree.addNode("",lastNode, operatorNode,-1);
        lastNode = operatorNode;
        // add right node
        rightNode.setCondition(true);
        if (right instanceof BinaryExpr) {
            dealBinaryCondition(right, lastNode);
        } else if (right instanceof EnclosedExpr) {
            dealEnclosedCondition(right, lastNode);
        } else {
            dealAtomLeftAndRightBinaryCondition(right, lastNode, rightNode);
        }

    }

    protected String getOperator(Operator operator) {
        String ope = operator.toString();
        switch (ope) {
            case "notEquals":
                ope = "!=";
                break;
            case "equals":
                ope = "==";
                break;
            case "greaterEquals":
                ope = ">=";
                break;
            case "lessEquals":
                ope = "<=";
                break;
            case "less":
                ope = "<";
                break;
            case "greater":
                ope = ">";
                break;
            case "plus":
                ope = "+";
                break;
            case "minus":
                ope = "-";
                break;
            case "times":
                ope = "*";
                break;
            case "divide":
                ope = "/";
                break;
            case "or":
                ope = "||";
                break;
            case "and":
                ope = "&&";
                break;
            case "not":
                ope = "!";
                break;
            case "remainder":
                ope = "%";
                break;
            default:
                parsedFlag = false;
                System.err.println(ope + " " + "can not be parsed");
        }
        return ope;
    }

    protected void dealAtomLeftAndRightBinaryCondition(Expression n, TreeNode parentNode, TreeNode currentNode) {
        if (n instanceof NameExpr) {
            dealNameCondition(n, parentNode);
        } else if (n instanceof MethodCallExpr) {
            dealMethodCallCondition(n, parentNode);
        } else if (n instanceof NullLiteralExpr) {
            currentNode.setAddMethodName(false);
            setNodeClassAndMethod(currentNode, "null", "null", "", "");
            codeTree.addNode("",parentNode, currentNode,-1);
            lastNode = currentNode;
        } else if (n instanceof FieldAccessExpr) {
            FieldAccessExpr expr = (FieldAccessExpr) n;
            dealFieldAccessExpr(expr, currentNode);
            codeTree.addNode(n.toString(),parentNode, currentNode,n.getBeginLine());
            lastNode = currentNode;
        } else if (n instanceof StringLiteralExpr || n instanceof IntegerLiteralExpr
                || n instanceof CharLiteralExpr || n instanceof DoubleLiteralExpr) {
            currentNode.setAddMethodName(false);
            String constant = handleConstant(n.toString());
            setNodeClassAndMethod(currentNode, constant, constant, "", "");
            codeTree.addNode("",parentNode, currentNode,-1);
            lastNode = currentNode;
        } else if (n instanceof BooleanLiteralExpr) {
            dealBooleanLiteralCondition(n, parentNode);
        } else if (n instanceof UnaryExpr) {
            dealUnaryCondition(n, parentNode);
        } else if (n instanceof ArrayAccessExpr) {
            currentNode.setAddMethodName(false);
            setNodeClassAndMethod(currentNode, "arrayAccess", "arrayAccess", "", "");
            codeTree.addNode("",parentNode, currentNode,-1);
            lastNode = currentNode;
        } else if (n instanceof CastExpr) {
            dealCastCondition(n, parentNode);
        } else {
            System.err.println(n + " can not be parsed");
            parsedFlag = false;
        }
        currentNode.setCondition(true);
        parentNode.setCondition(true);
    }

    protected void dealMethodReturnType(String variable, String type) {
        if (type != null && type.contains(".")) {
            String[] strs = type.split("\\.");
            String simpleType = strs[strs.length - 1];
            if (!class_name_map.containsKey(simpleType)) {
                class_name_map.put(simpleType, type);
            }
            if (variable != null && class_variable.containsKey(variable)) {
                setVariableType(variable, getVariableType(class_variable.get(variable), true), simpleType);
            }
        } else if (type != null && class_variable.containsKey(variable)) {
            if (!class_name_map.containsKey(type)) {
                class_name_map.put(type, type);
            }
            if (variable != null) {
                setVariableType(variable, getVariableType(class_variable.get(variable), true), type);
            }
        }
    }

    protected String getMethodReturnType(TreeNode node) {
        String type = node.getCompleteClassName();
        if (node.getCompleteClassName() != null && !node.getCompleteClassName().contains("[]")) {
            MethodReflection methodReflection = new MethodReflection(node.getCompleteClassName());
            Map<String, String> map = methodReflection.getAllMethodsReturnTypeMap(node.getCompleteClassName());
            if (map.containsKey(node.toString()) && !map.get(node.toString()).equals("void")) {
                type = map.get(node.toString());
            }
        }
        return type;
    }

    protected String dealBinaryReturnType(List<String> list) {
        if (list.contains("null")) {
            return "null";
        } else if (list.contains("String")) {
            return "String";
        } else if (list.contains("double")) {
            return "double";
        } else if (list.contains("int")) {
            return "int";
        } else if (list.contains("boolean")) {
            return "boolean";
        } else if (list.contains("char")) {
            return "char";
        } else {
            return "null";
        }
    }

    protected void dealArrayAccessExprVariableType(ArrayAccessExpr n, TreeNode node) {
        String expressionString = n.toString();
        String expressionNameString = new String("");
        String expressionWithoutIndexString = new String("");
        String expressionWithoutIndexAndNameString = new String("");
        /*the following code is used to filter out the index (b[1][2] -> b[][])*/
        boolean flag = true;
        for (int i = 0; i < expressionString.length(); i++) {
            char ch = expressionString.charAt(i);
            if (flag) {
                expressionWithoutIndexString += ch;
                expressionNameString += ch;
            }
            if (ch == '[') {
                expressionWithoutIndexAndNameString += "[index";
                expressionNameString = expressionNameString.substring(0, expressionNameString.length() - 1);
                flag = false;
            } else if (ch == ']') {
                expressionWithoutIndexString += ch;
                expressionWithoutIndexAndNameString += ']';
                flag = true;
            }
        }
        /*the following code is used to construct tree node*/
        node.setControl(false);
        setNodeMethod(node, expressionWithoutIndexAndNameString, expressionWithoutIndexAndNameString);
        if (class_variable.get(expressionWithoutIndexString) != null) {
            setNodeClass(node, getVariableType(class_variable.get(expressionWithoutIndexString), false), class_name_map.get(filterSquareBracket(getVariableType(class_variable.get(expressionWithoutIndexString), false))));
            returnType = class_name_map.get(getVariableType(class_variable.get(expressionWithoutIndexString), false));
        } else if (class_variable.get(expressionNameString) != null) {
            String type = getVariableType(class_variable.get(expressionNameString), false);
            String type2 = getVariableType(class_variable.get(expressionNameString), true);
            if (type.contains("[")) {
                int firstIndexOfLeftSquareBracket = type.indexOf('[');
                type = type.substring(0, firstIndexOfLeftSquareBracket);
            }
            int count = type2.split("\\[").length - expressionWithoutIndexString.split("\\[").length;
            setNodeClass(node, type, class_name_map.get(filterSquareBracket(type)));
            returnType = class_name_map.get(type);
            for (int i = 0; i < count; i++) {
                setNodeMethod(node, node.getMethodName() + "[]", node.getCompleteMethodName() + "[]");
                returnType = class_name_map.get(type) + "[]";
            }
        } else if (class_variable.get(expressionWithoutIndexString + "[]") != null) {
            String type = getVariableType(class_variable.get(expressionWithoutIndexString + "[]"), false);
            if (type.contains("[")) {
                int firstIndexOfLeftSquareBracket = type.indexOf('[');
                type = type.substring(0, firstIndexOfLeftSquareBracket);
            }
            String completeType = class_name_map.get(filterSquareBracket(type)) + "[]";
            setNodeClass(node, type, class_name_map.get(filterSquareBracket(type)));
            setNodeMethod(node, node.getMethodName() + "[]", node.getCompleteMethodName() + "[]");
            returnType = completeType;
        } else {
            parsedFlag = false;
            System.err.println(n + " " + "can not be parsed");
        }
        node.setExit(false);
    }


    protected void dealLastFieldOfFieldAccess(FieldAccessExpr n, TreeNode node) {
        String type;
        if (node.getCompleteMethodName() != null) {
            if (node.getCompleteMethodName().contains("[]")) {
                type = returnType;
            } else {
                type = getMethodReturnType(node);
            }
            if (type != null) {
                if (type.contains("[")) {
                    if (n.getField().toString().equals("length")) {
                        setNodeMethod(node, node.getMethodName() + "." + n.getField(), node.getCompleteMethodName() + "." + n.getField());
                    } else {
                        parsedFlag = false;
                        System.err.println(n.getField() + " " + "can not be parsed");
                    }
                } else {
                    String[] strs = type.split("\\.");
                    String simpleType = strs[strs.length - 1];
                    dealClassNameMap(simpleType);
                    TreeNode tempNode = new TreeNode();
                    setNodeClassAndMethod(tempNode, simpleType, type, n.getField(), n.getField());
                    if (!verifyFieldAccess(tempNode)) {
                        parsedFlag = false;
                        System.err.println(n.toString() + ": can not be parsed");
                    } else {
                        setNodeMethod(node, node.getMethodName() + "." + n.getField(), node.getCompleteMethodName() + "." + n.getField());
                    }
                }
            } else {
                parsedFlag = false;
                System.err.println(n + " can not be parsed");
            }
        } else {
            parsedFlag = false;
            System.err.println(n.toString() + " " + "can not be parsed");
        }
    }

    protected void dealEnclosedExpr(EnclosedExpr n, TreeNode node) {
        if (n.getInner() instanceof MethodCallExpr) {
            dealMethodExpr((MethodCallExpr) n.getInner(), node);
        } else if (n.getInner() instanceof AssignExpr) {
            dealAssignExpr((AssignExpr) n.getInner(), node);
        } else if (n.getInner() instanceof EnclosedExpr) {
            dealEnclosedExpr((EnclosedExpr) n.getInner(), node);
        } else if (n.getInner() instanceof FieldAccessExpr) {
            dealFieldAccessExpr((FieldAccessExpr) n.getInner(), node);
        } else {
            parsedFlag = false;
            System.err.println(n + " " + "can not be parsed");
        }
    }

    protected void dealAssignExpr(AssignExpr n, TreeNode node) {
        //添加operator节点
        TreeNode assignNode = new TreeNode();
        assignNode.setAddMethodName(false);
        if (n.getOperator().toString().equals("minus")) {
            setNodeClassAndMethod(assignNode, "MinusAssign", "MinusAssign", "", "");
            codeTree.addNode("",lastNode, assignNode,-1);
            lastNode = assignNode;
        } else if (n.getOperator().toString().equals("plus")) {
            setNodeClassAndMethod(assignNode, "PlusAssign", "PlusAssign", "", "");
            codeTree.addNode("",lastNode, assignNode,-1);
            lastNode = assignNode;
        } else if (n.getOperator().toString().equals("assign")) {
            setNodeClassAndMethod(assignNode, "Assign", "Assign", "", "");
            codeTree.addNode("",lastNode, assignNode,-1);
            lastNode = assignNode;
        } else if (n.getOperator().toString().equals("star")) {
            setNodeClassAndMethod(assignNode, "StarAssign", "StarAssign", "", "");
            codeTree.addNode("",lastNode, assignNode,-1);
            lastNode = assignNode;
        } else if (n.getOperator().toString().equals("slash")) {
            setNodeClassAndMethod(assignNode, "SlashAssign", "SlashAssign", "", "");
            codeTree.addNode("",lastNode, assignNode,-1);
            lastNode = assignNode;
        } else {
            parsedFlag = false;
            System.err.println(n.toString() + ": can not be parsed");
        }

        //处理value
        boolean verifyFlag = true;
        String target = n.getTarget().toString();
        if (target.contains("[")) {
            String[] strs = target.split("\\[");
            target = strs[0];
            for (int length = 0; length < strs.length - 1; length++) {
                target += "[]";
            }
        }
        String type = "";
        if (!class_variable_list.contains(target)) {
            if (class_variable.get(filterSquareBracket(target)) != null) {
                type = class_variable.get(filterSquareBracket(target));
                target = filterSquareBracket(target);
            } else if (class_variable.get(filterSquareBracket(target) + "[]") != null) {
                type = class_variable.get(filterSquareBracket(target) + "[]");
                target = filterSquareBracket(target) + "[]";
            } else if (class_variable.get(filterSquareBracket(target) + "[][]") != null) {
                type = class_variable.get(filterSquareBracket(target) + "[][]");
                target = filterSquareBracket(target) + "[][]";
            } else {
                parsedFlag = false;
                System.err.println(n.toString() + " can not be parsed");
            }
        } else {
            type = class_variable.get(target);
        }
        setNodeClass(node, getVariableType(type, true), class_name_map.get(filterSquareBracket(getVariableType(type, true))));
        if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
            //addNode(node);
        } else {
            if (n.getValue() instanceof ObjectCreationExpr) {
                ObjectCreationExpr expr = (ObjectCreationExpr) n.getValue();
                setVariableType(target, getVariableType(type, true), expr.getType().toString());
                convert(expr);
                //verifyFlag = false;
            } else if (n.getValue() instanceof MethodCallExpr) {
                MethodCallExpr expr = (MethodCallExpr) n.getValue();
                convert(expr);
                //dealMethodReturnType(target, returnType);
                returnType = null;
                //verifyFlag = false;
            } else if (n.getValue() instanceof ArrayCreationExpr) {
                ArrayCreationExpr expr = (ArrayCreationExpr) n.getValue();
                setVariableType(target, getVariableType(type, true), expr.getType().toString());
                convert(expr);
                //verifyFlag = false;
            } else if (n.getValue().toString().equals("null")) {
                if (getVariableType(type, true) != null) {
                    type = getVariableType(type, true);
                    if (getVariableType(type, true).contains("[")) {
                        setNodeMethod(node, "ArrayNull" + preserveSquareBracket(type), "ArrayNull" + preserveSquareBracket(type));
                    } else if (target.contains("[")) {
                        setNodeMethod(node, "ArrayNull" + preserveSquareBracket(target), "ArrayNull" + preserveSquareBracket(target));
                    } else {
                        setNodeMethod(node, "Null", "Null");
                    }
                    addNode(n.toString(),node,n.getBeginLine());
                } else {
                    parsedFlag = false;
                    System.err.println(n + " can not be parsed");
                }
                verifyFlag = false;
            } else if (n.getValue() instanceof CastExpr) {
                CastExpr expr = (CastExpr) n.getValue();
                convert(expr);
                setVariableType(target, getVariableType(type, true), expr.getType().toString());
                //verifyFlag = false;
            } else if (n.getValue() instanceof ArrayAccessExpr) {
                ArrayAccessExpr expr = (ArrayAccessExpr) n.getValue();
                convert(expr);
                //dealMethodReturnType(target, returnType);
                returnType = null;
                //verifyFlag = false;
            } else if (n.getValue() instanceof FieldAccessExpr) {
                FieldAccessExpr expr = (FieldAccessExpr) n.getValue();
                convert(expr);
                //dealMethodReturnType(target, returnType);
                returnType = null;
                //verifyFlag = false;
            } else if (n.getValue() instanceof EnclosedExpr) {
                dealEnclosedExpr((EnclosedExpr) n.getValue(), node);
            } else if(n.getValue() instanceof BinaryExpr){
                Expression left = n.getValue();
                boolean flag = true;
                while(left != null){
                    Expression right;
                    if(left instanceof BinaryExpr){
                       right = ((BinaryExpr) left).getRight();
                       left = ((BinaryExpr) left).getLeft();
                    }else{
                        right = left;
                        left = null;
                    }
                    if(right instanceof MethodCallExpr){
                        dealMethodExpr((MethodCallExpr)right,node);
                        if(!node.getCompleteClassName().equals("userDefinedClass")){
                            addNode(n.toString(),node,n.getBeginLine());
                            flag = false;
                            break;
                        }
                    }else if(right instanceof FieldAccessExpr){
                        dealFieldAccessExpr((FieldAccessExpr)right,node);
                        if(!node.getCompleteClassName().equals("userDefinedClass")){
                            addNode(n.toString(),node,n.getBeginLine());
                            flag = false;
                            break;
                        }
                    }
                }
                if(flag){
                    if (getVariableType(type, true) != null) {
                        type = getVariableType(type, true);
                        if (type.contains("[")) {
                            setNodeMethod(node, "ArrayConstant" + preserveSquareBracket(type), "ArrayConstant" + preserveSquareBracket(type));
                        } else if (target.contains("[")) {
                            setNodeMethod(node, "ArrayConstant" + preserveSquareBracket(target), "ArrayConstant" + preserveSquareBracket(target));
                        } else {
                            setNodeMethod(node, "Constant", "Constant");
                        }
                    } else {
                        String constant = handleConstant(n.getValue().toString());
                        setNodeMethod(node, constant, constant);
                    }
                    addNode(n.toString(),node,n.getBeginLine());
                    verifyFlag = false;
                }
            }
            else {
                if (getVariableType(type, true) != null) {
                    type = getVariableType(type, true);
                    if (type.contains("[")) {
                        setNodeMethod(node, "ArrayConstant" + preserveSquareBracket(type), "ArrayConstant" + preserveSquareBracket(type));
                    } else if (target.contains("[")) {
                        setNodeMethod(node, "ArrayConstant" + preserveSquareBracket(target), "ArrayConstant" + preserveSquareBracket(target));
                    } else {
                        setNodeMethod(node, "Constant", "Constant");
                    }
                } else {
                    String constant = handleConstant(n.getValue().toString());
                    setNodeMethod(node, constant, constant);
                }
                addNode(n.toString(),node,n.getBeginLine());
                verifyFlag = false;
            }
            if (!verifyFlag && !verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
                parsedFlag = false;
                System.err.println(n.toString() + ": can not be parsed");
            }
        }

    }

    protected void dealFieldAccessExpr(FieldAccessExpr n, TreeNode node) {
        Expression expression = n.getScope();
        if (expression instanceof NameExpr) {
            dealClassNameMap(class_variable.get(expression.toString()));
            dealClassNameMap(expression.toString());
            if ((class_variable.get(n.getScope().toString()) == null && class_name_map.get(class_variable.get(n.getScope().toString())) != null) ||
                    (class_variable.get(n.getScope().toString()) != null && !getVariableType(class_variable.get(n.getScope().toString()), true).contains("[]"))) {
                setNodeClassAndMethod(node, class_variable.get(n.getScope().toString()), class_name_map.get(class_variable.get(n.getScope().toString())), n.getField().toString(), n.getField().toString());
                if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && !verifyFieldAccess(node)) {
                    parsedFlag = false;
                    System.err.println(n.toString() + ": can not be parsed");
                }
            } else if ((class_variable.get(n.getScope().toString()) == null && class_name_map.get(n.getScope().toString()) != null) ||
                    (class_variable.get(n.getScope().toString()) != null && !getVariableType(n.getScope().toString(), true).contains("[]"))) {
                setNodeClassAndMethod(node, n.getScope().toString(), class_name_map.get(n.getScope().toString()), n.getField().toString(), n.getField().toString());
                if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && !verifyFieldAccess(node)) {
                    parsedFlag = false;
                    System.err.println(n.toString() + ": can not be parsed");
                }
            } else if (class_name_map.get(n.getScope().toString()) != null) {
                setNodeClassAndMethod(node, n.getScope().toString(), class_name_map.get(n.getScope().toString()), n.getField().toString(), n.getField().toString());
                if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && !verifyFieldAccess(node)) {
                    parsedFlag = false;
                    System.err.println(n.toString() + ": can not be parsed");
                }
            } else if (class_variable.get(n.getScope().toString() + "[]") != null) {
                String type = getVariableType(class_variable.get(n.getScope().toString() + "[]"), false);
                String completeType = class_name_map.get(type.replaceAll("\\[\\]", "")) + "[]";
                if (!type.contains("[]")) {
                    type += "[]";
                }
                if (n.getField().toString().equals("length")) {
                    setNodeClassAndMethod(node, type, completeType, n.getField().toString(), n.getField().toString());
                } else {
                    parsedFlag = false;
                    System.err.println(n.getField() + " " + "can not be parsed");
                }
            } else if (class_variable.get(n.getScope().toString() + "[][]") != null) {
                String type = getVariableType(class_variable.get(n.getScope().toString() + "[][]"), false);
                String completeType = class_name_map.get(type.replaceAll("\\[\\]", "")) + "[][]";
                if (!type.contains("[]")) {
                    type += "[][]";
                }
                if (n.getField().toString().equals("length")) {
                    setNodeClassAndMethod(node, type, completeType, n.getField().toString(), n.getField().toString());
                } else {
                    parsedFlag = false;
                    System.err.println(n.getField() + " " + "can not be parsed");
                }
            } else {
                String type = getVariableType(class_variable.get(n.getScope().toString()), true);
                if (type != null) {
                    int index = type.indexOf('[');
                    String completeType = class_name_map.get(type.substring(0, index)) + type.substring(index, type.length());
                    if (n.getField().toString().equals("length")) {
                        setNodeClassAndMethod(node, type, completeType, n.getField().toString(), n.getField().toString());
                    } else {
                        parsedFlag = false;
                        System.err.println(n.getField() + " " + "can not be parsed");
                    }
                } else {
                    parsedFlag = false;
                    System.err.println(n.toString() + " " + "can not be parsed");
                }
            }
            if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
                returnType = getMethodReturnType(node);
            } else {
                returnType = "userDefinedClass";
            }
        } else if (expression instanceof FieldAccessExpr && countMethodCallNumber(expression) - 1 == 1) {
            dealFieldAccessExpr((FieldAccessExpr) expression, node);
            dealLastFieldOfFieldAccess(n, node);
        } else if (expression instanceof MethodCallExpr && countMethodCallNumber(expression) - 1 == 1) {
            List<Expression> list = new ArrayList<>();
            list = dealContinuedMethodCall(expression, ((MethodCallExpr) expression).getName(), list, node);
            if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && !verifyMethodNameAndParameter(node, list)) {
                parsedFlag = false;
                System.err.println(n.toString() + ": can not be parsed");
            } else if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
                returnType = getMethodReturnType(node);
                dealLastFieldOfFieldAccess(n, node);
            } else {
                returnType = "userDefinedClass";
            }
        } else if (expression instanceof ArrayAccessExpr) {
            dealArrayAccessExprVariableType((ArrayAccessExpr) expression, node);
            dealLastFieldOfFieldAccess(n, node);
        } else if (expression instanceof EnclosedExpr) {
            dealEnclosedExpr((EnclosedExpr) expression, node);
            dealLastFieldOfFieldAccess(n, node);
        } else {
            setNodeClassAndMethod(node, "", "", "", "");
            parsedFlag = false;
            System.err.println(n + " " + "can not be parsed");
        }
        node.setControl(false);
        node.setExit(false);
        if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
            setNodeClassAndMethod(node, "userDefinedClass", "userDefinedClass", "FieldAccess", "FieldAccess");
        }
        if (!verifyFieldAccess(node) && !verifyMethodNameAndParameterOfSpecial(node, node.getClassName()) && !node.getCompleteClassName().equals("userDefinedClass")) {
            parsedFlag = false;
        }
    }

    protected void dealUnaryExpr(UnaryExpr n, TreeNode node) {
        Expression expr = n.getExpr();
        String operator = n.getOperator().toString();
        //handel operator
        switch (operator) {
            case "posIncrement":
                operator = "++p";
                break;
            case "preIncrement":
                operator = "++";
                break;
            case "posDecrement":
                operator = "--p";
                break;
            case "preDecrement":
                operator = "--";
                break;
            default:
                operator = "UnaryExprOperator";
        }
        //handle expr
        if (expr instanceof ArrayAccessExpr) {
            dealArrayAccessExprVariableType((ArrayAccessExpr) expr, node);
            setNodeMethod(node, operator, operator);
        } else if (expr instanceof NameExpr) {
            setNodeClassAndMethod(node, getVariableType(class_variable.get(((NameExpr) expr).getName()), false), class_name_map.get(getVariableType(class_variable.get(((NameExpr) expr).getName()), false)), operator, operator);
        } else if (expr instanceof MethodCallExpr) {
            dealMethodExpr((MethodCallExpr) expr, node);
            if (!node.getCompleteMethodDeclaration().equals("userDefinedClass.Method")) {
                setNodeClassAndMethod(node, getMethodReturnType(node), getMethodReturnType(node), operator, operator);
            } else {
                setNodeClassAndMethod(node, "userDefinedClass", "userDefinedClass", operator, operator);
            }
        } else {
            parsedFlag = false;
            System.err.println(n + " " + "can not be parsed");
        }
        node.setControl(false);
        node.setExit(false);
        if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
            //addNode(userClassProcessing.createUnaryExprNode());
        } else {
            if (!verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
                parsedFlag = false;
                System.err.println(n.toString() + ": can not be parsed");
            }
        }
    }

    protected void dealMethodExpr(MethodCallExpr n, TreeNode node) {
        Expression expression = n.getScope();
        int methodCallNumber = countMethodCallNumber(expression);
        if (methodCallNumber > 2) {
            parsedFlag = false;
            System.err.println(n.toString() + ": can not be parsed");
        } else {
            Expression expr = n.getScope();
            List<Expression> list = n.getArgs();
            String methodName = n.getName();
            list = dealContinuedMethodCall(expr, methodName, list, node);
            node.setControl(false);
            node.setExit(false);
            if (methodCallNumber == 2) {
                if (node.getCompleteClassName() != null) {
                    if (!node.getCompleteClassName().equals("userDefinedClass")) {
                        if (!verifyMethodNameAndParameterOfTwoCalls(node, list)) {
                            parsedFlag = false;
                            System.err.println(n.toString() + ": can not be parsed");
                        }
                    }
                } else {
                    parsedFlag = false;
                    System.err.println(n.toString() + ": can not be parsed");
                }
            } else {
                if (node.getCompleteClassName() != null) {
                    if (!node.getCompleteClassName().equals("userDefinedClass")) {
                        if (!verifyMethodNameAndParameter(node, list)) {
                            parsedFlag = false;
                            System.err.println(n.toString() + ": can not be parsed");
                        }
                    }
                } else {
                    parsedFlag = false;
                    System.err.println(n.toString() + ": can not be parsed");
                }
            }
        }
    }

    protected List<Expression> dealContinuedMethodCall(Expression n, String methodName, List<Expression> list, TreeNode node) {
        List<Expression> args = new ArrayList<>();
        if (n != null) {
            /*get the type of scope*/
            if (n instanceof StringLiteralExpr) {
                setNodeClass(node, "String", "java.lang.String");
            } else if (n instanceof CharLiteralExpr) {
                setNodeClass(node, "char", "char");
            } else if (n instanceof IntegerLiteralExpr) {
                setNodeClass(node, "Integer", "java.lang.Integer");
            } else if (n instanceof DoubleLiteralExpr) {
                setNodeClass(node, "Double", "java.lang.Double");
            } else if (n instanceof BooleanLiteralExpr) {
                setNodeClass(node, "Boolean", "java.lang.Boolean");
            } else if (n instanceof NameExpr) {
                if (class_name_map.containsKey(n.toString())) {
                    setNodeClass(node, n.toString(), class_name_map.get(n.toString()));
                } else {
                    String variableName = n.toString();
                    String className = new String("");
                    if (class_variable.get(variableName) != null) {
                        className = class_variable.get(variableName);
                        className = getVariableType(className, false);
                        if (className.contains("<")) {
                            int index = className.indexOf("<");
                            className = className.substring(0, index);
                        }
                        setNodeClass(node, className, class_name_map.get(className));
                    }
                }
                if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
                    setNodeClassAndMethod(node, "userDefinedClass", "userDefinedClass", "Method", "Method");
                }
            } else if (n instanceof ArrayAccessExpr) {
                ArrayAccessExpr expr = (ArrayAccessExpr) n;
                dealArrayAccessExprVariableType(expr, node);
                setNodeMethod(node, "", "");
            } else if (n instanceof FieldAccessExpr) {
                FieldAccessExpr expr = (FieldAccessExpr) n;
                if (class_name_map.get(expr.getScope().toString()) != null) {
                    setNodeClassAndMethod(node, expr.getScope().toString(), class_name_map.get(expr.getScope().toString()), expr.getField().toString(), expr.getField().toString());
                } else {
                    setNodeClassAndMethod(node, class_variable.get(expr.getScope().toString()), class_name_map.get(class_variable.get(expr.getScope().toString())), expr.getField().toString(), expr.getField().toString());
                }
            } else if (n instanceof MethodCallExpr) {
                MethodCallExpr expr = (MethodCallExpr) n;
                List<Expression> tempList = dealContinuedMethodCall(expr.getScope(), expr.getName(), expr.getArgs(), node);
                for (int i = 0; i < tempList.size(); i++) {
                    args.add(tempList.get(i));
                }
            } else if (n instanceof EnclosedExpr) {
                Expression expr = ((EnclosedExpr) n).getInner();
                dealContinuedMethodCall(expr, methodName, new ArrayList<>(), node);
            }
             /*get the type of each argument*/
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    args.add(list.get(i));
                }
                String arguments = new String("");
                if (node.getMethodName() != null && !node.getMethodName().equals("")) {
                    node.setMethodName(node.getMethodName() + "." + methodName + "(" + getArguments(list, arguments) + ")");
                } else {
                    node.setMethodName(methodName + "(" + getArguments(list, arguments) + ")");
                }
            } else {
                if (node.getMethodName() != null && !node.getMethodName().equals("")) {
                    if (!(n.getParentNode() instanceof FieldAccessExpr)) {
                        node.setMethodName(node.getMethodName() + "." + methodName + "()");
                    } else {
                        node.setMethodName(node.getMethodName() + "." + methodName);
                    }
                } else {
                    if (!(n.getParentNode() instanceof FieldAccessExpr)) {
                        node.setMethodName(methodName + "()");
                    } else {
                        node.setMethodName(methodName);
                    }
                }
            }
        } else {
            setNodeClassAndMethod(node, "userDefinedClass", "userDefinedClass", "Method", "Method");
        }
        return args;
    }


    protected String getArguments(List<Expression> args, String arguments) {
        if (args.size() > 0) {
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    arguments += ",";
                }
                if (args.get(i).getClass().getSimpleName().equals("StringLiteralExpr")) {
                    arguments += "String";
                } else if (args.get(i).getClass().getSimpleName().equals("CharLiteralExpr")) {
                    arguments += "char";
                } else if (args.get(i).getClass().getSimpleName().equals("IntegerLiteralExpr")) {
                    arguments += "int";
                } else if (args.get(i).getClass().getSimpleName().equals("DoubleLiteralExpr")) {
                    arguments += "double";
                } else if (args.get(i).getClass().getSimpleName().equals("BooleanLiteralExpr")) {
                    arguments += "boolean";
                } else if (args.get(i).getClass().getSimpleName().equals("UnaryExpr")) {
                    TreeNode node = new TreeNode();
                    dealUnaryExpr((UnaryExpr) args.get(i), node);
                    arguments += node.getClassName();
                } else if (args.get(i).getClass().getSimpleName().equals("NameExpr")) {
                    if (class_variable.get(args.get(i).toString()) != null) {
                        arguments += getVariableType(class_variable.get(args.get(i).toString()), true);
                    } else if (class_variable.get(args.get(i).toString() + "[]") != null) {
                        arguments += getVariableType(class_variable.get(args.get(i).toString() + "[]"), true);
                    } else if (class_variable.get(args.get(i).toString() + "[][]") != null) {
                        arguments += getVariableType(class_variable.get(args.get(i).toString() + "[][]"), true);
                    } else {
                        arguments += "null";
                    }
                } else if (args.get(i).getClass().getSimpleName().equals("BinaryExpr")) {
                    if (((BinaryExpr) args.get(i)).getOperator().toString().equals("times") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("divide")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("remainder")) {
                        parsedFlag = false;
                        System.err.println(args.get(i) + " " + "can not be parsed");
                    } else if (((BinaryExpr) args.get(i)).getOperator().toString().equals("less") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("lessEquals")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("greater") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("greaterEquals")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("equals") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("notEquals")) {
                        arguments += "null";
                    } else if (((BinaryExpr) args.get(i)).getOperator().toString().equals("or") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("and")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("not")) {
                        arguments += "boolean";
                    } else {
                        BinaryExpr expr = (BinaryExpr) args.get(i);
                        List<String> typeList = new ArrayList<>();
                        List<Expression> rightExpressionList = new ArrayList<>();
                        rightExpressionList.add(expr.getRight());
                        String rightType = new String("");
                        rightType = getArguments(rightExpressionList, rightType);
                        List<Expression> leftExpressionList = new ArrayList<>();
                        rightExpressionList.add(expr.getLeft());
                        String leftType = new String("");
                        leftType = getArguments(leftExpressionList, leftType);
                        typeList.add(rightType);
                        typeList.add(leftType);
                        String binaryType = dealBinaryReturnType(typeList);
                        arguments += binaryType;
                    }
                } else if (args.get(i).getClass().getSimpleName().equals("ConditionalExpr")) {
                    arguments += "null";
                } else if (args.get(i).getClass().getSimpleName().equals("EnclosedExpr")) {
                    EnclosedExpr expr = (EnclosedExpr) args.get(i);
                    List<Expression> expressionList = new ArrayList<>();
                    expressionList.add(expr.getInner());
                    arguments = getArguments(expressionList, arguments);
                } else if (args.get(i).getClass().getSimpleName().equals("ArrayAccessExpr")) {
                    TreeNode node = new TreeNode();
                    ArrayAccessExpr expr = (ArrayAccessExpr) args.get(i);
                    dealArrayAccessExprVariableType(expr, node);
                    arguments += filterSquareBracket(node.getClassName());
                } else if (args.get(i).getClass().getSimpleName().equals("CastExpr")) {
                    CastExpr expr = (CastExpr) args.get(i);
                    arguments += expr.getType();
                } else if (args.get(i).getClass().getSimpleName().equals("ObjectCreationExpr")) {
                    ObjectCreationExpr expr = (ObjectCreationExpr) args.get(i);
                    convert(expr);
                    arguments += expr.getType();
                } else if (args.get(i).getClass().getSimpleName().equals("MethodCallExpr")) {
                    TreeNode node = new TreeNode();
                    MethodCallExpr n = (MethodCallExpr) args.get(i);
                    dealMethodExpr(n, node);
                    if(!"userDefinedClass".equals(node.getCompleteClassName())) {
                        TreeNode assignNew = new TreeNode();
                        assignNew.setAddMethodName(false);
                        setNodeClassAndMethod(assignNew, "AssignNew", "AssignNew", "", "");
                        addNode("",assignNew,-1);
                        addNode(n.toString(),node,n.getBeginLine());
                    }
                    String methodReturnType = getMethodReturnType(node);
                    if (methodReturnType != null && methodReturnType.contains(".")) {
                        String[] strs = methodReturnType.split("\\.");
                        methodReturnType = strs[strs.length - 1];
                    }
                    if (methodReturnType != null) {
                        arguments += methodReturnType;
                    } else {
                        arguments += "null";
                    }
                } else if (args.get(i).getClass().getSimpleName().equals("FieldAccessExpr")) {
                    TreeNode node = new TreeNode();
                    FieldAccessExpr n = (FieldAccessExpr) args.get(i);
                    dealFieldAccessExpr(n, node);
                    if(!node.getCompleteClassName().equals("userDefinedClass")) {
                        TreeNode assignNew = new TreeNode();
                        assignNew.setAddMethodName(false);
                        setNodeClassAndMethod(assignNew, "AssignNew", "AssignNew", "", "");
                        addNode("",assignNew,-1);
                        addNode(n.toString(),node,n.getBeginLine());
                    }
                    String methodReturnType = getMethodReturnType(node);
                    if (methodReturnType != null && methodReturnType.contains(".")) {
                        String[] strs = methodReturnType.split("\\.");
                        methodReturnType = strs[strs.length - 1];
                    }
                    if (methodReturnType != null) {
                        arguments += methodReturnType;
                    } else {
                        arguments += "null";
                    }
                } else {
                    arguments += "null";
                }
            }
        }
        return arguments;
    }

    protected String getArguments2(List<Expression> args, String arguments) {
        if (args.size() > 0) {
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    arguments += ",";
                }
                if (args.get(i).getClass().getSimpleName().equals("StringLiteralExpr")) {
                    arguments += "String";
                } else if (args.get(i).getClass().getSimpleName().equals("CharLiteralExpr")) {
                    arguments += "char";
                } else if (args.get(i).getClass().getSimpleName().equals("IntegerLiteralExpr")) {
                    arguments += "int";
                } else if (args.get(i).getClass().getSimpleName().equals("DoubleLiteralExpr")) {
                    arguments += "double";
                } else if (args.get(i).getClass().getSimpleName().equals("BooleanLiteralExpr")) {
                    arguments += "boolean";
                } else if (args.get(i).getClass().getSimpleName().equals("UnaryExpr")) {
                    TreeNode node = new TreeNode();
                    dealUnaryExpr((UnaryExpr) args.get(i), node);
                    arguments += node.getClassName();
                } else if (args.get(i).getClass().getSimpleName().equals("NameExpr")) {
                    if (class_variable.get(args.get(i).toString()) != null) {
                        arguments += getVariableType(class_variable.get(args.get(i).toString()), true);
                    } else if (class_variable.get(args.get(i).toString() + "[]") != null) {
                        arguments += getVariableType(class_variable.get(args.get(i).toString() + "[]"), true);
                    } else if (class_variable.get(args.get(i).toString() + "[][]") != null) {
                        arguments += getVariableType(class_variable.get(args.get(i).toString() + "[][]"), true);
                    } else {
                        arguments += "null";
                    }
                } else if (args.get(i).getClass().getSimpleName().equals("BinaryExpr")) {
                    if (((BinaryExpr) args.get(i)).getOperator().toString().equals("times") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("divide")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("remainder")) {
                        parsedFlag = false;
                        System.err.println(args.get(i) + " " + "can not be parsed");
                    } else if (((BinaryExpr) args.get(i)).getOperator().toString().equals("less") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("lessEquals")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("greater") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("greaterEquals")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("equals") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("notEquals")) {
                        arguments += "null";
                    } else if (((BinaryExpr) args.get(i)).getOperator().toString().equals("or") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("and")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("not")) {
                        arguments += "boolean";
                    } else {
                        BinaryExpr expr = (BinaryExpr) args.get(i);
                        List<String> typeList = new ArrayList<>();
                        List<Expression> rightExpressionList = new ArrayList<>();
                        rightExpressionList.add(expr.getRight());
                        String rightType = new String("");
                        rightType = getArguments2(rightExpressionList, rightType);
                        List<Expression> leftExpressionList = new ArrayList<>();
                        rightExpressionList.add(expr.getLeft());
                        String leftType = new String("");
                        leftType = getArguments2(leftExpressionList, leftType);
                        typeList.add(rightType);
                        typeList.add(leftType);
                        String binaryType = dealBinaryReturnType(typeList);
                        arguments += binaryType;
                    }
                } else if (args.get(i).getClass().getSimpleName().equals("ConditionalExpr")) {
                    arguments += "null";
                } else if (args.get(i).getClass().getSimpleName().equals("EnclosedExpr")) {
                    EnclosedExpr expr = (EnclosedExpr) args.get(i);
                    List<Expression> expressionList = new ArrayList<>();
                    expressionList.add(expr.getInner());
                    arguments = getArguments2(expressionList, arguments);
                } else if (args.get(i).getClass().getSimpleName().equals("ArrayAccessExpr")) {
                    TreeNode node = new TreeNode();
                    ArrayAccessExpr expr = (ArrayAccessExpr) args.get(i);
                    dealArrayAccessExprVariableType(expr, node);
                    arguments += filterSquareBracket(node.getClassName());
                } else if (args.get(i).getClass().getSimpleName().equals("CastExpr")) {
                    CastExpr expr = (CastExpr) args.get(i);
                    arguments += expr.getType();
                } else if (args.get(i).getClass().getSimpleName().equals("ObjectCreationExpr")) {
                    ObjectCreationExpr expr = (ObjectCreationExpr) args.get(i);
                    //convert(expr);
                    arguments += expr.getType();
                } else if (args.get(i).getClass().getSimpleName().equals("MethodCallExpr")) {
                    TreeNode node = new TreeNode();
                    MethodCallExpr n = (MethodCallExpr) args.get(i);
                    dealMethodExpr(n, node);
                    String methodReturnType = getMethodReturnType(node);
                    if (methodReturnType != null && methodReturnType.contains(".")) {
                        String[] strs = methodReturnType.split("\\.");
                        methodReturnType = strs[strs.length - 1];
                    }
                    if (methodReturnType != null) {
                        arguments += methodReturnType;
                    } else {
                        arguments += "null";
                    }
                } else if (args.get(i).getClass().getSimpleName().equals("FieldAccessExpr")) {
                    TreeNode node = new TreeNode();
                    FieldAccessExpr n = (FieldAccessExpr) args.get(i);
                    dealFieldAccessExpr(n, node);
                    String methodReturnType = getMethodReturnType(node);
                    if (methodReturnType != null && methodReturnType.contains(".")) {
                        String[] strs = methodReturnType.split("\\.");
                        methodReturnType = strs[strs.length - 1];
                    }
                    if (methodReturnType != null) {
                        arguments += methodReturnType;
                    } else {
                        arguments += "null";
                    }
                } else {
                    arguments += "null";
                }
            }
        }
        return arguments;
    }


    protected void setVariableType(String variable, String parentType, String childType) {
        String type;
        if (parentType != null) {
            if (parentType.equals(childType)) {
                type = parentType;
            } else {
                type = parentType + " " + childType;
            }
            class_variable.replace(variable, type);
        } else {
            parsedFlag = false;
            System.err.println("null exception");
        }
    }

    protected String getVariableType(String str, boolean isParameterFlag) {
        String type = null;
        if (str != null) {
            if (str.contains(" ")) {
                String[] types = str.split(" ");
                if (isParameterFlag) {
                    type = types[0];
                } else {
                    type = types[1];
                }
            } else {
                type = str;
            }
        } else {
            parsedFlag = false;
            System.err.println( "variable type  can not be parsed");
        }
        return type;
    }

    protected int countMethodCallNumber(Expression expression) {
        int count = 1;
        while (true) {
            if (expression instanceof MethodCallExpr) {
                expression = ((MethodCallExpr) expression).getScope();
                count++;
            } else if (expression instanceof FieldAccessExpr) {
                expression = ((FieldAccessExpr) expression).getScope();
                count++;
            } else if (expression instanceof ObjectCreationExpr) {
                count += 2;// such as new File("pop").close(), we consider it to be invalid
                break;
            } else if (expression instanceof ArrayInitializerExpr) {
                count += 2;
                break;
            } else if (expression instanceof ArrayCreationExpr) {
                count += 2;
                break;
            } else {
                break;
            }
        }
        return count;
    }

    protected boolean verifyMethodNameAndParameterOfSpecial(TreeNode node, String className) {
        List<String> list = new ArrayList<>();
        ConstructVocabulary constructVocabulary = new ConstructVocabulary();
        constructVocabulary.addSpecialVocabulary(className, list);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null && list.get(i).equals(node.toString())) {
                return true;
            }
        }
        return false;
    }

    // this method is used to compare whether the node.toSting() is consistent with method declaration consist of continued two api calls such as System.out.println();
    protected boolean verifyMethodNameAndParameterOfTwoCalls(TreeNode node, List<Expression> arguments) {
        /*get all the combinedSimpleMethods and combinedCompleteMethods*/
        String className = class_name_map.get(node.getClassName());
        List<String> combinedSimpleMethodList = new ArrayList<>();
        Map<String, List<String>> methodAndParameterTypeMap = new HashMap<>();
        Map<String, List<String>> methodAndParameterCompleteTypeMapString = new HashMap<>();
        Map<String, String> simpleToCompleteNameMap = new HashMap<>(); // store the simple to complete method declaration reflection
        MethodReflection methodReflection = new MethodReflection(className);
        List<Method> methodList = methodReflection.getMethodList();
        List<Field> fieldList = methodReflection.getFieldList();
        List<Object> objectList = new ArrayList<>();
        for (int i = 0; i < fieldList.size(); i++) {
            objectList.add(fieldList.get(i));
        }
        for (int i = 0; i < methodList.size(); i++) {
            objectList.add(methodList.get(i));
        }
        for (int i = 0; i < objectList.size(); i++) {
            String str = new String("");
            String str2 = new String("");
            methodReflection.combinedMethods(objectList.get(i), 1, str, str2, className);
        }
        combinedSimpleMethodList = methodReflection.getCombinedSimpleMethodList();
        simpleToCompleteNameMap = methodReflection.getMap();
        /*filter out the method the may be the candidates*/
        List<String> matchedList = new ArrayList<>();
        for (int i = 0; i < combinedSimpleMethodList.size(); i++) {
            // System.out.println(combinedSimpleMethodList.get(i));
            if ((node.toString()).equals(combinedSimpleMethodList.get(i))) {
                matchedList.removeAll(matchedList);
                matchedList.add(combinedSimpleMethodList.get(i));
                String combineSimpleMethodDeclaration = combinedSimpleMethodList.get(i);
                String combineCompleteMethodDeclaration = simpleToCompleteNameMap.get(combineSimpleMethodDeclaration);
                methodAndParameterTypeMap.put(combineSimpleMethodDeclaration, extractCombinedMethodParameterType(combineSimpleMethodDeclaration));
                methodAndParameterCompleteTypeMapString.put(combineSimpleMethodDeclaration, extractCombinedMethodParameterType(combineCompleteMethodDeclaration));
                break;
            } else if (getCombinedMethodNameOnly(node.toString()).equals(getCombinedMethodNameOnly(combinedSimpleMethodList.get(i)))) {
                matchedList.add(combinedSimpleMethodList.get(i));
                String combineSimpleMethodDeclaration = combinedSimpleMethodList.get(i);
                String combineCompleteMethodDeclaration = simpleToCompleteNameMap.get(combineSimpleMethodDeclaration);
                methodAndParameterTypeMap.put(combineSimpleMethodDeclaration, extractCombinedMethodParameterType(combineSimpleMethodDeclaration));
                methodAndParameterCompleteTypeMapString.put(combineSimpleMethodDeclaration, extractCombinedMethodParameterType(combineCompleteMethodDeclaration));
            }
        }
        /*choose the correct one from candidates*/
        if (matchedList.size() == 1) {
            modifyMethodDeclarationOfNode(matchedList.get(0), node, simpleToCompleteNameMap);
            return true;
        } else if (matchedList.size() > 1) {
            String result = chooseTheCorrectMethodDeclaration(matchedList, arguments, methodAndParameterTypeMap, methodAndParameterCompleteTypeMapString);
            if (result.equals("")) {
                return false;
            } else {
                modifyMethodDeclarationOfNode(result, node, simpleToCompleteNameMap);
                return true;
            }
        } else {
            return false;
        }
    }

    public String getCombinedMethodNameOnly(String methodDeclaration) {
        String[] onlyMethodName = methodDeclaration.split("\\.");
        String result = new String("");
        for (int i = 0; i < onlyMethodName.length; i++) {
            if (onlyMethodName[i].contains("(")) {
                int index = onlyMethodName[i].indexOf("(");
                result += onlyMethodName[i].substring(0, index);
            } else {
                result += onlyMethodName[i];
            }
        }
        // the following code is used to add parameter count of the second call such as System.our.peintln(String) -> 1
        while (methodDeclaration.contains(".")) {
            methodDeclaration = methodDeclaration.substring(methodDeclaration.indexOf(".") + 1, methodDeclaration.length());
        }
        int count = methodDeclaration.split(",").length;
        result += count;
        return result;
    }

    public List<String> extractCombinedMethodParameterType(String methodDeclaration) {
        List<String> result = new ArrayList<>();
        while (methodDeclaration.contains("(")) {
            String str = null;
            int startIndex = methodDeclaration.indexOf("(");
            int endIndex = methodDeclaration.indexOf(")");
            str = methodDeclaration.substring(startIndex + 1, endIndex);
            methodDeclaration = methodDeclaration.substring(endIndex + 1, methodDeclaration.length());
            String[] strs = str.split(",");
            for (int i = 0; i < strs.length; i++) {
                result.add(strs[i]);
            }
        }
        return result;
    }

    protected boolean verifyFieldAccess(TreeNode node) {
        MethodReflection methodReflection = new MethodReflection();
        List<String> fieldAccessList = methodReflection.getAllCompleteStaticFields(class_name_map.get(node.getClassName()));
        for (int i = 0; i < fieldAccessList.size(); i++) {
            if (fieldAccessList.get(i).equals(node.getCompleteMethodDeclaration())) {
                return true;
            }
        }
        return false;
    }

    // this method is used to compare whether the node.toSting() is consistent with method declaration
    protected boolean verifyMethodNameAndParameter(TreeNode node, List<Expression> arguments) {
        MethodReflection methodReflection = new MethodReflection();
        List<String> methodDeclarationList = methodReflection.getAllMethodDeclaration(class_name_map.get(node.getClassName()));
        Map<String, List<String>> methodAndParameterTypeMap = methodReflection.getMethodAndParameterTypeMap();
        Map<String, List<String>> methodAndParameterCompleteTypeMap = methodReflection.getMethodAndParameterCompleteTypeMap();
        Map<String, String> simpleToCompleteNameMap = methodReflection.getSimpleToCompleteName(class_name_map.get(node.getClassName()));
        if (methodAndParameterTypeMap.get(node.toString()) == null) {
            String methodDeclaration = node.toString();
            List<String> matchedList = new ArrayList<>();
            methodDeclaration = methodDeclaration.substring(0, methodDeclaration.indexOf('('));
            for (int i = 0; i < methodDeclarationList.size(); i++) {
                int index = methodDeclarationList.get(i).indexOf('(');
                if (methodDeclaration.equals(methodDeclarationList.get(i))) {
                    matchedList.removeAll(matchedList);
                    matchedList.add(methodDeclarationList.get(i));
                    break;
                } else if (index != -1 && methodDeclaration.equals(methodDeclarationList.get(i).substring(0, index))) {
                    matchedList.add(methodDeclarationList.get(i));
                }
            }
            if (matchedList.size() == 1) {
                modifyMethodDeclarationOfNode(matchedList.get(0), node, simpleToCompleteNameMap);
                return true;
            } else if (matchedList.size() > 1) {
                String result = chooseTheCorrectMethodDeclaration(matchedList, arguments, methodAndParameterTypeMap, methodAndParameterCompleteTypeMap);
                if (result.equals("")) {
                    return false;
                } else {
                    modifyMethodDeclarationOfNode(result, node, simpleToCompleteNameMap);
                    return true;
                }
            } else {
                return false;
            }
        } else {
            modifyMethodDeclarationOfNode(node.toString(), node, simpleToCompleteNameMap);
            return true;
        }
    }

    //this method is used modify the method name and parameter in node methodName
    protected void modifyMethodDeclarationOfNode(String str, TreeNode node, Map<String, String> map) {
        int startIndex = str.indexOf('.') + 1;
        int endIndex = str.length();
        String methodNameAndArguments = str.substring(startIndex, endIndex);
        String completeNameAndArguments = map.get(str);
        setNodeMethod(node, methodNameAndArguments, completeNameAndArguments);
    }

    // this method is used to choose the method declaration that is the most likely to be the correct one
    protected String chooseTheCorrectMethodDeclaration(List<String> matchedList, List<Expression> arguments, Map<String, List<String>> methodAndParameterTypeMap, Map<String, List<String>> methodAndParameterCompleteTypeMap) {
        String methodDeclaration = new String("");
        List<String> candidateMethodList = new ArrayList<>();
        //init the candidate list of possible matched method declarations
        for (int i = 0; i < matchedList.size(); i++) {
            candidateMethodList.add(matchedList.get(i));
        }
        //judge the correct method declaration
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < matchedList.size(); i++) {
            List<String> parameterList = methodAndParameterTypeMap.get(matchedList.get(i));
            List<String> parameterCompleteList = methodAndParameterCompleteTypeMap.get(matchedList.get(i));
            if (parameterList.size() == arguments.size()) {
                int matchedTypeCount = 0;
                for (int j = 0; j < arguments.size(); j++) {
                    List<Expression> list = new ArrayList<>();
                    list.add(arguments.get(j));
                    String parameterType = new String("");
                    parameterType = getArguments2(list, parameterType);
                    if (parameterList.get(j).equals(parameterType)) {
                        matchedTypeCount++;
                    } else if (judgeRelationshipOfCast(parameterType, parameterList.get(j))) {//judge whether exits cast relationship
                        matchedTypeCount++;
                    } else if (judgeRelationshipOfExtend(parameterType, parameterCompleteList.get(j))) {//judge whether is parent and child class relationship
                        matchedTypeCount++;
                    } else if (parameterType.equals("null")) {
                        matchedTypeCount++;
                    } else if (parameterList.get(j).equals("Object")) {
                        matchedTypeCount++;
                    } else if(parameterType.equals("userDefinedClass")){
                        matchedTypeCount++;
                    }
                }
                if (matchedTypeCount == 0) {
                    candidateMethodList.remove(matchedList.get(i));
                } else {
                    map.put(matchedList.get(i), matchedTypeCount);
                }
            } else {
                candidateMethodList.remove(matchedList.get(i));
            }
        }

        // choose the only correct one method declaration
        int lastCompletelyMatchedTypeCount = 0;
        for (int i = 0; i < candidateMethodList.size(); i++) {
            List<String> parameterList = methodAndParameterTypeMap.get(candidateMethodList.get(i));
            int completelyMatchedTypeCount = 0;
            for (int j = 0; j < arguments.size(); j++) {
                List<Expression> list = new ArrayList<>();
                list.add(arguments.get(j));
                String parameterType = new String("");
                parameterType = getArguments2(list, parameterType);
                if (parameterList.get(j).equals(parameterType)) {
                    completelyMatchedTypeCount++;
                }
            }
            if (completelyMatchedTypeCount == arguments.size()) {
                methodDeclaration = candidateMethodList.get(i);
                break;
            } else if (map.get(candidateMethodList.get(i)) == arguments.size()) {
                methodDeclaration = candidateMethodList.get(i);
                break;
            } else {
                if (completelyMatchedTypeCount > lastCompletelyMatchedTypeCount) {
                    methodDeclaration = candidateMethodList.get(i);
                    lastCompletelyMatchedTypeCount = completelyMatchedTypeCount;
                } else if (completelyMatchedTypeCount == lastCompletelyMatchedTypeCount) {
                    // the following code need to be discussed
                    if (map.get(methodDeclaration) != null) {
                        if (map.get(methodDeclaration) < map.get(candidateMethodList.get(i))) {
                            methodDeclaration = candidateMethodList.get(i);
                        }
                    } else if (map.get(methodDeclaration) == null) {
                        methodDeclaration = candidateMethodList.get(i);
                    } else if (!candidateMethodList.get(i).contains("null")) {
                        methodDeclaration = candidateMethodList.get(i);
                    }
                }
            }
        }
        return methodDeclaration;
    }

    protected boolean judgeRelationshipOfExtend(String childClassName, String parentClassName) {
        try {
            if (parentClassName.contains(".") && class_name_map.get(childClassName) != null && class_name_map.get(childClassName).contains(".") && !parentClassName.contains("[")) {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(class_name_map.get(childClassName));
                Class clazz2 = Thread.currentThread().getContextClassLoader().loadClass(parentClassName);
                if (clazz2.isAssignableFrom(clazz)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            if (!(e instanceof ClassNotFoundException)) {
                parsedFlag = false;
                System.err.println(e.getMessage());
            }
        } catch (Error e) {
            parsedFlag = false;
            System.err.println(e.getMessage());
        }
        return false;
    }

    protected boolean judgeRelationshipOfCast(String sourceClassName, String objectClassName) {
        if (class_name_map.get(sourceClassName) != null && class_name_map.get(objectClassName) != null) {
            if (castMap.get(class_name_map.get(sourceClassName) + "2" + class_name_map.get(objectClassName)) != null) {
                return true;
            } else {
                return false;
            }
        } else if (class_name_map.get(sourceClassName) != null && class_name_map.get(objectClassName) == null) {
            if (castMap.get(class_name_map.get(sourceClassName) + "2" + objectClassName) != null) {
                return true;
            } else {
                return false;
            }
        } else if (class_name_map.get(sourceClassName) == null && class_name_map.get(objectClassName) != null) {
            if (castMap.get(sourceClassName + "2" + class_name_map.get(objectClassName)) != null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (castMap.get(sourceClassName + "2" + objectClassName) != null) {
                return true;
            } else {
                return false;
            }
        }
    }

    protected void setNodeClassAndMethod(TreeNode node, String clazz, String completeClazz, String method, String completeMethod) {
        node.setClassName(clazz);
        node.setCompleteClassName(completeClazz);
        node.setMethodName(method);
        node.setCompleteMethodName(completeMethod);
    }

    protected void setNodeClass(TreeNode node, String clazz, String completeClazz) {
        node.setClassName(clazz);
        node.setCompleteClassName(completeClazz);
    }

    protected void setNodeMethod(TreeNode node, String method, String completeMethod) {
        node.setMethodName(method);
        node.setCompleteMethodName(completeMethod);
    }

    protected String filterSquareBracket(String type) {
        if (type != null && type.contains("[")) {
            int index = type.indexOf("[");
            type = type.substring(0, index);
        }
        return type;
    }

    protected String preserveSquareBracket(String type) {
        if (type.contains("[")) {
            int index = type.indexOf("[");
            type = type.substring(index, type.length());
        } else {
            type = "";
        }
        return type;
    }


    protected boolean isAllAnnotationStmt(List list) {
        boolean flag = true;
        for (int i = 0; i < list.size(); i++) {
            String str = list.get(i).toString();
            str = str.replaceAll("\n", "");
            str = str.replaceAll(" ", "");
            if (list.get(i) instanceof LineComment || list.get(i) instanceof BlockComment) {
                if (str.equals("//hole")) {
                    endFlag = false;
                    flag = false;
                } else {
                    continue;
                }
            } else {
                flag = false;
            }
        }
        return flag;
    }

    protected String handleConstant(String constant) {
        String result = "Constant";
        if (constant.equals("1") || constant.equals("-1") || constant.equals("0")) {
            result = constant;
        }
        return result;
    }

    protected void addEndNode() {
        TreeNode endNode = new TreeNode();
        setNodeClassAndMethod(endNode, "end", "end", "", "");
        endNode.setAddMethodName(false);
        if (lastNode.getCompleteClassName() != null) {
            if (!lastNode.getCompleteClassName().equals("break") && !lastNode.getCompleteClassName().equals("continue")
                    && !lastNode.getCompleteClassName().equals("return")) {
                codeTree.addNode("",lastNode, endNode,-1);
            }
        }
    }

    protected void addNode(String key, TreeNode node,int line) {
        codeTree.addNode(key, lastNode, node,line);
        lastNode = node;
    }
}
