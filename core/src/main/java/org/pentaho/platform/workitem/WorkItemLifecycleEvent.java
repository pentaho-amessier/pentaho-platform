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
 * Copyright (c) 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.workitem;

import org.pentaho.platform.api.workitem.IWorkItem;
import org.pentaho.platform.api.workitem.WorkItemLifecyclePhase;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * The event dispatched when a {@link WorkItem} enters a new lifecycle phase.
 */
public class WorkItemLifecycleEvent {

  private IWorkItem workItem;
  private WorkItemLifecyclePhase lifecyclePhase;
  private String details;
  private Date sourceTimestamp;
  private String sourceHostName;
  private String sourceHostIp;

  /**
   * An event encapsulating the {@link IWorkItem} and a change to its {@link WorkItemLifecyclePhase}
   *
   * @param workItem        the {@link IWorkItem}
   * @param lifecyclePhase  the {@link WorkItemLifecyclePhase}
   * @param details         any details associated with the state change (example: error message, exception stack
   *                        trace)
   * @param sourceTimestamp the time the event was triggered by the caller; this parameter can e null and will be set to
   *                        the current {@link Date}, but can also be set explicitly, in cases where the original event
   *                        may have been generated on another host and propagated via http or some other mechanism.
   */
  public WorkItemLifecycleEvent( final IWorkItem workItem, final WorkItemLifecyclePhase lifecyclePhase,
                                 final String details, final Date sourceTimestamp ) {
    this.workItem = workItem;
    this.details = details;
    this.lifecyclePhase = lifecyclePhase;
    this.sourceTimestamp = sourceTimestamp;
    init();
  }

  private void init() {
    // Set to current date only if not already provided
    if ( sourceTimestamp == null ) {
      sourceTimestamp = new Date();
    }
    try {
      sourceHostName = InetAddress.getLocalHost().getCanonicalHostName();
      sourceHostIp = InetAddress.getLocalHost().getHostAddress();
    } catch ( final UnknownHostException uhe ) {
      // TODO
    }
  }

  public IWorkItem getWorkItem() {
    return workItem;
  }

  public WorkItemLifecyclePhase getLifecyclePhase() {
    return lifecyclePhase;
  }

  public String getDetails() {
    return details;
  }

  public Date getSourceTimestamp() {
    return sourceTimestamp;
  }

  public String getSourceHostName() {
    return sourceHostName;
  }

  public String getSourceHostIp() {
    return sourceHostIp;
  }

  public String toString() {
    final StringBuilder info = new StringBuilder();
    info.append( "workItem: {" ).append( workItem.toString() ).append( "}, " );
    info.append( "lifecyclePhase: " ).append( lifecyclePhase ).append( ", " );
    info.append( "details: " ).append( details ).append( ", " );
    info.append( "sourceTimestamp: " ).append( sourceTimestamp ).append( ", " );
    info.append( "sourceHostName: " ).append( sourceHostName ).append( ", " );
    info.append( "sourceHostIp: " ).append( sourceHostIp ).append( ", " );
    return info.toString();
  }
}
