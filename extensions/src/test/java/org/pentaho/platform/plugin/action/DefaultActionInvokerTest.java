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
 * Copyright (c) 2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.action;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionDetails;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.plugin.action.builtin.ActionSequenceAction;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.bean.TestAction;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.spy;

public class DefaultActionInvokerTest {
  private DefaultActionInvoker defaultActionInvoker;

  @Before
  public void setup() {
    defaultActionInvoker = new DefaultActionInvoker();
  }

  @Test
  public void invokeActionLocallyTest() throws Exception {
    Map<String, Serializable> testMap = new HashMap<>();
    testMap.put( ActionUtil.QUARTZ_ACTIONCLASS, "one" );
    testMap.put( ActionUtil.QUARTZ_ACTIONUSER, "two" );
    IAction iaction = ActionUtil.createActionBean( ActionSequenceAction.class.getName() );
    ActionInvokeStatus actionInvokeStatus =
      (ActionInvokeStatus) defaultActionInvoker.invokeAction( new ActionDetails( null, iaction, "aUser", testMap ) );
    Assert.assertFalse( actionInvokeStatus.requiresUpdate() );
  }

  @Test
  public void invokeActionTest() throws Exception {
    Map<String, Serializable> testMap = new HashMap<>();
    testMap.put( ActionUtil.QUARTZ_ACTIONCLASS, "one" );
    testMap.put( ActionUtil.QUARTZ_ACTIONUSER, "two" );
    IAction iaction = ActionUtil.createActionBean( ActionSequenceAction.class.getName() );
    ActionInvokeStatus actionInvokeStatus =
      (ActionInvokeStatus) defaultActionInvoker.invokeAction( new ActionDetails( null, iaction, "aUser", testMap ) );
    Assert.assertFalse( actionInvokeStatus.requiresUpdate() );
  }

  @Test( expected = ActionInvocationException.class )
  public void invokeActionLocallyWithNullsThrowsExceptionTest() throws Exception {
    defaultActionInvoker.invokeAction( new ActionDetails( null, null, "aUser", null, null ) );
  }


  @Test
  public void getStreamProviderNullTest() {
    Map<String, Serializable> paramMap = new HashMap<>();
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, null );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = defaultActionInvoker.getStreamProvider( paramMap );
    Assert.assertNull( iBackgroundExecutionStreamProvider );
  }

  @Test
  public void getStreamProviderNullWithInputFileTest() throws IOException {
    Map<String, Serializable> paramMap = new HashMap<>();
    File inputFile = new File( "example.txt" );
    BufferedWriter output = new BufferedWriter( new FileWriter( inputFile ) );
    output.write( "TEST TEXT" );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, null );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, inputFile );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = defaultActionInvoker.getStreamProvider( paramMap );
    Assert.assertNull( iBackgroundExecutionStreamProvider );
  }

  @Test
  public void getStreamProviderWithInputAndOutputFileTest() throws IOException {
    Map<String, Serializable> paramMap = new HashMap<>();
    RepositoryFileStreamProvider repositoryFileStreamProvider = new RepositoryFileStreamProvider();
    File inputFile = new File( "example.txt" );
    BufferedWriter output = new BufferedWriter( new FileWriter( inputFile ) );
    output.write( "TEST TEXT" );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, repositoryFileStreamProvider );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, inputFile );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN, inputFile );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME, true );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = defaultActionInvoker.getStreamProvider( paramMap );
    Assert.assertEquals( iBackgroundExecutionStreamProvider, repositoryFileStreamProvider );
  }


  @Test
  public void getStreamProviderTest() {
    Map<String, Serializable> paramMap = new HashMap<>();
    RepositoryFileStreamProvider repositoryFileStreamProvider = new RepositoryFileStreamProvider();
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, repositoryFileStreamProvider );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = defaultActionInvoker.getStreamProvider( paramMap );
    Assert.assertEquals( repositoryFileStreamProvider, iBackgroundExecutionStreamProvider );
  }

  @Test( expected = ActionInvocationException.class )
  public void testValidateNullActionDetails() throws Exception{
    AbstractActionInvoker aaInvokerSpy = spy( new DefaultActionInvoker() );
    aaInvokerSpy.validate( null );
  }

  @Test( expected = ActionInvocationException.class )
  public void testValidateNullAction() throws Exception{
    AbstractActionInvoker aaInvokerSpy = spy( new DefaultActionInvoker() );
    aaInvokerSpy.validate( new ActionDetails( null, null, null, null ) );
  }

  @Test( expected = ActionInvocationException.class )
  public void testValidateNullParameters() throws Exception{
    AbstractActionInvoker aaInvokerSpy = spy( new DefaultActionInvoker() );
    aaInvokerSpy.validate( new ActionDetails( null, new TestAction(), null, null ) );
  }

  @Test( expected = ActionInvocationException.class )
  public void testValidateCannotInvoke() throws Exception{
    AbstractActionInvoker aaInvokerSpy = spy( new DefaultActionInvoker() );
    Mockito.when( aaInvokerSpy.canInvoke( Mockito.any( IActionDetails.class ) ) ).thenReturn( false );
    IActionDetails details = new ActionDetails( null, new TestAction(), null, new HashMap<String, Serializable>() );
    aaInvokerSpy.validate( details );
  }

  @Test
  public void testValidateCanInvoke() throws Exception{
    AbstractActionInvoker aaInvokerSpy = spy( new DefaultActionInvoker() );
    Mockito.when( aaInvokerSpy.canInvoke( Mockito.any( IActionDetails.class ) ) ).thenReturn( true );
    IActionDetails details = new ActionDetails( null, new TestAction(), null, new HashMap<String, Serializable>() );
    try {
      aaInvokerSpy.validate( details );
      // we expect this to NOT throw an exception
    } catch (final ActionInvocationException e ) {
      Assert.fail();
    }
  }
}
