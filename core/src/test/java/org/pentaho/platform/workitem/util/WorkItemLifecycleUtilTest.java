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

package org.pentaho.platform.workitem.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.workitem.WorkItemLifecycleEvent;
import org.pentaho.platform.workitem.WorkItemLifecycleRecord;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;

public class WorkItemLifecycleUtilTest {

  private AbstractApplicationContext contextMock = null;
  private WorkItemLifecycleUtil publisherUtilMock = null;
  private WorkItemLifecycleRecord workItemLifecycleRecordMock = null;
  private String workItemUid = "foo";
  private String workItemDetails = "foe";
  private WorkItemLifecyclePhase lifecyclePhase = WorkItemLifecyclePhase.DISPATCHED;
  private String lifecycleDetails = "foe";
  private Date currentTimeStamp = new Date();
  private WorkItemLifecycleEvent eventMock = null;

  public static boolean LISTENER_A_CALLED = false;
  public static boolean LISTENER_B_CALLED = false;
  public static boolean LISTENER_C_CALLED = false;

  @Before
  public void setup() throws Exception {
    contextMock = Mockito.spy( new ClassPathXmlApplicationContext( "/solution/system/pentahoSystemConfig.xml" ) );

    publisherUtilMock = Mockito.spy( contextMock.getBean( WorkItemLifecycleUtil.class ) );
    publisherUtilMock.setApplicationEventPublisher( contextMock );

    workItemLifecycleRecordMock = Mockito.spy( new WorkItemLifecycleRecord( workItemUid, workItemDetails ) );
    workItemLifecycleRecordMock.setWorkItemLifecyclePhase( lifecyclePhase );
    workItemLifecycleRecordMock.setLifecycleDetails( lifecycleDetails );

    eventMock = Mockito.spy( new WorkItemLifecycleEvent( workItemLifecycleRecordMock ) );
    Mockito.when( publisherUtilMock.createEvent( workItemLifecycleRecordMock ) ).thenReturn( eventMock );
  }

  @Test
  public void testPublisher() throws InterruptedException {
    publisherUtilMock.publishImpl( workItemLifecycleRecordMock );
    // verify that createEvent is called correctly
    Mockito.verify( publisherUtilMock, Mockito.times( 1 ) ).createEvent( workItemLifecycleRecordMock );
    // verify that the publishEvent method is called as expected
    Mockito.verify( contextMock, Mockito.times( 1 ) ).publishEvent( eventMock );

    // This isn't ideal, but the only way to verify that the correct listeners - those that are wired through spring
    // (which run in a separate thread) were invoked, and that those that were not wired are not invoked
    Thread.sleep( 100 );
    org.pentaho.di.core.util.Assert.assertTrue( LISTENER_A_CALLED );
    org.pentaho.di.core.util.Assert.assertTrue( LISTENER_B_CALLED );
    org.pentaho.di.core.util.Assert.assertFalse( LISTENER_C_CALLED );
  }
}
