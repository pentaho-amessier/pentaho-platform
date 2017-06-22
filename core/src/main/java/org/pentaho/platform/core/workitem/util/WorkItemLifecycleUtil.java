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

package org.pentaho.platform.core.workitem.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.core.workitem.IWorkItemLifecycleEventPublisher;
import org.pentaho.platform.core.workitem.WorkItem;
import org.pentaho.platform.core.workitem.WorkItemLifecycleEventPublisher;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * A class for common utility methods related to work item lifecycles.
 */
public class WorkItemLifecycleUtil {

  private static final Log log = LogFactory.getLog( WorkItemLifecycleUtil.class );

  /**
   * A convenience method for publishing changes to the {@link WorkItem}'s {@link WorkItem.LifecyclePhase}. Fetches
   * the available instance of {@link IWorkItemLifecycleEventPublisher}, and if available, calls its
   * {@link IWorkItemLifecycleEventPublisher#publish(WorkItem, WorkItem.LifecyclePhase, String)} method. Otherwise
   * does nothing, as the {@link IWorkItemLifecycleEventPublisher} may not be available, which is a perfectly valid
   * scenario.
   *
   * @param workItem       the {@link WorkItem}
   * @param lifecyclePhase the {@link WorkItem.LifecyclePhase}
   */
  public static void publish( final WorkItem workItem, final WorkItem.LifecyclePhase lifecyclePhase ) {
    publish( workItem, lifecyclePhase, null );
  }

  /**
   * A convenience method for publishing changes to the {@link WorkItem}'s {@link WorkItem.LifecyclePhase}. Fetches
   * the available instance of {@link IWorkItemLifecycleEventPublisher}, and if available, calls its
   * {@link IWorkItemLifecycleEventPublisher#publish(WorkItem, WorkItem.LifecyclePhase, String)} method. Otherwise
   * does nothing, as the {@link IWorkItemLifecycleEventPublisher} may not be available, which is a perfectly valid
   * scenario.
   *
   * @param workItem       the {@link WorkItem}
   * @param lifecyclePhase the {@link WorkItem.LifecyclePhase}
   * @param details        any details associated with the state change (example: error message, exception stack trace)
   */
  public static void publish( final WorkItem workItem, final WorkItem.LifecyclePhase lifecyclePhase,
                              final String details ) {

    final IWorkItemLifecycleEventPublisher publisher = PentahoSystem.get( WorkItemLifecycleEventPublisher.class,
      "IWorkItemLifecycleEventPublisher", PentahoSessionHolder.getSession() );
    if ( publisher != null ) {
      publisher.publish( workItem, lifecyclePhase, details );
    } else {
      log.debug( "'IWorkItemLifecycleEventPublisher' bean is not available, unable to publish work item lifecycle: "
        + workItem.toString() );
    }
  }
}
