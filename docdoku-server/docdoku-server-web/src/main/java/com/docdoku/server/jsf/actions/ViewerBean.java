/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.server.jsf.actions;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IFileViewerManagerLocal;
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
    private IFileViewerManagerLocal fileViewerService;

    public void process() {

        FacesContext currentInstance = FacesContext.getCurrentInstance();

        HttpServletRequest request = (HttpServletRequest) currentInstance.getExternalContext().getRequest();

        SharedEntity sharedEntity = null;
        String uuid = null;
        if(request.getAttribute("sharedEntity") != null){
            sharedEntity = (SharedEntity) request.getAttribute("sharedEntity");
            uuid = sharedEntity.getUuid();
        }

        Set<BinaryResource> attachedFiles = null;

        if (request.getAttribute("documentRevision") != null) {
            attachedFiles = ((DocumentRevision)request.getAttribute("documentRevision")).getLastIteration().getAttachedFiles();
        } else if (request.getAttribute("partRevision") != null) {
            attachedFiles = ((PartRevision)request.getAttribute("partRevision")).getLastIteration().getAttachedFiles();
        }

        if (attachedFiles != null) {
            UIComponent filesContainer = currentInstance.getViewRoot().findComponent("files");
            List<UIComponent> components = new ArrayList<UIComponent>();

            for (BinaryResource attachedFile : attachedFiles) {
                String template = fileViewerService.getHtmlForViewer(attachedFile,uuid);

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


}
