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


import org.apache.commons.lang.time.StopWatch;
import org.pentaho.platform.api.action.IActionDetails;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.util.ActionUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ActionInvokerAuditor implements IActionInvoker {

  private final IActionInvoker actionInvoker;

  public ActionInvokerAuditor( IActionInvoker actionInvoker ) {
    this.actionInvoker = actionInvoker;
  }

  @Override
  public IActionInvokeStatus invokeAction( final IActionDetails actionDetails ) throws Exception {
    if ( actionDetails == null ) {
      // TODO
    }
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    // the prams list key change after invokeAction. Need to preserve
    Map<String, Serializable> auditParams = new HashMap<>( actionDetails.getParameters() );
    makeAuditRecord( 0, MessageTypes.INSTANCE_START, auditParams, actionDetails );
    try {
      return actionInvoker.invokeAction( actionDetails );
    } finally {
      makeAuditRecord( stopWatch.getTime() / 1000, MessageTypes.INSTANCE_END, auditParams, actionDetails );
    }
  }

  private void makeAuditRecord( final float time, final String messageType,
                                final Map<String, Serializable> auditParams,
                                final IActionDetails actionDetails ) {

    AuditHelper.audit( PentahoSessionHolder.getSession() != null ? PentahoSessionHolder.getSession().getId() : "",
      actionDetails.getUserName(),
      getValue( auditParams, ActionUtil.INVOKER_STREAMPROVIDER ),
      actionDetails.getActionClassName(),
      actionDetails.getActionId(),
      messageType,
      getValue( auditParams, ActionUtil.QUARTZ_LINEAGE_ID ),
      null,
      time,
      null );
  }

  private String getValue( Map<String, Serializable> actionParams, String key ) {
    return actionParams.get( key ) != null ? actionParams.get( key ).toString() : "";
  }

  @Override
  public boolean canInvoke( final IActionDetails actionDetails ) {
    return false; // not needed
  }
}
