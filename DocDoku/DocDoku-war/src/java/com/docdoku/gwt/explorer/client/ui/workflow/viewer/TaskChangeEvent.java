/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.ui.workflow.viewer;

import com.docdoku.gwt.explorer.common.TaskDTO;
import java.util.EventObject;

/**
 *
 * @author manu
 */
public class TaskChangeEvent extends EventObject {

    public enum Type{REJECT, APPROVE}

    private Type type ;
    private TaskDTO dtoSource ;
    private int activity ;
    private int step ;
    private String comment ;

    public TaskChangeEvent(Object source, Type type, TaskDTO dtoSource) {
        super(source);
        this.type = type ;
        this.dtoSource = dtoSource;
    }

    public TaskDTO getDtoSource() {
        return dtoSource;
    }

    public Type getType() {
        return type;
    }

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    
}
