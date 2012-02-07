/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.gwt.explorer.client.ui.workflow.viewer;

import com.docdoku.gwt.explorer.client.ui.workflow.*;
import com.docdoku.server.rest.dto.ParallelActivityDTO;
import com.docdoku.server.rest.dto.TaskDTO;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.LinkedList;
import java.util.List;

public class ROParallelActivityPanel extends ROActivityPanel implements OptionPanelListener {

    private List<InteractiveTaskPanel> interactivePanels;

    public ROParallelActivityPanel(ParallelActivityDTO model, String userName, boolean active, int step, TaskListener listener) {
        VerticalPanel tasksPanel = new VerticalPanel();
        HorizontalPanel mainPanel = new HorizontalPanel();
        interactivePanels = new LinkedList<InteractiveTaskPanel>();
        int i = 0;
        for (TaskDTO task : model.getTasks()) {
            ROTaskPanel panel = null;
            if (!model.isStopped() && task.getWorker().getName().equals(userName) && active && (task.getStatus() == TaskDTO.Status.IN_PROGRESS || task.getStatus() == TaskDTO.Status.NOT_STARTED)) {
                InteractiveTaskPanel tmp = new InteractiveTaskPanel(task, active, step, i);
                tmp.addTaskListener(listener);
                panel = tmp;
                tmp.addOptionListener(this);
                interactivePanels.add(tmp);
            } else {
                panel = new ROTaskPanel(task);
            }
            tasksPanel.add(panel);
            VerticalLink l = new VerticalLink();
            tasksPanel.add(l);
            tasksPanel.setCellHorizontalAlignment(l, HasHorizontalAlignment.ALIGN_CENTER);
            i++;
        }
        tasksPanel.remove(tasksPanel.getWidgetCount() - 1);
        Label l = new Label(model.getTasksToComplete() + "/" + model.getTasks().size());
        mainPanel.add(l);
        mainPanel.setCellVerticalAlignment(l, HasVerticalAlignment.ALIGN_MIDDLE);
        mainPanel.add(tasksPanel);
        initWidget(mainPanel);
    }

    public void onOptionClicked() {
        hideAllPopups();
    }

    @Override
    public void hideAllPopups() {
        for (InteractiveTaskPanel interactiveTaskPanel : interactivePanels) {
            interactiveTaskPanel.hideOptions();
        }
    }
}
