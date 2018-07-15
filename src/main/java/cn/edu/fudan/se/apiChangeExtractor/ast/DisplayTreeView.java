package cn.edu.fudan.se.apiChangeExtractor.ast;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

public class DisplayTreeView {

    public DisplayTreeView(JTree tree,String title) {
        JFrame f = new JFrame("JTreeView");
        JScrollPane scroll = new JScrollPane();
        scroll.getViewport().add(tree);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        f.add(scroll);
        f.setSize(600, 600);
        f.setTitle(title);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
