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

import org.pentaho.platform.api.workitem.IWorkItemLifecycleRecord;
import org.pentaho.platform.api.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.workitem.util.WorkItemLifecycleUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * This class encapsulates all information pertaining to a "work item" at a specific point in its lifecycle.
 */
public class WorkItemLifecycleRecord implements IWorkItemLifecycleRecord {

  private String workItemUid;
  private String workItemDetails;
  private WorkItemLifecyclePhase workItemLifecyclePhase;
  private String lifecycleDetails;
  private Date sourceTimestamp;
  private String sourceHostName;
  private String sourceHostIp;

  /**
   * Creates the {@link WorkItemLifecycleRecord} with all the required parameters.
   *
   * @param workItemUid     a {@link String} containing unique identifier for the {@link WorkItemLifecycleRecord}
   * @param workItemDetails a {@link String} containing details of the {@link WorkItemLifecycleRecord}
   */
  public WorkItemLifecycleRecord( final String workItemUid, final String workItemDetails, final
    WorkItemLifecyclePhase workItemLifecyclePhase, final String lifecycleDetails, final Date sourceTimestamp ) {
    this.workItemUid = workItemUid;
    this.workItemDetails = workItemDetails;
    this.workItemLifecyclePhase = workItemLifecyclePhase;
    this.lifecycleDetails = lifecycleDetails;
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

  /**
   * {@inheritDoc}
   */
  public String getWorkItemUid() {
    return workItemUid;
  }

  /**
   * {@inheritDoc}
   */
  public String getWorkItemDetails() {
    return workItemDetails;
  }

  /**
   * {@inheritDoc}
   */
  public WorkItemLifecyclePhase getWorkItemLifecyclePhase() {
    return workItemLifecyclePhase;
  }

  /**
   * {@inheritDoc}
   */
  public String getLifecycleDetails() {
    return lifecycleDetails;
  }

  /**
   * {@inheritDoc}
   */
  public Date getSourceTimestamp() {
    return sourceTimestamp;
  }

  /**
   * {@inheritDoc}
   */
  public String getSourceHostName() {
    return sourceHostName;
  }

  /**
   * {@inheritDoc}
   */
  public String getSourceHostIp() {
    return sourceHostIp;
  }

  public String toString() {
    final StringBuilder info = new StringBuilder();
    info.append( "workItemUid: " ).append( workItemUid ).append( ", " );
    info.append( "workItemDetails: " ).append( workItemDetails ).append( ", " );
    info.append( "workItemLifecyclePhase.name: " ).append( WorkItemLifecycleUtil.getLifecyclePhaseName(
      workItemLifecyclePhase ) ).append( ", " );
    info.append( "workItemLifecyclePhase.description: " ).append( WorkItemLifecycleUtil.getLifecyclePhaseDescription(
      workItemLifecyclePhase ) ).append( ", " );
    info.append( "lifecycleDetails: " ).append( lifecycleDetails ).append( ", " );
    info.append( "sourceTimestamp: " ).append( sourceTimestamp ).append( ", " );
    info.append( "sourceHostName: " ).append( sourceHostName ).append( ", " );
    info.append( "sourceHostIp: " ).append( sourceHostIp ).append( ", " );
    return info.toString();
  }
}
