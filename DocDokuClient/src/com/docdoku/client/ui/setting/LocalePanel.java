package com.docdoku.client.ui.setting;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.localization.DisplayableLocale;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import com.docdoku.client.ui.common.GUIConstants;

public class LocalePanel extends JPanel {

    private JLabel mLanguageLabel;
    private JComboBox mLanguageComboBox;

    public LocalePanel(Locale pLocale) {
        mLanguageLabel = new JLabel(I18N.BUNDLE.getString("Language_label"));
        DisplayableLocale[] locales=I18N.getDisplayableLocales();
        mLanguageComboBox = new JComboBox(locales);
        mLanguageComboBox.setSelectedItem(new DisplayableLocale(pLocale));
        createLayout();
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Language_border")));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.weightx = 0;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;

        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        add(mLanguageLabel, constraints);

        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        add(mLanguageComboBox, constraints);
    }

    public Locale getSelectedLocale() {
        return ((DisplayableLocale)mLanguageComboBox.getSelectedItem()).locale;
    }
}
