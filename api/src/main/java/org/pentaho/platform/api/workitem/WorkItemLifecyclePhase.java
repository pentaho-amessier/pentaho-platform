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

/**
 * An enumeration of the known lifecycle events for the work item. Each instance contains the a message key for
 * the short name and a long description of the phase. Both of these keys are expected to be present within the
 * resources of the caller and used to construct the "pretty" name and message describing the lifecycle phase.
 */
public enum WorkItemLifecyclePhase {

  /**
   * The work item has been scheduled for execution; shortNameMessageKey=LifecyclePhase.SCHEDULED,
   * descriptionMessageKey=SCHEDULED_DESC.
   */
  SCHEDULED( "LifecyclePhase.SCHEDULED", "LifecyclePhase.SCHEDULED_DESC" ),
  /**
   * The work item has been submitted for execution; shortNameMessageKey=LifecyclePhase.SUBMITTED,
   * descriptionMessageKey=SUMBITTED_DESC.
   */
  SUBMITTED( "LifecyclePhase.SUMBITTED", "LifecyclePhase.SUMBITTED_DESC" ),
  /**
   * The work item has been dispatched to the component responsible for its execution;
   * shortNameMessageKey=LifecyclePhase .DISPATCHED, descriptionMessageKey=DISPATCHED_DESC.
   */
  DISPATCHED( "LifecyclePhase.DISPATCHED", "LifecyclePhase.DISPATCHED_DESC" ),
  /**
   * The work item has been received by the component responsible for its execution; shortNameMessageKey=LifecyclePhase
   * .RECEIVED, descriptionMessageKey=RECEIVED_DESC.
   */
  RECEIVED( "LifecyclePhase.RECEIVED", "LifecyclePhase.RECEIVED_DESC" ),
  /**
   * The work item execution has been rejected; shortNameMessageKey=LifecyclePhase.REJECTED,
   * descriptionMessageKey=REJECTED_DESC.
   */
  REJECTED( "LifecyclePhase.REJECTED", "LifecyclePhase.REJECTED_DESC" ),
  /**
   * The work item execution is in progress; shortNameMessageKey=LifecyclePhase.IN_PROGRESS,
   * descriptionMessageKey=IN_PROGRESS_DESC.
   */
  IN_PROGRESS( "LifecyclePhase.IN_PROGRESS", "LifecyclePhase.IN_PROGRESS_DESC" ),
  /**
   * The work item execution has succeeded; shortNameMessageKey=LifecyclePhase.SUCCEEDED,
   * descriptionMessageKey=SUCCEEDED_DESC.
   */
  SUCCEEDED( "LifecyclePhase.SUCCEEDED", "LifecyclePhase.SUCCEEDED_DESC" ),
  /**
   * The work item execution has failed; shortNameMessageKey=LifecyclePhase.FAILED, descriptionMessageKey=FAILED_DESC.
   */
  FAILED( "LifecyclePhase.FAILED", "LifecyclePhase.FAILED_DESC" );

  private String shortNameMessageKey;
  private String descriptionMessageKey;

  WorkItemLifecyclePhase( final String shortNameMessageKey, final String descriptionMessageKey ) {
    this.shortNameMessageKey = shortNameMessageKey;
    this.descriptionMessageKey = descriptionMessageKey;
  }

  public String getShortNameMessageKey() {
    return shortNameMessageKey;
  }

  public String getDescriptionMessageKey() {
    return descriptionMessageKey;
  }
}
