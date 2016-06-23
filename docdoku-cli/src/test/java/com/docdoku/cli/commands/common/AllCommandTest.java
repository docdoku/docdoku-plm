package com.docdoku.cli.commands.common;

import com.docdoku.cli.MainCommand;
import com.docdoku.cli.helpers.LangHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.json.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@RunWith(JUnit4.class)
public class AllCommandTest {

    private final static Logger LOGGER = Logger.getLogger(AllCommandTest.class.getName());

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

    @Test
    public void partCreationTest(){

        URL resource = AllCommandTest.class.getClassLoader().getResource("com/docdoku/cli/commands/common/upload-part.txt");

        String FILE_PATH = resource.getPath();
        String PART_NUMBER  = "PART-"+UUID.randomUUID().toString().substring(0,6);
        String PART_TITLE = "PartTitle";
        String PART_DESCRIPTION = "PartDescription";

        String[] args = {"cr", "part", "-u", "foo", "-p", "bar", "-h" , "localhost" , "-P", "8080" , "-F", "json",
                "-w" ,"foo", "-o", PART_NUMBER, "-N", PART_TITLE, "-d", PART_DESCRIPTION, FILE_PATH};

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

    @Test
    public void documentListTest(){
        String[] args = {"l", "document", "-u", "foo", "-p", "bar", "-h" , "localhost" , "-P", "8080" , "-F", "json", "-w" ,"foo"};
        MainCommand.main(args);
        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(NO_ERRORS_EXPECTED, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonArray documents = reader.readArray();
        reader.close();

        Assert.assertNotNull(documents);
        int documentsCount = documents.size();

        // Test first object only
        if(documentsCount > 0){
            LOGGER.log(Level.INFO, "Testing first document in documents array");
            JsonValue jsonValue = documents.get(0);
            JsonValue.ValueType valueType = jsonValue.getValueType();
            Assert.assertEquals(valueType, JsonValue.ValueType.OBJECT);
            JsonObject document = (JsonObject) jsonValue;
            String id = document.getString("id");
            Assert.assertNotNull(id);
            Assert.assertFalse(id.isEmpty());
        }
        else {
            LOGGER.log(Level.WARNING, "No document to test in documents array");
        }

    }

    @Test
    public void partListTest(){
        String[] args = {"l", "part", "-u", "foo", "-p", "bar", "-h" , "localhost" , "-P", "8080" , "-F", "json", "-w" ,"foo"};
        MainCommand.main(args);
        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(NO_ERRORS_EXPECTED, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonArray parts = reader.readArray();
        reader.close();

        Assert.assertNotNull(parts);
    }

    @Test
    public void partListCountTest(){
        String[] args = {"l", "part", "-u", "foo", "-p", "bar", "-h" , "localhost" , "-P", "8080" , "-F", "json", "-w" ,"foo", "-c"};
        MainCommand.main(args);
        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(NO_ERRORS_EXPECTED, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonObject partsCount = reader.readObject();
        reader.close();
        Assert.assertNotNull(partsCount);
        Assert.assertTrue(partsCount.getInt("count") >= 0 );
    }

    @Test
    public void partListTestWithLimit(){
        String[] args = {"l", "part", "-u", "foo", "-p", "bar", "-h" , "localhost" , "-P", "8080" , "-F", "json", "-w" ,"foo", "-s", "0", "-m", "1" };
        MainCommand.main(args);
        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(NO_ERRORS_EXPECTED, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonArray parts = reader.readArray();
        reader.close();

        Assert.assertNotNull(parts);
        Assert.assertTrue(parts.size() <= 1);
    }

}
