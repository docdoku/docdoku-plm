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
package com.docdoku.core.util;

import com.docdoku.core.product.PartLink;
import com.docdoku.core.product.PartLinkList;
import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.TaskModel;
import com.docdoku.core.workflow.WorkflowModel;
import org.apache.commons.lang.StringUtils;

import javax.swing.text.MaskFormatter;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Florent Garin
 */
public class Tools {
    private static final Logger LOGGER = Logger.getLogger(Tools.class.getName());

    private Tools() {
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

    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("\\p{javaSpaceChar}", "_");
    }

    public static String increaseId(String id, String mask) throws ParseException {
        LOGGER.info("#### Tools.increaseId id = " + id + " , mask = " + mask);
        MaskFormatter formatter = new MaskFormatter(mask);
        formatter.setValueContainsLiteralCharacters(false);
        String value = formatter.stringToValue(id).toString();
        StringBuilder newValue = new StringBuilder();
        boolean increase = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            switch (c) {
                case '9':
                    newValue.append((increase) ? '0' : '9');
                    break;

                case '8':
                    newValue.append((increase) ? '9' : '8');
                    increase = false;
                    break;

                case '7':
                    newValue.append((increase) ? '8' : '7');
                    increase = false;
                    break;

                case '6':
                    newValue.append((increase) ? '7' : '6');
                    increase = false;
                    break;

                case '5':
                    newValue.append((increase) ? '6' : '5');
                    increase = false;
                    break;

                case '4':
                    newValue.append((increase) ? '5' : '4');
                    increase = false;
                    break;

                case '3':
                    newValue.append((increase) ? '4' : '3');
                    increase = false;
                    break;

                case '2':
                    newValue.append((increase) ? '3' : '2');
                    increase = false;
                    break;

                case '1':
                    newValue.append((increase) ? '2' : '1');
                    increase = false;
                    break;

                case '0':
                    newValue.append((increase) ? '1' : '0');
                    increase = false;
                    break;

                default:
                    newValue.append(c);
                    break;
            }
        }
        return formatter.valueToString(newValue.reverse().toString());
    }
    
    public static boolean validateMask(String mask, String str){

        // '*' goes for any alpha-numeric char, '#' for numbers only
        if(mask == null || mask.length() == 0){
            return true;
        }

        // Not same length
        if(mask.length() != str.length()){
            return false;
        }

        Pattern alphaNum = Pattern.compile("[a-zA-Z0-9]");

        for (int i = 0; i < mask.length(); i++) {

            if('*' == mask.charAt(i) && !alphaNum.matcher(str.charAt(i)+"").find()){
                return false;
            }
            if ('#' == mask.charAt(i) && !Character.isDigit(str.charAt(i))){
                return false;
            }
        }

        return true;

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

    public static String getPathAsString(List<PartLink> path) {
        List<String> ids = new ArrayList<>();
        for (PartLink link : path) {
            ids.add(link.getFullId());
        }
        return String.join("-", ids); // java 8
    }

    public static String getPathInstanceAsString(List<PartLink> path, List<Integer> instancesIds) {

        if(path.size() != instancesIds.size()){
           throw new IllegalArgumentException("Path and instances must be same sized");
        }

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < path.size(); i++){
            sb.append(path.get(i).getFullId());
            sb.append("-");
            sb.append(instancesIds.get(i));
            sb.append("-");
        }

        String s = sb.toString();
        return s.substring(0, s.length() - 1);
    }

    public static String getPartLinksAsHumanString(Map<String, List<PartLinkList>> links){
        return getPartLinkAsString(links, " -> ");
    }

    public static String getPartLinksAsExcelString(Map<String, List<PartLinkList>> links){
        return getPartLinkAsString(links, " - ");
    }

    private static String getPartLinkAsString(Map<String, List<PartLinkList>> links, String joinWith) {
        List<String> componentNumbers = new ArrayList<>();
        List<String> pathStrings = new ArrayList<>();
        List<String> typeStrings = new ArrayList<>();

        for (String type : links.keySet()) {
            List<PartLinkList> paths = links.get(type);

            for (PartLinkList path : paths) {

                for (PartLink partLink : path.getPath()) {
                    String linkAsString = partLink.getComponent().getName() + " < " + partLink.getComponent().getNumber() + " > ";

                    if (partLink.getReferenceDescription() != null && !partLink.getReferenceDescription().isEmpty()) {
                        linkAsString += " ( " + partLink.getReferenceDescription() + " )";
                    }
                    componentNumbers.add(linkAsString);
                }

                String join = StringUtils.join(componentNumbers, joinWith);
                pathStrings.add(type + ": " + join);
                componentNumbers.clear();
            }

            String typeLines = StringUtils.join(pathStrings, "\n");
            typeStrings.add(typeLines);
            pathStrings.clear();
        }

        String fullString = StringUtils.join(typeStrings, "\n");
        typeStrings.clear();

        return fullString;
    }

}
