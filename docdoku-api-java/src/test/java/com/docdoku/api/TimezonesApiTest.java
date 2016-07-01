package com.docdoku.api;


import com.docdoku.api.client.ApiException;
import com.docdoku.api.services.TimezoneApi;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class TimezonesApiTest {

    @Test
    public void getTimezonesTest() throws ApiException {
        TimezoneApi timezoneApi = new TimezoneApi(TestConfig.GUEST_CLIENT);
        List<String> timeZones = timezoneApi.getTimeZones();
        Assert.assertTrue(timeZones.contains("CET"));
    }
}
