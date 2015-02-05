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
package com.docdoku.server.viewers.utils;

import java.util.ArrayList;
import java.util.List;

public class ScormActivity implements IScorm {

    private String title;

    private String resourceIdentifier;

    private String resourceHref;

    private List<ScormActivity> subActivities;

    public ScormActivity() {
        subActivities = new ArrayList<ScormActivity>();
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    public String getResourceHref() {
        return resourceHref;
    }

    public void setResourceHref(String resourceHref) {
        this.resourceHref = resourceHref;
    }

    public List<ScormActivity> getSubActivities() {
        return subActivities;
    }

    public void setSubActivities(List<ScormActivity> subActivities) {
        this.subActivities = subActivities;
    }

    @Override
    public void addSubActivity(ScormActivity subActivity) {
        this.subActivities.add(subActivity);
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "title='" + title + '\'' +
                ", resourceIdentifier='" + resourceIdentifier + '\'' +
                ", resourceHref='" + resourceHref + '\'' +
                ", subActivities=" + subActivities +
                '}';
    }
}
