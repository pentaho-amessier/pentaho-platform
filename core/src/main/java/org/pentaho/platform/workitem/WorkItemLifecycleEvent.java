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

/**
 * The event dispatched when a work item enters a new lifecycle phase.
 */
public class WorkItemLifecycleEvent {

  private IWorkItemLifecycleRecord workItemLifecycleRecord;

  /**
   * An event encapsulating the work item lifecycle change.
   *
   * @param workItemLifecycleRecord the {@link IWorkItemLifecycleRecord}
   */
  public WorkItemLifecycleEvent( final IWorkItemLifecycleRecord workItemLifecycleRecord ) {
    this.workItemLifecycleRecord = workItemLifecycleRecord;
  }

  public void setWorkItemLifecycleRecord( final IWorkItemLifecycleRecord workItemLifecycleRecord ) {
    this.workItemLifecycleRecord = workItemLifecycleRecord;
  }

  public IWorkItemLifecycleRecord getWorkItemLifecycleRecord() {
    return workItemLifecycleRecord;
  }

  public String toString() {
    return workItemLifecycleRecord.toString();
  }
}
