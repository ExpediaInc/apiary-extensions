/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.apiary.extensions.events.metastore.event;

import java.util.Objects;

import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.events.DropTableEvent;

public class ApiaryDropTableEvent extends ApiaryListenerEvent {
  private static final long serialVersionUID = 1L;

  private Table table;
  private boolean deleteData;

  ApiaryDropTableEvent() {}

  public ApiaryDropTableEvent(DropTableEvent event) {
    super(event);
    deleteData = event.getDeleteData();
    table = event.getTable();
  }

  @Override
  public String getDatabaseName() {
    return table.getDbName();
  }

  @Override
  public String getTableName() {
    return table.getTableName();
  }

  public Table getTable() {
    return table;
  }

  public boolean getDeleteData() {
    return deleteData;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ApiaryDropTableEvent)) {
      return false;
    }
    ApiaryDropTableEvent other = (ApiaryDropTableEvent) obj;
    return super.equals(other) && Objects.equals(deleteData, other.deleteData) && Objects.equals(table, other.table);
  }

}
