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

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * This class represents a request for a change,
 * which addresses one or more <a href="ChangeIssue.html">ChangeIssue</a>.
 * @author Florent Garin
 * @version 2.0, 09/01/14
 * @since   V2.0
 */
@Table(name="CHANGEREQUEST")
@Entity
public class ChangeRequest extends ChangeItem {


    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;

    @Lob
    private String changeProposal;

    private Priority priority;

    public ChangeRequest() {
    }


}
