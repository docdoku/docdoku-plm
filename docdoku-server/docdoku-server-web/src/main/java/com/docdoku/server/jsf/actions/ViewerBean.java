package com.docdoku.server.jsf.actions;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.services.IDocumentViewerManagerLocal;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ManagedBean(name = "viewerBean")
@RequestScoped
public class ViewerBean {

    @EJB
    private IDocumentViewerManagerLocal documentViewerService;

    @ManagedProperty(value = "#{docm.lastIteration.attachedFiles}")
    private Set<BinaryResource> attachedFiles;

    public void process() {

        UIComponent filesContainer = FacesContext.getCurrentInstance().getViewRoot().findComponent("files");

        List<UIComponent> components = new ArrayList<UIComponent>();

        for (BinaryResource attachedFile : this.attachedFiles) {
            String template = documentViewerService.getHtmlForViewer(attachedFile);

            if (template != null && !template.isEmpty()) {
                HtmlPanelGroup fileDiv = new HtmlPanelGroup();
                fileDiv.setLayout("block");
                fileDiv.setStyleClass("attached-file");

                UIOutput output = new UIOutput();
                output.setValue(template);

                fileDiv.getChildren().add(output);

                components.add(fileDiv);
            }

        }

        filesContainer.getChildren().addAll(components);
    }

    public Set<BinaryResource> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(Set<BinaryResource> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }


}
