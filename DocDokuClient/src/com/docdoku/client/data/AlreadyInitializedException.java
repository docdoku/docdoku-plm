package com.docdoku.client.data;

public class AlreadyInitializedException extends RuntimeException {
  public AlreadyInitializedException (String pObjectName) {
		super("Error trying to initialize " + pObjectName + " which is it already");
  }
}
