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
 * Copyright (c) 2017 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.plugin.action.DefaultActionInvoker;

import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tests the {@link ActionResource} within the context of CE.
 */
public class ActionResourceTest {

  private ActionResource resource;
  private ActionResource resourceMock;
  private Response expectedResult;
  private StandaloneSession session;
  private StandaloneSpringPentahoObjectFactory springFactory;
  private ExecutorService executorService;
  private ActionResource.RunnableAction runnableAction;

  @Before
  public void setUp() {
    resourceMock = Mockito.spy( ActionResource.class );
    resource = new ActionResource();
    expectedResult = Response.status( HttpStatus.SC_ACCEPTED ).build();
    executorService = Mockito.spy( Executors.newFixedThreadPool( 1 ) );
    resource.executorService = Mockito.mock( ExecutorService.class );
    runnableAction = Mockito.spy( ActionResource.RunnableAction.class );

    session = new StandaloneSession();
    PentahoSessionHolder.setSession( session );
    springFactory = new StandaloneSpringPentahoObjectFactory();
    springFactory.init( "src/test/resources/solution/system/pentahoObjects.spring.xml", null );
    PentahoSystem.registerObjectFactory( springFactory );

  }

  @After
  public void tearDown() {
    resourceMock = null;
    resource = null;
    expectedResult = null;
    executorService = null;
    runnableAction = null;
    session = null;
    springFactory = null;
  }

  @Test
  public void testGetActionInvoker() {
    final IActionInvoker actionInvoker = resource.getActionInvoker();
    Assert.assertNotNull( actionInvoker );
    Assert.assertEquals( DefaultActionInvoker.class, actionInvoker.getClass() );
  }

  @Test
  public void testRunInBackgroundNegative() {
    // verify that no matter what is passed to the runInBackground method, including nulls and other "bad" input, it
    // returns the expected status
    final String[] badStrInput = new String[] {null, "", " ", "foo"};
    for ( final String actionId : badStrInput ) {
      for ( final String actionClassName : badStrInput ) {
        for ( final String user : badStrInput ) {
          for ( final String params : badStrInput ) {
            final Response response = resource.runInBackground( actionId, actionClassName, user, params );
            Assert.assertNotNull( response );
            Assert.assertEquals( expectedResult.getStatus(), response.getStatus() );
          }
        }
      }
    }
  }

  @Test
  public void testRunInBackground() {
    final String actionId = null;
    final String actionClassName = DefaultActionInvoker.class.getName();
    final String actionUser = "user";
    final String actionParams = "";
    final ActionResource.RunnableAction runnableAction = Mockito.spy( ActionResource.RunnableAction.class );

    Mockito.doReturn( runnableAction ).when( resourceMock ).createRunnable( actionId, actionClassName, actionUser,
      actionParams );

    resourceMock.runInBackground( actionId, actionClassName, actionUser, actionParams );

    // verify that the createRunnable method is called
    Mockito.verify( resourceMock, Mockito.times( 1 ) ).createRunnable( actionId, actionClassName, actionUser,
      actionParams );

    // verity that the executor submit method is called to execute the action
    Mockito.verify( resourceMock.executorService, Mockito.times( 1 ) ).submit( runnableAction );
  }
}