/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
 * Constants that correspond to searchable fields.
 *
 * @author Morgan Guimard
 */
public interface QueryField {

    String PART_MASTER_NUMBER = "pm.number";
    String PART_MASTER_NAME = "pm.name";
    String PART_MASTER_TYPE = "pm.type";
    String PART_MASTER_IS_STANDARD = "pm.standardPart";

    String PART_REVISION_PART_KEY = "pr.partKey";
    String PART_REVISION_VERSION = "pr.version";
    String PART_REVISION_MODIFICATION_DATE = "pr.modificationDate";
    String PART_REVISION_CHECKIN_DATE = "pr.checkInDate";
    String PART_REVISION_CHECKOUT_DATE = "pr.checkOutDate";
    String PART_REVISION_CREATION_DATE = "pr.creationDate";
    String PART_REVISION_LIFECYCLE_STATE = "pr.lifeCycleState";
    String PART_REVISION_STATUS = "pr.status";
    String PART_ITERATION_LINKED_DOCUMENTS = "pr.linkedDocuments";
    String PART_REVISION_ATTRIBUTES_PREFIX = "attr-";
    String PATH_DATA_ATTRIBUTES_PREFIX = "pd-attr-";

    String AUTHOR_LOGIN = "author.login";
    String AUTHOR_NAME = "author.name";

    String CTX_SERIAL_NUMBER = "ctx.serialNumber";
    String CTX_PRODUCT_ID = "ctx.productId";
    String CTX_DEPTH = "ctx.depth";
    String CTX_AMOUNT = "ctx.amount";
    String CTX_P2P_SOURCE = "ctx.p2p.source";
    String CTX_P2P_TARGET = "ctx.p2p.target";


}
