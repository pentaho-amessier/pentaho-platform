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

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.text.SimpleTemplateEngine;
import groovy.text.TemplateEngine;
import org.apache.commons.collections4.CollectionUtils;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This component listens for {@link WorkItemLifecycleEvent}s and writes the information stored within the event into
 * a log file.
 */
@Component
public class WorkItemLifecycleEventFileWriter {

  private static final Log log = LogFactory.getLog( WorkItemLifecycleEventFileWriter.class );

  private static String WORK_ITEM_LOG_FILE = "work-item-status";

  private static final String DEFAULT_FIELD_DELIMITER = "|";

  private static List<String> DEFAULT_MESSAGE_FIELDS = new ArrayList<String>();

  static {
    DEFAULT_MESSAGE_FIELDS.add( "targetTimestamp" );
    DEFAULT_MESSAGE_FIELDS.add( "workItemUid" );
    DEFAULT_MESSAGE_FIELDS.add( "workItemDetails" );
    DEFAULT_MESSAGE_FIELDS.add( "workItemLifecyclePhaseName" );
    DEFAULT_MESSAGE_FIELDS.add( "lifecycleDetails" );
    DEFAULT_MESSAGE_FIELDS.add( "sourceTimestamp" );
    DEFAULT_MESSAGE_FIELDS.add( "sourceHostName" );
    DEFAULT_MESSAGE_FIELDS.add( "sourceHostIp" );
  }

  // TODO: add environment variables that may be docker / mesos / framework specific

  private String fieldDelimiter = DEFAULT_FIELD_DELIMITER;
  private List<String> messageFields = DEFAULT_MESSAGE_FIELDS;

  public void setMessageFields( final List<String> messageFields ) {
    this.messageFields = messageFields;
  }

  public List<String> getMessageFields() {
    return CollectionUtils.isEmpty( this.messageFields ) ? DEFAULT_MESSAGE_FIELDS : this.messageFields;
  }

  public void setFieldDelimiter( final String fieldDelimiter ) {
    this.fieldDelimiter = fieldDelimiter;
  }

  public String getFieldDelimiter() {
    return this.fieldDelimiter == null ? DEFAULT_FIELD_DELIMITER : this.fieldDelimiter;
  }

  @EventListener
  @Async
  public void onWorkItemLifecycleEvent( final WorkItemLifecycleEvent workItemLifecycleEvent ) {

    if ( workItemLifecycleEvent == null ) {
      log.error( getMessageBundle().getErrorString( "ERROR_0001_MISSING_WORK_ITEM_LIFECYCLE" ) );
      return;
    }

    if ( log.isDebugEnabled() ) {
      log.debug( String.format( "%s received a WorkItemLifecycleEvent:: %s", this.getClass().getName(),
        workItemLifecycleEvent.toString() ) );
    }

    final String actualMessagePattern = getContent( getMessageFields(), getFieldDelimiter(), workItemLifecycleEvent );
    getLogger().info( actualMessagePattern );
  }

  private Logger getLogger() {
    final Logger workItemLogger = Logger.getLogger( WorkItemLifecycleEventFileWriter.class );

    final PatternLayout layout = new PatternLayout();
    layout.setConversionPattern( "%m%n" );

    final DailyRollingFileAppender fileAppender = new DailyRollingFileAppender();
    fileAppender.setFile( ".." + File.separator + "logs" + File.separator + WORK_ITEM_LOG_FILE + ".log" );
    fileAppender.setLayout( layout );
    fileAppender.activateOptions();
    fileAppender.setAppend( true );
    fileAppender.setDatePattern( "'.'yyyy-MM-dd" );

    // TODO: why are the logs written to std out? remove...

    // The log level is arbitrary, it just needs to match the level used to log the work item lifecycle event
    workItemLogger.setLevel( Level.INFO );
    workItemLogger.addAppender( fileAppender );
    return workItemLogger;
  }

  protected static String getContent( final List<String> messageFields, final String fieldDelimiter,
                                      final Object... data ) {
    final StringBuilder content = new StringBuilder();
    try {
      final ObjectMapper mapper = new ObjectMapper();
      final TemplateEngine engine = new SimpleTemplateEngine();
      final Map dataMap = new HashMap();
      for ( final Object currentData : data ) {
        dataMap.putAll( mapper.convertValue( currentData, Map.class ) );
      }
      String actualDelimiter = "";
      for ( final String fieldName : messageFields ) {
        String fieldContent = getContent( engine, dataMap, fieldName );
        if ( fieldContent == null ) {
          fieldContent = "?";
        }
        content.append( actualDelimiter ).append( fieldContent );
        actualDelimiter = fieldDelimiter;
      }
      return content.toString();
    } catch ( final Exception e ) {
      log.error( e.getLocalizedMessage() );
      return null;
    }
  }

  protected static String getContent( final TemplateEngine engine, final Map dataMap, final String fieldName ) {
    try {
      return engine.createTemplate( "${" + fieldName + "}" ).make( dataMap ).toString();
    } catch ( final Exception e ) {
      log.error( e.getLocalizedMessage() );
      return null;
    }
  }

  protected Messages getMessageBundle() {
    return Messages.getInstance();
  }
}
