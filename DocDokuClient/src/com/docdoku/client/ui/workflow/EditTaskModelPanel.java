package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.TaskModel;
import com.docdoku.core.entities.User;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.data.MainModel;

import javax.swing.*;

import java.awt.*;


public class EditTaskModelPanel extends JPanel {

    private JLabel mTitleLabel;
    private JLabel mInstructionsLabel;
    private JLabel mWorkerLabel;

    private JTextField mTitleText;
    private JTextArea mInstructionsTextArea;
    private JComboBox mWorkerList;

    public EditTaskModelPanel() {
        this("","");
    }


    public EditTaskModelPanel(TaskModel pTask){
        this(pTask.getTitle(),pTask.getInstructions());
        mWorkerList.setSelectedItem(pTask.getWorker());
    }

    private EditTaskModelPanel(String pTitle, String pInstructions) {
        mTitleLabel = new JLabel(I18N.BUNDLE.getString("TitleMandatory_label"));
        mInstructionsLabel = new JLabel(I18N.BUNDLE.getString("Instructions_label"));
        mWorkerLabel = new JLabel(I18N.BUNDLE.getString("WorkerMandatory_label"));
        mTitleText = new JTextField(new MaxLengthDocument(50), pTitle, 10);
        mInstructionsTextArea=new JTextArea(new MaxLengthDocument(4096), pInstructions,10,35);
        mInstructionsTextArea.setLineWrap(true);
        mInstructionsTextArea.setWrapStyleWord(true);
        mWorkerList =
                new JComboBox(MainModel.getInstance().getUsers());
        mWorkerList.setRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                String label;
                if(value instanceof User){
                    User worker = (User)value;
                    label = worker.getName();
                }else
                    label = value + "";
                setText(label);
                return this;
            }
        });
        createLayout();
    }

    public String getTitle() {
        return mTitleText.getText();
    }

    public String getInstructions() {
        return mInstructionsTextArea.getText();
    }

    public User getUser() {
        return (User) mWorkerList.getSelectedItem();
    }

    public JTextField getTitleText() {
        return mTitleText;
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Task_border")));
        mTitleLabel.setLabelFor(mTitleText);
        mInstructionsLabel.setLabelFor(mInstructionsTextArea);
        mWorkerLabel.setLabelFor(mWorkerList);

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        add(mTitleLabel, constraints);

        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mWorkerLabel, constraints);
        add(mInstructionsLabel, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mTitleText, constraints);

        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mWorkerList, constraints);
        
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(mInstructionsTextArea), constraints);
        
    }


}
