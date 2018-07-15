package cn.edu.fudan.se.apiChangeExtractor.ast;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class TreeView {

    private JTree tree;

    public JTree getTree() {
        return tree;
    }

    public void setTree(JTree tree) {
        this.tree = tree;
    }

    public JTree convertCodeTree(CodeTree codeTree, boolean isCompleteFlag) {
        TreeNode codeTreeRoot = codeTree.getRoot();
        DefaultMutableTreeNode root;
        if(codeTreeRoot != null) {
            if (!isCompleteFlag) {
                root = new DefaultMutableTreeNode(codeTreeRoot.toString());
            } else {
                root = new DefaultMutableTreeNode(codeTreeRoot.getCompleteMethodDeclaration());
            }
            addJTreeNdoe(codeTreeRoot, root, isCompleteFlag);
            tree = new JTree(root);
            expandAll(tree, new TreePath(root), true);
        }
        return tree;

    }

    public void addJTreeNdoe(TreeNode codeTreeNode, DefaultMutableTreeNode treeNode, boolean isCompleteFlag) {
        for (int i = 0; i < codeTreeNode.getChildNodes().size(); i++) {
            DefaultMutableTreeNode currentParentTreeNode;
            if (!isCompleteFlag) {
                currentParentTreeNode = new DefaultMutableTreeNode(codeTreeNode.getChildNodes().get(i).toString());
            } else {
                currentParentTreeNode = new DefaultMutableTreeNode(codeTreeNode.getChildNodes().get(i).getCompleteMethodDeclaration());
            }
            treeNode.add(currentParentTreeNode);
            addJTreeNdoe(codeTreeNode.getChildNodes().get(i), currentParentTreeNode, isCompleteFlag);
        }
    }

    public void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }
}
