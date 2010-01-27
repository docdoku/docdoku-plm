package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.EditableWorkflowModelCanvas;
import com.docdoku.client.ui.workflow.HorizontalSeparatorCanvas;
import com.docdoku.client.ui.workflow.WorkflowModelToolBar;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;
import com.docdoku.core.entities.WorkflowModel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;


public class HorizontalSeparatorMouseListener extends MouseAdapter {


    public void mouseEntered(MouseEvent pME) {
        WorkflowModelFrame owner = (WorkflowModelFrame) SwingUtilities.getAncestorOfClass(WorkflowModelFrame.class, pME.getComponent());

        Component component = pME.getComponent();
        switch (owner.getBehaviorMode()) {
            case EDIT:
                component.setCursor(Cursor.getDefaultCursor());
                break;
            case PARALLEL:
                //component.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(WorkflowModelToolBar.PARALLEL_IMAGE, new Point(0, 0), "parallel"));
                component.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
            case SERIAL:
                component.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                //component.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(WorkflowModelToolBar.SERIAL_IMAGE, new Point(0, 0), "serial"));
                break;
        }
    }

    public void mouseClicked(MouseEvent pME) {
        HorizontalSeparatorCanvas source = (HorizontalSeparatorCanvas) pME.getSource();
        WorkflowModelFrame owner = (WorkflowModelFrame) SwingUtilities.getAncestorOfClass(WorkflowModelFrame.class,source);

        WorkflowModel model = owner.getWorkflowModel();
        switch (owner.getBehaviorMode()) {
            case PARALLEL:
                model.addActivityModel(source.getRank(), DefaultValueFactory.createDefaultParallelActivityModel(model));
                ((EditableWorkflowModelCanvas) source.getParent()).refresh();
                break;
            case SERIAL:
                model.addActivityModel(source.getRank(), DefaultValueFactory.createDefaultSerialActivityModel(model));
                ((EditableWorkflowModelCanvas) source.getParent()).refresh();
                break;
        }
    }

}
