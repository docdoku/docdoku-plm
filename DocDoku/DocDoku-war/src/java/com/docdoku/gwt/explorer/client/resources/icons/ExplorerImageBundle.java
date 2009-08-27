/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009 DocDoku SARL
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

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.TreeImages;

/**
 *
 * @author Florent GARIN
 */
public interface ExplorerImageBundle extends TreeImages {

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/home.png")
    AbstractImagePrototype homeNodeIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/branch_folder.png")
    AbstractImagePrototype workflowNodeIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/tags_blue.png")
    AbstractImagePrototype tagRootNodeIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/tag_blue.png")
    AbstractImagePrototype tagNodeIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/template_folder.png")
    AbstractImagePrototype templateNodeIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/safe.png")
    AbstractImagePrototype rootNodeIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/open_folder.png")
    AbstractImagePrototype openFolderNodeIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/closed_folder.png")
    AbstractImagePrototype closedFolderNodeIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/tree_open.png")
    AbstractImagePrototype treeOpen();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/tree_closed.png")
    AbstractImagePrototype treeClosed();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/template.png")
    AbstractImagePrototype templateRowIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/workflow.png")
    AbstractImagePrototype workflowRowIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/document.png")
    AbstractImagePrototype documentRowIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/document_edit.png")
    AbstractImagePrototype documentEditRowIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/document_lock.png")
    AbstractImagePrototype documentLockRowIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/document_version.png")
    AbstractImagePrototype documentNewVersionRowIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/alarm_iteration_on.png")
    AbstractImagePrototype alarmIterationOnIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/alarm_iteration_off.png")
    AbstractImagePrototype alarmIterationOffIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/alarm_state_on.png")
    AbstractImagePrototype alarmStateOnIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/icons/alarm_state_off.png")
    AbstractImagePrototype alarmStateOffIcon();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/clipboard_large.png")
    AbstractImagePrototype getTaskImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/editdelete.png")
    AbstractImagePrototype getDeleteImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/edit_large.png")
    AbstractImagePrototype getEditImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/flag_green.png")
    AbstractImagePrototype getStartImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/target.png")
    AbstractImagePrototype getEndImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_up.png")
    AbstractImagePrototype getUpImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_down.png")
    AbstractImagePrototype getDownImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_plus.png")
    AbstractImagePrototype getAddImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_minus.png")
    AbstractImagePrototype getRemoveImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/edit.png")
    AbstractImagePrototype getEditSmallImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/disk_blue_window.png")
    AbstractImagePrototype getSaveImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/hand_point.png")
    AbstractImagePrototype getHandImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/branch_element_serial.png")
    AbstractImagePrototype getSerialImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/branch_element_parallel.png")
    AbstractImagePrototype getParallelImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/lineV.png")
    AbstractImagePrototype getVerticalLine();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/lineH2.png")
    AbstractImagePrototype getHorizontalLine();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_plus_small.png")
    AbstractImagePrototype getAddSmallImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_minus_small.png")
    AbstractImagePrototype getRemoveSmallImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_left.png")
    AbstractImagePrototype getLeftImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/navigate_right.png")
    AbstractImagePrototype getRightImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/selection_delete.png")
    AbstractImagePrototype getRemoveTaskImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/trafficlight_green.png")
    AbstractImagePrototype getApproveTaskImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/workflow/icons/trafficlight_red.png")
    AbstractImagePrototype getRejectTaskImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/search/icons/navigate_plus.png")
    AbstractImagePrototype getSearchAddImage();

    @Resource("com/docdoku/gwt/explorer/client/resources/search/icons/navigate_minus.png")
    AbstractImagePrototype getSearchRemoveImage();
}
