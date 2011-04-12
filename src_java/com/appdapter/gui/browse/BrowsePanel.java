/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BrowsePanel.java
 *
 * Created on Oct 25, 2010, 1:18:06 PM
 */

package com.appdapter.gui.browse;

import java.awt.event.MouseAdapter;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreeModel;

/**
 * @author winston
 */
public class BrowsePanel extends javax.swing.JPanel implements DisplayContext {

	private	TreeModel	myTreeModel;
    /** Creates new form BrowsePanel */
    public BrowsePanel(TreeModel tm) {
		myTreeModel = tm;
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myVerticalSplitPane = new javax.swing.JSplitPane();
        myLowerPanel = new javax.swing.JPanel();
        myCmdInputTextField = new javax.swing.JTextField();
        myLogScrollPane = new javax.swing.JScrollPane();
        myLogTextArea = new javax.swing.JTextArea();
        myUpperPanel = new javax.swing.JPanel();
        myBrowserSplitPane = new javax.swing.JSplitPane();
        myTreeScrollPane = new javax.swing.JScrollPane();
        myTree = new javax.swing.JTree();
        myContentPanel = new javax.swing.JPanel();
        myBoxPanelStatus = new javax.swing.JTextField();
        myBoxPanelTabPane = new javax.swing.JTabbedPane();
        myHomeBoxPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        myVerticalSplitPane.setDividerLocation(300);
        myVerticalSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        myLowerPanel.setLayout(new java.awt.BorderLayout());

        myCmdInputTextField.setText("type/paste commands/uris/urls here");
        myCmdInputTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myCmdInputTextFieldActionPerformed(evt);
            }
        });
        myLowerPanel.add(myCmdInputTextField, java.awt.BorderLayout.NORTH);

        myLogTextArea.setColumns(20);
        myLogTextArea.setEditable(false);
        myLogTextArea.setRows(5);
        myLogTextArea.setText("one\ntwo\nthree\nfour\nfive\nsix\nseven\neight\nnine\nten");
        myLogScrollPane.setViewportView(myLogTextArea);

        myLowerPanel.add(myLogScrollPane, java.awt.BorderLayout.CENTER);

        myVerticalSplitPane.setBottomComponent(myLowerPanel);

        myUpperPanel.setLayout(new java.awt.BorderLayout());

        myBrowserSplitPane.setDividerLocation(140);

        myTree.setModel(myTreeModel);
        myTreeScrollPane.setViewportView(myTree);

        myBrowserSplitPane.setLeftComponent(myTreeScrollPane);

        myContentPanel.setBackground(new java.awt.Color(51, 0, 51));
        myContentPanel.setLayout(new java.awt.BorderLayout());

        myBoxPanelStatus.setText("is panel loading status here useful?");
        myContentPanel.add(myBoxPanelStatus, java.awt.BorderLayout.NORTH);

        javax.swing.GroupLayout myHomeBoxPanelLayout = new javax.swing.GroupLayout(myHomeBoxPanel);
        myHomeBoxPanel.setLayout(myHomeBoxPanelLayout);
        myHomeBoxPanelLayout.setHorizontalGroup(
            myHomeBoxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 367, Short.MAX_VALUE)
        );
        myHomeBoxPanelLayout.setVerticalGroup(
            myHomeBoxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 249, Short.MAX_VALUE)
        );

        myBoxPanelTabPane.addTab("home", myHomeBoxPanel);

        myContentPanel.add(myBoxPanelTabPane, java.awt.BorderLayout.CENTER);

        myBrowserSplitPane.setRightComponent(myContentPanel);

        myUpperPanel.add(myBrowserSplitPane, java.awt.BorderLayout.CENTER);

        myVerticalSplitPane.setLeftComponent(myUpperPanel);

        add(myVerticalSplitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

	private void myCmdInputTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myCmdInputTextFieldActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_myCmdInputTextFieldActionPerformed
	public void addTreeMouseAdapter(MouseAdapter ma) {
		myTree.addMouseListener(ma);
	}
	@Override public JTabbedPane getBoxPanelTabPane() {
		return myBoxPanelTabPane;
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField myBoxPanelStatus;
    private javax.swing.JTabbedPane myBoxPanelTabPane;
    private javax.swing.JSplitPane myBrowserSplitPane;
    private javax.swing.JTextField myCmdInputTextField;
    private javax.swing.JPanel myContentPanel;
    private javax.swing.JPanel myHomeBoxPanel;
    private javax.swing.JScrollPane myLogScrollPane;
    private javax.swing.JTextArea myLogTextArea;
    private javax.swing.JPanel myLowerPanel;
    private javax.swing.JTree myTree;
    private javax.swing.JScrollPane myTreeScrollPane;
    private javax.swing.JPanel myUpperPanel;
    private javax.swing.JSplitPane myVerticalSplitPane;
    // End of variables declaration//GEN-END:variables

}
