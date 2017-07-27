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

package org.pentaho.platform.util;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith( PowerMockRunner.class )
@PrepareForTest( PentahoSystem.class )
public class ActionUtilTest {

  @Test( expected = ActionInvocationException.class )
  public void resolveClassIllegalArgumentExceptionWithEmptyStrings()
    throws PluginBeanException, ActionInvocationException {
    ActionUtil.resolveActionClass( "", "" );
  }

  @Test( expected = ActionInvocationException.class )
  public void resolveClassIllegalArgumentExceptionWithNulls() throws PluginBeanException, ActionInvocationException {
    ActionUtil.resolveActionClass( null, null );
  }

  @Test( expected = ActionInvocationException.class )
  public void createActionBeanIllegalArgumentExceptionWithEmptyStrings()
    throws PluginBeanException, ActionInvocationException {
    ActionUtil.createActionBean( "", "" );
  }

  @Test( expected = ActionInvocationException.class )
  public void createActionBeanIllegalArgumentExceptionWithNulls()
    throws PluginBeanException, ActionInvocationException {
    ActionUtil.createActionBean( null, null );
  }

  @Test
  public void resolveClassTestHappyPathNoBeanID() throws Exception {
    Class<?> aClass = ActionUtil.resolveActionClass( MyTestAction.class.getName(), "" );
    assertEquals( MyTestAction.class, aClass );
  }

  @Test
  public void resolveClassTestHappyPath() throws Exception {
    // TODO: rewrite this test to read bean from spring rather than mocking it
    String beanId = "ktr.backgroundAction";
    Class<?> clazz = MyTestAction.class;

    IPluginManager pluginManager = mock( IPluginManager.class );
    PowerMockito.mockStatic( PentahoSystem.class );
    BDDMockito.given( PentahoSystem.get( IPluginManager.class ) ).willReturn( pluginManager );

    Mockito.doReturn( clazz ).when( pluginManager ).loadClass( anyString() );

    Class<?> aClass = ActionUtil.resolveActionClass( MyTestAction.class.getName(), beanId );

    assertEquals( MyTestAction.class, aClass );
  }

  @Test
  public void createActionBeanHappyPath() throws ActionInvocationException {
    IAction iaction = ActionUtil.createActionBean( MyTestAction.class.getName(), null );
    assertEquals( iaction.getClass(), MyTestAction.class );
  }


  @Test
  public void removeFromMapHappyPathTest() throws Exception {
    Map<String, String> testMap = new HashMap<>();
    testMap.put( "one", "one" );
    testMap.put( "two", "two" );
    testMap.remove( "one" );
    Assert.assertNull( testMap.get( "one" ) );
    assertEquals( testMap.get( "two" ), "two" );
  }

  @Test
  public void removeFromMapSecondHappyPathTest() throws Exception {
    Map<String, String> testMap = new HashMap<>();
    testMap.put( "one", "one" );
    testMap.put( "two", "two" );
    testMap.put( "actionClass", "actionClass" );
    testMap.remove( "actionClass" );
    assertNull( testMap.get( "actionClass" ) );
    assertEquals( testMap.get( "two" ), "two" );
  }

  @Test
  public void removeFromMapHappyPathMappedKeyTest() throws Exception {
    Map<String, String> testMap = new HashMap<>();
    testMap.put( ActionUtil.QUARTZ_ACTIONCLASS, "one" );
    testMap.put( "two", "two" );
    testMap.remove( ActionUtil.QUARTZ_ACTIONCLASS );
    Assert.assertNull( testMap.get( ActionUtil.QUARTZ_ACTIONCLASS ) );
    assertEquals( testMap.get( "two" ), "two" );
  }

  @Test
  public void prepareMapNullTest() throws Exception {
    Map<String, Serializable> testMap = null;
    ActionUtil.prepareMap( testMap );
    Assert.assertNull( testMap );
  }

  @Test
  public void prepareMapTest() throws Exception {
    Map<String, Serializable> testMap = new HashMap<>();
    testMap.put( ActionUtil.QUARTZ_ACTIONCLASS, "one" );
    testMap.put( ActionUtil.QUARTZ_ACTIONUSER, "two" );
    ActionUtil.prepareMap( testMap );
    assertEquals( testMap.get( ActionUtil.QUARTZ_ACTIONCLASS ), null );
    assertEquals( testMap.get( ActionUtil.QUARTZ_ACTIONUSER ), null );
  }

  @Test
  public void testExtractUid() {
    final Map<String, Serializable> params = new HashMap<>();

    Assert.assertNotNull( ActionUtil.extractUid( params ) );
    // the map should now contain a uid
    Assert.assertTrue( params.containsKey( ActionUtil.WORK_ITEM_UID ) );
    final String uid = (String) params.get( ActionUtil.WORK_ITEM_UID );
    assertEquals( uid, ActionUtil.extractUid( params ) );
    assertEquals( 1, params.size() );
  }

  @Test
  public void testGenerateUniqueWorkItemId() {
    int maxLength = ActionUtil.WORK_ITEM_ID_MAX_LENGTH;
    // simple case
    assertEquals( "WI-Test.prpt-admin-0", ActionUtil.generateUniqueWorkItemId( "Test.prpt", "admin", 0L, maxLength ) );

    // bad characters
    assertEquals( "WI-_.prpt-adm_in-0", ActionUtil.generateUniqueWorkItemId( "!@#$%^&*.prpt", "adm&*&in", 0L, maxLength ) );

    // all bad characters
    assertEquals( "WI-_.prpt-_-0", ActionUtil.generateUniqueWorkItemId( "!@#$%^&*.prpt", "&*&(*&", 0L, maxLength ) );

    // file path and spaces
    assertEquals( "WI-Test_File.prpt-adm_in-12345", ActionUtil.generateUniqueWorkItemId( "folder/Test File.prpt",
      "adm&*&in", 12345L, maxLength ) );

    // missing user and file
    assertEquals( "WI-0", ActionUtil.generateUniqueWorkItemId( "", "", 0L, maxLength ) );
    assertEquals( "WI-0", ActionUtil.generateUniqueWorkItemId( null, null, 0L, maxLength ) );
    assertEquals( "WI-123", ActionUtil.generateUniqueWorkItemId( null, null, 123L, maxLength ) );

    // exceeded length
    assertEquals( "WI-12", ActionUtil.generateUniqueWorkItemId( "", "admin", 123L, 5 ) );
    assertEquals( "WI-123", ActionUtil.generateUniqueWorkItemId( "", "admin", 123L, 6 ) );
    // this will be the same, because we're replacing the double '--' with a single -
    assertEquals( "WI-123", ActionUtil.generateUniqueWorkItemId( "", "admin", 123L, 7 ) );
    assertEquals( "WI-a-123", ActionUtil.generateUniqueWorkItemId( "", "admin", 123L, 8 ) );
    assertEquals( "WI-adm-123", ActionUtil.generateUniqueWorkItemId( "", "admin", 123L, 10 ) );

    // negative maxLength should yield original value
    assertEquals( "WI-admin-123", ActionUtil.generateUniqueWorkItemId( "", "admin", 123L, -1 ) );
  }
}
