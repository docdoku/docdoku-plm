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
package com.docdoku.core.util;

import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.document.InstanceAttributeTemplate;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.document.DocumentToDocumentLink;
import com.docdoku.core.document.Document;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.Activity;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.TaskModel;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.Workflow;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.regex.Pattern;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author Florent Garin
 */
public class Tools {

    private Tools() {
    }

    public static DocumentMaster resetParentReferences(DocumentMaster pDocM) {
        for (Document doc : pDocM.getDocumentIterations()) {
            doc.setDocumentMaster(pDocM);
            resetParentReferences(doc);
        }

        if (pDocM.getWorkflow() != null) {
            resetParentReferences(pDocM.getWorkflow());
        }

        return pDocM;
    }

    public static DocumentMaster[] resetParentReferences(DocumentMaster[] pDocMs) {
        for (DocumentMaster docM : pDocMs) {
            resetParentReferences(docM);
        }

        return pDocMs;
    }

    private static Document resetParentReferences(Document pDoc) {
        for (DocumentToDocumentLink link : pDoc.getLinkedDocuments()) {
            link.setFromDocument(pDoc);
        }

        /*
        for (InstanceAttribute attr : pDoc.getInstanceAttributes().values()) {
        attr.setDocument(pDoc);
        }
         */
        return pDoc;
    }

    private static Workflow resetParentReferences(Workflow pWf) {
        for (Activity activity : pWf.getActivities()) {
            activity.setWorkflow(pWf);
            resetParentReferences(activity);
        }

        return pWf;
    }

    private static Activity resetParentReferences(Activity pActivity) {
        for (Task task : pActivity.getTasks()) {
            task.setActivity(pActivity);
        }

        return pActivity;
    }

    public static WorkflowModel[] resetParentReferences(WorkflowModel[] pWfs) {
        for (WorkflowModel wf : pWfs) {
            resetParentReferences(wf);
        }

        return pWfs;
    }

    public static WorkflowModel resetParentReferences(WorkflowModel pWf) {
        for (ActivityModel activity : pWf.getActivityModels()) {
            activity.setWorkflowModel(pWf);
            resetParentReferences(activity);
        }

        return pWf;
    }

    private static ActivityModel resetParentReferences(ActivityModel pActivity) {
        for (TaskModel task : pActivity.getTaskModels()) {
            task.setActivityModel(pActivity);
        }

        return pActivity;
    }

    public static DocumentMasterTemplate[] resetParentReferences(DocumentMasterTemplate[] pTemplates) {
        for (DocumentMasterTemplate template : pTemplates) {
            resetParentReferences(template);
        }

        return pTemplates;
    }

    public static DocumentMasterTemplate resetParentReferences(DocumentMasterTemplate pTemplate) {
        for (InstanceAttributeTemplate attr : pTemplate.getAttributeTemplates()) {
            attr.setDocumentMasterTemplate(pTemplate);
        }

        return pTemplate;
    }

    public static boolean safeEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        } else if (o1 == null) {
            return o2.equals(o1);
        }
        return o1.equals(o2);
    }

    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }

    public static String increaseId(String id, String mask) throws ParseException {
        MaskFormatter formatter = new MaskFormatter(mask);
        formatter.setValueContainsLiteralCharacters(false);
        String value = formatter.stringToValue(id).toString();
        StringBuilder newValue = new StringBuilder();
        boolean increase = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            switch (c) {
                case '9':
                    if (increase) {
                        newValue.append('0');
                    } else {
                        newValue.append('9');
                    }
                    break;

                case '8':
                    if (increase) {
                        newValue.append('9');
                        increase = false;
                    } else {
                        newValue.append('8');
                    }

                    break;

                case '7':
                    if (increase) {
                        newValue.append('8');
                        increase = false;
                    } else {
                        newValue.append('7');
                    }

                    break;

                case '6':
                    if (increase) {
                        newValue.append('7');
                        increase = false;
                    } else {
                        newValue.append('6');
                    }

                    break;

                case '5':
                    if (increase) {
                        newValue.append('6');
                        increase = false;
                    } else {
                        newValue.append('5');
                    }

                    break;

                case '4':
                    if (increase) {
                        newValue.append('5');
                        increase = false;
                    } else {
                        newValue.append('4');
                    }

                    break;

                case '3':
                    if (increase) {
                        newValue.append('4');
                        increase = false;
                    } else {
                        newValue.append('3');
                    }

                    break;

                case '2':
                    if (increase) {
                        newValue.append('3');
                        increase = false;
                    } else {
                        newValue.append('2');
                    }

                    break;

                case '1':
                    if (increase) {
                        newValue.append('2');
                        increase = false;
                    } else {
                        newValue.append('1');
                    }

                    break;

                case '0':
                    if (increase) {
                        newValue.append('1');
                        increase = false;
                    } else {
                        newValue.append('0');
                    }

                    break;

                default:
                    newValue.append(c);
            }
        }
        return formatter.valueToString(newValue.reverse().toString());
    }

    public static String convertMask(String inputMask) {
        StringBuilder maskBuilder = new StringBuilder();
        for (int i = 0; i < inputMask.length(); i++) {
            char currentChar = inputMask.charAt(i);
            switch (currentChar) {
                case '#':
                case '*':
                    maskBuilder.append(currentChar);
                    break;

                case '\'':
                    if (i + 1 < inputMask.length()) {
                        char nextChar = inputMask.charAt(i + 1);
                        switch (nextChar) {
                            case '#':
                            case '*':
                                maskBuilder.append(currentChar);
                                break;
                            case '\'':
                                maskBuilder.append(currentChar);
                                maskBuilder.append(nextChar);
                                i++;
                                break;
                        }
                    }
                    break;

                case 'U':
                case 'L':
                case 'A':
                case '?':
                case 'H':
                    maskBuilder.append('\'');
                    maskBuilder.append(currentChar);
                    break;

                default:
                    maskBuilder.append(currentChar);
                    break;
            }

        }
        return maskBuilder.toString();
    }
}
