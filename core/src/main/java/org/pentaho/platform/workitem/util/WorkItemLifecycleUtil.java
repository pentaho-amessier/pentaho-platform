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
import org.pentaho.platform.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.Messages;
import org.pentaho.platform.workitem.WorkItemLifecycleEvent;
import org.pentaho.platform.workitem.WorkItemLifecycleRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;

/**
 * A class for common utility methods related to work item lifecycles.
 */
// TODO: do we need this annotation?
@Configuration
public class WorkItemLifecycleUtil {

  private static String PUBLISHER_BEAN_NAME = "workItemLifecyclePublisher";
  private static final Log log = LogFactory.getLog( WorkItemLifecycleUtil.class );

  public static WorkItemLifecycleUtil getInstance() {
    return PentahoSystem.get( WorkItemLifecycleUtil.class, PUBLISHER_BEAN_NAME, PentahoSessionHolder.getSession() );
  }

  @Autowired
  private ApplicationEventPublisher publisher = null;

  protected void setApplicationEventPublisher( final ApplicationEventPublisher publisher ) {
    this.publisher = publisher;
  }

  protected ApplicationEventPublisher getApplicationEventPublisher() {
    return this.publisher;
  }

  /**
   * A convenience method for publishing changes to the work item's lifecycles. Fetches the available
   * {@link ApplicationEventPublisher}, and if available, calls its
   * {@link ApplicationEventPublisher#publishEvent(Object)} method, where the Object passed to the method is the
   * {@link WorkItemLifecycleEvent} representing the {@link WorkItemLifecycleRecord}.  Otherwise does nothing, as the
   * {@link ApplicationEventPublisher} may not be available, which is a perfectly valid scenario, if we do not care
   * about publishing {@link WorkItemLifecycleEvent}'s.
   *
   * @param workItemLifecycleRecord the {@link WorkItemLifecycleRecord}
   */
  public void publish( final WorkItemLifecycleRecord workItemLifecycleRecord ) {

    if ( getApplicationEventPublisher() != null ) {
      getApplicationEventPublisher().publishEvent( createEvent( workItemLifecycleRecord ) );
    } else {
      log.debug( String.format( "'%s' bean is not available, unable to publish work item "
        + "lifecycle: %s", PUBLISHER_BEAN_NAME, workItemLifecycleRecord.toString() ) );
    }
  }

  protected WorkItemLifecycleEvent createEvent( final WorkItemLifecycleRecord workItemLifecycleRecord ) {
    return new WorkItemLifecycleEvent( workItemLifecycleRecord );
  }

  /**
   * Returns the short "pretty" name for the requested {@link WorkItemLifecyclePhase}.
   *
   * @param lifecyclePhase the {@link WorkItemLifecyclePhase}
   * @return a "pretty" short name for the {@link WorkItemLifecyclePhase}
   */
  public static String getLifecyclePhaseName( final WorkItemLifecyclePhase lifecyclePhase ) {
    return Messages.getInstance().getString( lifecyclePhase.getShortNameMessageKey() );
  }

  /**
   * Returns the short "pretty" full description for the requested {@link WorkItemLifecyclePhase}.
   *
   * @param lifecyclePhase the {@link WorkItemLifecyclePhase}
   * @return a "pretty" ull description for the {@link WorkItemLifecyclePhase}
   */
  public static String getLifecyclePhaseDescription( final WorkItemLifecyclePhase lifecyclePhase ) {
    return Messages.getInstance().getString( lifecyclePhase.getDescriptionMessageKey() );
  }
}
