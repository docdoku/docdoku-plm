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

package com.docdoku.core.query;

/**
 *
 * @author Morgan Guimard
 */
public class QueryField {

    public static final String PART_MASTER_NUMBER = "pm.number";
    public static final String PART_MASTER_NAME = "pm.name";
    public static final String PART_MASTER_TYPE = "pm.type";
    public static final String PART_MASTER_IS_STANDARD = "pm.standardPart";

    public static final String PART_REVISION_PART_KEY = "pr.partKey";
    public static final String PART_REVISION_VERSION = "pr.version";
    public static final String PART_REVISION_MODIFICATION_DATE = "pr.modificationDate";
    public static final String PART_REVISION_CHECKIN_DATE = "pr.checkInDate";
    public static final String PART_REVISION_CHECKOUT_DATE = "pr.checkOutDate";
    public static final String PART_REVISION_CREATION_DATE = "pr.creationDate";
    public static final String PART_REVISION_LIFECYCLE_STATE = "pr.lifeCycleState";
    public static final String PART_REVISION_STATUS = "pr.status";
    public static final String PART_ITERATION_LINKED_DOCUMENTS = "pr.linkedDocuments";
    public static final String PART_REVISION_ATTRIBUTES_PREFIX = "attr-";
    public static final String PATH_DATA_ATTRIBUTES_PREFIX = "pd-attr-";

    public static final String AUTHOR_LOGIN = "author.login";
    public static final String AUTHOR_NAME = "author.name";

    public static final String CTX_SERIAL_NUMBER = "ctx.serialNumber";
    public static final String CTX_PRODUCT_ID = "ctx.productId";
    public static final String CTX_DEPTH = "ctx.depth";
    public static final String CTX_AMOUNT = "ctx.amount";
    public static final String CTX_P2P_SOURCE = "ctx.p2p.source";
    public static final String CTX_P2P_TARGET = "ctx.p2p.target";

    private QueryField() {
    }
}
