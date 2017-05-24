package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.DefaultActionInvoker;

import javax.ws.rs.Consumes;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by amessier on 5/22/2017.
 */
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
  @Consumes( { APPLICATION_JSON  } )
  @Produces( { APPLICATION_JSON } )
  @StatusCodes(
    {
      @ResponseCode( code = 200, condition = "Action invoked successfully." ),
      @ResponseCode( code = 400, condition = "Bad input - could not invoke action." ),
      @ResponseCode( code = 401, condition = "User does not have permissions to  invoke action" ),
      @ResponseCode( code = 500, condition = "Error while retrieving system resources." ),
    }
  )
  public IActionInvokeStatus runInBackground( final String content ) {
    Map<String, Serializable> paramMap = null;
    try {
      paramMap = DefaultActionInvoker.jsonToObject( content, Map.class );
      int value = 0;
    } catch ( final IOException e ) {
      // TODO log
      int dummy = 0;
    }

    final IActionInvoker actionInvoker = PentahoSystem.get( IActionInvoker.class, "IActionInvoker", PentahoSessionHolder
      .getSession() );

    IActionInvokeStatus status = null;
    try {
      //status = actionInvoker.runInBackground( request.getActionClass(), request.getActionId()
      //  , request.getActionUser(), paramMap );
      status = actionInvoker.runInBackgroundLocally( paramMap );
    } catch ( final Exception e ) {
      // TODO
    }

    return status;//new JSONObject( paramMap ).toString();
  }

  private static String serializeMap ( final Map<String, Serializable> params ) {
    String serializedMap = null;
    try {
      final FileOutputStream fos = new FileOutputStream("temp.ser");
      final ObjectOutputStream oos = new ObjectOutputStream( fos );
      oos.writeObject( params );
      oos.close();
      fos.close();
      serializedMap = FileUtils.readFileToString(  new File( "temp.ser" ) );

    } catch ( final Exception e ) {
      // TODO
      e.printStackTrace();
    }
    return serializedMap;
  }
  private static Map<String, Serializable> deserializeMap ( final String content ) {
    Map<String, Serializable> map = null;
    try {
      //final InputStream is = new ByteArrayInputStream(content.getBytes());
      //final ObjectInputStream ois = new ObjectInputStream( is );
      //map = (HashMap) fis.readObject();

      FileInputStream fis = new FileInputStream("temp.ser");
      ObjectInputStream ois = new ObjectInputStream(fis);
      map = (HashMap) ois.readObject();
      fis.close();
      ois.close();
    } catch ( final Exception e ) {
      // TODO
      e.printStackTrace();
    }
    return map;
  }


}
