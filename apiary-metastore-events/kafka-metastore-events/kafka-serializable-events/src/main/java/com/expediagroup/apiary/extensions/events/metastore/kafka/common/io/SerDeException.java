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
package com.expediagroup.apiary.extensions.events.metastore.kafka.common.io;

import com.expediagroup.apiary.extensions.events.metastore.kafka.common.KafkaMetaStoreEventsException;

public class SerDeException extends KafkaMetaStoreEventsException {
  private static final long serialVersionUID = 1L;

  public SerDeException(String message) {
    super(message);
  }

  public SerDeException(String message, Throwable cause) {
    super(message, cause);
  }

  public SerDeException(Throwable cause) {
    super(cause);
  }

}