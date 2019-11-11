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
package com.expediagroup.apiary.extensions.events.metastore.common.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.expediagroup.apiary.extensions.events.metastore.common.metrics.HiveMetricsHelper;
import com.expediagroup.apiary.extensions.events.metastore.common.metrics.MetricsConstant;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HiveMetricsHelper.class)
public class ListenerUtilsTest {

  private @Mock AppenderSkeleton appender;
  private @Captor ArgumentCaptor<LoggingEvent> logCaptor;

  @Before
  public void init() {
    mockStatic(HiveMetricsHelper.class);
    Logger.getRootLogger().addAppender(appender);
  }

  @Test
  public void success() {
    ListenerUtils.success();
    verifyStatic(HiveMetricsHelper.class);
    HiveMetricsHelper.incrementCounter(MetricsConstant.LISTENER_SUCCESSES);
  }

  @Test
  public void error() {
    Exception e = new RuntimeException("ABC");
    ListenerUtils.error(e);
    verifyStatic(HiveMetricsHelper.class);
    HiveMetricsHelper.incrementCounter(MetricsConstant.LISTENER_FAILURES);
    // We want to make sure these keywords are logged
    verify(appender).doAppend(logCaptor.capture());
    assertThat(logCaptor.getValue().getRenderedMessage()).startsWith("Error in Kafka Listener");
    assertThat(logCaptor.getValue().getThrowableInformation().getThrowable()).isEqualTo(e);
  }

}
