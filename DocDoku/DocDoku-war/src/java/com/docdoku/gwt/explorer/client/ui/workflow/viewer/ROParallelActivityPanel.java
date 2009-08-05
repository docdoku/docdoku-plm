package com.docdoku.gwt.explorer.client.ui.workflow.viewer;

import com.docdoku.gwt.explorer.client.ui.workflow.*;
import com.docdoku.gwt.explorer.common.ParallelActivityDTO;
import com.docdoku.gwt.explorer.common.TaskDTO;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.LinkedList;
import java.util.List;

public class ROParallelActivityPanel extends ROActivityPanel implements OptionPanelListener{

    private List<InteractiveTaskPanel> interactivePanels ;

	public ROParallelActivityPanel(ParallelActivityDTO model, String userName, boolean active,  TaskListener listener){
		VerticalPanel tasksPanel = new VerticalPanel() ;
		HorizontalPanel mainPanel = new HorizontalPanel();
        interactivePanels = new LinkedList<InteractiveTaskPanel>();
		int i = 0 ;
		for (TaskDTO task : model.getTasks()) {
            ROTaskPanel panel = null ;
            if (!model.isStopped() && task.getWorkerName().equals(userName) && active && (task.getStatus() == TaskDTO.TaskStatus.IN_PROGRESS || task.getStatus() == TaskDTO.TaskStatus.NOT_STARTED) ){
                InteractiveTaskPanel tmp = new InteractiveTaskPanel(task, active, model.getStep(), i) ;
                tmp.addTaskListener(listener);
                panel = tmp ;
                tmp.addOptionListener(this) ;
                interactivePanels.add(tmp);
            }else{
                panel = new ROTaskPanel(task);
            }
			tasksPanel.add(panel) ;
			VerticalLink  l =new VerticalLink();
			tasksPanel.add(l);
			tasksPanel.setCellHorizontalAlignment(l, HasHorizontalAlignment.ALIGN_CENTER) ;
            i++ ;
		}
		tasksPanel.remove(tasksPanel.getWidgetCount()-1) ;
		Label l = new Label(model.getNbTaskToComplete()+"/"+ model.getTasks().size());
		mainPanel.add(l) ;
		mainPanel.setCellVerticalAlignment(l, HasVerticalAlignment.ALIGN_MIDDLE) ;
		mainPanel.add(tasksPanel) ;
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
