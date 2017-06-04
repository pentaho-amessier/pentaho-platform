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
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.util.ActionUtil;
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
    KEY_MAP = new HashMap<String, String>();
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, ActionUtil.INVOKER_ACTIONCLASS );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER, ActionUtil.INVOKER_ACTIONUSER );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONID, ActionUtil.INVOKER_ACTIONID );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, ActionUtil.INVOKER_STREAMPROVIDER );
    KEY_MAP.put( QuartzScheduler.RESERVEDMAPKEY_RESTART_FLAG, ActionUtil.INVOKER_RESTART_FLAG );
  }

  /**
   * This method finds the "sibling" of the provided {@code key}, which can be found in the {@code KEY_MAP}, either
   * as a key within {@code KEY_MAP} or a value. Both keys, the original and the "sibling" are removed from the
   * provided {@code map}. This is to ensure that when a key to be removed is provided, we remove both copies of the
   * value from the {@code map}, one that corresponds to the key directly and any that may have been copied under a
   * different key, as defined within {@code KEY_MAP}.<br><br>
   * Example:<br>
   * <pre>
   * Given maps with the following content:
   *   map:
   *      "name" : "john".
   *      "my-name" : "john",
   *      "age" : "30"
   *      "my-age" : "30"
   *      "address" : "Boston"
   *
   *   KEY_MAP:
   *     "name" : "my-name"
   *     "age" : "my-age"
   *
   * Calling removeFromMap( map, "name" ) results in:
   *   map:
   *      "age" : "30"
   *      "my-age" : "30"
   *      "address" : "Boston"
   * Calling removeFromMap( map, "my-age" ) results in:
   *   map:
   *      "address" : "Boston"
   * </pre>
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
   * specific, so that the action invocation code can operate on these mapped non-scheduler specific keys, rather than
   * any keys defined within the scheduler. The corresponding entries that use the scheduler related keys are removed.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   */
  void prepareMap( final Map<String, Serializable> params ) {
    if ( params == null ) {
      return;
    }

    final Map<String, Serializable> replaced = new HashMap<>();

    for ( final Map.Entry<String, Serializable> ent : params.entrySet() ) {
      final String key = ent.getKey();
      final Serializable value = params.get( key );
      final String altKey = KEY_MAP.get( key );

      replaced.put( altKey == null ? key : altKey, value );
    }

    params.clear();
    params.putAll( replaced );
  }

  /**
   * Returns the {@link IBackgroundExecutionStreamProvider} from information stored within the {@code params} {@link
   * Map}. Returns {@code null} if information needed to build the stream provider is not present in the {@code map},
   * which is perfectly ok for some {@link IAction} types.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return an instance of {@link IBackgroundExecutionStreamProvider}
   */
  private IBackgroundExecutionStreamProvider getStreamProvider( final Map<String, Serializable> params ) {
    return ActionHelper.getStreamProvider( params );
  }

  /**
   * Invokes the provided {@link IAction} as the provided {@code actionUser}.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params     the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  @Override
  public IActionInvokeStatus runInBackground( final IAction actionBean, final String actionUser, final
  Map<String, Serializable> params ) throws Exception {
    prepareMap( params );
    // call getStreamProvider, in addition to creating the provider, this method also adds values to the map that
    // serialize the stream provider and make it possible to deserialize and recreate it for remote execution.
    getStreamProvider( params );
    return runInBackgroundImpl( actionBean, actionUser, params );
  }

  /**
   * Invokes the provided {@link IAction} locally as the provided {@code actionUser}.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params     the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  protected IActionInvokeStatus runInBackgroundImpl( final IAction actionBean, final String actionUser, final
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
    removeFromMap( params, ActionUtil.INVOKER_ACTIONCLASS );
    removeFromMap( params, ActionUtil.INVOKER_ACTIONID );
    removeFromMap( params, ActionUtil.INVOKER_ACTIONUSER );
    // build the stream provider
    final IBackgroundExecutionStreamProvider streamProvider = getStreamProvider( params );
    removeFromMap( params, ActionUtil.INVOKER_STREAMPROVIDER );
    removeFromMap( params, ActionUtil.INVOKER_UIPASSPARAM );

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
