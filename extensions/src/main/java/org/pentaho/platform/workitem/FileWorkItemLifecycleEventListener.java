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
import org.pentaho.platform.api.workitem.IWorkItem;
import org.pentaho.platform.api.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.workitem.messages.Messages;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class FileWorkItemLifecycleEventListener {

  private static final Log log = LogFactory.getLog( FileWorkItemLifecycleEventListener.class );

  @EventListener
  @Async
  public void onWorkItemLifecycleEvent( final WorkItemLifecycleEvent event ) {
    if ( log.isDebugEnabled() ) {
      log.debug( String.format( "%s received a WorkItemLifecycleEvent:: %s", this.getClass().getName(), event
        .toString() ) );
    }

    final IWorkItem workItem = event.getWorkItem();
    if ( workItem == null ) {
      log.error( Messages.getInstance().getErrorString( "ERROR_0001_MISSING_WORK_ITEM" ) );
      return;
    }

    final String workItemUid = workItem.getUid();
    final String workItemDetails = workItem.getDetails();
    final WorkItemLifecyclePhase lifecyclePhase = event.getLifecyclePhase();
    final String details = event.getDetails();
    final Date sourceTimestamp = event.getSourceTimestamp();
    final String sourceHostName = event.getSourceHostName();
    final String sourceHostIp = event.getSourceHostIp();

    // the current date may be off from the time the event was generated, let's track both
    final Date targetTimeStamp = new Date();

  }
}



/*
TODO: remove - this is temp stuff...
public static void main(String[] args) {
        // creates pattern layout
        PatternLayout layout = new PatternLayout();
        String conversionPattern = "%-7p %d [%t] %c %x - %m%n";
        layout.setConversionPattern(conversionPattern);

        // creates console appender
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setLayout(layout);
        consoleAppender.activateOptions();

        // creates file appender
        FileAppender fileAppender = new FileAppender();
        fileAppender.setFile("applog3.txt");
        fileAppender.setLayout(layout);
        fileAppender.activateOptions();

        // configures the root logger
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);

        // creates a custom logger and log messages
        Logger logger = Logger.getLogger(ProgrammaticLog4jExample.class);
        logger.debug("this is a debug log message");
        logger.info("this is a information log message");
        logger.warn("this is a warning log message");
    }
 */
