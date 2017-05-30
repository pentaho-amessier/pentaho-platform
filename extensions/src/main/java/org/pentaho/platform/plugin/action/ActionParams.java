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

public class ActionParams {
    private String serializedParams;
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
    public void setParamsToRecreate(final List<String> paramsToRecreate) {
        this.paramsToRecreate = paramsToRecreate;
    }

    public ActionParams() {
    }

    private ActionParams( final String serializedParams, List<String> unserializedParams ) {
      this.serializedParams = serializedParams;
      this.paramsToRecreate = unserializedParams;
    }

    public static ActionParams serialize( final IAction action, final Map<String, Serializable> params ) throws ActionInvocationException {
        final Map<String, Serializable> clonedParams = new HashMap<>( params );
        final List<String> paramsToRecreate = filter( action, clonedParams );

        try {
            return new ActionParams( serializeParams( clonedParams ), paramsToRecreate );
        } catch ( final JsonProcessingException ex ) {
            throw new ActionInvocationException( "Failed to serialize action params", ex );
        }
    }

    public static Map<String, Serializable> deserialize( final IAction action, final ActionParams params ) throws ActionInvocationException {
        try {
            Map<String, Serializable> res = deserializeParams( params.getSerializedParams() );
            recreate( params.getParamsToRecreate(), res );
            return res;
        } catch ( final Exception ex ) {
            // TODO: localize
            throw new ActionInvocationException( "Action Parameters could not be deserialized." );
        }
    }

    public static String toJson( final ActionParams params ) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString( params );
    }

    public static ActionParams fromJson( final String params ) throws IOException {
        return new ObjectMapper().readValue( params, ActionParams.class );
    }

    private static void recreate( final List<String> paramsToRecreate, final Map<String, Serializable> res ) {
        //TODO: implements
    }

    private static List<String> filter( final IAction action, Map<String, Serializable> params ) {
        List<String> res = new ArrayList<>();
        for ( final String name : params.keySet() ) {
            if ( name.equals( "streamProvider" )) {
                res.add( name );
                params.remove( name );
            }
        }

        return res;
    }

    private static String serializeParams( final Map<String, Serializable> params ) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.configure(SerializationFeature.INDENT_OUTPUT,true);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper.writeValueAsString( params );
    }

    private static Map<String, Serializable> deserializeParams( final String serializedParams ) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.configure(SerializationFeature.INDENT_OUTPUT,true);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper.readValue( serializedParams, HashMap.class );
    }



}
