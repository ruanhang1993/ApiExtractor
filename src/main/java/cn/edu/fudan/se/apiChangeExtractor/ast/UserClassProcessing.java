package cn.edu.fudan.se.apiChangeExtractor.ast;


/**
 * Created by chen chi on 17/4/6.
 */

import java.util.*;

public class UserClassProcessing {
    private List<String> userClassList = new ArrayList<>();

    public void setUserClassList(List<String> userClassList) {
        this.userClassList = userClassList;
    }

    public void addUserClass(String clazz){
        userClassList.add(clazz);
    }

    public boolean isUserClassProcessing(String type) {
        if(type != null) {
            type = type.replaceAll("\\[\\]", "");
            if(type.equals("null")){
                return true;
            }
            for (int i = 0; i < userClassList.size(); i++) {
                String str = userClassList.get(i);
                String[] strs = str.split("\\.");
                str = strs[strs.length - 1];
                if (userClassList.get(i).equals(type) || str.equals(type)) {
                    return true;
                }
            }
        }else{
            return true;
        }
        return false;
    }

    public TreeNode createVariableDeclarationNode() {
        TreeNode node = new TreeNode();
        node.setClassName("userDefinedClass");
        node.setCompleteClassName("userDefinedClass");
        node.setMethodName("Declaration");
        node.setCompleteMethodName("Declaration");
        return node;
    }

    public TreeNode createAssignExprNode() {
        TreeNode node = new TreeNode();
        node.setClassName("userDefinedClass");
        node.setCompleteClassName("userDefinedClass");
        node.setMethodName("Assign");
        node.setCompleteMethodName("Assign");
        return node;
    }

    public TreeNode createObjectCreationExprNode() {
        TreeNode node = new TreeNode();
        node.setClassName("userDefinedClass");
        node.setCompleteClassName("userDefinedClass");
        node.setMethodName("ObjectCreation");
        node.setCompleteMethodName("ObjectCreation");
        return node;
    }

    public TreeNode createArrayCreationExprNode() {
        TreeNode node = new TreeNode();
        node.setClassName("userDefinedClass");
        node.setCompleteClassName("userDefinedClass");
        node.setMethodName("ArrayCreation");
        node.setCompleteMethodName("ArrayCreation");
        return node;
    }

    public TreeNode createArrayInitExprNode() {
        TreeNode node = new TreeNode();
        node.setClassName("userDefinedClass");
        node.setCompleteClassName("userDefinedClass");
        node.setMethodName("ArrayInit");
        node.setCompleteMethodName("ArrayInit");
        return node;
    }

    public TreeNode createCastExprNode() {
        TreeNode node = new TreeNode();
        node.setClassName("userDefinedClass");
        node.setCompleteClassName("userDefinedClass");
        node.setMethodName("Cast");
        node.setCompleteMethodName("Cast");
        return node;
    }

    public TreeNode createUnaryExprNode() {
        TreeNode node = new TreeNode();
        node.setClassName("userDefinedClass");
        node.setCompleteClassName("userDefinedClass");
        node.setMethodName("UnaryExprOperator");
        node.setCompleteMethodName("UnaryExprOperator");
        return node;
    }

    public TreeNode createFieldAccessExprNode() {
        TreeNode node = new TreeNode();
        node.setClassName("userDefinedClass");
        node.setCompleteClassName("userDefinedClass");
        node.setMethodName("FieldAccess");
        node.setCompleteMethodName("FieldAccess");
        return node;
    }


    public TreeNode createArrayAccessExprNode() {
        TreeNode node = new TreeNode();
        node.setClassName("userDefinedClass");
        node.setCompleteClassName("userDefinedClass");
        node.setMethodName("ArrayAccess");
        node.setCompleteMethodName("ArrayAccess");
        return node;
    }
}
