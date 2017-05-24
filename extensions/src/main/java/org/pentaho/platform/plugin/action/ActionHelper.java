package org.pentaho.platform.plugin.action;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by amessier on 5/24/2017.
 */
public class ActionHelper {

  private static final Log logger = LogFactory.getLog( ActionHelper.class );

  public static final String INVOKER_ACTIONCLASS = "actionClass"; //$NON-NLS-1$

  public static final String INVOKER_ACTIONUSER = "actionUser"; //$NON-NLS-1$

  public static final String INVOKER_ACTIONID = "actionId"; //$NON-NLS-1$

  public static final String INVOKER_STREAMPROVIDER = "streamProvider"; //$NON-NLS-1$
  public static final String INVOKER_STREAMPROVIDER_INPUT_FILE = "inputFile"; //$NON-NLS-
  public static final String INVOKER_STREAMPROVIDER_OUTPUT_DIR = "outputDirectory"; //$
  public static final String INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME = "uniqueFileName"; //$NON-NLS-1$

  public static final String INVOKER_RESTART_FLAG = "restart"; //$NON-NLS-1$

  private static final long RETRY_COUNT = 6;
  private static final long RETRY_SLEEP_AMOUNT = 10000;

  static Class<?> resolveClass( final String actionClassName, final String beanId  ) throws
    PluginBeanException, IllegalArgumentException {

    Class<?> clazz = null;

    if ( StringUtils.isEmpty( beanId ) && StringUtils.isEmpty( actionClassName ) ) {
      throw new IllegalArgumentException( Messages.getInstance().getErrorString(
        "ActionInvoker.ERROR_0001_REQUIRED_PARAM_MISSING", //$NON-NLS-1$
        INVOKER_ACTIONCLASS, INVOKER_ACTIONID ) );
    }

    for ( int i = 0; i < RETRY_COUNT; i++ ) {
      try {
        if ( !StringUtils.isEmpty( beanId ) ) {
          IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
          clazz = pluginManager.loadClass( beanId );
          return clazz;
        } else if ( !StringUtils.isEmpty( actionClassName ) ) {
          clazz = Class.forName( actionClassName );
          return clazz;
        }
      } catch ( Throwable t ) {
        try {
          Thread.sleep( RETRY_SLEEP_AMOUNT );
        } catch ( InterruptedException ie ) {
          logger.info( ie.getMessage(), ie );
        }
      }
    }

    // we have failed to locate the class for the actionClass
    // and we're giving up waiting for it to become available/registered
    // which can typically happen at system startup
    throw new IllegalArgumentException( Messages.getInstance().getErrorString(
      "ActionInvoker.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
      StringUtils.isEmpty( beanId ) ? actionClassName : beanId ) );
  }

  public static IAction createActionBean( final String actionClassName, final String actionId ) throws ActionInvocationException{
    Object actionBean;
    Class<?> actionClass = null;
    try {
      actionClass = resolveClass( actionClassName, actionId );
      actionBean = actionClass.newInstance();
    } catch ( Exception e ) {
      throw new ActionInvocationException( Messages.getInstance().getErrorString(
        "ActionInvoker.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
        ( actionClass == null ) ? "unknown" : actionClass.getName() ), e ); //$NON-NLS-1$
    }

    if ( !( actionBean instanceof IAction ) ) {
      throw new ActionInvocationException( Messages.getInstance().getErrorString(
        "ActionInvoker.ERROR_0003_ACTION_WRONG_TYPE", actionClass.getName(), //$NON-NLS-1$
        IAction.class.getName() ) );
    }
    return (IAction)actionBean;
  }

  /**
   * Gets the stream provider from the INVOKER_STREAMPROVIDER, or builds it from the input file and output dir
   * @param params
   * @return
   */
  public static IBackgroundExecutionStreamProvider getStreamProvider( final Map<String, Serializable> params ) {

    IBackgroundExecutionStreamProvider streamProvider = null;

    Object objsp = params.get( INVOKER_STREAMPROVIDER );
    if ( objsp != null && IBackgroundExecutionStreamProvider.class.isAssignableFrom( objsp.getClass() ) ) {
      streamProvider = (IBackgroundExecutionStreamProvider) objsp;
      // TODO: fetch input and output params and put in map
      if (streamProvider instanceof RepositoryFileStreamProvider) {
        params.put( INVOKER_STREAMPROVIDER_INPUT_FILE, ( (RepositoryFileStreamProvider) streamProvider ).inputFilePath );
        // TODO: the outputPath needs to have the file name removed - we need the name of the folder containing it
        params.put( INVOKER_STREAMPROVIDER_OUTPUT_DIR, ( (RepositoryFileStreamProvider) streamProvider ).getOutputFilePath()  );
        params.put( INVOKER_STREAMPROVIDER_UNIQUE_FILE_NAME, ( (RepositoryFileStreamProvider) streamProvider )
          .autoCreateUniqueFilename() );
      }
    } else {
      String inputFile = params.get( INVOKER_STREAMPROVIDER_INPUT_FILE ) == null ? null : params.get( INVOKER_STREAMPROVIDER_INPUT_FILE ).toString();
      String outputDir = params.get( INVOKER_STREAMPROVIDER_OUTPUT_DIR ) == null ? null : params.get( INVOKER_STREAMPROVIDER_OUTPUT_DIR ).toString();
      // check for nulls
      boolean hasInputFile = !StringUtils.isEmpty( inputFile );
      if ( hasInputFile ) {
        ///home/admin/Buyer Report (sparkline report).*
        final String inputFileName = FilenameUtils.getBaseName( new File( inputFile ).getName() );
        final String outputFile = outputDir + inputFileName + ".*";

        boolean autoCreateUniqueFilename = params.get( "autoCreateUniqueFilename" ) == null || params.get(
          "autoCreateUniqueFilename" ).toString().equalsIgnoreCase( "true" );
        streamProvider = new RepositoryFileStreamProvider( inputFile, outputFile, autoCreateUniqueFilename );
        // put in the map for future lookup
        params.put( INVOKER_STREAMPROVIDER, streamProvider ); // TODO: is this necessary?
      }
    }

    return streamProvider;
  }

}
