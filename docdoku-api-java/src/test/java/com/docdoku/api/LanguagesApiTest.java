package com.docdoku.api;


import com.docdoku.api.client.ApiException;
import com.docdoku.api.services.LanguagesApi;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class LanguagesApiTest {

    @Test
    public void getLanguagesTest() throws ApiException {
        LanguagesApi languagesApi = new LanguagesApi(TestConfig.GUEST_CLIENT);
        List<String> languages = languagesApi.getLanguages();
        Assert.assertTrue(languages.contains("en"));
        Assert.assertTrue(languages.contains("fr"));
    }
}
