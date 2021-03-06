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
package com.expediagroup.apiary.extensions.events.metastore.io;

import com.expediagroup.apiary.extensions.events.metastore.common.MetaStoreEventsException;
import com.expediagroup.apiary.extensions.events.metastore.event.ApiaryListenerEvent;

public interface MetaStoreEventSerDe {

  static <T extends MetaStoreEventSerDe> T serDeForClassName(String className) {
    try {
      Class<T> clazz = (Class<T>) Class.forName(className);
      return clazz.newInstance();
    } catch (Exception e) {
      throw new MetaStoreEventsException("Unable to instantiate MetaStoreEventSerDe of class " + className, e);
    }
  }

  byte[] marshal(ApiaryListenerEvent listenerEvent) throws SerDeException;

  <T extends ApiaryListenerEvent> T unmarshal(byte[] payload) throws SerDeException;

}
