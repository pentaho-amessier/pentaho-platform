package org.pentaho.platform.workitem;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

public class WorkItemLifecycleEventPublisher implements IWorkItemLifecycleEventPublisher {

  public WorkItemLifecycleEventPublisher() {
    int dummy = 0;
  }

  private ApplicationEventPublisher publisher = null;

  @Override
  public void setApplicationEventPublisher( final ApplicationEventPublisher publisher ) {
    this.publisher = publisher;
  }

  public ApplicationEventPublisher getApplicationEventPublisher() {
    return this.publisher;
  }

  /**
   * A convenience method for publishing changes to the work item's lifecycles. Fetches the available
   * {@link ApplicationEventPublisher}, and if available, calls its
   * {@link ApplicationEventPublisher#publishEvent( ApplicationEvent )} method. Otherwise does nothing, as the
   * {@link ApplicationEventPublisher} may not be available, which is a perfectly valid scenario, if we do not care
   * about publishing {@link WorkItemLifecycleEvent}'s.
   *
   * @param workItemLifecycleEvent the {@link WorkItemLifecycleEvent}
   */
  public void publishEvent( final WorkItemLifecycleEvent workItemLifecycleEvent ) {
    if ( getApplicationEventPublisher() != null ) {
      getApplicationEventPublisher().publishEvent( workItemLifecycleEvent );
    }
  }

}
