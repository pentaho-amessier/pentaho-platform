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
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.Emailer;
import org.pentaho.platform.util.web.MimeHelper;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.Serializable;
import java.util.Map;

/**
 * Helper methods related to the invocation of {@link org.pentaho.platform.api.action.IAction}s.
 */
public class ActionHelper {

  private static final Log logger = LogFactory.getLog( ActionHelper.class );

  /**
   * Gets the stream provider from the {@code INVOKER_STREAMPROVIDER,} or builds it from the input file and output
   * dir {@link Map} values. Returns {@code null} if information needed to build the stream provider is not present in
   * the {@code map}, which is perfectly ok for some {@link org.pentaho.platform.api.action.IAction} types.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link org.pentaho.platform.api.action.IAction}
   * @return a {@link IBackgroundExecutionStreamProvider} represented in the {@code params} {@link Map}
   */
  public static IBackgroundExecutionStreamProvider getStreamProvider( final Map<String, Serializable> params ) {

    if ( params == null ) {
      logger.warn( Messages.getInstance().getMapNullCantReturnSp() );
      return null;
    }
    IBackgroundExecutionStreamProvider streamProvider = null;

    final Object objsp = params.get( ActionUtil.INVOKER_STREAMPROVIDER );
    if ( objsp != null && IBackgroundExecutionStreamProvider.class.isAssignableFrom( objsp.getClass() ) ) {
      streamProvider = (IBackgroundExecutionStreamProvider) objsp;
      if ( streamProvider instanceof RepositoryFileStreamProvider ) {
        params.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, ( (RepositoryFileStreamProvider) streamProvider )
          .getInputFilePath() );
        params.put( ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN, ( (RepositoryFileStreamProvider)
          streamProvider ).getOutputFilePath() );
        params.put( ActionUtil.INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME, ( (RepositoryFileStreamProvider)
          streamProvider ).autoCreateUniqueFilename() );
      }
    } else {
      final String inputFile = params.get( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE ) == null ? null : params.get(
        ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE ).toString();
      final String outputFilePattern = params.get( ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ) == null
        ? null : params.get( ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ).toString();
      boolean hasInputFile = !StringUtils.isEmpty( inputFile );
      boolean hasOutputPattern = !StringUtils.isEmpty( outputFilePattern );
      if ( hasInputFile && hasOutputPattern ) {
        boolean autoCreateUniqueFilename = params.get( "autoCreateUniqueFilename" ) == null || params.get(
          "autoCreateUniqueFilename" ).toString().equalsIgnoreCase( "true" );
        streamProvider = new RepositoryFileStreamProvider( inputFile, outputFilePattern, autoCreateUniqueFilename );
        // put in the map for future lookup
        params.put( ActionUtil.INVOKER_STREAMPROVIDER, streamProvider );
      } else {
        if ( logger.isWarnEnabled() ) {
          logger.warn( Messages.getInstance().getMissingParamsCantReturnSp( String.format( "%s, %s",
            ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN ),
            params ) ); //$NON-NLS-1$
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
   * @param params       a {@link Map} of parameter used to invoke the action
   * @param filePath     the path of the repository file that was generated when the action was invoked
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
}