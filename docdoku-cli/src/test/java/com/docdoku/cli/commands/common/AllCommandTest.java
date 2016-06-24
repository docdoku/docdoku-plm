package com.docdoku.cli.commands.common;

import com.docdoku.cli.MainCommand;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.LangHelper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.json.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@RunWith(JUnit4.class)
public class AllCommandTest {

    String[] AUTH_ARGS = {"-u", "foo", "-p", "bar", "-h" , "localhost" , "-P", "8080" , "-F", "json"};

    private final static Logger LOGGER = Logger.getLogger(AllCommandTest.class.getName());
    private final static URL documentFile = AllCommandTest.class.getClassLoader().getResource("com/docdoku/cli/commands/common/upload-document.txt");
    private final static URL partFile = AllCommandTest.class.getClassLoader().getResource("com/docdoku/cli/commands/common/upload-part.txt");
    private final static URL putDocumentFile = AllCommandTest.class.getClassLoader().getResource("com/docdoku/cli/commands/common/put-document.txt");
    private final static URL putPartFile = AllCommandTest.class.getClassLoader().getResource("com/docdoku/cli/commands/common/put-part.txt");
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

        String[] command = {"wl"};
        String[] args = {};
        MainCommand.main(getArgs(command, args));

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

        String[] command = {"a"};
        String[] args = {};
        MainCommand.main(getArgs(command, args));

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

        String filePath = documentFile.getPath();
        String documentId = "DOC-"+UUID.randomUUID().toString().substring(0,6);
        String documentTitle = "DocTitle";
        String documentDescription = "DocDescription";

        String[] command = {"cr", "document"};
        String[] args = {"-w" ,"foo", "-o", documentId, "-N", documentTitle, "-d", documentDescription, filePath};
        MainCommand.main(getArgs(command, args));

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

        String filePath = partFile.getPath();
        String partNumber  = "PART-"+UUID.randomUUID().toString().substring(0,6);
        String partTitle = "PartTitle";
        String partDescription = "PartDescription";

        String[] command = {"cr", "part"};
        String[] args = {"-w" ,"foo", "-o", partNumber, "-N", partTitle, "-d", partDescription, filePath};
        MainCommand.main(getArgs(command, args));

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

        String[] command = {"l", "document"};
        String[] args = {"-w" ,"foo"};
        MainCommand.main(getArgs(command, args));

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
        String[] command = {"l", "part"};
        String[] args = {"-w", "foo"};
        MainCommand.main(getArgs(command, args));

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

        String[] command = {"l", "part"};
        String[] args = {"-w", "foo", "-c"};
        MainCommand.main(getArgs(command, args));

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

        createPart();

        String[] command = {"l", "part"};
        String[] args = {"-w" ,"foo", "-s", "0", "-m", "1" };
        MainCommand.main(getArgs(command, args));

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(NO_ERRORS_EXPECTED, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonArray parts = reader.readArray();
        reader.close();

        Assert.assertNotNull(parts);
        Assert.assertTrue(parts.size() == 1);
    }

    @Test
    public void checkInCheckOutPartTest(){

        String partNumber = createPart();

        String[] command = {"ci", "part"};
        String[] args = {"-w" ,"foo", "-n", "-o", partNumber, "-r" , "A", "-m", "Checked in from junit tests"};
        MainCommand.main(getArgs(command, args));

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonObject checkinInfos = reader.readObject();
        reader.close();

        String checkingInFile = LangHelper.getLocalizedMessage("CheckingInPart", new Locale("en"));
        Assert.assertTrue(checkinInfos.getString("info").startsWith(checkingInFile));

        outContent.reset();
        errContent.reset();

        String[] checkoutCommand = {"co", "part"};
        String[] checkoutArgs = {"-w" ,"foo", "-n", "-o", partNumber, "-r" , "A"};
        MainCommand.main(getArgs(checkoutCommand, checkoutArgs));

        programOutput = outContent.toString();
        programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader checkoutReader = Json.createReader(new StringReader(programOutput));
        JsonObject checkoutInfos = checkoutReader.readObject();
        reader.close();

        String checkingOutFile = LangHelper.getLocalizedMessage("CheckingOutPart", new Locale("en"));
        Assert.assertTrue(checkoutInfos.getString("info").startsWith(checkingOutFile));

        outContent.reset();
        errContent.reset();

        String[] undoCheckoutCommand = {"uco", "part"};
        String[] undoCheckoutArgs = {"-w" ,"foo",  "-o", partNumber, "-r" , "A"};
        MainCommand.main(getArgs(undoCheckoutCommand, undoCheckoutArgs));

        programOutput = outContent.toString();
        programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader undoCheckoutReader = Json.createReader(new StringReader(programOutput));
        JsonObject undoCheckoutInfos = undoCheckoutReader.readObject();
        reader.close();

        String undoCheckingOutFile = LangHelper.getLocalizedMessage("UndoCheckoutPart", new Locale("en"));
        Assert.assertTrue(undoCheckoutInfos.getString("info").startsWith(undoCheckingOutFile));

    }


    @Test
    public void checkInCheckOutDocumentTest(){

        String documentId = createDocument();

        String[] command = {"ci", "document"};
        String[] args = {"-w" ,"foo", "-n", "-o", documentId, "-r" , "A", "-m", "Checked in from junit tests"};
        MainCommand.main(getArgs(command, args));

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonObject checkinInfos = reader.readObject();
        reader.close();

        String checkingInFile = LangHelper.getLocalizedMessage("CheckingInDocument", new Locale("en"));
        Assert.assertTrue(checkinInfos.getString("info").startsWith(checkingInFile));

        outContent.reset();
        errContent.reset();

        String[] checkoutCommand = {"co", "document"};
        String[] checkoutArgs = {"-w" ,"foo", "-n", "-o", documentId, "-r" , "A"};
        MainCommand.main(getArgs(checkoutCommand, checkoutArgs));

        programOutput = outContent.toString();
        programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader checkoutReader = Json.createReader(new StringReader(programOutput));
        JsonObject checkoutInfos = checkoutReader.readObject();
        reader.close();

        String checkingOutFile = LangHelper.getLocalizedMessage("CheckingOutDocument", new Locale("en"));
        Assert.assertTrue(checkoutInfos.getString("info").startsWith(checkingOutFile));

        outContent.reset();
        errContent.reset();

        String[] undoCheckoutCommand = {"uco", "document"};
        String[] undoCheckoutArgs = {"-w" ,"foo",  "-o", documentId, "-r" , "A"};
        MainCommand.main(getArgs(undoCheckoutCommand, undoCheckoutArgs));

        programOutput = outContent.toString();
        programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader undoCheckoutReader = Json.createReader(new StringReader(programOutput));
        JsonObject undoCheckoutInfos = undoCheckoutReader.readObject();
        reader.close();

        String undoCheckingOutFile = LangHelper.getLocalizedMessage("UndoCheckoutDocument", new Locale("en"));
        Assert.assertTrue(undoCheckoutInfos.getString("info").startsWith(undoCheckingOutFile));

    }

    @Test
    public void documentGetTest() throws IOException {

        String documentId = createDocument();

        File tmpDir = Files.createTempDirectory("docdoku-cli-").toFile();

        String[] command = {"get", "document"};
        String[] args = {"-w" ,"foo", "-o", documentId, "-r" , "A", "-i", "1", tmpDir.getPath()};
        MainCommand.main(getArgs(command, args));


        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        File downloadedFile = new File(tmpDir.getAbsolutePath() + File.separator + FileHelper.getFileName(documentFile.getPath()));
        Assert.assertTrue(downloadedFile.exists());
        Assert.assertTrue("File content should be the same",FileUtils.contentEquals(downloadedFile, new File(documentFile.getPath())));

        tmpDir.deleteOnExit();

    }

    @Test
    public void partGetTest() throws IOException {

        String partNumber = createPart();

        File tmpDir = Files.createTempDirectory("docdoku-cli-").toFile();

        String[] command = {"get", "part"};
        String[] args = {"-w" ,"foo", "-o", partNumber, "-r" , "A", "-i", "1", tmpDir.getPath()};
        MainCommand.main(getArgs(command, args));

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        File downloadedFile = new File(tmpDir.getAbsolutePath() + File.separator + FileHelper.getFileName(partFile.getPath()));
        Assert.assertTrue(downloadedFile.exists());
        Assert.assertTrue("File content should be the same",FileUtils.contentEquals(downloadedFile, new File(partFile.getPath())));

        tmpDir.deleteOnExit();

    }


    @Test
    public void documentPutTest() throws IOException {

        String documentId = createDocument();

        File tmpDir = Files.createTempDirectory("docdoku-cli-").toFile();

        String[] command = {"put", "document"};
        String[] args = {"-w" ,"foo", "-o", documentId, "-r" , "A", putDocumentFile.getPath()};
        MainCommand.main(getArgs(command, args));

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        String[] splitOutput = programOutput.split("\n");
        int linesCount = splitOutput.length;

        Assert.assertTrue(linesCount >= 2);

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

        tmpDir.deleteOnExit();

    }

    @Test
    public void partPutTest() throws IOException {

        String partNumber = createPart();

        File tmpDir = Files.createTempDirectory("docdoku-cli-").toFile();

        String[] command = {"put", "part"};
        String[] args = {"-w" ,"foo", "-o", partNumber, "-r" , "A", putPartFile.getPath()};
        MainCommand.main(getArgs(command, args));

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        String[] splitOutput = programOutput.split("\n");
        int linesCount = splitOutput.length;

        Assert.assertTrue(linesCount >= 2);

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

        tmpDir.deleteOnExit();

    }

    @Test
    public void documentStatusTest(){

        String documentId = createDocument();

        String[] command = {"st", "document"};
        String[] args = {"-w" ,"foo", "-o", documentId, "-r" , "A"};
        MainCommand.main(getArgs(command, args));

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonObject document = reader.readObject();
        reader.close();

        Assert.assertEquals(document.getString("id"),documentId);
        Assert.assertTrue(document.getBoolean("isCheckedOut"));
        Assert.assertEquals(document.getString("workspace"), "foo");
        Assert.assertEquals(document.getString("version"), "A");
        Assert.assertNotNull(document.getJsonArray("files"));
        Assert.assertTrue(document.getJsonArray("files").size() == 1);

    }


    @Test
    public void partStatusTest(){

        String partNumber = createPart();

        String[] command = {"st", "part"};
        String[] args = {"-w" ,"foo", "-o", partNumber, "-r" , "A"};
        MainCommand.main(getArgs(command, args));

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonObject part = reader.readObject();
        reader.close();

        Assert.assertEquals(part.getString("partNumber"),partNumber);
        Assert.assertTrue(part.getBoolean("isCheckedOut"));
        Assert.assertTrue(part.getString("cadFileName").endsWith(FileHelper.getFileName(partFile.getPath())));
        Assert.assertEquals(part.getString("workspace"), "foo");
        Assert.assertEquals(part.getString("version"), "A");

    }

    @Test
    public void folderListTest(){

        String[] command = {"f"};
        String[] args = {"-w" ,"foo"};
        MainCommand.main(getArgs(command, args));

        String programOutput = outContent.toString();
        String programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonArray folders = reader.readArray();
        reader.close();

        Assert.assertNotNull(folders);

        if(!folders.isEmpty()){
            testSubFolder("", folders);
        }

    }

    private void testSubFolder(String parent, JsonArray folders){

        outContent.reset();
        errContent.reset();

        String subFolder = folders.getString(0);

        String[] command = {"f"};
        String[] args = {"-w" ,"foo", "-f", parent+subFolder};
        MainCommand.main(getArgs(command, args));

        String programOutput = outContent.toString();
        String  programError = errContent.toString();

        Assert.assertTrue(programError, programError.isEmpty());
        Assert.assertFalse(OUTPUT_EXPECTED, programOutput.isEmpty());

        JsonReader reader = Json.createReader(new StringReader(programOutput));
        JsonArray subFolders = reader.readArray();
        reader.close();

        if(!subFolders.isEmpty()){
            testSubFolder(parent+subFolder+":", subFolders);
        }

        Assert.assertNotNull(subFolders);

    }

    private String createPart(){

        String filePath = partFile.getPath();
        String partNumber = "Part-"+UUID.randomUUID().toString().substring(0,6);

        String[] command = {"cr", "part"};
        String[] args = {"-w" ,"foo", "-o", partNumber, filePath};
        MainCommand.main(getArgs(command, args));

        outContent.reset();
        errContent.reset();

        return partNumber;
    }


    private String createDocument(){

        String filePath = documentFile.getPath();
        String documentId = "Doc-"+UUID.randomUUID().toString().substring(0,6);

        String[] command = {"cr", "document"};
        String[] args = {"-w" ,"foo", "-o", documentId, filePath};
        MainCommand.main(getArgs(command, args));

        outContent.reset();
        errContent.reset();

        return documentId;
    }

    private String[] getArgs(String[] command, String[] args) {
        return Stream.concat(
                Stream.concat(Arrays.stream(command), Arrays.stream(AUTH_ARGS)),
                Arrays.stream(args)
        ).toArray(String[]::new);
    }

}
