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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

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
    Assert.assertEquals( MyTestAction.class, aClass );
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

    Assert.assertEquals( MyTestAction.class, aClass );
  }

  @Test
  public void createActionBeanHappyPath() throws ActionInvocationException {
    IAction iaction = ActionUtil.createActionBean( MyTestAction.class.getName(), null );
    Assert.assertEquals( iaction.getClass(), MyTestAction.class );
  }
}
