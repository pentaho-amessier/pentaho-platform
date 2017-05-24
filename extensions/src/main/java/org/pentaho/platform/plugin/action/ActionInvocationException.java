package org.pentaho.platform.plugin.action;

public class ActionInvocationException extends Exception {

  public ActionInvocationException( String msg ) {
    super( msg );
    //logger.error( msg );
  }

  public ActionInvocationException( String msg, Throwable t ) {
    super( msg, t );
    //logger.error( msg, t );
  }
}
