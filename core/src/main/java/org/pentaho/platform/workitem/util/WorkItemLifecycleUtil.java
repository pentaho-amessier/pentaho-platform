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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleEventPublisher;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleRecord;
import org.pentaho.platform.api.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.Messages;

/**
 * A class for common utility methods related to work item lifecycles.
 */
public class WorkItemLifecycleUtil {

  private static String PUBLISHER_BEAN_NAME = "IWorkItemLifecycleEventPublisher";
  private static final Log log = LogFactory.getLog( WorkItemLifecycleUtil.class );

  /**
   * A convenience method for publishing changes to the work item's lifecycles. Fetches the available instance of
   * {@link IWorkItemLifecycleEventPublisher}, and if available, calls its
   * {@link IWorkItemLifecycleEventPublisher#publish(IWorkItemLifecycleRecord)} method. Otherwise does nothing, as the
   * {@link IWorkItemLifecycleEventPublisher} may not be available, which is a perfectly valid cenario.
   *
   * @param workItemLifecycleRecord        the {@link IWorkItemLifecycleRecord}
   * @see IWorkItemLifecycleEventPublisher#publish(IWorkItemLifecycleRecord)
   */
  public static void publish( final IWorkItemLifecycleRecord workItemLifecycleRecord ) {

    final IWorkItemLifecycleEventPublisher publisher = PentahoSystem.get( IWorkItemLifecycleEventPublisher.class,
      PUBLISHER_BEAN_NAME, PentahoSessionHolder.getSession() );
    if ( publisher != null ) {
      publisher.publish( workItemLifecycleRecord );
    } else {
      log.debug( String.format( "'%s' bean is not available, unable to publish work item "
        + "lifecycle: %s", PUBLISHER_BEAN_NAME, workItemLifecycleRecord.toString() ) );
    }
  }

  /**
   * Returns the short "pretty" name for the requested {@link WorkItemLifecyclePhase}.
   * @param lifecyclePhase the {@link WorkItemLifecyclePhase}
   * @return a "pretty" short name for the {@link WorkItemLifecyclePhase}
   */
  public static String getLifecyclePhaseName( final WorkItemLifecyclePhase lifecyclePhase ) {
    return Messages.getInstance().getString( lifecyclePhase.getShortNameMessageKey() );
  }

  /**
   * Returns the short "pretty" full description for the requested {@link WorkItemLifecyclePhase}.
   * @param lifecyclePhase the {@link WorkItemLifecyclePhase}
   * @return a "pretty" ull description for the {@link WorkItemLifecyclePhase}
   */
  public static String getLifecyclePhaseDescription( final WorkItemLifecyclePhase lifecyclePhase ) {
    return Messages.getInstance().getString( lifecyclePhase.getDescriptionMessageKey() );
  }
}
