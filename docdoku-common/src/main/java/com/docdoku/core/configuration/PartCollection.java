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
package com.docdoku.core.configuration;

import com.docdoku.core.common.User;
import com.docdoku.core.product.PartIteration;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class maintains a collection of part iterations which cannot hold
 * more than one {@link com.docdoku.core.product.PartIteration} linked
 * to the same {@link com.docdoku.core.product.PartMaster}.
 *
 * PartCollection is a foundation for the definition of {@link ProductBaseline}
 * and {@link ProductInstanceIteration}.
 *
 * @author Florent Garin
 * @version 2.0, 25/02/14
 * @since V2.0
 */
@Table(name="PARTCOLLECTION")
@Entity
public class PartCollection implements Serializable {


    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "AUTHOR_LOGIN", referencedColumnName = "LOGIN"),
            @JoinColumn(name = "AUTHOR_WORKSPACE_ID", referencedColumnName = "WORKSPACE_ID")
    })
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date creationDate;

    @MapKey(name="baselinedPartKey")
    @OneToMany(mappedBy="partCollection", cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    private Map<BaselinedPartKey, BaselinedPart> baselinedParts=new HashMap<>();

    public PartCollection() {
    }

    public void removeAllBaselinedParts() {
        baselinedParts.clear();
    }

    public Map<BaselinedPartKey, BaselinedPart> getBaselinedParts() {
        return baselinedParts;
    }

    public void addBaselinedPart(PartIteration targetPart){
        BaselinedPart baselinedPart = new BaselinedPart(this, targetPart);
        baselinedParts.put(baselinedPart.getBaselinedPartKey(),baselinedPart);
    }

    public BaselinedPart getBaselinedPart(BaselinedPartKey baselinedPartKey){
        return baselinedParts.get(baselinedPartKey);
    }

    public boolean hasBaselinedPart(BaselinedPartKey baselinedPartKey){
        return baselinedParts.containsKey(baselinedPartKey);
    }

    public Date getCreationDate() {
        return (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PartCollection)) {
            return false;
        }

        PartCollection collection = (PartCollection) o;
        return id == collection.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
