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

package org.pentaho.platform.core.workitem;

/**
 * The public interface for publishing {@link WorkItem} {@link WorkItem.LifecyclePhase} changes.
 */
public interface IWorkItemLifecycleEventPublisher {


  /**
   * Publishes the change to the {@link WorkItem}'s {@link WorkItem.LifecyclePhase}.
   *
   * @param workItem the {@link WorkItem}
   * @param phase    the {@link WorkItem.LifecyclePhase}
   */
  void publish( final WorkItem workItem, final WorkItem.LifecyclePhase phase );

  /**
   * Publishes the change to the {@link WorkItem}'s {@link WorkItem.LifecyclePhase}.
   *
   * @param workItem the {@link WorkItem}
   * @param phase    the {@link WorkItem.LifecyclePhase}
   * @param details  any details associated with the state change (example: error message, exception stack trace)
   */
  void publish( final WorkItem workItem, final WorkItem.LifecyclePhase phase, final String details );
}
