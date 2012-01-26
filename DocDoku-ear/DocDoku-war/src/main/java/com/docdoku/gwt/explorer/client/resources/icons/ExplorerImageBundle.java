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

package com.docdoku.gwt.explorer.client.resources.icons;


import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Tree;

/**
 *
 * @author Florent Garin
 */
public interface ExplorerImageBundle extends Tree.Resources {

    @Source("com/docdoku/gwt/explorer/client/resources/icons/home.png")
    ImageResource homeNodeIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/branch_folder.png")
    ImageResource workflowNodeIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/tags_blue.png")
    ImageResource tagRootNodeIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/tag_blue.png")
    ImageResource tagNodeIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/template_folder.png")
    ImageResource templateNodeIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/safe.png")
    ImageResource rootNodeIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/open_folder.png")
    ImageResource openFolderNodeIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/closed_folder.png")
    ImageResource closedFolderNodeIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/tree_open.png")
    ImageResource treeOpen();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/tree_closed.png")
    ImageResource treeClosed();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/template.png")
    ImageResource templateRowIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/workflow.png")
    ImageResource workflowRowIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/document.png")
    ImageResource documentRowIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/document_edit.png")
    ImageResource documentEditRowIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/document_lock.png")
    ImageResource documentLockRowIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/document_version.png")
    ImageResource documentNewVersionRowIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/alarm_iteration_on.png")
    ImageResource alarmIterationOnIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/alarm_iteration_off.png")
    ImageResource alarmIterationOffIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/alarm_state_on.png")
    ImageResource alarmStateOnIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/icons/alarm_state_off.png")
    ImageResource alarmStateOffIcon();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/clipboard_large.png")
    ImageResource getTaskImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/editdelete.png")
    ImageResource getDeleteImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/edit_large.png")
    ImageResource getEditImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/flag_green.png")
    ImageResource getStartImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/target.png")
    ImageResource getEndImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_up.png")
    ImageResource getUpImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_down.png")
    ImageResource getDownImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_plus.png")
    ImageResource getAddImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_minus.png")
    ImageResource getRemoveImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/edit.png")
    ImageResource getEditSmallImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/disk_blue_window.png")
    ImageResource getSaveImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/hand_point.png")
    ImageResource getHandImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/branch_element_serial.png")
    ImageResource getSerialImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/branch_element_parallel.png")
    ImageResource getParallelImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/lineV.png")
    ImageResource getVerticalLine();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/lineH2.png")
    ImageResource getHorizontalLine();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_plus_small.png")
    ImageResource getAddSmallImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_minus_small.png")
    ImageResource getRemoveSmallImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_left.png")
    ImageResource getLeftImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_right.png")
    ImageResource getRightImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/selection_delete.png")
    ImageResource getRemoveTaskImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/trafficlight_green.png")
    ImageResource getApproveTaskImage();

    @Source("com/docdoku/gwt/explorer/client/resources/workflow/icons/trafficlight_red.png")
    ImageResource getRejectTaskImage();

    @Source("com/docdoku/gwt/explorer/client/resources/search/icons/navigate_plus.png")
    ImageResource getSearchAddImage();

    @Source("com/docdoku/gwt/explorer/client/resources/search/icons/navigate_minus.png")
    ImageResource getSearchRemoveImage();
}
