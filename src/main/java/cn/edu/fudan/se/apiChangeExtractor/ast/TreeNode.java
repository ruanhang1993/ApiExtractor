package cn.edu.fudan.se.apiChangeExtractor.ast;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {

    private TreeNode parentNode;
    private List<TreeNode> childNodes;
    private String className;// API simple name or control name(if, while)
    private String completeClassName; // API complete name or control name(if, while)
    private String methodName;// simple method call or control expression(if, while)
    private String completeMethodName;// complete method call or control expression(if, while)
    private List<String> argumentList;// method argument
    private List<TreeNode> dataDependency;// store data dependency
    private boolean isControl;// this flag is used to judge whether a node is a structure node(if while for)
    //	private boolean isTruePath;// used to set up the path to be true if it satisfy the if condition or loop condition
    private boolean isExit;//used to judge whether a node is the exit
    private int depth;
    private int serialNumber;
    private boolean isCondition;
    private String completeMethodDeclaration;

    public void setCompleteMethodDeclaration(String completeMethodDeclaration){
        this.completeMethodDeclaration = completeMethodDeclaration;
    }
    public boolean isCondition() {
        return isCondition;
    }

    public void setCondition(boolean condition) {
        isCondition = condition;
    }

    public boolean isAddMethodName() {
        return isAddMethodName;
    }

    public void setAddMethodName(boolean addMethodName) {
        isAddMethodName = addMethodName;
    }

    private boolean isAddMethodName;

    public TreeNode() {
        completeMethodDeclaration = null;
        parentNode = null;
        childNodes = new ArrayList<TreeNode>();
        className = null;
        methodName = null;
        argumentList = new ArrayList<String>();
        dataDependency = new ArrayList<TreeNode>();
        isControl = false;
        //isTruePath = true;
        isExit = false;
        isAddMethodName = true;
        isCondition = false;
    }

    public List<TreeNode> getDataDependency() {
        return dataDependency;
    }

    public void setDataDependency(List<TreeNode> dataDependency) {
        this.dataDependency = dataDependency;
    }


    public TreeNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(TreeNode parentNode) {
        this.parentNode = parentNode;
    }

    public List<TreeNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<TreeNode> childNodes) {
        this.childNodes = childNodes;
    }

//	public boolean isTruePath() {
//		return isTruePath;
//	}
//
//	public void setTruePath(boolean isTruePath) {
//		this.isTruePath = isTruePath;
//	}

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getArgumentList() {
        return argumentList;
    }

    public void setArgumentList(List<String> argumentList) {
        this.argumentList = argumentList;
    }

    public boolean isControl() {
        return isControl;
    }

    public void setControl(boolean isControl) {
        this.isControl = isControl;
    }

    public boolean isExit() {
        return isExit;
    }

    public void setExit(boolean isExit) {
        this.isExit = isExit;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public String getCompleteMethodName() {
        return completeMethodName;
    }

    public void setCompleteMethodName(String completeMethodName) {
        this.completeMethodName = completeMethodName;
    }

    public String getCompleteClassName() {
        return completeClassName;
    }

    public void setCompleteClassName(String completeClassName) {
        this.completeClassName = completeClassName;
    }

    public String toString() {
        String str;
        if (isControl) {
            str = new String(className);
        } else if(!isAddMethodName){
            str = new String(className);
        }
        else{
            str = new String(className + "." + methodName);
        }
        return str;

    }

    public String getCompleteMethodDeclaration() {
        if(completeMethodDeclaration == null) {
            if (isControl) {
                return completeClassName;
            } else if (!isAddMethodName) {
                return completeClassName;
            } else {
                return completeClassName + "." + completeMethodName;
            }
        }
        else{
            return completeMethodDeclaration;
        }
    }


    //
    public void copyNode(TreeNode node) {
        completeMethodDeclaration = node.getCompleteMethodDeclaration();
        parentNode = null;
        childNodes = new ArrayList<TreeNode>();
        className = node.getClassName();
        methodName = node.getMethodName();
        argumentList = node.getArgumentList();
        dataDependency = node.getDataDependency();
        isControl = node.isControl();
//		isTruePath = node.isTruePath();
        isExit = node.isExit();
        int depth = node.getDepth();
        serialNumber = node.getSerialNumber();
        completeMethodName = node.getCompleteMethodName();
        completeClassName = node.getCompleteClassName();
        isAddMethodName = node.isAddMethodName();
        isCondition = node.isCondition;
    }

}

