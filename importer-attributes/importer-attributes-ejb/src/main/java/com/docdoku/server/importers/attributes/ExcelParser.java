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

package com.docdoku.server.importers.attributes;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.polarsys.eplmp.i18n.PropertiesLoader;
import org.polarsys.eplmp.server.importers.*;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class ExcelParser that give method to parse the Excel File
 *
 * @author Laurent Le Van
 * @version 1.0.0
 * @since 29/01/16.
 */
public class ExcelParser {

    public static final List<String> TYPES_OF_ATTRIBUTE = Arrays.asList("Text", "Date", "Boolean", "Number", "URL", "Long_Text");
    public static final Pattern PATTERN_NEW_LOV = Pattern.compile("(.*) <(.*)> <(.*)>"); //pattern for a new attribute  of type ListOfValues
    public static final Pattern PATTERN_NEW_ATT = Pattern.compile("(.*) <(.*)>"); //pattern for a new attribute
    public static final String SPLITTER = "\\|";
    private static final Logger LOGGER = Logger.getLogger(ExcelParser.class.getName());
    private static final String I18N_CONF = "/com/docdoku/server/importers/attributes/ExcelImport";
    private static final Pattern FLOAT_PATTERN;
    private static final String INVALID_TEXT_VALUE = "InvalidTextValue";
    private static final String INVALID_BOOLEAN_VALUE = "InvalidBooleanValue";
    private static final String INVALID_DATE_VALUE = "InvalidDateValue";
    private static final String INVALID_NUMBER_VALUE = "InvalidNumberValue";
    private static final String INVALID_URL_VALUE = "InvalidURLValue";
    private static final String INVALID_LOV_VALUE = "InvalidLOVValue";
    private static final String MISSING_COMMENT = "MissingComment";
    private static final String ATTRIBUTE_TYPE_NOT_FOUND = "AttributeTypeNotFound";
    private static final String DUPLICATE_ATTRIBUTE = "DuplicateAttribute";
    private static final String MISSING_ATTRIBUTE_ID = "MissingAttributeId";
    private static final String INVALID_ATTRIBUTE_ID = "InvalidAttributeId";
    private static final String EMPTY_FIELD = "EmptyField";
    private static final String INVALID_HEADER = "InvalidHeader";
    private static final String EMPTY_FILE = "EmptyFile";
    private static final String INVALID_COLUMNS_NUMBER = "InvalidColumnNumber";
    private static final Integer NB_OF_PATH_DATA_IDENTIFIER = 3;
    private static final boolean REJECT_ON_MISSING_COMMENT = false;
    private static final Pattern NUM_PATTERN = Pattern.compile("^[0-9]*$");
    private static final Pattern TEXT_PATTERN = Pattern.compile(".{0,255}");

    static {
        // Pattern taken from http://docs.oracle.com/javase/8/docs/api/java/lang/Double.html#valueOf-java.lang.String-
        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex =
                ("[\\x00-\\x20]*" +
                        "[+-]?(" +
                        "NaN|" +
                        "Infinity|" +
                        "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +
                        "(\\.(" + Digits + ")(" + Exp + ")?)|" +
                        "((" +
                        "(0[xX]" + HexDigits + "(\\.)?)|" +
                        "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +
                        ")[pP][+-]?" + Digits + "))" +
                        "[fFdD]?))" +
                        "[\\x00-\\x20]*");
        FLOAT_PATTERN = Pattern.compile(fpRegex);
    }

    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Properties properties;
    /**
     * Workbook instance
     */
    private Workbook wb;

    /**
     * Sheet we need to read
     */
    private Sheet sheet;

    /**
     * Cells in a 2 dimensions mat to analyse.
     */
    private Cell[][] cells;

    /**
     * A String array to stock the head formats (format for each attributes)
     */
    private String[] headFormat;


    /**
     * Default Constructor that create an empty object
     */
    public ExcelParser() {
        this.wb = null;
        this.sheet = null;
        this.cells = null;
    }

    /**
     * Load the XLS file
     *
     * @param file XLS file
     */
    public ExcelParser(File file, Locale locale) throws IOException, InvalidFormatException {

        LOGGER.log(Level.INFO, "Parsing Excel file");
        this.wb = WorkbookFactory.create(file);
        this.sheet = wb.getSheetAt(0);

        if (sheet.getPhysicalNumberOfRows() > 0) {
            int numberOfRow;

            if (sheet.getRow(sheet.getLastRowNum()).getPhysicalNumberOfCells() != 0) {
                numberOfRow = sheet.getLastRowNum() + 1;

            } else {
                numberOfRow = sheet.getLastRowNum();
            }

            int numberOfCol = sheet.getRow(0).getPhysicalNumberOfCells();

            LOGGER.log(Level.INFO, "Number of Rows : " + numberOfRow);
            LOGGER.log(Level.INFO, "Number of columns : " + numberOfCol);

            this.cells = new Cell[numberOfRow][numberOfCol];

            for (int i = 0; i < numberOfRow; i++) {
                for (int j = 0; j < numberOfCol; j++) {
                    Row row = sheet.getRow(i);
                    if (null != row) {
                        cells[i][j] = row.getCell(j, Row.RETURN_BLANK_AS_NULL);
                    }
                }
            }

            LOGGER.log(Level.INFO, "Excel file parsing done");

        } else {
            this.cells = null;//empty file
            LOGGER.log(Level.WARNING, "Empty File");
        }

        properties = PropertiesLoader.loadLocalizedProperties(locale, I18N_CONF, ExcelParser.class);
    }


    /**
     * Function that check if an empty value type corresponds to the attribute type
     * 1. Check header - 2. Check attribute type 3.Check id
     *
     * @param header type of attributes
     * @param row    row of the data
     * @param column column of the data
     * @param result Map of Error
     * @return Map of Error
     */
    private List<String> checkTypeForEmptyValue(String header, int row, int column, List<String> result) {

        switch (header.toUpperCase()) {
            case "BOOLEAN":
                LOGGER.log(Level.WARNING, "Bad type attribute, empty value cannot be a boolean");
                result = addError(result, INVALID_BOOLEAN_VALUE, (row + 1), (column + 1));
                break;
            case "NUMBER":
                LOGGER.log(Level.WARNING, "Bad type attribute, empty value cannot be a number");
                result = addError(result, INVALID_NUMBER_VALUE, (row + 1), (column + 1));
                break;
            case "LOV":
                LOGGER.log(Level.WARNING, "Bad type attribute, empty value cannot be a LOV");
                result = addError(result, INVALID_LOV_VALUE, (row + 1), (column + 1));
                break;
        }

        return result;
    }


    /**
     * Function that check if the value type correspond to the attribute type
     * 1. Check header - 2. Check attribute type 3.Check id
     *
     * @param header type of attributes
     * @param value  value of that attribute
     * @param row    row of the data
     * @param column column of the data
     * @param result Map of Error
     * @return Map of Error
     */
    private List<String> checkType(String header, String value, int row, int column, List<String> result) {

        switch (header.toUpperCase()) {
            case "TEXT":
                if (!TEXT_PATTERN.matcher(value).matches()) {
                    LOGGER.log(Level.WARNING, "Bad type attribute, has to be a Text, with length <= 255");
                    result = addError(result, INVALID_TEXT_VALUE, (row + 1), (column + 1));
                }

                break;
            case "BOOLEAN":
                if (!value.equals("true") && !value.equals("false")) {
                    LOGGER.log(Level.WARNING, "Bad type attribute, has to be a boolean");
                    result = addError(result, INVALID_BOOLEAN_VALUE, (row + 1), (column + 1));
                }

                break;
            case "DATE":
                if (!checkDate(value.trim())) {
                    LOGGER.log(Level.WARNING, "Bad date format check if like YYYY-MM-DD HH:mm:ss");
                    result = addError(result, INVALID_DATE_VALUE, (row + 1), (column + 1));
                }
                break;
            case "NUMBER":
                if (!FLOAT_PATTERN.matcher(value.trim()).matches()) {
                    LOGGER.log(Level.WARNING, "'" + value + "' not a number");
                    result = addError(result, INVALID_NUMBER_VALUE, (row + 1), (column + 1));
                }

                break;
            case "URL":
                UrlValidator urlValidator = new UrlValidator();
                if (!urlValidator.isValid(value)) {
                    LOGGER.log(Level.WARNING, value + " not an URL");
                    result = addError(result, INVALID_URL_VALUE, (row + 1), (column + 1));
                }

                break;
            case "LOV":
                // check later in the database if the name of list of values exist

                break;
        }

        return result;
    }

    /**
     * Check if a data has a valid date format
     *
     * @param dateToCheck the String data you want to check
     * @return True if valid format , False else
     */
    private boolean checkDate(String dateToCheck) {
        SDF.setLenient(false);
        return SDF.parse(dateToCheck, new ParsePosition(0)) != null;
    }


    /**
     * Method that add error on the error Map
     *
     * @param result   the List which will contain error
     * @param errorKey error key
     * @param message  error message
     * @return Map
     */
    public List<String> addError(List<String> result, String errorKey, Object... message) {
        String error = MessageFormat.format(properties.getProperty(errorKey), message);
        result.add(error);

        return result;
    }

    /**
     * Check if the header of the file is valid
     *
     * @param result a Map of String error
     * @return a Map of String error or null if no error
     */
    public List<String> checkIfValidHeader(List<String> result) {

        //check name attribute
        for (int i = 0; i < cells[0].length; i++) {
            String value = cells[0][i].getStringCellValue();
            RichTextString comment = null;

            if (cells[0][i].getCellComment() != null) {
                comment = cells[0][i].getCellComment().getString();
                headFormat[i] = comment.toString();
            }

            Matcher matchLov = PATTERN_NEW_LOV.matcher(value);
            Matcher matchNew = PATTERN_NEW_ATT.matcher(value);

            if (matchLov.matches() || matchNew.matches()) {

                if (matchLov.matches()) {

                    if (matchLov.group(2).equals("ListOfValues")) {
                        headFormat[i] = "LOV " + matchLov.group(3);
                    } else {
                        result = addError(result, ATTRIBUTE_TYPE_NOT_FOUND, (i + 1));
                    }

                } else {
                    //It means that we have a new attribute which type is not ListOfValues
                    if (!TYPES_OF_ATTRIBUTE.contains(matchNew.group(2))) {
                        LOGGER.log(Level.WARNING, "Attribute type " + matchNew.group(2) + " not exist");

                        result = addError(result, ATTRIBUTE_TYPE_NOT_FOUND, (i + 1));

                    } else {
                        headFormat[i] = matchNew.group(2);
                    }
                }

            } else {

                if (comment == null) {
                    LOGGER.log(Level.WARNING, "Missing comment for attribute, bad format");

                    result = addError(result, MISSING_COMMENT, 1, (i + 1));
                }
            }

            if (i != cells[0].length - 1) {
                //check for duplicate entries
                for (int j = i + 1; j < cells[0].length; j++) {
                    if ((cells[0][i].getStringCellValue().equals(cells[0][j].getStringCellValue()) &&
                            ((cells[0][i].getCellComment() == null && cells[0][j].getCellComment() == null) ||
                                    (cells[0][i].getCellComment() != null && cells[0][j].getCellComment() != null && cells[0][i].getCellComment().getString().equals(cells[0][j].getCellComment().getString()))))) {

                        LOGGER.log(Level.WARNING, "Duplicate entries " + cells[0][i].getStringCellValue() + "on line 1 column " + i + "\n");

                        result = addError(result, DUPLICATE_ATTRIBUTE, (i + 1), (j + 1));
                    }
                }
            }

        }

        return result;

    }


    /**
     * Check file's body
     *
     * @param result a Map of String error
     * @return a Map of String error or null if no error
     */
    private List<String> checkBodyFile(List<String> result) {

        LOGGER.log(Level.INFO, "Checking file body");

        //check each cell content

        for (int i = 1; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {

                String headerType = headFormat[j];

                if (cells[i][j] != null) {

                    //We don't have to check format if special attribute of an article or a deliverable
                    if ((!headFormat[j].equals("pm.number")) &&
                            (!headFormat[j].equals("ctx.serialNumber")) &&
                            (!headFormat[j].equals("ctx.productId"))) {

                        //We can't get String Value of a Numeric Cell so we have to check the type
                        if (cells[i][j].getCellType() == Cell.CELL_TYPE_NUMERIC) {

                            result = (DateUtil.isCellDateFormatted(cells[i][j])) ? checkType(headerType, SDF.format(cells[i][j].getDateCellValue()), i, j, result) : checkType(headerType, "" + cells[i][j].getNumericCellValue(), i, j, result);

                            if (REJECT_ON_MISSING_COMMENT && cells[i][j].getCellComment() == null && cells[0][j].getCellComment() != null)
                                result = addError(result, MISSING_COMMENT, (i + 1), (j + 1));

                        } else { //We suppose it is Text Cell

                            if (cells[i][j].getStringCellValue().split(SPLITTER).length > 1 ||
                                    (cells[i][j].getCellComment() != null &&
                                            cells[i][j].getCellComment().getString().toString().split(SPLITTER).length > 1)) { // in case there are several values
                                String[] values = cells[i][j].getStringCellValue().split(SPLITTER);
                                if (values.length >= 1) {
                                    if (values.length > cells[i][j].getCellComment().getString().toString().split(SPLITTER).length) {
                                        result = addError(result, MISSING_ATTRIBUTE_ID, (i + 1), (j + 1));
                                    }

                                    for (String value : values) {
                                        result = checkType(headerType, value, i, j, result);
                                    }
                                }
                            } else { //case 1 value
                                if (cells[i][j].getStringCellValue().trim().isEmpty()) {
                                    result = checkType(headerType, cells[i][j].getStringCellValue(), i, j, result);
                                } else {
                                    result = checkType(headerType, cells[i][j].getStringCellValue().trim(), i, j, result);
                                }

                                if (REJECT_ON_MISSING_COMMENT && cells[i][j].getCellComment() == null && cells[0][j].getCellComment() != null) {
                                    result = addError(result, MISSING_COMMENT, (i + 1), (j + 1));
                                }
                            }

                            if (cells[i][j].getCellComment() != null) {

                                String comment = cells[i][j].getCellComment().getString().toString();


                                if (comment.split("\\|").length > 1) {
                                    String[] comments = comment.split("\\|");
                                    for (int k = 0; k < comments.length; k++) {
                                        if (!NUM_PATTERN.matcher(comments[k]).matches())
                                            result = addError(result, INVALID_ATTRIBUTE_ID, (i + 1), (j + 1));
                                    }

                                } else {
                                    if (!NUM_PATTERN.matcher(comment).matches())
                                        result = addError(result, INVALID_ATTRIBUTE_ID, (i + 1), (j + 1));
                                }

                            }
                        }

                    } else {//case of special attribute , check if empty only
                        if (cells[i][j].getStringCellValue() == null) {
                            result = addError(result, EMPTY_FIELD, (i + 1), (j + 1));
                        } else if (cells[i][j].getStringCellValue().isEmpty()) {
                            result = addError(result, EMPTY_FIELD, (i + 1), (j + 1));
                        }
                    }

                } else { //if empty cell
                    if ((headFormat[j].equals("pm.number")) ||
                            (headFormat[j].equals("ctx.serialNumber")) ||
                            (headFormat[j].equals("ctx.productId"))) {

                        for (int k = 0; k < cells[i].length; k++) {
                            if (cells[i][k] != null) {
                                result = addError(result, EMPTY_FIELD, (i + 1), (j + 1));
                                break;
                            }
                        }

                    } else if (sheet.getCellComment(i, j) != null) {
                        result = checkTypeForEmptyValue(headerType, i, j, result);
                    }
                }
            }
        }

        LOGGER.log(Level.INFO, "File body check finished");

        return result;
    }


    /**
     * Check if the file respect conditions for import
     *
     * @return null if no error else return a HashMap of the different errors
     */
    public List<String> checkFile() {

        LOGGER.log(Level.INFO, "Checking file");

        List<String> result = new ArrayList<>();

        if (wb != null && sheet != null && cells != null) {

            if (cells[0] != null && cells[0].length > 1) {

                this.headFormat = new String[cells[0].length]; //types of attributes

                if (cells[0][0] != null && cells[0][0].getCellComment() != null && ((cells[0].length > 1 && cells[0][0].getCellComment().getString().toString().equals("pm.number"))
                        || (cells[0].length > NB_OF_PATH_DATA_IDENTIFIER &&
                        (cells[0][0].getCellComment().getString().toString().equals("ctx.productId")
                                && cells[0][1] != null && cells[0][1].getCellComment() != null && cells[0][1].getCellComment().getString().toString().equals("ctx.serialNumber")
                                && cells[0][2] != null && cells[0][2].getCellComment() != null && cells[0][2].getCellComment().getString().toString().equals("pm.number"))))) {


                    //check if the header is valid
                    result = checkIfValidHeader(result);

                    if (result.size() == 0) {
                        //check body file if the header is valid
                        result = checkBodyFile(result);
                    }


                } else { //INVALID COLUMNS

                    //bad length
                    if (cells[0].length <= NB_OF_PATH_DATA_IDENTIFIER && cells[0][0] != null && cells[0][0].getCellComment() != null && cells[0][0].getCellComment().getString().toString().equals("ctx.productId")) {

                        result = addError(result, INVALID_COLUMNS_NUMBER);

                    } else if (cells[0].length > NB_OF_PATH_DATA_IDENTIFIER &&
                            ((cells[0][0] != null && cells[0][0].getCellComment() != null && !(cells[0][0].getCellComment().getString().toString().equals("ctx.productId"))) ||
                                    (cells[0][1] != null && cells[0][1].getCellComment() != null && !(cells[0][1].getCellComment().getString().toString().equals("ctx.serialNumber"))) ||
                                    (cells[0][2] != null && cells[0][2].getCellComment() != null && !(cells[0][2].getCellComment().getString().toString().equals("pm.Number"))))
                            ) {
                        //bad attribute name
                        result = addError(result, INVALID_HEADER);

                    } else if (cells[0][1] != null && cells[0][0].getCellComment() != null && !cells[0][0].getCellComment().getString().toString().equals("pm.number")) {
                        result = addError(result, INVALID_HEADER);
                    }

                    LOGGER.log(Level.WARNING, "Invalid Column data make sure you have partnumber as first column or configuration item id then serial number then partnumber");

                }


            } else {
                result = addError(result, INVALID_HEADER, "XLS File must have at least 2 columns");
            }

        } else {
            if (this.sheet != null) {
                LOGGER.log(Level.WARNING, "Empty file");
                result = addError(result, EMPTY_FILE, "");
            } else {

                result = addError(result, INVALID_HEADER, "");
            }
        }

        LOGGER.log(Level.INFO, "Checking file finished");

        return result;

    }


    public Map<String, PartToImport> getPartsToImport() throws WrongCellCommentException {
        //1st step : Get attribute type

        if (cells != null) {

            AttributeModel listOfAttribute[] = new AttributeModel[cells[0].length];

            initListOfAttribute(listOfAttribute);

            //Let's check if we have to add attribute on articles or on deliverable

            if (cells[0][0].getCellComment() != null && cells[0][0].getCellComment().getString().toString().equals("pm.number")) {
                //import of attribute
                return this.getParts(listOfAttribute);

            } else {
                LOGGER.log(Level.SEVERE, "First cell should contain comment with pm.number");
                throw new WrongCellCommentException();
            }
        } else {
            return null;
        }

    }

    private void initListOfAttribute(AttributeModel listOfAttribute[]) throws WrongCellCommentException {
        for (int i = 0; i < cells[0].length; i++) {

            if (cells[0][i] != null) {

                Matcher matchLov = PATTERN_NEW_LOV.matcher(cells[0][i].getStringCellValue());
                Matcher matchNew = PATTERN_NEW_ATT.matcher(cells[0][i].getStringCellValue());

                if (matchLov.matches()) {

                    listOfAttribute[i] = new AttributeModel(matchLov.group(1), "LOV", matchLov.group(3));
                } else if (matchNew.matches()) {
                    listOfAttribute[i] = new AttributeModel(matchNew.group(1), matchNew.group(2).toUpperCase());
                } else if (cells[0][i].getCellComment() != null) {
                    listOfAttribute[i] = new AttributeModel(cells[0][i].getStringCellValue().trim(), cells[0][i].getCellComment().getString().toString().trim());
                } else {
                    throw new WrongCellCommentException();
                }
            }

        }
    }

    /**
     * @return a Map of ProductInstance
     */
    public Map<String, PathDataToImport> importPathData() throws WrongCellCommentException {
        //1st step : Get attribute type
        LOGGER.log(Level.INFO, "Import path data start");
        AttributeModel listOfAttribute[] = new AttributeModel[cells[0].length];

        initListOfAttribute(listOfAttribute);

        //Let's check if we have to add attribute on articles or on deliverable
        if (
                cells[0].length > NB_OF_PATH_DATA_IDENTIFIER &&
                        cells[0][0].getCellComment() != null && cells[0][0].getCellComment().getString().toString().equals("ctx.productId") &&
                        cells[0][1].getCellComment() != null && cells[0][1].getCellComment().getString().toString().equals("ctx.serialNumber") &&
                        cells[0][2].getCellComment() != null && cells[0][2].getCellComment().getString().toString().equals("pm.number")
                ) {
            //import attribute on data's copies
            return this.getProductInstances(listOfAttribute);

        } else {
            LOGGER.log(Level.SEVERE, "First cells should contain comments with ctx.productId, ctx.serialNumber, pm.number");
            throw new WrongCellCommentException();
        }
    }


    /**
     * @param id              id of this attribute
     * @param j               line of the data in the file
     * @param k               column of the data in the file
     * @param listOfAttribute list of the differents attributes which are present in the file
     * @return a new Attribute
     */
    public Attribute createDateOrNumeric(String id, int j, int k, AttributeModel[] listOfAttribute) {
        String value;
        if (DateUtil.isCellDateFormatted(cells[j][k])) {
            value = SDF.format(cells[j][k].getDateCellValue());
        } else {
            value = "" + cells[j][k].getNumericCellValue();
        }
        return new Attribute(id, listOfAttribute[k], value);
    }


    /**
     * This method parse the file and create a Map of articles
     *
     * @param listOfAttribute List of names of Attributes we found in the header
     * @return Map that has part number as keys and article as values
     */
    public Map<String, PartToImport> getParts(AttributeModel[] listOfAttribute) {

        Map<String, PartToImport> data = new HashMap<>();

        for (int j = 1; j < cells.length; j++) {

            if (cells[j][0] == null) {
                break;
            }

            //2nd step : Create ImportParts objects
            PartToImport newPartToImport = new PartToImport(cells[j][0].getStringCellValue());

            //3rd step : Create and add Attribute
            for (int k = 1; k < cells[j].length; k++) {

                if (cells[j][k] != null) {

                    //Case that we have a numeric or Date Cell
                    if (cells[j][k].getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        String id = cells[j][k].getCellComment() == null ? null : cells[j][k].getCellComment().getString().toString();
                        newPartToImport.addAttribute(createDateOrNumeric(id, j, k, listOfAttribute));

                    } else if (!cells[j][k].getStringCellValue().trim().equals("")) {


                        //Get id in the cell comment
                        if (cells[j][k].getCellComment() != null) {

                            if (cells[j][k].getCellComment().getString().toString().split(SPLITTER).length < 2) {
                                //1st option : cell with one value
                                Attribute newAttribute = new Attribute(cells[j][k].getCellComment().getString().toString(), listOfAttribute[k], cells[j][k].getStringCellValue().trim());

                                newPartToImport.addAttribute(newAttribute);


                            } else {


                                //2nd option : cell with several value
                                String ids[] = cells[j][k].getCellComment().getString().toString().split(SPLITTER);
                                String values[] = cells[j][k].getStringCellValue().split(SPLITTER);

                                addMultiplesAttributes(values, ids, listOfAttribute, k, newPartToImport);


                            }


                        } else {//3rd option : new cell without id
                            Attribute newAttribute = new Attribute(null, listOfAttribute[k], cells[j][k].getStringCellValue().trim());
                            newPartToImport.addAttribute(newAttribute);
                        }


                    } else {//the cell is null
                        if (cells[j][k].getCellComment() != null) {
                            if (!cells[j][k].getCellComment().getString().toString().trim().equals("")) {
                                Attribute newAttribute = new Attribute(cells[j][k].getCellComment().getString().toString(), listOfAttribute[k], null);
                                newPartToImport.addAttribute(newAttribute);
                            }
                        }
                    }

                } else {
                    if (sheet.getCellComment(j, k) != null) {
                        Attribute newAttribute = new Attribute(sheet.getCellComment(j, k).getString().toString(), listOfAttribute[k], null);
                        newPartToImport.addAttribute(newAttribute);
                    }
                }

                data.put(newPartToImport.getNumber(), newPartToImport);

            }
        }

        return data;
    }


    /**
     * This method parse the file and create a Map of Deliverable
     *
     * @param listOfAttribute list of attributes names
     * @return Map that has part number as keys and article as values
     */
    public Map<String, PathDataToImport> getProductInstances(AttributeModel[] listOfAttribute) {
        LOGGER.log(Level.INFO, "Get product instances ... ");

        Map<String, PathDataToImport> data = new HashMap<>();

        for (int i = 1; i < cells.length; i++) {

            if (cells[i][0] == null || cells[i][1] == null || cells[i][2] == null) {
                break;
            }

            PathDataToImport newProductInstance;

            if (cells[i][1].getCellComment() == null) {
                newProductInstance = new PathDataToImport(cells[i][0].getStringCellValue(), cells[i][1].getStringCellValue(), cells[i][2].getStringCellValue(), null);
            } else {
                newProductInstance = new PathDataToImport(cells[i][0].getStringCellValue(), cells[i][1].getStringCellValue(), cells[i][2].getStringCellValue(), cells[i][1].getCellComment().getString().toString().trim());
            }

            //the 3 first cells are not new attribute we want to add
            for (int j = NB_OF_PATH_DATA_IDENTIFIER; j < cells[i].length; j++) {

                if (cells[i][j] != null) {

                    //Case that we have a numeric Cell
                    if (cells[i][j].getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        String id = cells[i][j].getCellComment() == null ? null : cells[i][j].getCellComment().getString().toString();
                        newProductInstance.addAttribute(createDateOrNumeric(id, i, j, listOfAttribute));

                    } else if (!cells[i][j].getStringCellValue().trim().equals("")) {

                        if (cells[i][j].getCellComment() != null) {

                            if (cells[i][j].getCellComment().getString().toString().split(SPLITTER).length < 2) {
                                //1st option : cell with one value

                                Attribute newAttribute;

                                if (!cells[i][j].getStringCellValue().trim().equals("")) {
                                    newAttribute = new Attribute(cells[i][j].getCellComment().getString().toString(), listOfAttribute[j], cells[i][j].getStringCellValue().trim());
                                } else {
                                    newAttribute = new Attribute(cells[i][j].getCellComment().getString().toString(), listOfAttribute[j], null);
                                }
                                newProductInstance.addAttribute(newAttribute);


                            } else {

                                //2nd option : cell with several value
                                String ids[] = cells[i][j].getCellComment().getString().toString().split(SPLITTER);
                                String values[] = cells[i][j].getStringCellValue().split(SPLITTER);

                                addMultiplesAttributes(values, ids, listOfAttribute, j, newProductInstance);

                            }

                        } else {//3rd option : new cell without id

                            Attribute newAttribute = new Attribute(null, listOfAttribute[j], cells[i][j].getStringCellValue().trim());
                            newProductInstance.addAttribute(newAttribute);
                        }
                    } else {//the cell value is null
                        if (cells[i][j].getCellComment() != null) {
                            if (!cells[i][j].getCellComment().getString().toString().trim().equals("")) {
                                Attribute newAttribute = new Attribute(cells[i][j].getCellComment().getString().toString(), listOfAttribute[j], null);
                                newProductInstance.addAttribute(newAttribute);
                            }
                        }
                    }


                } else {
                    if (sheet.getCellComment(i, j) != null) {
                        Attribute newAttribute = new Attribute(sheet.getCellComment(i, j).getString().toString(), listOfAttribute[j], null);
                        newProductInstance.addAttribute(newAttribute);
                    }
                }
            }

            data.put(newProductInstance.getPath(), newProductInstance);
        }
        LOGGER.log(Level.INFO, "Get product instances finished");
        return data;
    }

    public boolean addMultiplesAttributes(String[] values, String[] ids, AttributeModel[] listOfAttribute, int j, AttributesHolder newObjectToImport) {
        if (values.length == ids.length) {

            for (int i = 0; i < ids.length; i++) {

                Attribute newAttribute;

                if (values[i].isEmpty()) {
                    newAttribute = new Attribute(ids[i], listOfAttribute[j], null);
                } else {
                    newAttribute = new Attribute(ids[i], listOfAttribute[j], values[i]);
                }

                newObjectToImport.addAttribute(newAttribute);

            }

        } else if (values.length < ids.length) {
            for (int l = 0; l < ids.length; l++) {

                Attribute newAttribute;

                if (l < values.length) {
                    if (values[l].isEmpty()) {
                        newAttribute = new Attribute(ids[l], listOfAttribute[j], null);
                    } else {
                        newAttribute = new Attribute(ids[l], listOfAttribute[j], values[l]);
                    }

                } else {
                    newAttribute = new Attribute(ids[l], listOfAttribute[j], null);
                }
                newObjectToImport.addAttribute(newAttribute);

            }

        }

        return true;
    }


}


