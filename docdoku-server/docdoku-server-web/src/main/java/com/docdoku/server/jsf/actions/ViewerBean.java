package com.docdoku.server.jsf.actions;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.services.IDocumentViewerManagerLocal;
import com.docdoku.core.sharing.SharedEntity;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Named("viewerBean")
@RequestScoped
public class ViewerBean {

    @EJB
    private IDocumentViewerManagerLocal documentViewerService;

    public void process() {

        FacesContext currentInstance = FacesContext.getCurrentInstance();

        HttpServletRequest request = (HttpServletRequest) currentInstance.getExternalContext().getRequest();

        SharedEntity sharedEntity = null;
        String uuid = null;
        if(request.getAttribute("sharedEntity") != null){
            sharedEntity = (SharedEntity) request.getAttribute("sharedEntity");
            uuid = sharedEntity.getUuid();
        }

        UIComponent filesContainer = currentInstance.getViewRoot().findComponent("files");

        List<UIComponent> components = new ArrayList<UIComponent>();
        Set<BinaryResource> attachedFiles = ((DocumentRevision)request.getAttribute("documentRevision")).getLastIteration().getAttachedFiles();
        for (BinaryResource attachedFile : attachedFiles) {
            String template = documentViewerService.getHtmlForViewer(attachedFile,uuid);

            if (template != null && !template.isEmpty()) {
                HtmlPanelGroup fileDiv = new HtmlPanelGroup();
                fileDiv.setLayout("block");
                fileDiv.setStyleClass("attached-file accordion-group");
                UIOutput output = new UIOutput();
                output.setValue(template);
                fileDiv.getChildren().add(output);
                components.add(fileDiv);
            }

        }

        filesContainer.getChildren().addAll(components);
    }


}
