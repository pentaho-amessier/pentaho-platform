package org.pentaho.platform.plugin.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.pentaho.platform.api.action.IAction;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a POJO representing the parameters coming in to the ActionResource.runInBackground()
 * endpoint. The incoming json fromatted params should be converted and used in the runInBackground method
 * singnature (TODO: investigate using the jax rs entity body reader customization for this).
 *
 * This class provides logic to serialize and de-serialize the action parameters.
 * TODO: in the future investigate replacing this class with generic platform API if any.
 */
public class ActionParams {

  /**
   * The serialized params are encapsulated in a serialized json by jackson fastxml library.
   * (which provide a generic way to encapsulate a map of <String,Serializable>
   */
  private String serializedParams;

  /**
   * Some of the action parameters may not be serializable or be meaningless to pass around.
   * For this the name of those are captured so that they could be recreated as needed
   * on the other side.
   */
  private List<String> paramsToRecreate;

  public String getSerializedParams() {
    return serializedParams;
  }

  public void setSerializedParams( String serializedParams ) {
    this.serializedParams = serializedParams;
  }

  public List<String> getParamsToRecreate() {
    return paramsToRecreate;
  }

  public void setParamsToRecreate( final List<String> paramsToRecreate ) {
    this.paramsToRecreate = paramsToRecreate;
  }

  /**
   * Empty constructor, required for jackson library to work properly
   */
  public ActionParams() {
  }

  /**
   * Private constructor used by the class only.
   *
   * @param serializedParams the params that were serialized in the action request.
   * @param unserializedParams the params that were not serialized and potentially need
   *                           to be recreated to execute the action.
   */
  private ActionParams( final String serializedParams, List<String> unserializedParams ) {
    this.serializedParams = serializedParams;
    this.paramsToRecreate = unserializedParams;
  }

  /**
   * Serialize an action parameters.
   *
   * @param action the action.
   * @param params the parameters
   * @return an ActionParams instance.
   *
   * @throws ActionInvocationException when serialization fails for any reason.
   */
  public static ActionParams serialize( final IAction action, final Map<String, Serializable> params ) throws ActionInvocationException {

    // since we end up filtering the un-serialized params, use a clone
    //
    final Map<String, Serializable> clonedParams = new HashMap<>( params );
    final List<String> paramsToRecreate = filter( action, clonedParams );

    try {
      return new ActionParams( serializeParams( clonedParams ), paramsToRecreate );
    } catch ( final JsonProcessingException ex ) {
      throw new ActionInvocationException( "Failed to serialize action params", ex );
    }
  }

  /**
   * De-serialize ActionParams. That is, convert them back to a map as the code down stream expects.
   *
   * @param action the action these params belong too.
   * @param params the ActionParams instance to deserialize.
   *
   * @return a map of parameters, with String keys and values that are all serializable.
   *
   * @throws ActionInvocationException if anything goes wrong.
   */
  public static Map<String, Serializable> deserialize( final IAction action, final ActionParams params ) throws ActionInvocationException {
    try {
      Map<String, Serializable> res = deserializeParams( params.getSerializedParams() );
      // at this point recreate the unserialized parameters, as needed
      //
      recreate( params.getParamsToRecreate(), res );
      return res;
    } catch ( final Exception ex ) {
      // TODO: localize
      throw new ActionInvocationException( "Action Parameters could not be deserialized." );
    }
  }

 /**
  * A conversion to a json string form an ActionParams instance.
  *
  * @param params the instance to convert from.
  * @return a json formatted String.
  * @throws JsonProcessingException if the conversion fails.
  */
  public static String toJson( final ActionParams params ) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString( params );
  }

  /**
   * A conversion from a json string to an ActionParams instance.
   *
   * @param params the json string to convert from.
   * @return an ActionParams instance.
   * @throws IOException on a json string parsing error
   */
  public static ActionParams fromJson( final String params ) throws IOException {
    return new ObjectMapper().readValue( params, ActionParams.class );
  }

  /**
   * Recreates the un-serializable params of an action. A inverse sibling of filter().
   *
   * @param paramsToRecreate a list with the names of the un-serializable parameters.
   * @param res the current map parameters where the un-serialized parameters will be recreated and
   *            added to.
   */
  private static void recreate( final List<String> paramsToRecreate, final Map<String, Serializable> res ) {
    //TODO: for now only the stream provider needs to be recreated, but that happens implicitly
    //during invocation - so no code is added here at this time.
  }

  /**
   * Removes un-serializable action parameters from the parameters map. An inverse sibling of the {@link recreate() }
   *
   * @param action the action that has these parameters.
   * @param params the parameter map to filter un-serialized parameters from.
   * @return A list holding the key names of the parameters that were filtered.
   */
  private static List<String> filter( final IAction action, Map<String, Serializable> params ) {
    List<String> res = new ArrayList<>();
    for ( final String name : params.keySet() ) {
      if ( name.equals( ActionHelper.INVOKER_STREAMPROVIDER ) ) {
        res.add( name );
      }
    }

    for ( final String paramName : res ) {
      params.remove( paramName );
    }

    return res;
  }

    /**
     * Convenience method for serializing a params map.
     *
     * @param params the parameter map.
     * @return the serialized representation of the map.
     * @throws JsonProcessingException if anything goes wrong.
     */
  private static String serializeParams( final Map<String, Serializable> params ) throws JsonProcessingException {
    final ObjectMapper mapper = new ObjectMapper();
    // this mapper configuration is required to enable de-serialization.
    // the mapper object will inject type info into the json for this purpose.
    //
    mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
    mapper.enableDefaultTyping( ObjectMapper.DefaultTyping.NON_FINAL );
    return mapper.writeValueAsString( params );
  }

  /**
   * Convenience method for de-serialize a params map that was serialize with serializeParams() method.
   *
   * @param serializedParams the parameter map.
   * @return the serialized representation of the map.
   * @throws JsonProcessingException if anything goes worng.
   */
  private static Map<String, Serializable> deserializeParams( final String serializedParams ) throws IOException {
    final ObjectMapper mapper = new ObjectMapper();

    mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
    mapper.enableDefaultTyping( ObjectMapper.DefaultTyping.NON_FINAL );
    return mapper.readValue( serializedParams, HashMap.class );
  }

  @Override
  public boolean equals( final Object other ) {
    if ( other == null || (! ( other instanceof ActionParams ) ) ) {
      return false;
    } else if ( this == other ) {
      return true;
    } else {
      return serializedParams.equals( ( (ActionParams) other ).serializedParams ) &&
        paramsToRecreate.equals( ( (ActionParams) other ).paramsToRecreate );
    }
  }

}
