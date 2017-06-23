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
   * The unique identifier for the work item.
   *
   * @return the {@link String} work item identifier
   */
  String getWorkItemUid();

  /**
   * Returns details associated with the work item, if any.
   *
   * @return the {@link String} containing details associated with the work item, if any
   */
  String getWorkItemDetails();

  /**
   * Returns the {@link WorkItemLifecyclePhase} representing the work item's current lifecycle phase.
   *
   * @return the {@link WorkItemLifecyclePhase} representing the work item's current lifecycle phase
   */
  WorkItemLifecyclePhase getWorkItemLifecyclePhase();

  /**
   * Returns details associated with the work item lifecycle change (example: error message, exception stack trace);
   * this field can be null
   *
   * @return details associated with the work item lifecycle change or null
   */
  String getLifecycleDetails();

  /**
   * Returns the time the lifecycle change was triggered by the caller.
   *
   * @return the time the lifecycle change was triggered by the caller
   */
  Date getSourceTimestamp();

  /**
   * Returns the host name of the host where the lifecycle change occurred.
   *
   * @return the host name of the host where the lifecycle change occurred
   */
  String getSourceHostName();

  /**
   * Returns the IP of the host where the lifecycle change occurred.
   *
   * @return the IP of the host where the lifecycle change occurred
   */
  String getSourceHostIp();
}
