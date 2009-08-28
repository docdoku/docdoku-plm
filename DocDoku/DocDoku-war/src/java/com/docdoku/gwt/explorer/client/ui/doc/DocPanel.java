package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.ui.widget.FilesPanel;
import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.common.DocumentDTO;
import com.docdoku.gwt.explorer.common.InstanceAttributeDTO;
import com.docdoku.gwt.explorer.common.MasterDocumentDTO;
import com.docdoku.gwt.explorer.common.UserDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cobogw.gwt.user.client.ui.Button;

/**
 *
 * @author Florent GARIN
 */
public class DocPanel extends FlexTable {

    private Button m_okBtn;
    private Label m_backAction;
    private DocMainPanel m_mainPanel;
    private FilesPanel m_filesPanel;
    private InstanceAttributesPanel m_attributesPanel;
    private LinksPanel m_linksPanel;
    private IterationNavigator m_iterationNavigator;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public DocPanel(final Map<String, Action> cmds) {
        FlexCellFormatter cellFormatter = getFlexCellFormatter();
        m_filesPanel = new FilesPanel();
        m_attributesPanel = new InstanceAttributesPanel();
        m_linksPanel = new LinksPanel();
        m_iterationNavigator = new IterationNavigator(cmds.get("ShowIterationCommand"));
        m_filesPanel.injectDeleteAction(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("DeleteDocFileCommand").execute(m_filesPanel.getSelectedFiles());
            }
        });
        m_filesPanel.injectUploadAction(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("UploadDocFileCommand").execute();
            }
        });
        m_filesPanel.injectFormHandler(new FormPanel.SubmitCompleteHandler() {

            public void onSubmitComplete(SubmitCompleteEvent event) {
                cmds.get("UploadCompleteDocFileCommand").execute();
            }
        });
        m_mainPanel = new DocMainPanel(cmds);

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(5);
        m_backAction = new Label(i18n.btnBack());
        m_backAction.setStyleName("normalLinkAction");
        m_backAction.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("BackCommand").execute();
            }
        });
        m_okBtn = new Button(i18n.btnSave());
        buttonsPanel.add(m_backAction);
        buttonsPanel.add(m_okBtn);
        m_okBtn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
//                Map<String, InstanceAttributeDTO> attributes = m_attributesPanel.getAttributesOld();
                List<InstanceAttributeDTO> attributes = m_attributesPanel.getAttributes();
                DocumentDTO[] links = m_linksPanel.getLinks();
                cmds.get("EditElementCommand").execute(m_mainPanel.getRevisionNote(), attributes.toArray(new InstanceAttributeDTO[attributes.size()]), links);
            }
        });

        setWidget(0, 0, m_mainPanel);
        setWidget(0, 1, m_filesPanel);
        setWidget(1, 0, m_attributesPanel);
        setWidget(1, 1, m_linksPanel);

        setWidget(2, 0, m_iterationNavigator);
//        setWidget(2,0,buttonsPanel);
        setWidget(3, 0, buttonsPanel);

        cellFormatter.setColSpan(2, 0, 2);
        cellFormatter.setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);

        cellFormatter.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        cellFormatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        cellFormatter.setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        cellFormatter.setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);

    }

    public FilesPanel getFilesPanel() {
        return m_filesPanel;
    }

    public void setFiles(Map<String, String> files) {
        m_filesPanel.setFiles(files);
    }

    public void setAttributes(Map<String, InstanceAttributeDTO> attrs) {
        m_attributesPanel.setAttributes(attrs);
    }

    public void setMDoc(final MasterDocumentDTO mdoc) {
        setMDoc(mdoc, mdoc.getIterations().size() - 1);
    }

    public void setMDoc(final MasterDocumentDTO mdoc, int it) {
        m_mainPanel.setMDocAuthor(mdoc.getAuthor().toString());
        int iteration = 0;
        String revision = "";

        if (it == mdoc.getIterations().size() - 1) {
            if (mdoc.getLastIteration() != null) {
                iteration = mdoc.getLastIteration().getIteration();
                revision = mdoc.getLastIteration().getRevisionNote();
            }
        } else {
            iteration = it + 1;
            revision = mdoc.getIterations().get(it).getRevisionNote();
        }

        //TODO make it relative
        String webappContext = "mydocdoku";
        String htmlLink = "<a href=\"/" + webappContext + "/documents/" + mdoc.getWorkspaceId() + "/" + mdoc.getId() + "/" + mdoc.getVersion() + "\">" + i18n.permaLink() + "</a>";
        m_mainPanel.setPermaLink(htmlLink);

        m_mainPanel.setDocID(mdoc.getId() + "-" + mdoc.getVersion() + "-" + iteration);

        m_mainPanel.setCreationDate(mdoc.getCreationDate());
        m_mainPanel.setModificationDate(mdoc.getCheckOutDate());

        m_mainPanel.setCheckOutUser(mdoc.getCheckOutUser());

        m_mainPanel.setMDocType(mdoc.getType());
        m_mainPanel.setMDocTitle(mdoc.getTitle());
        m_mainPanel.setLifeCycleState(mdoc.getLifeCycleState());
        m_mainPanel.setRevisionNote(revision);
        m_mainPanel.setTags(mdoc.getTags() == null ? "" : Arrays.toString(mdoc.getTags()));


        // iteration stuff
        m_iterationNavigator.setIterationsNumber(it, mdoc.getIterations().size() - 1);
        m_iterationNavigator.setVisible(mdoc.getIterations().size() != 1);


        if (mdoc.getWorkflow() != null) {
            m_mainPanel.setWorkflow(mdoc.getWorkflow());
        }

    }

    public void setLinks(Set<DocumentDTO> links, String workspaceId) {
        m_linksPanel.setLinks(links, workspaceId);

    }

    public void setEditionMode(boolean editionMode) {
        m_mainPanel.setEditionMode(editionMode);
        m_attributesPanel.setEditionMode(editionMode);
        m_filesPanel.setEditionMode(editionMode);
        m_linksPanel.setEditionMode(editionMode);
        m_okBtn.setVisible(editionMode);
    }

    public void clearInputs() {
        m_mainPanel.clearInputs();
        m_attributesPanel.clearInputs();
    }
}
