package com.docdoku.cli.commands.common;

import com.docdoku.cli.MainCommand;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;

@RunWith(JUnit4.class)
public class AllCommandTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Test
    public void workspaceListTest() throws Exception {

        // Launch command
        String[] args = {"wl", "-u", "foo", "-p", "bar", "-h" ,"localhost" , "-P", "8080" , "-F", "json"};
        MainCommand.main(args);

        JsonReader reader = Json.createReader(new StringReader(systemOutRule.getLog()));
        JsonArray workspaces = reader.readArray();
        reader.close();

        systemOutRule.clearLog();

        Assert.assertNotNull(workspaces);
        Assert.assertTrue(workspaces.size()>0);

    }

    @Test
    public void accountInfoTest() throws IOException {

        // Launch command
        String[] args = {"a", "-u", "foo", "-p", "bar", "-h" ,"localhost" , "-P", "8080" , "-F", "json"};
        MainCommand.main(args);

        JsonReader reader = Json.createReader(new StringReader(systemOutRule.getLog()));
        JsonObject account = reader.readObject();
        reader.close();

        systemOutRule.clearLog();

        Assert.assertNotNull(account);
        Assert.assertEquals(account.getString("login"), "foo");

    }

}
