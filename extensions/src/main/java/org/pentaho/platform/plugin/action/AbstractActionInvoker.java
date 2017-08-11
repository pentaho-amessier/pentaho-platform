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
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionDetails;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;
import org.pentaho.platform.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.workitem.WorkItemLifecyclePublisher;

import java.io.Serializable;
import java.util.Map;

/**
 * A base implementation of {@link IActionInvoker}.
 */
public abstract class AbstractActionInvoker implements IActionInvoker {

  private static final Log logger = LogFactory.getLog( AbstractActionInvoker.class );

  /**
   * Gets the stream provider from the {@code INVOKER_STREAMPROVIDER,} or builds it from the input file and output
   * dir {@link Map} values. Returns {@code null} if information needed to build the stream provider is not present in
   * the {@code map}, which is perfectly ok for some {@link org.pentaho.platform.api.action.IAction} types.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link org.pentaho.platform.api.action.IAction}
   * @return a {@link IBackgroundExecutionStreamProvider} represented in the {@code params} {@link Map}
   */
  protected IBackgroundExecutionStreamProvider getStreamProvider( final Map<String, Serializable> params ) {

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
        boolean autoCreateUniqueFilename = params.get( ActionUtil.INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME ) == null
          || params.get( ActionUtil.INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME ).toString().equalsIgnoreCase( "true" );
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
   * Invokes the provided {@link IAction} as the provided {@code actionUser}.
   *
   * @param actionDetails The {@link IActionDetails} representing the {@link IAction} to be invoked
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  @Override
  public final IActionInvokeStatus invokeAction( final IActionDetails actionDetails ) throws Exception {
    validate( actionDetails );
    ActionUtil.prepareMap( actionDetails.getParameters() );
    // call getStreamProvider, in addition to creating the provider, this method also adds values to the map that
    // serialize the stream provider and make it possible to deserialize and recreate it for remote execution.
    getStreamProvider( actionDetails.getParameters() );
    return invokeActionImpl( actionDetails );
  }



  /**
   * Validates that the conditions required for the {@link IAction} to be invoked are true, throwing an
   * {@link ActionInvocationException}, if the conditions are not met.
   *
   * @param actionDetails The {@link IActionDetails} representing the {@link IAction} to be invoked
   * @throws ActionInvocationException when conditions needed to invoke the {@link IAction} are not met
   */
  public void validate( final IActionDetails actionDetails ) throws ActionInvocationException {

    final String workItemUid = ActionUtil.extractUid( actionDetails );

    if ( actionDetails == null ) {
      final String failureMessage = Messages.getInstance().getCantInvokeNullAction();
      WorkItemLifecyclePublisher.publish( workItemUid, null, WorkItemLifecyclePhase.FAILED, failureMessage );
      throw new ActionInvocationException( failureMessage );
    }

    if ( actionDetails.getAction() == null || actionDetails.getParameters() == null ) {
      final String failureMessage = Messages.getInstance().getCantInvokeNullAction();
      WorkItemLifecyclePublisher.publish( workItemUid, actionDetails.getParameters(), WorkItemLifecyclePhase.FAILED,
        failureMessage );
      throw new ActionInvocationException( failureMessage );
    }

    if ( !canInvoke( actionDetails ) ) {
      final String failureMessage = Messages.getInstance().getCantInvokeAction();
      WorkItemLifecyclePublisher.publish( workItemUid, actionDetails.getParameters(), WorkItemLifecyclePhase.FAILED,
        failureMessage );
      throw new ActionInvocationException( failureMessage );
    }
  }

  /**
   * Invokes the provided {@link IAction} locally as the provided {@code actionUser}.
   *
   * @param actionDetails The {@link IActionDetails} representing the {@link IAction} to be invoked
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  abstract protected IActionInvokeStatus invokeActionImpl( final IActionDetails actionDetails ) throws Exception;

  @Override
  public boolean canInvoke( final IActionDetails actionDetails ) {
    return true; // supports all
  }
}
