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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Unit tests for the {@link org.pentaho.platform.util.StringUtil} class.
 */
public class StringUtilTest {

  @Test
  public void testTokenStringToArray() {

    String token = " "; //$NON-NLS-2$
    String tokenString = "This is a test to convert this string into an array"; //$NON-NLS-2$
    String[] array = StringUtil.tokenStringToArray( tokenString, token );
    StringBuilder buffer = new StringBuilder();

    for ( String element : array ) {
      buffer.append( element );
      buffer.append( token );
    }
    Assert.assertEquals( buffer.toString().trim(), tokenString );

    // when tokenizedString is null, the result should be null
    Assert.assertNull( StringUtil.tokenStringToArray( null, token ) );
    Assert.assertNull( StringUtil.tokenStringToArray( null, null ) );

    // when no token is provided, we should get back an array containing the entire input token as a single string
    tokenString = "some text"; //$NON-NLS-2$
    array = StringUtil.tokenStringToArray( tokenString, null );
    Assert.assertNotNull( array );
    Assert.assertEquals( 1, array.length );
    Assert.assertEquals( tokenString, array[ 0 ] );
  }

  @Test
  public void testDoesPathContainParentPathSegment() {

    String[] matchStrings = { "../bart/maggie.xml", //$NON-NLS-1$
      "/bart/..", //$NON-NLS-1$
      "/bart/../maggie/homer.xml", //$NON-NLS-1$
      "..//bart/maggie.xml", //$NON-NLS-1$
      "/bart//..", //$NON-NLS-1$
      "/bart//../maggie/homer.xml", //$NON-NLS-1$
      "/bart/judi" + '\0' + ".xml", //$NON-NLS-1$ //$NON-NLS-2$
      "/../", //$NON-NLS-1$
      "/..", //$NON-NLS-1$
      "../", //$NON-NLS-1$
      "..\\bart\\maggie.xml", //$NON-NLS-1$
      "\\bart\\..", //$NON-NLS-1$
      "\\bart\\" + '\0' + ".png", //$NON-NLS-1$ //$NON-NLS-2$
      "\\bart\\..\\maggie\\homer.xml", //$NON-NLS-1$
      ".." //$NON-NLS-1$
    };

    for ( String testString : matchStrings ) {
      boolean matches = StringUtil.doesPathContainParentPathSegment( testString );
      Assert.assertTrue( matches );
    }

    String[] noMatchStrings = { "I am clean", //$NON-NLS-1$
      "/go/not/parent.xml", //$NON-NLS-1$
      "\\go\\not\\parent.xml", //$NON-NLS-1$
      "should/not..not/match", //$NON-NLS-1$
      "should/not%0Dnot/match", //$NON-NLS-1$
      "..should/not/match.xml", //$NON-NLS-1$
      "should/not..", //$NON-NLS-1$
      "..." //$NON-NLS-1$
    };

    for ( String testString : noMatchStrings ) {
      boolean matches = StringUtil.doesPathContainParentPathSegment( testString );
      Assert.assertFalse( matches );
    }
  }

  @Test
  public void testMain() {
    try {
      StringUtil.main( null );
      Assert.assertTrue( true );
    } catch ( final Exception e ) {
      Assert.fail( "Exception occured: " + e );
    }
  }

  @Test
  public void testIsEmpty() {
    Assert.assertTrue( StringUtil.isEmpty( null ) );
    Assert.assertTrue( StringUtil.isEmpty( "" ) );
    Assert.assertFalse( StringUtil.isEmpty( " " ) );
    Assert.assertFalse( StringUtil.isEmpty( "foo" ) );
  }

  @Test
  public void testGetMapAsPrettyStringNoArgs() {
    // When the provided map is empty or null, the result should be an empty string
    Assert.assertEquals( "", StringUtil.getMapAsPrettyString( null ) );
    Assert.assertEquals( "", StringUtil.getMapAsPrettyString( new HashMap() ) );

    final Map testMap = getTestMap();
    final String output = StringUtil.getMapAsPrettyString( testMap );

    final String expectedOutput = getExpectedPrettyMapOutput( null, null );
    Assert.assertEquals( expectedOutput, output );
  }

  @Test
  public void testGetMapAsPrettyStringWithArgs() {
    final String kvSep = "foo";
    final String kvpSep = "foe";
    // When the provided map is empty or null, the result should be an empty string
    Assert.assertEquals( "", StringUtil.getMapAsPrettyString( null, kvSep, kvpSep ) );
    Assert.assertEquals( "", StringUtil.getMapAsPrettyString( new HashMap(), kvSep, kvpSep ) );

    final Map testMap = getTestMap();
    final String output = StringUtil.getMapAsPrettyString( testMap, kvSep, kvpSep  );

    final String expectedOutput = getExpectedPrettyMapOutput( kvSep, kvpSep );
    Assert.assertEquals( expectedOutput, output );
  }

  private String getExpectedPrettyMapOutput( String kvSep, String kvpSep ) {

    if ( kvSep == null ) {
      kvSep = StringUtil.KV_SEPARATOR;
    }
    if ( kvpSep == null ) {
      kvpSep = StringUtil.KVP_SEPARATOR;
    }

    final StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append( "[" ).append( System.getProperty( "line.separator" ) );
    expectedOutput.append( "   " ).append( kvSep ).append( kvpSep );
    expectedOutput.append( "   " ).append( "null" ).append( kvSep ).append( kvpSep );
    expectedOutput.append( "   " ).append( "Suzy").append( kvSep ).append( "null" ).append( kvpSep );
    expectedOutput.append( "   " ).append( "John").append( kvSep ).append( "Doe" ).append( kvpSep );
    expectedOutput.append( "   " ).append( "map").append( kvSep ).append( "{John=Doe, testObj=someVar:testVar}" )
      .append( kvpSep );
    expectedOutput.append( "   " ).append( "testObj2").append( kvSep ).append( "someVar:testVar2" );
    expectedOutput.append( System.getProperty( "line.separator" ) ).append( "]" );
    return expectedOutput.toString();
  }

  private static Map getTestMap() {

    final Map subMap = new TreeMap();
    subMap.put( "John", "Doe" );
    subMap.put( "testObj", new TestObject( "testVar" ) );

    final Map testMap = new HashMap();
    testMap.put( "", "" );
    testMap.put( null, "" );
    testMap.put( "Suzy", null );
    testMap.put( "John", "Doe" );
    testMap.put( "map", subMap );
    testMap.put( "testObj2", new TestObject( "testVar2" ) );
    return testMap;
  }

  static class TestObject {
    private String someVar;
    TestObject ( final String someVar ) {
      this.someVar = someVar;
    }
    public String toString () {
      return "someVar:" + this.someVar;
    }
  }
}
