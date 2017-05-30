package org.pentaho.platform.plugin.action;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;
import org.pentaho.reporting.platform.plugin.SimpleReportingAction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;

/**
 * Created by kbryant on 5/25/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PentahoSystem.class)
public class ActionHelperTest {

    private Map<String, Integer> testMap;
    RepositoryFile repositoryFile;

    @Before public void initialize() {
        testMap = new HashMap<>();
        testMap.put("one", 1);
        testMap.put("two", 2);

        repositoryFile = new RepositoryFile(12, "string", false, Boolean.FALSE, Boolean.FALSE, false, 12,
                "path", null, null, false, "aOwner", "lockMessage", null, "EN-US", "title", "description",
                "originalParentFolderPath", null, 12345L, "creatorId", null);
    }

    // TODO: Once locales are resolved, enable a test similar to this
    @Test(expected=ActionInvocationException.class)
    public void resolveClassIllegalArgumentExceptionWithEmptyStrings() throws PluginBeanException, ActionInvocationException {
        ActionHelper.resolveClass("", "");
    }

    @Test(expected=ActionInvocationException.class)
    public void resolveClassIllegalArgumentExceptionWithNulls() throws PluginBeanException, ActionInvocationException {
        ActionHelper.resolveClass(null, null);
    }

    @Test(expected=ActionInvocationException.class)
    public void createActionBeanIllegalArgumentExceptionWithEmptyStrings() throws PluginBeanException, ActionInvocationException {
        ActionHelper.createActionBean("", "");
    }

    @Test(expected=ActionInvocationException.class)
    public void createActionBeanIllegalArgumentExceptionWithNulls() throws PluginBeanException, ActionInvocationException {
        ActionHelper.createActionBean(null, null);
    }

    @Test
    public void resolveClassTestHappyPathNoBeanID() throws Exception {
        Class<?> aClass = ActionHelper.resolveClass(
                "org.pentaho.reporting.platform.plugin.SimpleReportingAction",
                "");
        Assert.assertEquals(SimpleReportingAction.class, aClass);
    }

    @Test
    public void resolveClassTestHappyPath() throws Exception {
        String beanId = "ktr.backgroundAction";
        Class<?> clazz = SimpleReportingAction.class;

        IPluginManager pluginManager = mock(IPluginManager.class);
        PowerMockito.mockStatic(PentahoSystem.class);
        BDDMockito.given(PentahoSystem.get( IPluginManager.class )).willReturn(pluginManager);

        Mockito.doReturn(clazz).when(pluginManager).loadClass(anyString());

        Class<?> aClass = ActionHelper.resolveClass(
                "org.pentaho.reporting.platform.plugin.SimpleReportingAction",
                beanId);

        Assert.assertEquals(SimpleReportingAction.class, aClass);
    }

    @Test
    public void createActionBeanHappyPath() throws ActionInvocationException {
        IAction iaction = ActionHelper.createActionBean("org.pentaho.reporting.platform.plugin.SimpleReportingAction", null);
        Assert.assertNotNull(iaction);
    }

    @Test
    public void sendEmailTestEmailNotConfigured() {
        Map<String, Serializable> metadata = new HashMap<>();
        Map<String, Serializable> params = new HashMap<>();
        params.put(QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID, "lineageID");

        IUnifiedRepository repo = mock(IUnifiedRepository.class);
        PowerMockito.mockStatic(PentahoSystem.class);
        BDDMockito.given(PentahoSystem.get( IUnifiedRepository.class )).willReturn(repo);

        Mockito.doReturn(repositoryFile).when(repo).getFile(anyString());
        Mockito.doReturn(metadata).when(repo).getFileMetadata(repositoryFile.getId());

        ActionHelper.sendEmail(null, params, "filePath");
    }

    @Test
    public void getStreamProviderNullTest() {
        Map<String, Serializable> paramMap = new HashMap<>();
        paramMap.put(ActionHelper.INVOKER_STREAMPROVIDER, null);
        IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = ActionHelper.getStreamProvider(paramMap);
        Assert.assertNull(iBackgroundExecutionStreamProvider);
    }

    @Test
    public void getStreamProviderNullWithInputFileTest() throws IOException {
        Map<String, Serializable> paramMap = new HashMap<>();
        File inputFile = new File("example.txt");
        BufferedWriter output = new BufferedWriter(new FileWriter(inputFile));
        output.write("TEST TEXT");
        paramMap.put(ActionHelper.INVOKER_STREAMPROVIDER, null);
        paramMap.put(ActionHelper.INVOKER_STREAMPROVIDER_INPUT_FILE, inputFile);
        IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = ActionHelper.getStreamProvider(paramMap);
        Assert.assertNull(iBackgroundExecutionStreamProvider);
    }

    @Test
    public void getStreamProviderTest() {
        Map<String, Serializable> paramMap = new HashMap<>();
        paramMap.put(ActionHelper.INVOKER_STREAMPROVIDER, new RepositoryFileStreamProvider());
        IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = ActionHelper.getStreamProvider(paramMap);
        Assert.assertNotNull(iBackgroundExecutionStreamProvider);
    }

}