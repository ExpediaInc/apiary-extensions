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
package com.expediagroup.apiary.extensions.events.metastore.io.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TFieldIdEnum;
import org.junit.Test;

public class ThriftSerDeUtilsTest {

  @Test
  public void returnFields() {
    TFieldIdEnum[] fields = ThriftSerDeUtils.fields(Table.class);
    assertThat(fields).isNotNull().isEqualTo(Table._Fields.values());
  }

  @Test
  public void returnNull() {
    assertThat(ThriftSerDeUtils.fields(InvalidTBase.class)).isNull();
  }

}
