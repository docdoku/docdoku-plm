/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.client.ui.doc;


import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;

import javax.swing.*;
import java.awt.*;

public class ScanPanel extends JPanel {

    private JRadioButton mTiffRadio;
    private JRadioButton mPDFRadio;
    private JLabel mFileNameLabel;
    private JTextField mFileNameText;
    private JLabel mScanSourceLabel;
    private JComboBox mScanSourceList;
    private JLabel mFormatLabel;

    /**
     * @author Gary Gautruche
     * @version 1.4, 15/07/10
     * @since   V1.4
     */
    public ScanPanel(String[] pDeviceNames, String pDefaultDevice, String pDefaultFormat) {

        boolean selectTiff="tiff".equals(pDefaultFormat);
        mTiffRadio = new JRadioButton("Tiff", selectTiff);
        mPDFRadio = new JRadioButton("PDF", !selectTiff);

        ButtonGroup formatGroup = new ButtonGroup();
        formatGroup.add(mTiffRadio);
        formatGroup.add(mPDFRadio);
        mFileNameLabel = new JLabel(I18N.BUNDLE.getString("Name_label"));
        mScanSourceLabel = new JLabel(I18N.BUNDLE.getString("Source_label"));
        mFormatLabel = new JLabel(I18N.BUNDLE.getString("Format_label"));
        mFileNameText = new JTextField(new MaxLengthDocument(50), "", 10);
        mScanSourceList = new JComboBox(pDeviceNames);

        if(pDefaultDevice!=null)
            mScanSourceList.setSelectedItem(pDefaultDevice);
        createLayout();
    }

    public String getFileName() {
        return mFileNameText.getText();
    }

    public String getFileFormat() {
        return mPDFRadio.isSelected()?"pdf":"tiff";
    }

    public String getSelectedDevice(){
        return mScanSourceList.getSelectedItem()+"";
    }

    public JTextField getFileNameText(){
        return mFileNameText;
    }
    private void createLayout() {
        mFileNameLabel.setLabelFor(mFileNameText);
        mScanSourceLabel.setLabelFor(mScanSourceList);

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;

        add(mScanSourceLabel, constraints);
        add(mFileNameLabel, constraints);
        add(mFormatLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mScanSourceList, constraints);
        constraints.gridy = 1;
        add(mFileNameText, constraints);
        constraints.gridwidth = 1;
        constraints.gridy = 2;
        add(mPDFRadio, constraints);
        constraints.gridx = 2;
        add(mTiffRadio, constraints);

    }
}