package com.docdoku.gwt.explorer.client.ui.widget;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Florent GARIN
 */
public class FilesPanel extends DataRoundedPanel implements FormPanel.SubmitCompleteHandler, FormPanel.SubmitHandler {

    private FormPanel m_form;
    private Grid m_fileList;
    private FileUpload m_upload;
    private ClickHandler m_action;
    private Label m_loading;
    private Map<String, String> m_files;
    private SimplePanel m_placeHolder;
    private HorizontalPanel m_loadingCmdPanel;
    private Label m_delLink;
    private boolean m_editionMode;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public FilesPanel() {
        super(ServiceLocator.getInstance().getExplorerI18NConstants().tabFiles());
        m_form = new FormPanel();
        m_placeHolder = new SimplePanel();
        m_fileList = new Grid();
        m_delLink = new Label(i18n.btnRemove());
        m_delLink.setStyleName("normalLinkAction");

        m_form.setEncoding(FormPanel.ENCODING_MULTIPART);
        m_form.setMethod(FormPanel.METHOD_POST);

        m_upload = new FileUpload() {

            @Override
            public void onBrowserEvent(Event event) {
                super.onBrowserEvent(event);
                if (event.getTypeInt() == Event.ONCHANGE) {
                    if (m_action != null) {
                        m_action.onClick(null);
                    }
                }
            }
        };
        m_upload.sinkEvents(Event.ONCHANGE);
        m_upload.setName("upload");
        m_loading = new Label();
        m_loading.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                m_form.removeFromParent();
                m_loading.removeStyleName("busy");
                m_loading.setText("");
                m_placeHolder.setWidget(m_form);
            }
        });

        m_form.setWidget(m_upload);
        m_placeHolder.setWidget(m_form);
        inputPanel.setWidget(0, 0, m_placeHolder);
        m_loadingCmdPanel = new HorizontalPanel();
        m_loadingCmdPanel.setSpacing(5);
        m_loadingCmdPanel.add(m_delLink);
        m_loadingCmdPanel.add(m_loading);
        inputPanel.setWidget(1, 0, m_loadingCmdPanel);
        ScrollPanel scroll = new ScrollPanel(m_fileList);
        scroll.setHeight("10em");
        inputPanel.setWidget(2, 0, scroll);

        //Due to GWT bug we use the deprecated method at compilation time
        m_form.addSubmitCompleteHandler(this);
        m_form.addSubmitHandler(this);
    }

    public void injectUploadAction(ClickHandler lst) {
        m_action = lst;
    }

    public void injectDeleteAction(ClickHandler lst) {
        m_delLink.addClickHandler(lst);
    }

    public void injectFormHandler(FormPanel.SubmitCompleteHandler formHandler) {
        m_form.addSubmitCompleteHandler(formHandler);
    }

    public FileUpload getFileUpload() {
        return m_upload;
    }

    public FormPanel getForm() {
        return m_form;
    }

    public void setEditionMode(boolean editionMode) {
        m_editionMode = editionMode;
        m_placeHolder.setVisible(editionMode);
        m_loadingCmdPanel.setVisible(editionMode);
        for (int i = 0; i < m_fileList.getRowCount(); i++) {
            Widget fileCheckBox = m_fileList.getWidget(i, 0);
            if (fileCheckBox instanceof CheckBox) {
                ((CheckBox) fileCheckBox).setVisible(editionMode);
            }
        }
    }

    public void setFiles(Map<String, String> files) {
        m_fileList.resize(files.size(), 2);
        m_files = files;
        int i = 0;
        for (Map.Entry<String, String> file : files.entrySet()) {
            CheckBox fileCheckBox = new CheckBox();
            fileCheckBox.setVisible(m_editionMode);
            m_fileList.setWidget(i, 0, fileCheckBox);

            //TODO make it relative
            String webappContext = "mydocdoku";
            String htmlLink = "<a href=\"/" + webappContext + "/files/" + file.getValue() + "\">" + file.getKey() + "</a>";
            m_fileList.setHTML(i, 1, htmlLink);
            i++;
        }
    }

    public List<String> getSelectedFiles() {
        List<String> fullNames = new ArrayList<String>();
        int i = 0;
        for (Map.Entry<String, String> file : m_files.entrySet()) {
            CheckBox box = (CheckBox) m_fileList.getWidget(i, 0);
            if (box.getValue()) {
                fullNames.add(file.getValue());
            }
            i++;
        }
        return fullNames;
    }

    public void onSubmitComplete(SubmitCompleteEvent event) {
        m_loading.removeStyleName("busy");
        m_loading.setText("");
    }

    public void onSubmit(SubmitEvent event) {
        m_loading.addStyleName("busy");
        m_loading.setText(i18n.btnCancel());
    }
    /*
    public void onSubmit(FormSubmitEvent event) {
    onSubmit((SubmitEvent)null);
    }

    public void onSubmitComplete(FormSubmitCompleteEvent event) {
    onSubmitComplete((SubmitCompleteEvent)null);
    }
     */
}