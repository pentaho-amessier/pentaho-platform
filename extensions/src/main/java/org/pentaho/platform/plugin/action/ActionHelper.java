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
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.util.Emailer;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.web.MimeHelper;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Helper methods related to the invocation of {@link IAction}s.
 */
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

  /**
   * Returns the {@link Class} that corresponds to the provides {@code actionClassName} and {@code beanId}.
   *
   * @param actionClassName the name of the class being resolved
   * @param beanId the beanId of the class being resolved
   *
   * @return the {@link Class} that corresponds to the provides {@code actionClassName} and {@code beanId}
   *
   * @throws PluginBeanException when the plugin required to resolve the bean class from the {@code beanId} cannot be
   * created
   * @throws ActionInvocationException when the required parameters are not provided
   */
  static Class<?> resolveClass( final String actionClassName, final String beanId  ) throws
    PluginBeanException, ActionInvocationException {

    Class<?> clazz = null;

    if ( StringUtils.isEmpty( beanId ) && StringUtils.isEmpty( actionClassName ) ) {
      throw new ActionInvocationException( Messages.getInstance().getRequiredParamMissing( INVOKER_ACTIONCLASS,
        INVOKER_ACTIONID ) );
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
    throw new ActionInvocationException( Messages.getInstance().getFailedToCreateAction( StringUtils.isEmpty(
      beanId ) ? actionClassName : beanId ) );
  }

  /**
   * Returns an instance of {@link IAction} created from the provided parameters.
   *
   * @param actionClassName the name of the class being resolved
   * @param actionId the is of the action which corresponds to some bean id
   *
   * @return {@link IAction} created from the provided parameters.
   * @throws ActionInvocationException when the {@link IAction} cannot be created for some reason
   */
  public static IAction createActionBean( final String actionClassName, final String actionId ) throws
    ActionInvocationException {
    Object actionBean;
    Class<?> actionClass = null;
    try {
      actionClass = resolveClass( actionClassName, actionId );
      actionBean = actionClass.newInstance();
    } catch ( Exception e ) {
      throw new ActionInvocationException( Messages.getInstance().getFailedToCreateAction( ( actionClass == null )
        ? "?" : actionClass.getName() ) );
    }

    if ( !( actionBean instanceof IAction ) ) {
      throw new ActionInvocationException( Messages.getInstance().getActionWrongType( actionClass.getName(), IAction
        .class.getName() ) );
    }
    return (IAction) actionBean;
  }

  /**
   * Gets the stream provider from the {@code INVOKER_STREAMPROVIDER,} or builds it from the input file and output
   * dir {@link Map} values. Returns {@code null} if information needed to build the stream provider is not present in
   * the {@code map}, which is perfectly ok for some {@link IAction} types.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   *
   * @return a {@link IBackgroundExecutionStreamProvider} represented in the {@code params} {@link Map}
   */
  public static IBackgroundExecutionStreamProvider getStreamProvider( final Map<String, Serializable> params ) {

    if ( params == null ) {
      logger.warn( Messages.getInstance().getMapNullCantReturnSp() );
      return null;
    }
    IBackgroundExecutionStreamProvider streamProvider = null;

    final Object objsp = params.get( INVOKER_STREAMPROVIDER );
    if ( objsp != null && IBackgroundExecutionStreamProvider.class.isAssignableFrom( objsp.getClass() ) ) {
      streamProvider = (IBackgroundExecutionStreamProvider) objsp;
      if ( streamProvider instanceof RepositoryFileStreamProvider ) {
        params.put( INVOKER_STREAMPROVIDER_INPUT_FILE, ( (RepositoryFileStreamProvider) streamProvider )
          .getInputFilePath() );
        params.put( INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN, ( (RepositoryFileStreamProvider) streamProvider )
          .getOutputFilePath() );
        params.put( INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME, ( (RepositoryFileStreamProvider) streamProvider )
          .autoCreateUniqueFilename() );
      }
    } else {
      final String inputFile = params.get( INVOKER_STREAMPROVIDER_INPUT_FILE ) == null ? null : params.get(
        INVOKER_STREAMPROVIDER_INPUT_FILE ).toString();
      final String outputFilePattern = params.get( INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ) == null ? null : params.get(
        INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ).toString();
      boolean hasInputFile = !StringUtils.isEmpty( inputFile );
      boolean hasOutputPattern = !StringUtils.isEmpty( outputFilePattern );
      if ( hasInputFile && hasOutputPattern ) {
        boolean autoCreateUniqueFilename = params.get( "autoCreateUniqueFilename" ) == null || params.get(
          "autoCreateUniqueFilename" ).toString().equalsIgnoreCase( "true" );
        streamProvider = new RepositoryFileStreamProvider( inputFile, outputFilePattern, autoCreateUniqueFilename );
        // put in the map for future lookup
        params.put( INVOKER_STREAMPROVIDER, streamProvider );
      } else {
        if ( logger.isWarnEnabled() ) {
          logger.warn( Messages.getInstance().getMissingParamsCantReturnSp( String.format( "%s, %s",
            INVOKER_STREAMPROVIDER_INPUT_FILE, INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ), params ) ); //$NON-NLS-1$
        }
      }
    }
    return streamProvider;
  }

  /**
   * Sends an email with the file representing the provided {@code filePath}  as an attachment. All information
   * needed to send the email (to, from, cc, bcc etc) is expected to be proviced in the {@code actionParams}
   * {@link Map}.
   *
   * @param actionParams a {@link Map} of parameters needed to send the email
   * @param params a {@link Map} of parameter used to invoke the action
   * @param filePath the path of the repository file that was generated when the action was invoked
   */
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

  /**
   * Converts the {@code map} into a JSON string
   *
   * @param map the {@link Map} being converted to a string.
   * @return a JSON {@link String} representation of the {@code map}
   */
  public static String mapToJson( final Map map ) {
    final JSONObject jsonFeed =  new JSONObject( map );
    final String jsonString = jsonFeed.toString();
    return jsonString;
  }

  /**
   * Returns an Object instance generated from the provided {@code jsonStr}
   *
   * @param jsonStr the JSON string being converted the an Object
   * @param clazz the {@link Class} of the object being instantiated
   * @param <T> the type of object being instantiated
   *
   * @return an Object instance generated from the provided {@code jsonStr}
   *
   * @throws IOException when the object cannot be instantiates from the provided {@code jsonStr} for some reason
   */
  public static <T> T jsonToObject( final String jsonStr, final Class<T> clazz ) throws IOException {
    final ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue( jsonStr, clazz );
  }

  /**
   * Returns a string identifier for this action. This is either the {@code actionBean}s class name, the {@code
   * actionClassName} or the {@code actionId}, whichever is available, in that order.
   *
   * @param actionBean the {@link IAction} bean
   * @param actionClassName the full class name
   * @param actionId the action id
   *
   * @return a {@code String} representation of the action identifier.
   */
  public static String getActionIdentifier( final IAction actionBean, final String actionClassName, final String
    actionId ) {
    if ( actionBean != null ) {
      return actionBean.getClass().getName();
    } else if ( !StringUtil.isEmpty( actionClassName ) ) {
      return actionClassName;
    } else if ( !StringUtil.isEmpty( actionId ) ) {
      return actionId;
    }
    return "?"; //$NON-NLS-1$
  }
}
