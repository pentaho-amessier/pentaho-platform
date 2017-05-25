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

package org.pentaho.platform.api.action;

import java.io.Serializable;
import java.util.Map;

/**
 * The purpose of this interface is to provide functionality needed to invoke an {@link IAction} instance in a
 * generic fashion.
 */
public interface IActionInvoker {

  /**
   * Generates an instance of {@link IAction} given the {@code actionClassName} and {@code actionId}.
   *
   * @param actionClassName the full classname of the {@link IAction} - used only in the absence of {@code actionId},
   *   ignored otherwise.
   * @param actionId the bean id of the [@link IAction} being created - must be present if {@code actionClassName} is
   *    not provided.
   *
   * @return an instance of {@link IAction}
   *
   * @throws Exception when the {@link IAction} bean cannot be created for some reason
   */
  IAction createActionBean( final String actionClassName, final String actionId ) throws Exception;


  /**
   * Runs the action in background locally.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return
   * @throws Exception if the action cannot be run for some reason
   */
  IActionInvokeStatus runInBackgroundLocally( final Map<String, Serializable> params )
    throws Exception;

  // called by ActionAdapterQuartzJob

  /**
   * Invokes the {@link IAction} in the background.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   *
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   *
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  IActionInvokeStatus runInBackground( final IAction actionBean, final String actionUser, final Map<String,
    Serializable> params ) throws Exception;
}
