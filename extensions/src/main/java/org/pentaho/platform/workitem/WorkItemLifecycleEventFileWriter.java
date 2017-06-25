/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.workitem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.pentaho.platform.workitem.messages.Messages;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * This component listens for {@link WorkItemLifecycleEvent}s and writes the information stored within the event into
 * a log file.
 */
@Component
public class WorkItemLifecycleEventFileWriter {

  private static final Log log = LogFactory.getLog( WorkItemLifecycleEventFileWriter.class );

  private static String WORK_ITEM_LOG_FILE = "work-item-status";
  private static final Logger workItemLogger = Logger.getLogger( WorkItemLifecycleEventFileWriter.class );

  static {
    final PatternLayout layout = new PatternLayout();
    // TODO: make message configurable, use variable names of WorkItemLifecycleRecord and resolve using reflection
    // make sure to encode properly (escape the separator)
    layout.setConversionPattern( "%m%n" );

    final DailyRollingFileAppender fileAppender = new DailyRollingFileAppender();
    fileAppender.setFile( "../logs/work-item-status.log" );
    // TODO: why is this not working?
    //fileAppender.setFile( ".." + File.pathSeparator + "logs" + File.pathSeparator + WORK_ITEM_LOG_FILE + ".log" );
    fileAppender.setLayout( layout );
    fileAppender.activateOptions();
    fileAppender.setAppend( true );
    fileAppender.setDatePattern( "'.'yyyy-MM-dd" );

    // TODO: why are the logs written to std out? remove...
    // configures the root logger
    // TODO: make log configurable
    workItemLogger.setLevel( Level.INFO );
    workItemLogger.addAppender( fileAppender );
  }

  @EventListener
  @Async
  public void onWorkItemLifecycleEvent( final WorkItemLifecycleEvent workItemLifecycleEvent ) {
    if ( log.isDebugEnabled() ) {
      log.debug( String.format( "%s received a WorkItemLifecycleEvent:: %s", this.getClass().getName(), workItemLifecycleEvent
        .toString() ) );
    }

    if ( workItemLifecycleEvent == null ) {
      log.error( getMessageBundle().getErrorString( "ERROR_0001_MISSING_WORK_ITEM_LIFECYCLE" ) );
      return;
    }

    final String workItemUid = workItemLifecycleEvent.getWorkItemUid();
    final String workItemDetails = workItemLifecycleEvent.getWorkItemDetails();
    final WorkItemLifecyclePhase lifecyclePhase = workItemLifecycleEvent.getWorkItemLifecyclePhase();
    // TODO: null checks
    final String lifeCyclePhaseName = lifecyclePhase.getShortName();
    final String lifeCyclePhaseDesc = lifecyclePhase.getDescription();
    final String lifecycleDetails = workItemLifecycleEvent.getLifecycleDetails();
    final Date sourceTimestamp = workItemLifecycleEvent.getSourceTimestamp();
    final String sourceHostName = workItemLifecycleEvent.getSourceHostName();
    final String sourceHostIp = workItemLifecycleEvent.getSourceHostIp();

    // the current date may be off from the time the event was generated, let's track both
    final Date targetTimeStamp = new Date();

    // TODO: come up with a better/standard way to format the message content (cvs? json?...)
    workItemLogger.info( targetTimeStamp + "|" + workItemUid + "|" + workItemDetails + "|"
      +  lifeCyclePhaseName + "|" + lifecycleDetails + "|" + sourceTimestamp + "|"
      + sourceHostName + "|" + sourceHostIp );
  }

  protected Messages getMessageBundle() {
    return Messages.getInstance();
  }
}
