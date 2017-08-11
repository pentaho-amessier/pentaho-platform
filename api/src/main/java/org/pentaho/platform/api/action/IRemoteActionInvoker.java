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
package org.pentaho.platform.api.action;

/**
 * A remote action invocation
 */
public interface IRemoteActionInvoker extends IActionInvoker {

  /**
   * Returns the authentication scheme to be used to log into the remote host (if any).
   * @return {@link String}
   */
  String getAuthenticationScheme();

  /**
   * @return {@link String} username for worker node security
   */
  String getUserName();

  /**
   * @return {@link String} password for worker node user
   */
  String getPassword();

  /**
   * @return hostname
   */
  String getHostname();

  /**
   * @return port
   */
  Integer getPort();

  /**
   * @return endpoint
   */
  String getEndpoint();

  /**
   * @return true if connection is made via https
   */
  boolean isHttps();

}
