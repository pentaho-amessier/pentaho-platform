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

import org.junit.Assert;
import org.junit.Test;

public class WorkItemLifecyclePhaseTest {

  @Test
  public void testLifecycleNames() {
    Assert.assertEquals( "Scheduled", WorkItemLifecyclePhase.SCHEDULED.getShortName() );
    Assert.assertEquals( "Submitted", WorkItemLifecyclePhase.SUBMITTED.getShortName() );
    Assert.assertEquals( "Dispatched", WorkItemLifecyclePhase.DISPATCHED.getShortName() );
    Assert.assertEquals( "Received", WorkItemLifecyclePhase.RECEIVED.getShortName() );
    Assert.assertEquals( "Rejected", WorkItemLifecyclePhase.REJECTED.getShortName() );
    Assert.assertEquals( "In progress", WorkItemLifecyclePhase.IN_PROGRESS.getShortName() );
    Assert.assertEquals( "Succeeded", WorkItemLifecyclePhase.SUCCEEDED.getShortName() );
    Assert.assertEquals( "Failed", WorkItemLifecyclePhase.FAILED.getShortName() );

  }

  @Test
  public void testLifecycleDescriptions() {
    Assert.assertEquals( "The work item has been scheduled for execution", WorkItemLifecyclePhase.SCHEDULED
      .getDescription() );
    Assert.assertEquals( "The work item has been submitted to the component responsible for its execution",
      WorkItemLifecyclePhase.SUBMITTED.getDescription() );
    Assert
      .assertEquals( "The work item has been dispatched to the component responsible for its execution",
        WorkItemLifecyclePhase.DISPATCHED.getDescription() );
    Assert.assertEquals( "The work item has been received by the component responsible for its execution",
      WorkItemLifecyclePhase.RECEIVED.getDescription() );
    Assert.assertEquals( "The work item execution has been rejected",
      WorkItemLifecyclePhase.REJECTED.getDescription() );
    Assert
      .assertEquals( "The work item execution is in progress",
        WorkItemLifecyclePhase.IN_PROGRESS.getDescription() );
    Assert.assertEquals( "The work item execution has succeeded",
      WorkItemLifecyclePhase.SUCCEEDED.getDescription() );
    Assert.assertEquals( "The work item execution has failed",
      WorkItemLifecyclePhase.FAILED.getDescription() );
  }
}
