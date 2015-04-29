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

    public final static String PART_MASTER_NUMBER = "pm.number";
    public final static String PART_MASTER_NAME = "pm.name";
    public final static String PART_MASTER_TYPE = "pm.type";
    public final static String PART_MASTER_IS_STANDARD = "pm.standardPart";

    public final static String PART_REVISION_PART_KEY = "pr.partKey";
    public final static String PART_REVISION_VERSION = "pr.version";
    public final static String PART_REVISION_MODIFICATION_DATE = "pr.modificationDate";
    public final static String PART_REVISION_CHECKIN_DATE = "pr.checkinDate";
    public final static String PART_REVISION_CHECKOUT_DATE = "pr.checkoutDate";
    public final static String PART_REVISION_CREATION_DATE = "pr.creationDate";
    public final static String PART_REVISION_LIFECYCLE_STATE = "pr.lifeCycleState";
    public final static String PART_REVISION_STATUS = "pr.status";
    public final static String PART_REVISION_ATTRIBUTES_PREFIX = "attr-";

    public final static String AUTHOR_LOGIN = "author.login";
    public final static String AUTHOR_NAME = "author.name";

    public final static String CTX_SERIAL_NUMBER = "ctx.serialNumber";
    public final static String CTX_PRODUCT_ID = "ctx.productId";
    public final static String CTX_DEPTH = "ctx.depth";
    public final static String CTX_AMOUNT = "ctx.amount";

}
