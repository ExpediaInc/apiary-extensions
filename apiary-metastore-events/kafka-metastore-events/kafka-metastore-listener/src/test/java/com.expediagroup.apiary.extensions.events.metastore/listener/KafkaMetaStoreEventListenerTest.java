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
package com.expediagroup.apiary.extensions.events.metastore.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.events.AddIndexEvent;
import org.apache.hadoop.hive.metastore.events.AddPartitionEvent;
import org.apache.hadoop.hive.metastore.events.AlterIndexEvent;
import org.apache.hadoop.hive.metastore.events.AlterPartitionEvent;
import org.apache.hadoop.hive.metastore.events.AlterTableEvent;
import org.apache.hadoop.hive.metastore.events.ConfigChangeEvent;
import org.apache.hadoop.hive.metastore.events.CreateDatabaseEvent;
import org.apache.hadoop.hive.metastore.events.CreateFunctionEvent;
import org.apache.hadoop.hive.metastore.events.CreateTableEvent;
import org.apache.hadoop.hive.metastore.events.DropDatabaseEvent;
import org.apache.hadoop.hive.metastore.events.DropFunctionEvent;
import org.apache.hadoop.hive.metastore.events.DropIndexEvent;
import org.apache.hadoop.hive.metastore.events.DropPartitionEvent;
import org.apache.hadoop.hive.metastore.events.DropTableEvent;
import org.apache.hadoop.hive.metastore.events.InsertEvent;
import org.apache.hadoop.hive.metastore.events.LoadPartitionDoneEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.expediagroup.apiary.extensions.events.metastore.common.event.SerializableAddPartitionEvent;
import com.expediagroup.apiary.extensions.events.metastore.common.event.SerializableAlterPartitionEvent;
import com.expediagroup.apiary.extensions.events.metastore.common.event.SerializableAlterTableEvent;
import com.expediagroup.apiary.extensions.events.metastore.common.event.SerializableCreateTableEvent;
import com.expediagroup.apiary.extensions.events.metastore.common.event.SerializableDropPartitionEvent;
import com.expediagroup.apiary.extensions.events.metastore.common.event.SerializableDropTableEvent;
import com.expediagroup.apiary.extensions.events.metastore.common.event.SerializableInsertEvent;
import com.expediagroup.apiary.extensions.events.metastore.common.event.SerializableListenerEvent;
import com.expediagroup.apiary.extensions.events.metastore.common.event.SerializableListenerEventFactory;
import com.expediagroup.apiary.extensions.events.metastore.common.io.MetaStoreEventSerDe;
import com.expediagroup.apiary.extensions.events.metastore.common.messaging.Message;
import com.expediagroup.apiary.extensions.events.metastore.common.messaging.MessageTask;
import com.expediagroup.apiary.extensions.events.metastore.common.messaging.MessageTaskFactory;

@RunWith(MockitoJUnitRunner.class)
public class KafkaMetaStoreEventListenerTest {

  private static final String DATABASE = "db";
  private static final String TABLE = "tbl";
  private static final byte[] PAYLOAD = "payload".getBytes();

  private @Mock MetaStoreEventSerDe eventSerDe;
  private @Mock MessageTask messageTask;
  private @Mock MessageTaskFactory messageTaskFactory;
  private @Mock SerializableListenerEventFactory serializableListenerEventFactory;
  private @Mock ExecutorService executorService;

  private final Configuration config = new Configuration();
  private KafkaMetaStoreEventListener listener;

  @Before
  public void init() throws Exception {
    when(eventSerDe.marshal(any(SerializableListenerEvent.class))).thenReturn(PAYLOAD);
    when(messageTaskFactory.newTask(any(Message.class))).thenReturn(messageTask);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        ((Runnable) invocation.getArgument(0)).run();
        return null;
      }
    }).when(executorService).submit(any(Runnable.class));
    listener = new KafkaMetaStoreEventListener(config, serializableListenerEventFactory, eventSerDe, messageTaskFactory,
        executorService);
  }

  @Test
  public void onCreateTable() throws Exception {
    CreateTableEvent event = mock(CreateTableEvent.class);
    SerializableCreateTableEvent serializableEvent = mock(SerializableCreateTableEvent.class);
    when(serializableEvent.getDatabaseName()).thenReturn(DATABASE);
    when(serializableEvent.getTableName()).thenReturn(TABLE);
    when(serializableListenerEventFactory.create(event)).thenReturn(serializableEvent);
    listener.onCreateTable(event);
    verify(messageTask).run();
  }

  @Test
  public void onAlterTable() throws Exception {
    AlterTableEvent event = mock(AlterTableEvent.class);
    SerializableAlterTableEvent serializableEvent = mock(SerializableAlterTableEvent.class);
    when(serializableEvent.getDatabaseName()).thenReturn(DATABASE);
    when(serializableEvent.getTableName()).thenReturn(TABLE);
    when(serializableListenerEventFactory.create(event)).thenReturn(serializableEvent);
    listener.onAlterTable(event);
    verify(messageTask).run();
  }

  @Test
  public void onDropTable() throws Exception {
    DropTableEvent event = mock(DropTableEvent.class);
    SerializableDropTableEvent serializableEvent = mock(SerializableDropTableEvent.class);
    when(serializableEvent.getDatabaseName()).thenReturn(DATABASE);
    when(serializableEvent.getTableName()).thenReturn(TABLE);
    when(serializableListenerEventFactory.create(event)).thenReturn(serializableEvent);
    listener.onDropTable(event);
    verify(messageTask).run();
  }

  @Test
  public void onAddPartition() throws Exception {
    AddPartitionEvent event = mock(AddPartitionEvent.class);
    SerializableAddPartitionEvent serializableEvent = mock(SerializableAddPartitionEvent.class);
    when(serializableEvent.getDatabaseName()).thenReturn(DATABASE);
    when(serializableEvent.getTableName()).thenReturn(TABLE);
    when(serializableListenerEventFactory.create(event)).thenReturn(serializableEvent);
    listener.onAddPartition(event);
    verify(messageTask).run();
  }

  @Test
  public void onAlterPartition() throws Exception {
    AlterPartitionEvent event = mock(AlterPartitionEvent.class);
    SerializableAlterPartitionEvent serializableEvent = mock(SerializableAlterPartitionEvent.class);
    when(serializableEvent.getDatabaseName()).thenReturn(DATABASE);
    when(serializableEvent.getTableName()).thenReturn(TABLE);
    when(serializableListenerEventFactory.create(event)).thenReturn(serializableEvent);
    listener.onAlterPartition(event);
    verify(messageTask).run();
  }

  @Test
  public void onDropPartition() throws Exception {
    DropPartitionEvent event = mock(DropPartitionEvent.class);
    SerializableDropPartitionEvent serializableEvent = mock(SerializableDropPartitionEvent.class);
    when(serializableEvent.getDatabaseName()).thenReturn(DATABASE);
    when(serializableEvent.getTableName()).thenReturn(TABLE);
    when(serializableListenerEventFactory.create(event)).thenReturn(serializableEvent);
    listener.onDropPartition(event);
    verify(messageTask).run();
  }

  @Test
  public void onInsert() throws Exception {
    InsertEvent event = mock(InsertEvent.class);
    SerializableInsertEvent serializableEvent = mock(SerializableInsertEvent.class);
    when(serializableEvent.getDatabaseName()).thenReturn(DATABASE);
    when(serializableEvent.getTableName()).thenReturn(TABLE);
    when(serializableListenerEventFactory.create(event)).thenReturn(serializableEvent);
    listener.onInsert(event);
    verify(messageTask).run();
  }

  @Test
  public void onConfigChange() throws Exception {
    listener.onConfigChange(mock(ConfigChangeEvent.class));
    verify(executorService, never()).submit(any(Runnable.class));
    verify(eventSerDe, never()).marshal(any(SerializableListenerEvent.class));
  }

  @Test
  public void onCreateDatabase() throws Exception {
    listener.onCreateDatabase(mock(CreateDatabaseEvent.class));
    verify(executorService, never()).submit(any(Runnable.class));
    verify(eventSerDe, never()).marshal(any(SerializableListenerEvent.class));
  }

  @Test
  public void onDropDatabase() throws Exception {
    listener.onDropDatabase(mock(DropDatabaseEvent.class));
    verify(executorService, never()).submit(any(Runnable.class));
    verify(eventSerDe, never()).marshal(any(SerializableListenerEvent.class));
  }

  @Test
  public void onLoadPartitionDone() throws Exception {
    listener.onLoadPartitionDone(mock(LoadPartitionDoneEvent.class));
    verify(executorService, never()).submit(any(Runnable.class));
    verify(eventSerDe, never()).marshal(any(SerializableListenerEvent.class));
  }

  @Test
  public void onAddIndex() throws Exception {
    listener.onAddIndex(mock(AddIndexEvent.class));
    verify(executorService, never()).submit(any(Runnable.class));
    verify(eventSerDe, never()).marshal(any(SerializableListenerEvent.class));
  }

  @Test
  public void onDropIndex() throws Exception {
    listener.onDropIndex(mock(DropIndexEvent.class));
    verify(executorService, never()).submit(any(Runnable.class));
    verify(eventSerDe, never()).marshal(any(SerializableListenerEvent.class));
  }

  @Test
  public void onAlterIndex() throws Exception {
    listener.onAlterIndex(mock(AlterIndexEvent.class));
    verify(executorService, never()).submit(any(Runnable.class));
    verify(eventSerDe, never()).marshal(any(SerializableListenerEvent.class));
  }

  @Test
  public void onCreateFunction() throws Exception {
    listener.onCreateFunction(mock(CreateFunctionEvent.class));
    verify(executorService, never()).submit(any(Runnable.class));
    verify(eventSerDe, never()).marshal(any(SerializableListenerEvent.class));
  }

  @Test
  public void onDropFunction() throws Exception {
    listener.onDropFunction(mock(DropFunctionEvent.class));
    verify(executorService, never()).submit(any(Runnable.class));
    verify(eventSerDe, never()).marshal(any(SerializableListenerEvent.class));
  }

}
