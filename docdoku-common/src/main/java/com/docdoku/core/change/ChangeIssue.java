/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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


package com.docdoku.core.change;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents an identified issue.
 * The issue may result in one or more <a href="ChangeRequest.html">ChangeRequest</a>.
 *
 * @author Florent Garin
 * @version 2.0, 06/01/14
 * @since   V2.0
 */
@Table(name="CHANGEISSUE")
@Entity
public class ChangeIssue extends ChangeItem {

    /**
     * Identifies the person or organization at the origin of the change, may be null
     * if it is the user who created the object.
     */
    private String initiator;

    private Priority priority;



    public ChangeIssue() {
    }




}
