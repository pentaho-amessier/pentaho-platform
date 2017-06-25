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
package org.pentaho.platform.web.http.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.workitem.WorkItemLifecycleRecord;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class RequestIdFilter implements Filter {

  private static final Log logger = LogFactory.getLog( RequestIdFilter.class );

  public void destroy() {
  }

  public void doFilter( ServletRequest req, ServletResponse resp, FilterChain chain )
    throws ServletException, IOException {

    HttpServletRequest request = (HttpServletRequest) req;
    String requestId = Optional.ofNullable( request.getHeader( ActionUtil.X_REQUEST_ID ) ).orElse(
      WorkItemLifecycleRecord
        .generateWorkItemId() );

    try {

      if ( logger.isDebugEnabled() ) {
        logger.debug( "received request with request id of: " + requestId );
      }
      MDC.put( ActionUtil.REQUEST_ID, requestId );

      chain.doFilter( req, resp );

    } finally {

      if ( logger.isDebugEnabled() ) {
        logger.debug( "Exiting request with request id of: " + requestId );
      }
      ( (HttpServletResponse) resp ).setHeader( ActionUtil.X_REQUEST_ID, requestId );
      MDC.remove( ActionUtil.X_REQUEST_ID );
    }
  }

  public void init( FilterConfig config ) throws ServletException {

  }

}
