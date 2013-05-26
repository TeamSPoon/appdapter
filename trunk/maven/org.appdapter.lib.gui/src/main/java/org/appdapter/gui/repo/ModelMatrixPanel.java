/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * ModelMatrixPanel.java
 *
 * Created on Oct 25, 2010, 8:12:03 PM
 */

package org.appdapter.gui.repo;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.pojo.AbstractScreenBoxedPOJOPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModelMatrixPanel extends AbstractScreenBoxedPOJOPanel {
	static Logger theLogger = LoggerFactory.getLogger(ModelMatrixPanel.class);

	/** Creates new form ModelMatrixPanel */
	public ModelMatrixPanel() {
		initComponents();
	}

	@Override protected void initGUI() {
		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		myTopSplitPane = new javax.swing.JSplitPane();
		myBottomSplitPane = new javax.swing.JSplitPane();
		myTableScrollPane = new javax.swing.JScrollPane();
		myTable = new javax.swing.JTable();
		myActionPanel = new javax.swing.JPanel();
		myAddButton = new javax.swing.JButton();
		myEditButton = new javax.swing.JButton();
		myRemoveButton = new javax.swing.JButton();
		myFilterPanel = new javax.swing.JPanel();
		myQueryField = new javax.swing.JTextField();

		setLayout(new java.awt.BorderLayout());

		myTopSplitPane.setDividerLocation(60);
		myTopSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

		myBottomSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

		myTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } },
				new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
		myTableScrollPane.setViewportView(myTable);

		myBottomSplitPane.setTopComponent(myTableScrollPane);

		myAddButton.setText("add");
		myAddButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				myAddButtonActionPerformed(evt);
			}
		});
		myActionPanel.add(myAddButton);

		myEditButton.setText("save");
		myEditButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				myEditButtonActionPerformed(evt);
			}
		});
		myActionPanel.add(myEditButton);

		myRemoveButton.setText("remove");
		myActionPanel.add(myRemoveButton);

		myBottomSplitPane.setBottomComponent(myActionPanel);

		myTopSplitPane.setBottomComponent(myBottomSplitPane);

		myQueryField.setText("query");
		myQueryField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				myQueryFieldActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout myFilterPanelLayout = new javax.swing.GroupLayout(myFilterPanel);
		myFilterPanel.setLayout(myFilterPanelLayout);
		myFilterPanelLayout.setHorizontalGroup(myFilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				myFilterPanelLayout.createSequentialGroup().addContainerGap()
						.addComponent(myQueryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(31, Short.MAX_VALUE)));
		myFilterPanelLayout.setVerticalGroup(myFilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				javax.swing.GroupLayout.Alignment.TRAILING,
				myFilterPanelLayout.createSequentialGroup().addContainerGap(52, Short.MAX_VALUE)
						.addComponent(myQueryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(28, 28, 28)));

		myTopSplitPane.setTopComponent(myFilterPanel);

		add(myTopSplitPane, java.awt.BorderLayout.CENTER);
	}// </editor-fold>//GEN-END:initComponents

	private void myAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myAddButtonActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_myAddButtonActionPerformed

	private void myQueryFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myQueryFieldActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_myQueryFieldActionPerformed

	private void myEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myEditButtonActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_myEditButtonActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel myActionPanel;
	private javax.swing.JButton myAddButton;
	private javax.swing.JSplitPane myBottomSplitPane;
	private javax.swing.JButton myEditButton;
	private javax.swing.JPanel myFilterPanel;
	private javax.swing.JTextField myQueryField;
	private javax.swing.JButton myRemoveButton;
	private javax.swing.JTable myTable;
	private javax.swing.JScrollPane myTableScrollPane;
	private javax.swing.JSplitPane myTopSplitPane;

	// End of variables declaration//GEN-END:variables

	@Override public void focusOnBox(Box b) {
		theLogger.info("Focusing on box: " + b);
	}

	@Override public Object getObject() {
		return this;
	}
}
