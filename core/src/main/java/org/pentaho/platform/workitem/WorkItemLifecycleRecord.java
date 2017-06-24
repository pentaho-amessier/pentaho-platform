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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.workitem.util.WorkItemLifecycleUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * This class encapsulates all information pertaining to a "work item" at a specific point in its lifecycle.
 */
public class WorkItemLifecycleRecord {

  private static final Log logger = LogFactory.getLog( WorkItemLifecycleRecord.class );

  private String workItemUid;
  private String workItemDetails;
  private WorkItemLifecyclePhase workItemLifecyclePhase;
  private String lifecycleDetails;
  private Date sourceTimestamp;
  private String sourceHostName;
  private String sourceHostIp;
  private static String HOST_NAME;
  private static String HOST_IP;

  static {
    try {
      HOST_NAME = InetAddress.getLocalHost().getCanonicalHostName();
      HOST_IP = InetAddress.getLocalHost().getHostAddress();
    } catch ( final UnknownHostException uhe ) {
      logger.error( uhe.getLocalizedMessage() );
    }
  }

  /**
   * Creates the {@link WorkItemLifecycleRecord} with all the required parameters. The {@code sourceTimestmap} is
   * set to the current date.
   *
   * @param workItemUid     a {@link String} containing unique identifier for the {@link WorkItemLifecycleRecord}
   * @param workItemDetails a {@link String} containing details of the {@link WorkItemLifecycleRecord}
   */
  public WorkItemLifecycleRecord( final String workItemUid, final String workItemDetails ) {
    this( workItemUid, workItemDetails, new Date() );
  }

  /**
   * Creates the {@link WorkItemLifecycleRecord} with all the required parameters.
   *
   * @param workItemUid     a {@link String} containing unique identifier for the {@link WorkItemLifecycleRecord}
   * @param workItemDetails a {@link String} containing details of the {@link WorkItemLifecycleRecord}
   * @param sourceTimestamp a {@link Date} representing the time the lifecycle change occured.
   */

  public WorkItemLifecycleRecord( final String workItemUid, final String workItemDetails, final Date sourceTimestamp ) {
    this.workItemUid = workItemUid;
    this.workItemDetails = workItemDetails;
    this.sourceTimestamp = sourceTimestamp;

    // if the workItemUid is null, generate it
    if ( this.workItemUid == null ) {
      this.workItemUid = generateWorkItemId();
    }
    // Set sourceTimestamp to current date only if not already provided
    if ( this.sourceTimestamp == null ) {
      this.sourceTimestamp = new Date();
    }

    // set the default values for host name and ip, they can be changed directly if needed
    this.sourceHostName = HOST_NAME;
    this.sourceHostIp = HOST_IP;
  }

  public static String generateWorkItemId() {
    return String.format( ActionUtil.REQUEST_ID_FORMAT, UUID.randomUUID().toString() );
  }

  /**
   * Adds the {@code workItemUid} to the given {@link Map}.
   *
   * @param map the {@link Map} to which the {@code workItemUid} is being added
   */
  public void addUidToMap( final Map map ) {
    if ( map != null ) {
      map.put( ActionUtil.REQUEST_ID, getWorkItemUid() );
    }
  }

  /**
   * Looks up the {@code ActionUtil.REQUEST_ID} within the {@link Map}. If available, the value is returned,
   * otherwise a new uid is generated.
   *
   * @param map a {@link Map} that may contain the {@code ActionUtil.REQUEST_ID}
   * @return {@code ActionUtil.REQUEST_ID} from the {@link Map} or a new uid
   */
  public static String getUidFromMap( final Map map ) {
    String workItemUid = null;
    if ( map == null ) {
      workItemUid = generateWorkItemId();
    } else {
      workItemUid = (String) map.get( ActionUtil.REQUEST_ID );
      if ( workItemUid == null ) {
        workItemUid = generateWorkItemId();
      }
    }
    return workItemUid;
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
  public void setWorkItemUid( final String workItemUid ) {
    this.workItemUid = workItemUid;
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
  public void setWorkItemDetails( final String workItemDetails ) {
    this.workItemDetails = workItemDetails;
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
  public void setWorkItemLifecyclePhase( final WorkItemLifecyclePhase workItemLifecyclePhase ) {
    this.workItemLifecyclePhase = workItemLifecyclePhase;
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
  public void setLifecycleDetails( final String lifecycleDetails ) {
    this.lifecycleDetails = lifecycleDetails;
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
  public void setSourceTimestamp( final Date sourceTimestamp ) {
    this.sourceTimestamp = sourceTimestamp;
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
  public void setSourceHostName( final String sourceHostName ) {
    this.sourceHostName = sourceHostName;
  }

  /**
   * {@inheritDoc}
   */
  public String getSourceHostIp() {
    return sourceHostIp;
  }

  /**
   * {@inheritDoc}
   */
  public void setSourceHostIp( final String sourceHostIp ) {
    this.sourceHostIp = sourceHostIp;
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
