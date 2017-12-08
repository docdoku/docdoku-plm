package com.docdoku.server.importers.attributes;

/**
 * Exception raised when comments are not present or in a wrong order
 *
 * @author Morgan Guimard
 * @version 1.0.0
 * @since 05/04/16
 */
public class WrongCellCommentException extends Exception {
    public WrongCellCommentException(){
        super();
    }
}
