package com.docdoku.client.data;

public class NotInitializedException extends RuntimeException {
  public NotInitializedException (String pObjectName) {
		super("Error trying to access " + pObjectName + " which is not yet initialized");
  }
}
