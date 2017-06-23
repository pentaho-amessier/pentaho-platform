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

import java.util.Date;

public class WorkItemLifecycleRecordTest {

  private String workItemUid = "foo";
  private String workItemDetails = "foe";
  private WorkItemLifecyclePhase lifecyclePhase = WorkItemLifecyclePhase.DISPATCHED;
  private String lifecycleDetails = "foe";
  private Date currentTimeStamp = new Date();

  @Test
  public void testValidInstaitiation() {
    final WorkItemLifecycleRecord workItemLifecycleRecord = new WorkItemLifecycleRecord( workItemUid,
      workItemDetails, lifecyclePhase, lifecycleDetails, currentTimeStamp );
    Assert.assertEquals( workItemUid, workItemLifecycleRecord.getWorkItemUid() );
    Assert.assertEquals( workItemDetails, workItemLifecycleRecord.getWorkItemDetails() );
    Assert.assertEquals( lifecyclePhase, workItemLifecycleRecord.getWorkItemLifecyclePhase() );
    Assert.assertEquals( lifecycleDetails, workItemLifecycleRecord.getLifecycleDetails() );
    Assert.assertEquals( currentTimeStamp, workItemLifecycleRecord.getSourceTimestamp() );

    // source host name and ip should be set within the constructor
    Assert.assertNotNull( workItemLifecycleRecord.getSourceHostName() );
    Assert.assertNotNull( workItemLifecycleRecord.getSourceHostIp() );
  }

  @Test
  public void testNulls() {
    final WorkItemLifecycleRecord workItemLifecycleRecord = new WorkItemLifecycleRecord( null, null, null, null,
      null );
    Assert.assertNull( workItemLifecycleRecord.getWorkItemUid() );
    Assert.assertNull( workItemLifecycleRecord.getWorkItemDetails() );
    Assert.assertNull( workItemLifecycleRecord.getWorkItemLifecyclePhase() );
    Assert.assertNull( workItemLifecycleRecord.getLifecycleDetails() );

    // if null sourceTimestamp is passed, it is initialized when the WorkItemLifecycleRecord is initialized
    Assert.assertNotNull( workItemLifecycleRecord.getSourceTimestamp() );
    // source host name and ip should be set within the constructor
    Assert.assertNotNull( workItemLifecycleRecord.getSourceHostName() );
    Assert.assertNotNull( workItemLifecycleRecord.getSourceHostIp() );
  }
}
