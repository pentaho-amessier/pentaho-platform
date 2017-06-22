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

package org.pentaho.platform.api.workitem;

import java.util.Date;

/**
 * The public interface for publishing {@link IWorkItem} {@link WorkItemLifecyclePhase} changes.
 */
public interface IWorkItemLifecycleEventPublisher {

  /**
   * Publishes the change to the {@link IWorkItem}'s {@link WorkItemLifecyclePhase}.
   *
   * @param workItem        the {@link IWorkItem}
   * @param phase           the {@link WorkItemLifecyclePhase}
   * @param details         any details associated with the state change (example: error message, exception stack
   *                        trace); this field can be null
   * @param sourceTimestamp the time the event was triggered by the caller; this parameter can be null and will be set
   *                        to the current {@link Date}, but can also be set explicitly, in cases where the original
   *                        event may have been generated on another host and propagated via http or some other
   *                        mechanism.
   */
  void publish( final IWorkItem workItem, final WorkItemLifecyclePhase phase, final String details,
                final Date sourceTimestamp );
}
