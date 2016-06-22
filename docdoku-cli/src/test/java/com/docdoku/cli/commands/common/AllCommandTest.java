package com.docdoku.cli.commands.common;

import com.docdoku.cli.MainCommand;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

@RunWith(JUnit4.class)
public class AllCommandTest {

    private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final static ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    static {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void resetStreams(){
        outContent.reset();
        errContent.reset();
    }

    @Test
    public void workspaceListTest() throws Exception {

        String[] args = {"wl", "-u", "foo", "-p", "bar", "-h" ,"localhost" , "-P", "8080" , "-F", "json"};
        MainCommand.main(args);

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError.isEmpty());
        Assert.assertFalse(programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonArray workspaces = reader.readArray();
        reader.close();

        Assert.assertNotNull(workspaces);
        Assert.assertTrue(workspaces.size()>0);

    }

    @Test
    public void accountInfoTest() throws Exception {

        String[] args = {"a", "-u", "foo", "-p", "bar", "-h" ,"localhost" , "-P", "8080" , "-F", "json"};
        MainCommand.main(args);

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError.isEmpty());
        Assert.assertFalse(programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonObject account = reader.readObject();
        reader.close();

        Assert.assertNotNull(account);
        Assert.assertEquals(account.getString("login"), "foo");

    }

}
