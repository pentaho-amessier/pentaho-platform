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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.util.Assert;
import org.pentaho.platform.api.workitem.WorkItemLifecyclePhase;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;

public class WorkItemLifecyclePublisherTest {

  private AbstractApplicationContext contextMock = null;
  private WorkItemLifecycleEventPublisher publisherMock = null;
  private WorkItem workItemMock = null;
  private String uid = "foo";
  private String workItemDetails = "foe";
  private WorkItemLifecyclePhase lifecyclePhase = WorkItemLifecyclePhase.DISPATCHED;
  private String eventDetails = "foe";
  private Date currentTimeStamp = new Date();
  private WorkItemLifecycleEvent eventMock = null;

  public static boolean LISTENER_A_CALLED = false;
  public static boolean LISTENER_B_CALLED = false;

  @Before
  public void setup() throws Exception {
    contextMock = Mockito.spy( new ClassPathXmlApplicationContext( "/solution/system/pentahoSystemConfig.xml" ) );

    publisherMock = Mockito.spy( new WorkItemLifecycleEventPublisher() );
    publisherMock.setApplicationEventPublisher( contextMock );

    workItemMock = Mockito.spy( new WorkItem( uid, workItemDetails ) );

    eventMock =
      Mockito.spy( new WorkItemLifecycleEvent( workItemMock, lifecyclePhase, eventDetails, currentTimeStamp ) );
    Mockito.when( publisherMock.createEvent( workItemMock, lifecyclePhase, eventDetails, currentTimeStamp ) )
      .thenReturn(
        eventMock );
  }

  @Test
  public void testPublisher() throws InterruptedException {
    publisherMock.publish( workItemMock, lifecyclePhase, eventDetails, currentTimeStamp );
    // verify that createEvent is called correctly
    Mockito.verify( publisherMock, Mockito.times( 1 ) )
      .createEvent( workItemMock, lifecyclePhase, eventDetails, currentTimeStamp );
    // verify that the publishEvent method is called as expected
    Mockito.verify( contextMock, Mockito.times( 1 ) ).publishEvent( eventMock );

    // This isn't ideal, but the only way to verify that the correct listeners - those that are wired through spring
    // (which run in a separate thread) were invoked
    Thread.sleep( 100 );
    Assert.assertTrue( LISTENER_A_CALLED );
    Assert.assertFalse( LISTENER_B_CALLED );
  }
}


