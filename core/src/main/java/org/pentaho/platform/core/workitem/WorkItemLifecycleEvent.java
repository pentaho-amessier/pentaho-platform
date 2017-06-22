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

package org.pentaho.platform.core.workitem;

import org.springframework.context.ApplicationEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * The event dispatched when a {@link WorkItem} enters a new lifecycle phase.
 */
public class WorkItemLifecycleEvent extends ApplicationEvent {

  private WorkItem workItem;
  private WorkItem.LifecyclePhase lifecyclePhase;
  private String details;
  private Date sourceTimestamp;
  private String sourceHostName;
  private String sourceHostIp;


  /**
   * An event encapsulating the {@link WorkItem} and a change to its {@link WorkItem.LifecyclePhase}
   *
   * @param workItem       the {@link WorkItem}
   * @param lifecyclePhase the {@link WorkItem.LifecyclePhase}
   */
  public WorkItemLifecycleEvent( final WorkItem workItem, final WorkItem.LifecyclePhase lifecyclePhase ) {
    this( workItem, lifecyclePhase, null );
  }

  /**
   * An event encapsulating the {@link WorkItem} and a change to its {@link WorkItem.LifecyclePhase}
   *
   * @param workItem       the {@link WorkItem}
   * @param lifecyclePhase the {@link WorkItem.LifecyclePhase}
   * @param details        any details associated with the state change (example: error message, exception stack trace)
   */
  public WorkItemLifecycleEvent( final WorkItem workItem, final WorkItem.LifecyclePhase lifecyclePhase,
                                 final String details ) {
    super( workItem );
    this.workItem = workItem;
    this.lifecyclePhase = lifecyclePhase;
    this.details = details;
    init();
  }

  private void init() {
    sourceTimestamp = new Date();
    try {
      sourceHostName = InetAddress.getLocalHost().getCanonicalHostName();
      sourceHostIp = InetAddress.getLocalHost().getHostAddress();
    } catch ( final UnknownHostException uhe ) {
      // TODO
    }
  }

  public WorkItem getWorkItem() {
    return workItem;
  }

  public WorkItem.LifecyclePhase getLifecyclePhase() {
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
    info.append( "workItem=[" ).append( workItem.toString() ).append( "] / " );
    info.append( "lifecyclePhase=" ).append( lifecyclePhase ).append( " / " );
    info.append( "details=" ).append( details ).append( " / " );
    info.append( "sourceTimestamp=" ).append( sourceTimestamp ).append( " / " );
    info.append( "sourceHostName=" ).append( sourceHostName ).append( " / " );
    info.append( "sourceHostIp=" ).append( sourceHostIp ).append( " / " );
    return info.toString();
  }
}
