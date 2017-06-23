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

package org.pentaho.platform.api.workitem;

import java.util.Date;

public interface IWorkItemLifecycleRecord {

  /**
   * Returns the unique identifier for the work item.
   *
   * @return the {@link String} unique identifier for the work item
   */
  String getWorkItemUid();

  /**
   * Sets the unique identifier for the work item.
   *
   * @param workItemUid the {@link String} unique identifier for the work item
   */
  void setWorkItemUid( final String workItemUid );

  /**
   * Returns the details associated with the work item, if any.
   *
   * @return a {@link String} containing details associated with the work item, if any
   */
  String getWorkItemDetails();

  /**
   * Sets the details associated with the work item, if any
   *
   * @param workItemDetails the {@link String} containing details associated with the work item, if any
   */
  void setWorkItemDetails( final String workItemDetails );

  /**
   * Returns the {@link WorkItemLifecyclePhase} representing the work item's current lifecycle phase.
   *
   * @return the {@link WorkItemLifecyclePhase} representing the work item's current lifecycle phase
   */
  WorkItemLifecyclePhase getWorkItemLifecyclePhase();

  /**
   * Sets the {@link WorkItemLifecyclePhase} representing the work item's current lifecycle phase
   *
   * @param workItemLifecyclePhase the {@link WorkItemLifecyclePhase} representing the work item's current lifecycle
   *                               phase
   */
  void setWorkItemLifecyclePhase( final WorkItemLifecyclePhase workItemLifecyclePhase );

  /**
   * Returns the details associated with the work item lifecycle change (example: error message, exception stack trace);
   * this field can be null
   *
   * @return a {@link String} containing details associated with the work item lifecycle change or null
   */
  String getLifecycleDetails();

  /**
   * Sets the details associated with the work item lifecycle change (example: error message, exception stack trace);
   * this field can be null
   *
   * @param lifecycleDetails a {@link String} containing details associated with the work item lifecycle change or null
   */
  void setLifecycleDetails( final String lifecycleDetails );

  /**
   * Returns the time the lifecycle change was triggered by the caller.
   *
   * @return a {@link Date} representing the time the lifecycle change was triggered by the caller
   */
  Date getSourceTimestamp();

  /**
   * Sets the time the lifecycle change was triggered by the caller.
   *
   * @param sourceTimestamp the {@link Date} representing the time the lifecycle change was triggered by the caller
   */
  void setSourceTimestamp( final Date sourceTimestamp );

  /**
   * Returns the host name of the host where the lifecycle change occurred.
   *
   * @return a {@link String} host name of the host where the lifecycle change occurred
   */
  String getSourceHostName();

  /**
   * Sets the host name of the host where the lifecycle change occurred.
   *
   * @param sourceHostName the {@link String} host name of the host where the lifecycle change occurred
   */
  void setSourceHostName( final String sourceHostName );

  /**
   * Returns the IP of the host where the lifecycle change occurred.
   *
   * @return a {@link String} IP of the host where the lifecycle change occurred
   */
  String getSourceHostIp();

  /**
   * Sets the IP of the host where the lifecycle change occurred.
   *
   * @param sourceHostIp the {@link String} IP of the host where the lifecycle change occurred
   */
  void setSourceHostIp( final String sourceHostIp );
}
