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
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A concrete implementation of the {@link IActionInvoker} interface that invokes the {@link IAction} locally.
 */
public class DefaultActionInvoker implements IActionInvoker {

  private static final Log logger = LogFactory.getLog( DefaultActionInvoker.class );

  private static final Map<String, String> KEY_MAP;
  static {
    KEY_MAP = new HashMap<String, String>( );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, ActionHelper.INVOKER_ACTIONCLASS );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER, ActionHelper.INVOKER_ACTIONUSER );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONID, ActionHelper.INVOKER_ACTIONID );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, ActionHelper.INVOKER_STREAMPROVIDER );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_RESTART_FLAG, ActionHelper.INVOKER_RESTART_FLAG );
  }

  /**
   * Given a {@code key}, removes it from the map, as well as its corresponding key in the {@code KEY_MAP}. The {@code key} can
   * be  both a key or or a value in the {@code KEY_MAP}. This is to ensure that when a key to be removed is
   * provided, we remove both copies of the value from the map, one that corresponds to the key directly and any that
   * may have been copied under a different key.
   *
   * @param map a {@link Map} of values
   * @param key the key being removed from the map
   */
  static void removeFromMap( final Map<String, ?> map, final String key ) {

    if ( map == null ) {
      // nothing to do
      return;
    }
    // remove the item with this key from the map
    map.remove( key );
    // find this key in the KEY_MAP
    final String mappedKey = KEY_MAP.get( key );
    if ( mappedKey != null ) {
      map.remove( mappedKey );
    } else {
      // mapped key was not found - see if we have a value in the KEY_MAP that matches this key
      final Iterator<Map.Entry<String, String>> keyMapEntries = KEY_MAP.entrySet().iterator();
      while ( keyMapEntries.hasNext() ) {
        final Map.Entry<String, String> entry = keyMapEntries.next();
        if ( key.equals( entry.getValue() ) ) {
          map.remove( entry.getValue() );
          break;
        }
      }
    }
  }

  /**
   * Prepares the {@code params} {@link Map} for action invocation, adding appropriate keys that are not scheduler
   * specific, so that the action invocation code canoperate on these mapped non-scheduler specific keys, rather than
   * any keys defined within the scheduler.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   */
  void prepareMap( final Map<String, Serializable> params ) {
    if ( params == null ) {
      return;
    }
    final Map<String, Serializable> newParams = new HashMap<String, Serializable>( );
    final Iterator<String> mapKeys = params.keySet().iterator();
    while ( mapKeys.hasNext() ) {
      final String key = mapKeys.next();
      // get the alternate key from KEY_MAP
      final String alternateKey = KEY_MAP.get( key );
      if ( StringUtils.isEmpty( alternateKey ) ) {
        continue;
      }
      final Serializable value = params.get( key );
      newParams.put( alternateKey, value );
    }
    params.putAll( newParams );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IAction createActionBean( final String actionClassName, final String actionId ) throws Exception {
    return ActionHelper.createActionBean( actionClassName, actionId );
  }

  /**
   * Returns the {@link IBackgroundExecutionStreamProvider} from information stored within the {@code params} {@link
   * Map}. Returns {@code null} if information needed to build the stream provider is not present in the {@code map},
   * which is perfectly ok for some {@link IAction} types.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   *
   * @return an instance of {@link IBackgroundExecutionStreamProvider}
   */
  private IBackgroundExecutionStreamProvider getStreamProvider( final Map<String, Serializable> params ) {
    return ActionHelper.getStreamProvider( params );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IActionInvokeStatus runInBackground( final IAction actionBean, final String actionUser, final Map<String,
    Serializable> params ) throws Exception {
    prepareMap( params );
    // call getStreamProvider, in addition to creating the provider, this method also adds values to the map that
    // serialize the stream provider and make it possible to deserialize and recreate it for remote execution.
    getStreamProvider( params );
    return runInBackgroundImpl( actionBean, actionUser, params );
  }

  /**
   * The concrete implementation of the "run in background" functionality. Implemented here to invoke the
   * {@link IAction} locally.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   *
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   *
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  protected IActionInvokeStatus runInBackgroundImpl( final IAction actionBean, final String actionUser, final Map<String, Serializable> params ) throws Exception {
    return runInBackgroundLocally( actionBean, actionUser, params );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IActionInvokeStatus runInBackgroundLocally( final Map<String, Serializable> params ) throws
    Exception {
    if ( params == null ) {
      throw new ActionInvocationException( Messages.getInstance().getCantInvokeActionWithNullMap() );
    }
    final String actionId = (String) params.get( ActionHelper.INVOKER_ACTIONID );
    final String actionClassName = (String) params.get( ActionHelper.INVOKER_ACTIONCLASS );
    final String actionUser = (String) params.get( ActionHelper.INVOKER_ACTIONUSER );
    final IAction actionBean = createActionBean( actionClassName, actionId );
    return runInBackgroundLocally( actionBean, actionUser, params );
  }

  /**
   * Invokes the provided {@link IAction} locally as the provided {@code actionUser}.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   *
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   *
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  protected final IActionInvokeStatus runInBackgroundLocally( final IAction actionBean, final String actionUser, final
    Map<String, Serializable> params ) throws Exception {

    if ( actionBean == null || params == null ) {
      throw new ActionInvocationException( Messages.getInstance().getCantInvokeNullAction() );
    }

    if ( logger.isDebugEnabled() ) {
      logger.debug( Messages.getInstance().getRunningInBackgroundLocally( actionBean.getClass().getName(), params ) );
    }

    // set the locale, if not already set
    if ( params.get( LocaleHelper.USER_LOCALE_PARAM ) == null || StringUtils.isEmpty(
      params.get( LocaleHelper.USER_LOCALE_PARAM ).toString() ) ) {
      params.put( LocaleHelper.USER_LOCALE_PARAM, LocaleHelper.getLocale() );
    }

    // remove the scheduling infrastructure properties
    removeFromMap( params, ActionHelper.INVOKER_ACTIONCLASS  );
    removeFromMap( params, ActionHelper.INVOKER_ACTIONID  );
    removeFromMap( params, ActionHelper.INVOKER_ACTIONUSER  );
    // build the stream provider
    final IBackgroundExecutionStreamProvider streamProvider = getStreamProvider( params );
    removeFromMap( params, ActionHelper.INVOKER_STREAMPROVIDER  );
    removeFromMap( params, ActionHelper.INVOKER_UIPASSPARAM  );

    final ActionRunner actionBeanRunner = new ActionRunner( actionBean, actionUser, params, streamProvider );
    final ActionInvokeStatus status = new ActionInvokeStatus();

    boolean requiresUpdate = false;
    if ( ( StringUtil.isEmpty( actionUser ) ) || ( actionUser.equals( "system session" ) ) ) { //$NON-NLS-1$
      // For now, don't try to run quartz jobs as authenticated if the user
      // that created the job is a system user. See PPP-2350
      requiresUpdate = SecurityHelper.getInstance().runAsAnonymous( actionBeanRunner );
    } else {
      try {
        requiresUpdate = SecurityHelper.getInstance().runAsUser( actionUser, actionBeanRunner );
      } catch ( final Throwable t ) {
        status.setThrowable( t );
      }
    }
    status.setRequiresUpdate( requiresUpdate );

    return status;
  }
}
