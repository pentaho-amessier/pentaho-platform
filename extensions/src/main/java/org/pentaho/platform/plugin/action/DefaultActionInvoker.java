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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.action.IPostProcessingAction;
import org.pentaho.platform.api.action.IStreamingAction;
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository2.unified.ISourcesStreamEvents;
import org.pentaho.platform.api.repository2.unified.IStreamListener;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.core.output.FileContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.solution.ActionSequenceCompatibilityFormatter;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.scheduler2.quartz.SchedulerOutputPathResolver;
import org.pentaho.platform.util.Emailer;
import org.pentaho.platform.util.beans.ActionHarness;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.MimeHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A concrete implementation of the {@link IActionInvoker} interface.
 */
// TODO: this is a singletop bean, make it thread safe
public class DefaultActionInvoker implements IActionInvoker {
  private static final Log logger = LogFactory.getLog( DefaultActionInvoker.class );

  private String outputFilePath = null;
  private Object lock = new Object();

  private static final Map<String, String> KEY_MAP;
  static {
    KEY_MAP = new HashMap<String, String>( );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, ActionHelper.INVOKER_ACTIONCLASS );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER, ActionHelper.INVOKER_ACTIONUSER );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONID, ActionHelper.INVOKER_ACTIONID );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, ActionHelper.INVOKER_STREAMPROVIDER );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_RESTART_FLAG, ActionHelper.INVOKER_RESTART_FLAG );
  }
  private static final long RETRY_COUNT = 6;
  private static final long RETRY_SLEEP_AMOUNT = 10000;

  void prepareMap( final Map<String, Serializable> params ){
    final Map<String, Serializable> newParams = new HashMap<String, Serializable>( );
    final Iterator<String> mapKeys = params.keySet().iterator();
    while (mapKeys.hasNext()) {
      final String key = mapKeys.next();
      // get the alternate key from KEY_MAP
      final String alternateKey = KEY_MAP.get( key );
      if ( StringUtils.isEmpty( alternateKey )) {
        continue;
      }
      final Serializable value = params.get( key );
      newParams.put( alternateKey, value );
    }
    params.putAll( newParams );
    getStreamProvider( params );
  }

  @Override
  public IAction createActionBean( final String actionClassName, final String actionId ) throws Exception{
    return ActionHelper.createActionBean( actionClassName, actionId );
  }

  private IBackgroundExecutionStreamProvider streamProvider = null;
  /**
   * Gets the stream provider from the INVOKER_STREAMPROVIDER, or builds it from the input file and output dir
   * @param params
   * @return
   */
  private IBackgroundExecutionStreamProvider getStreamProvider( final Map<String, Serializable> params ) {

    if ( streamProvider == null ) {
      streamProvider = ActionHelper.getStreamProvider( params );
      return streamProvider;
    }
    return streamProvider;
  }

  /**
   * Invokes the {@link IAction} in the background.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   *
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  @Override
  public IActionInvokeStatus runInBackground( final IAction actionBean, final String actionUser, final Map<String,
    Serializable> params ) throws Exception {
    // prepare the map, this converts any map keys that are scheduler specific to generic map keys
    prepareMap( params );
    return runInBackgroundImpl( actionBean, actionUser, params );
  }

  /**
   * The concrete implementation of the "run in background" functionality. Implemented here to invoke the action
   * {@link IAction} locally.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   *
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  protected IActionInvokeStatus runInBackgroundImpl( final IAction actionBean, final String actionUser, final Map<String, Serializable> params ) throws Exception {
    return runInBackgroundLocally( actionBean, actionUser, params );
  }

  /**
   * Created the {@link IAction} bean based on information stored within the provided {@code params} map and runs it
   * locally
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   *
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  @Override
  public IActionInvokeStatus runInBackgroundLocally( final Map<String, Serializable> params ) throws
    Exception {
    if ( params == null ) {
      // TODO
    }
    final String actionId = (String) params.get( ActionHelper.INVOKER_ACTIONID );
    final String actionClassName = (String) params.get( ActionHelper.INVOKER_ACTIONCLASS );
    final String actionUser = (String) params.get( ActionHelper.INVOKER_ACTIONUSER );
    final IAction actionBean = createActionBean( actionClassName, actionId );
    return runInBackgroundLocally( actionBean, actionUser, params );
  }

  protected IActionInvokeStatus runInBackgroundLocally( final IAction actionBean, final String actionUser, final Map<String, Serializable> params ) throws
    Exception {
    try {
      return runInBackgroundLocallyImpl( actionBean, actionUser, params );

    } catch ( Throwable t ) {
      // ensure that the main thread isn't blocked on lock
      synchronized ( lock ) {
        lock.notifyAll();
      }

      // We should not distinguish between checked and unchecked exceptions here. All job execution failures
      // should result in a rethrow of a quartz exception
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
        "ActionInvoker.ERROR_0004_ACTION_FAILED", actionBean //$NON-NLS-1$
          .getClass().getName() ), t );
    }
  }


  protected IActionInvokeStatus runInBackgroundLocallyImpl( final IAction actionBean, final String actionUser, final
  Map<String, Serializable> params ) throws
    Exception {
    // TODO: handle nulls

    // set the locale, if not already set
    if ( params.get( LocaleHelper.USER_LOCALE_PARAM ) == null || org.pentaho.reporting.libraries.base.util
      .StringUtils.isEmpty( params.get( LocaleHelper
        .USER_LOCALE_PARAM ).toString() ) ) {
      params.put( LocaleHelper.USER_LOCALE_PARAM, LocaleHelper.getLocale() );
    }

    // remove the scheduling infrastructure properties
    params.remove( ActionHelper.INVOKER_ACTIONCLASS );
    params.remove( ActionHelper.INVOKER_ACTIONID );
    params.remove( ActionHelper.INVOKER_ACTIONUSER );
    // build the stream provider
    streamProvider = getStreamProvider( params );
    params.remove( ActionHelper.INVOKER_STREAMPROVIDER );
    params.remove( QuartzScheduler.RESERVEDMAPKEY_UIPASSPARAM );

    if ( logger.isDebugEnabled() ) {
      logger.debug( MessageFormat.format(
        "Scheduling system invoking action {0} as user {1} with params [ {2} ]", actionBean //$NON-NLS-1$
          .getClass().getName(), actionUser, QuartzScheduler.prettyPrintMap( params ) ) );
    }

    Callable<Boolean> actionBeanRunner = new Callable<Boolean>() {

      public Boolean call() throws Exception {
        final Object locale = params.get( LocaleHelper.USER_LOCALE_PARAM );
        if ( locale instanceof Locale ) {
          LocaleHelper.setLocaleOverride( (Locale) locale );
        } else {
          LocaleHelper.setLocaleOverride( new Locale( (String) locale ) );
        }
        // sync job params to the action bean
        ActionHarness actionHarness = new ActionHarness( actionBean );
        boolean updateJob = false;

        final Map<String, Object> actionParams = new HashMap<String, Object>();
        actionParams.putAll( params );
        if ( streamProvider != null ) {
          actionParams.put( "inputStream", streamProvider.getInputStream() );
        }
        actionHarness.setValues( actionParams, new ActionSequenceCompatibilityFormatter() );

        if ( actionBean instanceof IVarArgsAction ) {
          actionParams.remove( "inputStream" );
          actionParams.remove( "outputStream" );
          ( (IVarArgsAction) actionBean ).setVarArgs( actionParams );
        }

        boolean waitForFileCreated = false;
        OutputStream stream = null;

        if ( streamProvider != null ) {
          actionParams.remove( "inputStream" );
          if ( actionBean instanceof IStreamingAction ) {
            streamProvider.setStreamingAction( (IStreamingAction) actionBean );
          }

          // BISERVER-9414 - validate that output path still exist
          SchedulerOutputPathResolver resolver =
            new SchedulerOutputPathResolver( streamProvider.getOutputPath(), actionUser );
          String outputPath = resolver.resolveOutputFilePath();
          actionParams.put( "useJcr", Boolean.TRUE );
          actionParams.put( "jcrOutputPath", outputPath.substring( 0, outputPath.lastIndexOf( "/" ) ) );

          if ( !outputPath.equals( streamProvider.getOutputPath() ) ) {
            streamProvider.setOutputFilePath( outputPath ); // set fallback path
            updateJob = true; // job needs to be deleted and recreated with the new output path
          }

          stream = streamProvider.getOutputStream();
          if ( stream instanceof ISourcesStreamEvents ) {
            ( (ISourcesStreamEvents) stream ).addListener( new IStreamListener() {
              public void fileCreated( final String filePath ) {
                synchronized ( lock ) {
                  outputFilePath = filePath;
                  lock.notifyAll();
                }
              }
            } );
            waitForFileCreated = true;
          }
          actionParams.put( "outputStream", stream );
          // The lineage_id is only useful for the metadata and not needed at this level see PDI-10171
          actionParams.remove( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
          actionHarness.setValues( actionParams );
        }

        actionBean.execute();

        if ( stream != null ) {
          IOUtils.closeQuietly( stream );
        }

        if ( waitForFileCreated ) {
          synchronized ( lock ) {
            if ( outputFilePath == null ) {
              lock.wait();
            }
          }
          sendEmail( actionParams, params, outputFilePath );
        }
        if ( actionBean instanceof IPostProcessingAction ) {
          closeContentOutputStreams( (IPostProcessingAction) actionBean );
          markContentAsGenerated( (IPostProcessingAction) actionBean );
        }
        return updateJob;
      }

      private void closeContentOutputStreams( IPostProcessingAction actionBean ) {
        for ( IContentItem contentItem : actionBean.getActionOutputContents() ) {
          contentItem.closeOutputStream();
        }
      }

      private void markContentAsGenerated( IPostProcessingAction actionBean ) {
        IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );
        String lineageId = (String) params.get( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
        for ( IContentItem contentItem : actionBean.getActionOutputContents() ) {
          RepositoryFile sourceFile = getRepositoryFileSafe( repo, contentItem.getPath() );
          // add metadata if we have access and we have file
          if ( sourceFile != null ) {
            Map<String, Serializable> metadata = repo.getFileMetadata( sourceFile.getId() );
            metadata.put( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID, lineageId );
            repo.setFileMetadata( sourceFile.getId(), metadata );
          } else {
            String fileName = getFSFileNameSafe( contentItem );
            logger.warn( Messages.getInstance().getString( "ActionInvoker.WARN_0001_SKIP_REMOVING_OUTPUT_FILE", fileName ) );
          }
        }
      }

      private RepositoryFile getRepositoryFileSafe( IUnifiedRepository repo, String path ) {
        try {
          return repo.getFile( path );
        } catch ( Exception e ) {
          logger.debug( MessageFormat.format( "Cannot get repository file \"{0}\": {1}", path, e.getMessage() ), e );
          return null;
        }
      }
      private String getFSFileNameSafe( IContentItem contentItem ) {
        if ( contentItem instanceof FileContentItem ) {
          return ( (FileContentItem) contentItem ).getFile().getName();
        }
        return null;
      }
    };

    final ActionInvokeStatus status = new ActionInvokeStatus();

    boolean requiresUpdate = false;
    if ( ( actionUser == null ) || ( actionUser.equals( "system session" ) ) ) { //$NON-NLS-1$
      // For now, don't try to run quartz jobs as authenticated if the user
      // that created the job is a system user. See PPP-2350
      requiresUpdate = SecurityHelper.getInstance().runAsAnonymous( actionBeanRunner );
    } else {
      try {
        requiresUpdate = SecurityHelper.getInstance().runAsUser( actionUser, actionBeanRunner );
      } catch ( Throwable t ) { // TODO: retry only when run as user, not when anonymous?
        status.setThrowable( t );
      }
    }
    status.setRequiresUpdate( requiresUpdate );

    return status;
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

  private static String objectToJson ( final Object obj ) {
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
