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

package org.pentaho.platform.workitem;

import org.pentaho.platform.api.workitem.IWorkItemLifecycleEventPublisher;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkItemLifecycleEventPublisher implements IWorkItemLifecycleEventPublisher {

  @Autowired
  private ApplicationEventPublisher publisher = null;

  protected void setApplicationEventPublisher( final ApplicationEventPublisher publisher ) {
    this.publisher = publisher;
  }

  /**
   * {@inheritDoc}
   */
  public void publish( final IWorkItemLifecycleRecord workItemLifecycleRecord ) {
    publisher.publishEvent( createEvent( workItemLifecycleRecord ) );
  }

  protected WorkItemLifecycleEvent createEvent( final IWorkItemLifecycleRecord workItemLifecycleRecord ) {
    return new WorkItemLifecycleEvent( workItemLifecycleRecord );
  }
}