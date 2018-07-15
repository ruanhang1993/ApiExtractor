package cn.edu.fudan.se.apiChangeExtractor.ast;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.apiChangeExtractor.bean.JdkSequence;
import cn.edu.fudan.se.apiChangeExtractor.bean.MethodCall;

public class CodeTree{

    private TreeNode root;
    private int totalNumber = 0; // used to store the total number of nodes
    private Map<String, String> class_variable = new HashMap<String, String>();
    private List<String> class_variable_list = new ArrayList<>();
    private Map<String, String> class_name_map = new HashMap<>();
    private Map<Integer, JdkSequence> jdkCall = new HashMap<>();

    
    public Map<Integer, JdkSequence> getJdkCall() {
		return jdkCall;
	}

	public void setJdkCall(Map<Integer, JdkSequence> jdkCall) {
		this.jdkCall = jdkCall;
	}

	public Map<String, String> getClass_variable() {
        return class_variable;
    }

    public void setClass_variable(Map<String, String> class_variable) {
        this.class_variable = class_variable;
    }

    public List<String> getClass_variable_list() {
        return class_variable_list;
    }

    public void setClass_variable_list(List<String> class_variable_list) {
        this.class_variable_list = class_variable_list;
    }

    public Map<String, String> getClass_name_map() {
        return class_name_map;
    }

    public void setClass_name_map(Map<String, String> class_name_map) {
        this.class_name_map = class_name_map;
    }

    public TreeNode getRoot() {
        return root;
    }

    public int getTotalNumber() {
//        if(totalNumber != 0){
//            return totalNumber;
//        }
//        else {
//            computeTotalNumberOfNodes(root);
//            return totalNumber;
//        }
        totalNumber = 0;
        computeTotalNumberOfNodes(root);
        return totalNumber;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public CodeTree() {
        root = null;
    }

    public void addNode(String key, TreeNode node, TreeNode newNode, int line) { // first parameter
        // "node" is the
        // node that will to
        // be the parent of
        // newNode
        if (node == null) {
            if (root == null) {
                root = newNode;
            }
        } else {
            node.getChildNodes().add(newNode);
            newNode.setParentNode(node);
        }
        if((!"".equals(key)) && (newNode!=null)){
    		if(jdkCall.get(line)!=null){
    			MethodCall mc = new MethodCall(newNode.getCompleteClassName(), newNode.getCompleteMethodName());
    			jdkCall.get(line).getApiList().add(mc);
    		}else{
    			JdkSequence j = new JdkSequence(line, key);
    			MethodCall mc = new MethodCall(newNode.getCompleteClassName(), newNode.getCompleteMethodName());
    			j.getApiList().add(mc);
    			jdkCall.put(line, j);
    		}
    	}
    }

//    public void addNodeRecurrently(TreeNode parentNode, TreeNode node) {
//        for (int i = 0; i < node.getChildNodes().size(); i++) {
//            TreeNode newNode = new TreeNode();
//            newNode.copyNode(node.getChildNodes().get(i));
//            addNode(parentNode, newNode);
//            addNodeRecurrently(newNode, node.getChildNodes().get(i));
//        }
//
//    }

    public void computeTotalNumberOfNodes(TreeNode node){
        if(node != null) {
            totalNumber++;
            for(int i = 0; i < node.getChildNodes().size(); i ++){
                computeTotalNumberOfNodes(node.getChildNodes().get(i));
            }
        }
    }

    public CodeTree copyCodeTree(CodeTree codeTree){
        TreeNode root = new TreeNode();
        root.copyNode(codeTree.getRoot());
        List<TreeNode> nodeList = new ArrayList<TreeNode>();
        List<TreeNode> correspondingNodeList = new ArrayList<>();
        nodeList.add(root);
        correspondingNodeList.add(codeTree.getRoot());
        while (correspondingNodeList.size() > 0) {
            List<TreeNode> tempList = new ArrayList<TreeNode>();
            List<TreeNode> correspondingTempList = new ArrayList<>();
            for (int i = 0; i < correspondingNodeList.size(); i++) {
                TreeNode correspondingNode = correspondingNodeList.get(i);
                TreeNode node = nodeList.get(i);
                for (int j = 0; j < correspondingNode.getChildNodes().size(); j++) {
                    TreeNode tempNode = new TreeNode();
                    tempNode.copyNode(correspondingNode.getChildNodes().get(j));
                    tempNode.setParentNode(node);
                    node.getChildNodes().add(tempNode);

                    tempList.add(tempNode);
                    correspondingTempList.add(correspondingNode.getChildNodes().get(j));
                }

            }
            nodeList.removeAll(nodeList);
            nodeList = tempList;
            correspondingNodeList.removeAll(correspondingNodeList);
            correspondingNodeList = correspondingTempList;
        }
        this.setRoot(root);
        return this;
    }

    public TreeNode getTreeNode(int serialNumber){
        List<TreeNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<TreeNode> tempList = new ArrayList<TreeNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                TreeNode node = nodeList.get(index);
                if(node.getSerialNumber() == serialNumber){
                    return node;
                }
                else {
                    for (int j = 0; j < node.getChildNodes().size(); j++) {
                        tempList.add(node.getChildNodes().get(j));
                    }
                }

            }
            nodeList.removeAll(nodeList);
            nodeList = tempList;
        }
        return null;
    }

    public TreeNode getHoleNode(){
        List<TreeNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<TreeNode> tempList = new ArrayList<TreeNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                TreeNode node = nodeList.get(index);
                if(node.getCompleteMethodDeclaration().equals("//hole")){
                    return node;
                }
                else {
                    for (int j = 0; j < node.getChildNodes().size(); j++) {
                        tempList.add(node.getChildNodes().get(j));
                    }
                }
            }
            nodeList.removeAll(nodeList);
            nodeList = tempList;
        }
        return null;
    }


    // 由于要考虑program
    // structure，所以一个子节点可能存在多个父节点，所以刚开始的树并不是树，而是图，但是我也用树来定义，因为只有node没有edge，我定义其为树图，得到树图后再转化为树
//    public TreeNode converterGraphToTree(TreeNode rootNode) {
//        root = new TreeNode();
//        root.copyNode(rootNode);
//        addNodeRecurrently(root, rootNode);
//        return root;
//    }

}
