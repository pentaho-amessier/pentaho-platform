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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionDetails;
import org.pentaho.platform.util.ActionUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * A simple POJO that encapsulates all information needed in order to execute an {@link IAction} via the generic
 * action invocation mechanism.
 */
public class ActionDetails implements IActionDetails {

  static final Log logger = LogFactory.getLog( ActionDetails.class );

  private String workItemUid;
  private IAction action;
  private String actionId;
  private String userName;
  private Map<String, Serializable> parameters;

  /**
   * @param workItemUid     the unique work item identifier
   * @param actionClassName the name of the class being resolved
   * @param actionId        the is of the action which corresponds to some bean id
   * @param userName        The user invoking the {@link IAction}
   * @param parameters      the {@link Map} or parameters needed to invoke the {@link IAction}
   */
  public ActionDetails( final String workItemUid,
                        final String actionClassName,
                        final String actionId,
                        final String userName,
                        final Map<String, Serializable> parameters ) {
    try {
      action = ActionUtil.createActionBean( actionClassName, actionId );
    } catch ( final ActionInvocationException exception ) {
      // TODO: externalize
      logger.error( String.format( "Action could not be instantiated from class '%s' and id  '%s", actionClassName,
        actionId ) );
    }
    this.workItemUid = workItemUid;
    this.actionId = actionId;
    this.userName = userName;
    this.parameters = parameters;
  }

  /**
   * @param workItemUid the unique work item identifier
   * @param action      he {@link IAction} being invoked
   * @param userName    The user invoking the {@link IAction}
   * @param parameters  the {@link Map} or parameters needed to invoke the {@link IAction}
   */
  public ActionDetails( final String workItemUid,
                        final IAction action,
                        final String userName,
                        final Map<String, Serializable> parameters ) {
    this.workItemUid = workItemUid;
    this.action = action;
    this.userName = userName;
    this.parameters = parameters;
  }

  public String getWorkItemUid() {
    return workItemUid;
  }

  public IAction getAction() {
    return action;
  }

  public String getActionId() {
    return actionId;
  }

  public String getUserName() {
    return userName;
  }

  public Map<String, Serializable> getParameters() {
    return parameters;
  }

  public String getActionClassName() {
    return action == null ? null : action.getClass().getCanonicalName();
  }

}
