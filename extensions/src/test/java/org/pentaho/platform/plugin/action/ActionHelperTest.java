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

package org.pentaho.platform.plugin.action;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.web.http.api.resources.RepositoryFileStreamProvider;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@RunWith( PowerMockRunner.class )
@PrepareForTest( PentahoSystem.class )
public class ActionHelperTest {

  private Map<String, Integer> testMap;
  RepositoryFile repositoryFile;

  @Before public void initialize() {
    testMap = new HashMap<>();
    testMap.put( "one", 1 );
    testMap.put( "two", 2 );

    repositoryFile = new RepositoryFile( 12, "string", false, Boolean.FALSE, Boolean.FALSE, false, 12,
      "path", null, null, false, "aOwner", "lockMessage", null, "EN-US", "title", "description",
      "originalParentFolderPath", null, 12345L, "creatorId", null );
  }

  @Test
  public void getStreamProviderNullTest() {
    Map<String, Serializable> paramMap = new HashMap<>();
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, null );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = ActionHelper.getStreamProvider( paramMap );
    Assert.assertNull( iBackgroundExecutionStreamProvider );
  }

  @Test
  public void getStreamProviderNullWithInputFileTest() throws IOException {
    Map<String, Serializable> paramMap = new HashMap<>();
    File inputFile = new File( "example.txt" );
    BufferedWriter output = new BufferedWriter( new FileWriter( inputFile ) );
    output.write( "TEST TEXT" );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, null );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, inputFile );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = ActionHelper.getStreamProvider( paramMap );
    Assert.assertNull( iBackgroundExecutionStreamProvider );
  }

  @Test
  public void getStreamProviderWithInputAndOutputFileTest() throws IOException {
    Map<String, Serializable> paramMap = new HashMap<>();
    RepositoryFileStreamProvider repositoryFileStreamProvider = new RepositoryFileStreamProvider();
    File inputFile = new File( "example.txt" );
    BufferedWriter output = new BufferedWriter( new FileWriter( inputFile ) );
    output.write( "TEST TEXT" );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, repositoryFileStreamProvider );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_INPUT_FILE, inputFile );
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER_OUTPUT_FILE_PATTERN, inputFile );
    paramMap.put( "autoCreateUniqueFilename", true );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = ActionHelper.getStreamProvider( paramMap );
    Assert.assertEquals( iBackgroundExecutionStreamProvider, repositoryFileStreamProvider );
  }


  @Test
  public void getStreamProviderTest() {
    Map<String, Serializable> paramMap = new HashMap<>();
    RepositoryFileStreamProvider repositoryFileStreamProvider = new RepositoryFileStreamProvider();
    paramMap.put( ActionUtil.INVOKER_STREAMPROVIDER, repositoryFileStreamProvider );
    IBackgroundExecutionStreamProvider iBackgroundExecutionStreamProvider = ActionHelper.getStreamProvider( paramMap );
    Assert.assertEquals( repositoryFileStreamProvider, iBackgroundExecutionStreamProvider );
  }
}