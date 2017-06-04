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

package org.pentaho.platform.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.Messages;

public class ActionUtil {

  private static final Log logger = LogFactory.getLog( ActionUtil.class );

  public static final String INVOKER_ACTIONCLASS = "actionClass"; //$NON-NLS-1$
  public static final String INVOKER_ACTIONUSER = "actionUser"; //$NON-NLS-1$
  public static final String INVOKER_ACTIONID = "actionId"; //$NON-NLS-1$
  public static final String INVOKER_STREAMPROVIDER = "streamProvider"; //$NON-NLS-1$
  public static final String INVOKER_STREAMPROVIDER_INPUT_FILE = "inputFile"; //$NON-NLS-
  public static final String INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN = "outputFilePattern"; //$
  public static final String INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME = "uniqueFileName"; //$NON-NLS-1$
  public static final String INVOKER_UIPASSPARAM = "uiPassParam"; //$NON-NLS-1$
  public static final String INVOKER_RESTART_FLAG = "restart"; //$NON-NLS-1$
  public static final String INVOKER_SESSION = "::session"; //$NON-NLS-1$

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
   * @throws Exception when the required parameters are not provided
   */
  static Class<?> resolveActionClass( final String actionClassName, final String beanId  ) throws
    PluginBeanException, ActionInvocationException {

    Class<?> clazz = null;

    if ( StringUtils.isEmpty( beanId ) && StringUtils.isEmpty( actionClassName ) ) {
      throw new ActionInvocationException( Messages.getInstance().getErrorString(
        "ActionUtil.ERROR_0001_REQUIRED_PARAM_MISSING", INVOKER_ACTIONCLASS, INVOKER_ACTIONID ) );
    }

    for ( int i = 0; i < RETRY_COUNT; i++ ) {
      try {
        if ( !StringUtils.isEmpty( beanId ) ) {
          IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
          clazz = pluginManager.loadClass( beanId );
          if ( clazz != null ) {
            return clazz;
          }
        }
        // we will execute this only if the beanId is not provided, or if the beanId cannot be resolved
        if ( !StringUtils.isEmpty( actionClassName ) ) {
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
    throw new ActionInvocationException( Messages.getInstance().getErrorString(
      "ActionUtil.ERROR_0002_FAILED_TO_CREATE_ACTION", StringUtils.isEmpty( beanId ) ? actionClassName : beanId ) );
  }

  /**
   * Returns an instance of {@link IAction} created from the provided parameters.
   *
   * @param actionClassName the name of the class being resolved
   * @param actionId the is of the action which corresponds to some bean id
   *
   * @return {@link IAction} created from the provided parameters.
   * @throws Exception when the {@link IAction} cannot be created for some reason
   */
  public static IAction createActionBean( final String actionClassName, final String actionId ) throws
    ActionInvocationException {
    Object actionBean = null;
    Class<?> actionClass = null;
    try {
      actionClass = resolveActionClass( actionClassName, actionId );
      actionBean = actionClass.newInstance();
    } catch ( final Exception e ) {
      throw new ActionInvocationException( Messages.getInstance().getErrorString(
        "ActionUtil.ERROR_0002_FAILED_TO_CREATE_ACTION", ( actionClass == null ) ? "?" : actionClass.getName() ) );
    }

    if ( !( actionBean instanceof IAction ) ) {
      throw new ActionInvocationException( Messages.getInstance().getErrorString(
        "ActionUtil.ERROR_0003_ACTION_WRONG_TYPE", actionClass.getName(), IAction.class.getName() ) );
    }
    return (IAction) actionBean;
  }
}
