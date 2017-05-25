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

package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.ActionHelper;

import javax.ws.rs.Consumes;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path( "/action" )
public class ActionResource {
  protected static final Log logger = LogFactory.getLog( ActionResource.class );


  @HEAD
  @Path ( "/runInBackground" )
  public boolean runInBackground() {
    return true;
  }

  @POST
  @Path ( "/runInBackground" )
  @Consumes( { APPLICATION_JSON } )
  @Produces( { APPLICATION_JSON } )
  @StatusCodes(
    {
      @ResponseCode( code = 200, condition = "Action invoked successfully." ),
      @ResponseCode( code = 400, condition = "Bad input - could not invoke action." ),
      @ResponseCode( code = 401, condition = "User does not have permissions to  invoke action" ),
      @ResponseCode( code = 500, condition = "Error while retrieving system resources." ),
    }
  )
  public String runInBackground( final String content ) {
    // TODO: to achieve asynchronous execution of the endpoint, execute the contents of this method in a new thread
    // and return immediately
    Map<String, Serializable> paramMap = null;
    try {
      paramMap = ActionHelper.jsonToObject( content, Map.class );
    } catch ( final IOException e ) {
      // TODO log
    }

    final IActionInvoker actionInvoker = PentahoSystem.get( IActionInvoker.class, "IActionInvoker", PentahoSessionHolder
      .getSession() );

    IActionInvokeStatus status = null;
    try {
      status = actionInvoker.runInBackgroundLocally( paramMap );
    } catch ( final Exception e ) {
      // TODO log
    }

    return ActionHelper.objectToJson( status );
  }
}
