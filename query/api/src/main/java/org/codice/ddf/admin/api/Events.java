/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.admin.api;

/**
 * OSGi events related to the GraphQL schema.
 */
public class Events {

  /**
   * Event key for refreshing the GraphQL schema.
   */
  public static final String REFRESH_SCHEMA = "graphql/servlet/REFRESH_SCHEMA";

  /**
   * Key used to extract reason for event. Typically used for logging.
   */
  public static final String EVENT_REASON = "EVENT_REASON";

  private Events() {}
}
