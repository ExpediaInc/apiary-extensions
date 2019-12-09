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
package com.expediagroup.apiary.extensions.events.metastore.kafka.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expediagroup.apiary.extensions.events.metastore.kafka.metrics.HiveMetricsHelper;
import com.expediagroup.apiary.extensions.events.metastore.kafka.metrics.MetricsConstant;

public final class ListenerUtils {
  private static final Logger log = LoggerFactory.getLogger(ListenerUtils.class);

  private ListenerUtils() {}

  public static void success() {
    HiveMetricsHelper.incrementCounter(MetricsConstant.LISTENER_SUCCESSES);
  }

  public static void error(Exception e) {
    log.error("Error in Kafka Listener", e);
    HiveMetricsHelper.incrementCounter(MetricsConstant.LISTENER_FAILURES);
  }

}
