/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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
package com.docdoku.core.product;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.meta.InstanceAttribute;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This class represents the
 *
 * @author Florent Garin
 * @version 1.1, 18/05/11
 * @since   V1.1
 */
public class Part implements Serializable {

    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(inverseJoinColumns = {
        @JoinColumn(name = "ATTACHEDFILES_FULLNAME", referencedColumnName = "FULLNAME")
    },
    joinColumns = {
        @JoinColumn(name = "PART_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID"),
        @JoinColumn(name = "PART_MASTERPART_ID", referencedColumnName = "MASTERPART_ID"),
        @JoinColumn(name = "PART_MASTERPART_VERSION", referencedColumnName = "MASTERPART_VERSION"),
        @JoinColumn(name = "PART_ITERATION", referencedColumnName = "ITERATION")
    })
    private Set<BinaryResource> attachedFiles = new HashSet<BinaryResource>();

    private String revisionNote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
        @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

/*
    @OneToMany(orphanRemoval=true, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @MapKey(name="name")
    @JoinTable(
    inverseJoinColumns={
        @JoinColumn(name="INSTANCEATTRIBUTE_ID", referencedColumnName="ID")
    },
    joinColumns={
        @JoinColumn(name="PART_WORKSPACE_ID", referencedColumnName="WORKSPACE_ID"),
        @JoinColumn(name="PART_MASTERPART_ID", referencedColumnName="MASTERPART_ID"),
        @JoinColumn(name="PART_MASTERPART_VERSION", referencedColumnName="MASTERPART_VERSION"),
        @JoinColumn(name="PART_ITERATION", referencedColumnName="ITERATION")
    })
    private Map<String, InstanceAttribute> instanceAttributes=new HashMap<String, InstanceAttribute>();
    */
    
}
