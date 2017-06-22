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

import org.pentaho.platform.core.workitem.messages.Messages;

/**
 * This class encapsulates all information pertaining to a "work item" that does not change throughout the work item
 * lifecycle.
 */
public class WorkItem {

  private String uid;
  private String details;

  /**
   * Created the {@link WorkItem} with all the required parameters.
   *
   * @param uid     a {@link String} containing unique identifier for the {@link WorkItem}
   * @param details a {@link String} containing details of the {@link WorkItem}
   */
  public WorkItem( final String uid, final String details ) {
    this.uid = uid;
    this.details = details;
  }

  public void setUid( final String uid ) {
    this.uid = uid;
  }

  public String getUid() {
    return uid;
  }

  public void setDetails( final String details ) {
    this.details = details;
  }

  public String getDetails() {
    return details;
  }

  public String toString() {
    return String.format( "uid=%s / details=%s", uid, details );
  }

  /**
   * An enumeration of the known lifecycle events for the work item.
   */
  public enum LifecyclePhase {

    SCHEDULED( "LifecyclePhase.SCHEDULED" ),
    SUMBITTED( "LifecyclePhase.SUMBITTED" ),
    DISPATCHED( "LifecyclePhase.DISPATCHED" ),
    RECEIVED( "LifecyclePhase.RECEIVED" ),
    REJECTED( "LifecyclePhase.REJECTED" ),
    IN_PROGRESS( "LifecyclePhase.IN_PROGRESS" ),
    SUCCEEDED( "LifecyclePhase.SUCCEEDED" ),
    FAILED( "LifecyclePhase.FAILED" );

    private String nameKey;

    LifecyclePhase( final String nameKey ) {
      this.nameKey = nameKey;
    }

    @Override
    public String toString() {
      return Messages.getInstance().getString( nameKey );
    }
  }
}
