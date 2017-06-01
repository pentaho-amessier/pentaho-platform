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
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.plugin.action.builtin.ActionSequenceAction;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DefaultActionInvokerTest {

    DefaultActionInvoker defaultActionInvoker;

    @Before
    public void initialize() {
        defaultActionInvoker = new DefaultActionInvoker();
    }

    @Test
    public void removeFromMapHappyPathTest() throws Exception {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("one", "one");
        testMap.put("two", "two");
        DefaultActionInvoker.removeFromMap(testMap, "one");
        Assert.assertNull(testMap.get("one"));
        Assert.assertEquals(testMap.get("two"), "two");
    }

    @Test
    public void removeFromMapSecondHappyPathTest() throws Exception {
        Map<String, String> testMap = new HashMap<>();
        testMap.put("one", "one");
        testMap.put("two", "two");
        testMap.put("actionClass", "actionClass");
        DefaultActionInvoker.removeFromMap(testMap, "actionClass");
        Assert.assertNull(testMap.get("actionClass"));
        Assert.assertEquals(testMap.get("two"), "two");
    }

    @Test
    public void removeFromMapHappyPathMappedKeyTest() throws Exception {
        Map<String, String> testMap = new HashMap<>();
        testMap.put(QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, "one");
        testMap.put("two", "two");
        DefaultActionInvoker.removeFromMap(testMap, QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS);
        Assert.assertNull(testMap.get(QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS));
        Assert.assertEquals(testMap.get("two"), "two");
    }

    @Test
    public void removeFromMapNullMapTest() throws Exception {
        Map<String, String> testMap = null;
        DefaultActionInvoker.removeFromMap(testMap, "one");
        Assert.assertNull(testMap);
    }

    @Test
    public void prepareMapNullTest() throws Exception {
        Map<String, Serializable> testMap = null;
        defaultActionInvoker.prepareMap(testMap);
        Assert.assertNull(testMap);
    }

    @Test
    public void prepareMapTest() throws Exception {
        Map<String, Serializable> testMap = new HashMap<>();
        testMap.put(QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, "one");
        testMap.put(QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER, "two");
        defaultActionInvoker.prepareMap(testMap);
        Assert.assertEquals(testMap.get(QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS), null);
        Assert.assertEquals(testMap.get(QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER), null);
    }

    @Test
    public void createActionBeanTest() throws Exception {
        IAction iaction = defaultActionInvoker.createActionBean(ActionSequenceAction.class.getName(), null);
        Assert.assertEquals(iaction.getClass(), ActionSequenceAction.class);
    }

    @Test
    public void runInBackgroundLocallyTest() throws Exception {
        Map<String, Serializable> testMap = new HashMap<>();
        testMap.put(QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, "one");
        testMap.put(QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER, "two");
        IAction iaction = ActionHelper.createActionBean(ActionSequenceAction.class.getName(), null);
        ActionInvokeStatus actionInvokeStatus = (ActionInvokeStatus) defaultActionInvoker.runInBackgroundLocally(iaction, "aUser", testMap);
        Assert.assertFalse(actionInvokeStatus.requiresUpdate());
    }

    @Test
    public void runInBackgroundTest() throws Exception {
        Map<String, Serializable> testMap = new HashMap<>();
        testMap.put(QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, "one");
        testMap.put(QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER, "two");
        IAction iaction = ActionHelper.createActionBean(ActionSequenceAction.class.getName(), null);
        ActionInvokeStatus actionInvokeStatus = (ActionInvokeStatus) defaultActionInvoker.runInBackground(iaction, "aUser", testMap);
        Assert.assertFalse(actionInvokeStatus.requiresUpdate());
    }

    @Test(expected=ActionInvocationException.class)
    public void runInBackgroundLocallyWithNullsThrowsExceptionTest() throws Exception {
        defaultActionInvoker.runInBackgroundLocally(null, "aUser", null);
    }

}
