package com.docdoku.cli.commands.common;

import com.docdoku.cli.MainCommand;
import com.docdoku.cli.helpers.LangHelper;
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
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

@RunWith(JUnit4.class)
public class AllCommandTest {

    private final static String NO_ERRORS_EXPECTED = "Should be no errors";
    private final static String OUTPUT_EXPECTED = "Should have an output";

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

        Assert.assertTrue(NO_ERRORS_EXPECTED, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

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

        Assert.assertTrue(NO_ERRORS_EXPECTED, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonObject account = reader.readObject();
        reader.close();

        Assert.assertNotNull(account);
        Assert.assertEquals(account.getString("login"), "foo");

    }

    @Test
    public void documentCreationTest() throws Exception {

        URL resource = AllCommandTest.class.getClassLoader().getResource("com/docdoku/cli/commands/common/upload-document.txt");

        String FILE_PATH = resource.getPath();
        String DOCUMENT_ID = "DOC-"+UUID.randomUUID().toString().substring(0,6);
        String DOCUMENT_TITLE = "DocTitle";
        String DOCUMENT_DESCRIPTION = "DocDescription";

        String[] args = {"cr", "document", "-u", "foo", "-p", "bar", "-h" , "localhost" , "-P", "8080" , "-F", "json",
                        "-w" ,"foo", "-o", DOCUMENT_ID, "-N", DOCUMENT_TITLE, "-d", DOCUMENT_DESCRIPTION, FILE_PATH};

        MainCommand.main(args);

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(NO_ERRORS_EXPECTED, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        String[] splitOutput = programOutput.split("\n");
        int linesCount = splitOutput.length;
        Assert.assertTrue("Should have at least two lines", linesCount >= 2);

        String firstLine = splitOutput[0];
        JsonReader firstLineReader = Json.createReader(new StringReader(firstLine));
        JsonObject infoLine = firstLineReader.readObject();
        firstLineReader.close();

        String uploadingFile = LangHelper.getLocalizedMessage("UploadingFile", new Locale("en"));
        Assert.assertTrue(infoLine.getString("info").startsWith(uploadingFile));

        String lastLine = splitOutput[linesCount-1];
        JsonReader lastLineReader = Json.createReader(new StringReader(lastLine));
        JsonObject progressLine = lastLineReader.readObject();
        lastLineReader.close();
        Assert.assertEquals(progressLine.getInt("progress"), 100);

    }

}
