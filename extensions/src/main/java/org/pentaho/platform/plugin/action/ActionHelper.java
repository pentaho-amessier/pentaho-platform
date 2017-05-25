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
 * Copyright (c) 2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.util.Emailer;
import org.pentaho.platform.util.web.MimeHelper;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class ActionHelper {

  private static final Log logger = LogFactory.getLog( ActionHelper.class );

  public static final String INVOKER_ACTIONCLASS = "actionClass"; //$NON-NLS-1$

  public static final String INVOKER_ACTIONUSER = "actionUser"; //$NON-NLS-1$

  public static final String INVOKER_ACTIONID = "actionId"; //$NON-NLS-1$

  public static final String INVOKER_STREAMPROVIDER = "streamProvider"; //$NON-NLS-1$
  public static final String INVOKER_STREAMPROVIDER_INPUT_FILE = "inputFile"; //$NON-NLS-
  public static final String INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN = "outputFilePattern"; //$
  public static final String INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME = "uniqueFileName"; //$NON-NLS-1$
  public static final String INVOKER_UIPASSPARAM = QuartzScheduler.RESERVEDMAPKEY_UIPASSPARAM;

  public static final String INVOKER_RESTART_FLAG = "restart"; //$NON-NLS-1$

  private static final long RETRY_COUNT = 6;
  private static final long RETRY_SLEEP_AMOUNT = 10000;

  static Class<?> resolveClass( final String actionClassName, final String beanId  ) throws
    PluginBeanException, IllegalArgumentException {

    Class<?> clazz = null;

    if ( StringUtils.isEmpty( beanId ) && StringUtils.isEmpty( actionClassName ) ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
        "ActionInvoker.ERROR_0001_REQUIRED_PARAM_MISSING", //$NON-NLS-1$
        INVOKER_ACTIONCLASS, INVOKER_ACTIONID ) );
    }

    for ( int i = 0; i < RETRY_COUNT; i++ ) {
      try {
        if ( !StringUtils.isEmpty( beanId ) ) {
          IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
          clazz = pluginManager.loadClass( beanId );
          return clazz;
        } else if ( !StringUtils.isEmpty( actionClassName ) ) {
          clazz = Class.forName( actionClassName );
          return clazz;
        }
      } catch ( Throwable t ) {
        try {
          Thread.sleep( RETRY_SLEEP_AMOUNT );
        } catch ( InterruptedException ie ) {
          logger.info( ie.getMessage(), ie );
        }
      }
    }

    // we have failed to locate the class for the actionClass
    // and we're giving up waiting for it to become available/registered
    // which can typically happen at system startup
    throw new IllegalArgumentException( Messages.getInstance().getErrorString(
      "ActionInvoker.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
      StringUtils.isEmpty( beanId ) ? actionClassName : beanId ) );
  }

  public static IAction createActionBean( final String actionClassName, final String actionId ) throws ActionInvocationException{
    Object actionBean;
    Class<?> actionClass = null;
    try {
      actionClass = resolveClass( actionClassName, actionId );
      actionBean = actionClass.newInstance();
    } catch ( Exception e ) {
      throw new ActionInvocationException( Messages.getInstance().getErrorString(
        "ActionInvoker.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
        ( actionClass == null ) ? "unknown" : actionClass.getName() ), e ); //$NON-NLS-1$
    }

    if ( !( actionBean instanceof IAction ) ) {
      throw new ActionInvocationException( Messages.getInstance().getErrorString(
        "ActionInvoker.ERROR_0003_ACTION_WRONG_TYPE", actionClass.getName(), //$NON-NLS-1$
        IAction.class.getName() ) );
    }
    return (IAction)actionBean;
  }

  /**
   * Gets the stream provider from the INVOKER_STREAMPROVIDER, or builds it from the input file and output dir
   * @param params
   * @return
   */
  public static IBackgroundExecutionStreamProvider getStreamProvider( final Map<String, Serializable> params ) {

    IBackgroundExecutionStreamProvider streamProvider = null;

    final Object objsp = params.get( INVOKER_STREAMPROVIDER );
    if ( objsp != null && IBackgroundExecutionStreamProvider.class.isAssignableFrom( objsp.getClass() ) ) {
      streamProvider = (IBackgroundExecutionStreamProvider) objsp;
      // TODO: fetch input and output params and put in map
      if (streamProvider instanceof RepositoryFileStreamProvider) {
        params.put( INVOKER_STREAMPROVIDER_INPUT_FILE, ( (RepositoryFileStreamProvider) streamProvider ).inputFilePath );
        params.put( INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN, ( (RepositoryFileStreamProvider) streamProvider )
          .getOutputFilePath()  );
        params.put( INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME, ( (RepositoryFileStreamProvider) streamProvider )
          .autoCreateUniqueFilename() );
      }
    } else {
      final String inputFile = params.get( INVOKER_STREAMPROVIDER_INPUT_FILE ) == null ? null : params.get(
        INVOKER_STREAMPROVIDER_INPUT_FILE ).toString();
      final String outputFilePattern = params.get( INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ) == null ? null : params.get(
        INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ).toString();
      // TODO: check for nulls
      boolean hasInputFile = !StringUtils.isEmpty( inputFile );
      if ( hasInputFile ) {
        boolean autoCreateUniqueFilename = params.get( "autoCreateUniqueFilename" ) == null || params.get(
          "autoCreateUniqueFilename" ).toString().equalsIgnoreCase( "true" );
        streamProvider = new RepositoryFileStreamProvider( inputFile, outputFilePattern, autoCreateUniqueFilename );
        // put in the map for future lookup
        params.put( INVOKER_STREAMPROVIDER, streamProvider );
      }
    }

    return streamProvider;
  }

  public static void sendEmail( Map<String, Object> actionParams, Map<String, Serializable> params, String filePath ) {
    try {
      IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );
      RepositoryFile sourceFile = repo.getFile( filePath );
      // add metadata
      Map<String, Serializable> metadata = repo.getFileMetadata( sourceFile.getId() );
      String lineageId = (String) params.get( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
      metadata.put( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID, lineageId );
      repo.setFileMetadata( sourceFile.getId(), metadata );
      // send email
      SimpleRepositoryFileData data = repo.getDataForRead( sourceFile.getId(), SimpleRepositoryFileData.class );
      // if email is setup and we have tos, then do it
      Emailer emailer = new Emailer();
      if ( !emailer.setup() ) {
        // email not configured
        return;
      }
      String to = (String) actionParams.get( "_SCH_EMAIL_TO" );
      String cc = (String) actionParams.get( "_SCH_EMAIL_CC" );
      String bcc = (String) actionParams.get( "_SCH_EMAIL_BCC" );
      if ( ( to == null || "".equals( to ) ) && ( cc == null || "".equals( cc ) )
        && ( bcc == null || "".equals( bcc ) ) ) {
        // no destination
        return;
      }
      emailer.setTo( to );
      emailer.setCc( cc );
      emailer.setBcc( bcc );
      emailer.setAttachment( data.getInputStream() );
      emailer.setAttachmentName( "attachment" );
      String attachmentName = (String) actionParams.get( "_SCH_EMAIL_ATTACHMENT_NAME" );
      if ( attachmentName != null && !"".equals( attachmentName ) ) {
        String path = filePath;
        if ( path.endsWith( ".*" ) ) {
          path = path.replace( ".*", "" );
        }
        String extension = MimeHelper.getExtension( data.getMimeType() );
        if ( extension == null ) {
          extension = ".bin";
        }
        if ( !attachmentName.endsWith( extension ) ) {
          emailer.setAttachmentName( attachmentName + extension );
        } else {
          emailer.setAttachmentName( attachmentName );
        }
      } else if ( data != null ) {
        String path = filePath;
        if ( path.endsWith( ".*" ) ) {
          path = path.replace( ".*", "" );
        }
        String extension = MimeHelper.getExtension( data.getMimeType() );
        if ( extension == null ) {
          extension = ".bin";
        }
        path = path.substring( path.lastIndexOf( "/" ) + 1, path.length() );
        if ( !path.endsWith( extension ) ) {
          emailer.setAttachmentName( path + extension );
        } else {
          emailer.setAttachmentName( path );
        }
      }
      if ( data == null || data.getMimeType() == null || "".equals( data.getMimeType() ) ) {
        emailer.setAttachmentMimeType( "binary/octet-stream" );
      } else {
        emailer.setAttachmentMimeType( data.getMimeType() );
      }
      String subject = (String) actionParams.get( "_SCH_EMAIL_SUBJECT" );
      if ( subject != null && !"".equals( subject ) ) {
        emailer.setSubject( subject );
      } else {
        emailer.setSubject( "Pentaho Scheduler: " + emailer.getAttachmentName() );
      }
      String message = (String) actionParams.get( "_SCH_EMAIL_MESSAGE" );
      if ( subject != null && !"".equals( subject ) ) {
        emailer.setBody( message );
      }
      emailer.send();
    } catch ( Exception e ) {
      logger.warn( e.getMessage(), e );
    }
  }

  private static ObjectMapper getMapper () {
    final ObjectMapper mapper = new ObjectMapper();
    //mapper.configure( SerializationFeature.INDENT_OUTPUT,true);
    //mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    return mapper;
  }

  public static String objectToJson ( final Object obj ) {
    final ObjectMapper mapper = getMapper();
    //final String jsonString = mapper.writeValueAsString( obj );
    final JSONObject jsonFeed =  new JSONObject( obj );
    final String jsonString = jsonFeed.toString();
    return jsonString;
  }

  public static String mapToJson ( final Map map ) {
    final JSONObject jsonFeed =  new JSONObject( map );
    final String jsonString = jsonFeed.toString();
    return jsonString;
  }

  public static <T> T jsonToObject ( final String jsonStr, final Class<T> clazz) throws IOException {
    final ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue( jsonStr, clazz );
  }
}
