/**
 * Created by kelto on 01/06/15.
 */
@XmlJavaTypeAdapter(value=DateAdapter.class, type=Date.class)
package com.docdoku.server.rest.dto.change;

import com.docdoku.server.rest.util.DateAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
