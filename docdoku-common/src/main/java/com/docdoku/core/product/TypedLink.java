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

package com.docdoku.core.product;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by morgan on 29/04/15.
 *
 *
 */

@Table(name="TYPEDLINK")
@Entity
@NamedQueries({
        @NamedQuery(name="TypedLink.findTypedLinksTypeByProductInstanceIteration", query="SELECT DISTINCT t.type FROM TypedLink t JOIN ProductInstanceIteration pi ON t member of pi.typedLinks AND pi = :productInstanceIteration")
})

public class TypedLink implements Serializable{

    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Id
    @Column(name="ID")
    private int id;

    private String type;
    private String pathFrom;
    private String pathTo;

    public TypedLink() {
    }

    public TypedLink(String type, String pathFrom, String pathTo) {
        this.type = type;
        this.pathFrom = pathFrom;
        this.pathTo = pathTo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPathFrom() {
        return pathFrom;
    }

    public void setPathFrom(String pathFrom) {
        this.pathFrom = pathFrom;
    }

    public String getPathTo() {
        return pathTo;
    }

    public void setPathTo(String pathTo) {
        this.pathTo = pathTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
