/**
 * Created by kelto on 01/06/15.
 */
@XmlJavaTypeAdapter(value=DateAdapter.class, type=Date.class)
package com.docdoku.server.rest.dto.product;

import com.docdoku.server.rest.converters.DateAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
