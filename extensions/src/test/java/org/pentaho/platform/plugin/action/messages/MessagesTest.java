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

package org.pentaho.platform.plugin.action.messages;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MessagesTest {

  private Messages messages;
  private String NL;

  @Before
  public void setup() {

    messages = Messages.getInstance();
    NL = System.getProperty( "line.separator" );
  }

  @Test
  public void tesGetRunningInBackgroundLocally() {

    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );
    Assert.assertEquals( "Running action \"foo\" in background locally: [" + NL + "   key1 -> val1" + NL + "   key2 "
      + "-> val2" + NL + "]", messages.getRunningInBackgroundLocally( "foo", params ) );
  }

  @Test
  public void tesGetRunningInBackgroundRemotely() {

    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );
    Assert.assertEquals( "Running action \"foo\" in background remotely: [" + NL + "   key1 -> val1" + NL + "   key2 "
      + "-> val2" + NL + "]", messages.getRunningInBackgroundRemotely( "foo", params ) );
  }

  @Test
  public void testGetPostingToResource() {

    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );
    Assert.assertEquals( "POSTing to resource \"foo\": [" + NL + "   key1 "
      + "-> val1" + NL + "   key2 -> val2" + NL + "]", messages.getPostingToResource( "foo", params ) );
  }

  @Test
  public void testGetResourceResponded() {
    Assert.assertEquals( "Resource \"foo\" responded with \"200\"", messages.getResourceResponded( "foo", 200 ) );
  }

  @Test
  public void testGetCantInvokeActionWithNullMap() {
    Assert.assertEquals( "ActionInvoker.ERROR_0006 - Cannot invoke action when the map is null",
      messages.getCantInvokeActionWithNullMap() );
  }

  @Test
  public void testGetCantInvokeNullAction() {
    Assert.assertEquals( "ActionInvoker.ERROR_0005 - Action is null, cannot invoke", messages.getCantInvokeNullAction() );
  }

  @Test
  public void testGetRemoteEndpointFailure() {
    Assert.assertEquals( "ActionInvoker.ERROR_0007 - Unable to execute the remote endpoint", messages
      .getRemoteEndpointFailure() );
  }

  @Test
  public void testGetRequiredParamMissing() {
    Assert.assertEquals( "ActionInvoker.ERROR_0001 - Property \"foo\" or \"foe\" must be set in "
      + "the action data map", messages.getRequiredParamMissing( "foo", "foe" ) );
  }

  @Test
  public void testGetFailedToCreateAction() {
    Assert.assertEquals( "ActionInvoker.ERROR_0002 - Failed to create an instance of action "
      + "\"foo\"", messages.getFailedToCreateAction( "foo" ) );
  }

  @Test
  public void testGetActionWrongType() {
    Assert.assertEquals( "ActionInvoker.ERROR_0003 - class foo must be an instance of \"foe\"",
      messages.getActionWrongType( "foo", "foe" ) );
  }

  @Test
  public void testGetMapNullCantReturnSp() {
    Assert.assertEquals( "ActionInvoker.ERROR_0008 - Map is null, cannot return stream provider", messages
      .getMapNullCantReturnSp() );
  }
  @Test
  public void testGetMissingParamsCantReturnSp() {

    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );
    Assert.assertEquals( "ActionInvoker.ERROR_0009 - Parameters required to create the stream provider (foo) are not "
      + "available in the map: [" + NL + "   key1 -> val1" + NL + "   key2 -> val2" + NL + "]", messages
      .getMissingParamsCantReturnSp( "foo", params ) );
  }

  @Test
  public void testGetActionFailedToExecute() {
    Assert.assertEquals( "ActionInvoker.ERROR_0004 - Action \"foo\" failed to execute", messages
      .getActionFailedToExecute( "foo" ) );
  }

  @Test
  public void testGetSkipRemovingOutputFile() {
    Assert.assertEquals( "File written by XActions must be cleaned up by external means: foo", messages
      .getSkipRemovingOutputFile( "foo" ) );
  }

  @Test
  public void testGetCannotGetRepoFile() {
    Assert.assertEquals( "ActionInvoker.ERROR_0010 - Cannot get repository file \"foo\": foe", messages
      .getCannotGetRepoFile( "foo", "foe" ) );
  }

  @Test
  public void testGetCouldNotConvertContentToMap() {
    Assert.assertEquals( "ActionInvoker.ERROR_0011 - Could not convert content to map: foo", messages
      .getCouldNotConvertContentToMap( "foo" ) );
  }

  @Test
  public void testGetCouldNotInvokeActionLocally() {

    final Map<String, String> params = new HashMap<String, String>();
    params.put( "key1", "val1" );
    params.put( "key2", "val2" );

    Assert.assertEquals( "ActionInvoker.ERROR_0012 - Could not invoke action locally: [" + NL + "   key1 -> val1" +
      NL + "   key2 -> val2" + NL + "]", messages.getCouldNotInvokeActionLocally( params ) );
  }
}
