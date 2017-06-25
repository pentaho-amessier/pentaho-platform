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

/**
 * The event dispatched when a work item enters a new lifecycle phase.
 */
public class WorkItemLifecycleEvent {

  private WorkItemLifecycleRecord workItemLifecycleRecord;

  /**
   * An event encapsulating the work item lifecycle change.
   *
   * @param workItemLifecycleRecord the {@link WorkItemLifecycleRecord}
   */
  public WorkItemLifecycleEvent( final WorkItemLifecycleRecord workItemLifecycleRecord ) {
    this.workItemLifecycleRecord = workItemLifecycleRecord;
  }

  public WorkItemLifecycleEvent() {
  }

  public void setWorkItemLifecycleRecord( final WorkItemLifecycleRecord workItemLifecycleRecord ) {
    this.workItemLifecycleRecord = workItemLifecycleRecord;
  }

  public WorkItemLifecycleRecord getWorkItemLifecycleRecord() {
    return workItemLifecycleRecord;
  }

  public String toString() {
    return workItemLifecycleRecord.toString();
  }

  @Override
  public boolean equals( final Object other ) {
    if ( this == other ) {
      return true;
    } else {
      if ( other instanceof WorkItemLifecycleEvent ) {
        return toString().equals( other.toString() );
      } else {
        return false;
      }
    }
  }
}
