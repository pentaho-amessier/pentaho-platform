package org.pentaho.platform.workitem;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.pentaho.platform.api.action.IRemoteActionInvoker;
import org.pentaho.platform.api.action.IWorkerNodeConfig;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.ActionUtil;

public class WorkerNodeLoggingFilter extends Filter {

  public int decide( final LoggingEvent event ) {
    // if wn-mode is disabled, and the event is determined to be wn-related, do not log it
    return !isWnModeEnabled() && isWnRelated( event ) ? DENY : ACCEPT;
  }


  public boolean isWnRelated( final LoggingEvent event ) {
    // return true if the event is worker node related - do this based on a combination of package names, class
    // names, message content, MDC values???
    if ( event == null ) {
      return false;
    }

    return hasRelatedMessage( event ) || hasRelatedName( event );
  }

  private boolean hasRelatedName( final LoggingEvent event ) {
    if ( event == null || event.getLoggerName() == null ) {
      return false;
    }
   // TODO: IndexOutOfBoundsException
    final String property =  event.getLoggerName().toString();
    return property.substring( 0, property.lastIndexOf( '.' ) ).equals( "org.pentaho.platform.plugin.action" )
      || property.substring( 0, property.lastIndexOf( '.' ) ).equals( "com.pentaho.platform.plugin.action" )
      || property.contains( ".workitem.");
  }


  private boolean hasRelatedMessage( final LoggingEvent event ) {
    if ( event == null || event.getMessage() == null ) {
      return false;
    }

    final String property =  event.getMessage().toString();
    return property.contains( ActionUtil.WORK_ITEM_UID )
      || property.toLowerCase().contains( ".workitem.")
      || property.toLowerCase().contains( "work item" )
      || property.toLowerCase().contains( "worker node" );
  }

  public boolean isWnModeEnabled() {
    final IWorkerNodeConfig workerNodeConfig = PentahoSystem.get( IWorkerNodeConfig.class, IWorkerNodeConfig.BEAN_ID,
      PentahoSessionHolder.getSession() );
    final IRemoteActionInvoker remoteInvoker = PentahoSystem.get( IRemoteActionInvoker.class );
    return workerNodeConfig != null && workerNodeConfig.isWnModeEnabled();
  }
}