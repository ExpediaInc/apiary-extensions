/**
 * Copyright (C) 2018-2019 Expedia Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.apiary.extensions.receiver.common.event;

public class DropTableEvent extends ListenerEvent {
  private static final long serialVersionUID = 1L;

  private String protocolVersion;
  private String dbName;
  private String tableName;
  private String tableLocation;

  DropTableEvent() {}

  public String getProtocolVersion() {
    return protocolVersion;
  }

  @Override
  public String getDbName() {
    return dbName;
  }

  @Override
  public String getTableName() {
    return tableName;
  }

  public String getTableLocation() {
    return tableLocation;
  }
}
