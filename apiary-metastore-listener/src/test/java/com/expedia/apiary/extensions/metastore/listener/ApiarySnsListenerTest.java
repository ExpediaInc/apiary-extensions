/**
 * Copyright (C) 2018 Expedia Inc.
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
package com.expedia.apiary.extensions.metastore.listener;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.expedia.apiary.extensions.metastore.listener.ApiarySnsListener.PROTOCOL_VERSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.events.AddPartitionEvent;
import org.apache.hadoop.hive.metastore.events.AlterPartitionEvent;
import org.apache.hadoop.hive.metastore.events.AlterTableEvent;
import org.apache.hadoop.hive.metastore.events.CreateTableEvent;
import org.apache.hadoop.hive.metastore.events.DropPartitionEvent;
import org.apache.hadoop.hive.metastore.events.DropTableEvent;
import org.apache.hadoop.hive.metastore.events.InsertEvent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class ApiarySnsListenerTest {

  @Rule
  public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Mock
  private AmazonSNS snsClient;
  @Mock
  private Configuration configuration;
  @Mock
  private PublishResult publishResult;
  @Mock
  private StorageDescriptor newStorageDescriptor;
  @Mock
  private StorageDescriptor storageDescriptor;

  @Captor
  private ArgumentCaptor<PublishRequest> requestCaptor;

  private final Table table = new Table();
  private static final String TABLE_NAME = "some_table";
  private static final String DB_NAME = "some_db";
  private static final String TABLE_LOCATION = "s3://table_location";
  private static final String NEW_TABLE_LOCATION = "s3://table_location_1";

  private static final String PARTITION_LOCATION = "s3://table_location/partition_location=2";
  private static final String OLD_PARTITION_LOCATION = "s3://table_location/partition_location=1";

  private static final List<String> PARTITION_VALUES = ImmutableList.of("value_1", "1000", "value_2");
  private static final List<String> NEW_PARTITION_VALUES = ImmutableList.of("value_3", "2000", "value_4");

  private static final Map<String, String> PARAMETERS = ImmutableMap
      .of("HIVE.VERSION", "2.3.4-amzn-0", "HIVE_METASTORE_TRANSACTION_ACTIVE", "false", "HIVE.METASTORE.URIS",
          "thrift://test_uri:9083", "HKAAS_ENABLED", "true", "HKAAS_EXPIRY_DAYS", "5");

  private List<FieldSchema> partitionKeys;
  private ApiarySnsListener snsListener;

  @Before
  public void setup() {
    environmentVariables.set("HKAAS_REGEX", "HKAAS.*");

    snsListener = new ApiarySnsListener(configuration, snsClient);
    when(snsClient.publish(any(PublishRequest.class))).thenReturn(publishResult);
    when(storageDescriptor.getLocation()).thenReturn(TABLE_LOCATION);
    when(newStorageDescriptor.getLocation()).thenReturn(NEW_TABLE_LOCATION);

    FieldSchema partitionColumn1 = new FieldSchema("column_1", "string", "");
    FieldSchema partitionColumn2 = new FieldSchema("column_2", "int", "");
    FieldSchema partitionColumn3 = new FieldSchema("column_3", "string", "");

    partitionKeys = ImmutableList.of(partitionColumn1, partitionColumn2, partitionColumn3);

    table.setTableName(TABLE_NAME);
    table.setDbName(DB_NAME);
    table.setPartitionKeys(partitionKeys);
    table.setSd(storageDescriptor);
    table.setParameters(PARAMETERS);
  }

  @Test
  public void onCreateTable() throws MetaException {
    CreateTableEvent event = mock(CreateTableEvent.class);
    when(event.getStatus()).thenReturn(true);
    when(event.getTable()).thenReturn(table);

    snsListener.onCreateTable(event);
    verify(snsClient).publish(requestCaptor.capture());
    PublishRequest publishRequest = requestCaptor.getValue();
    assertThat(publishRequest.getMessage(), is("{\"protocolVersion\":\""
        + PROTOCOL_VERSION
        + "\",\"eventType\":\"CREATE_TABLE\",\"dbName\":\"some_db\",\"tableName\":\"some_table\",\"tableLocation\":\"s3://table_location\",\"hkaasParameters\":\"{HKAAS_EXPIRY_DAYS=5, HKAAS_ENABLED=true}\"}"));
  }

  @Test
  public void onInsert() throws MetaException {
    InsertEvent event = mock(InsertEvent.class);
    when(event.getStatus()).thenReturn(true);
    when(event.getTable()).thenReturn(TABLE_NAME);
    when(event.getDb()).thenReturn(DB_NAME);
    List<String> files = Arrays.asList("file:/a/b.txt", "file:/a/c.txt");
    when(event.getFiles()).thenReturn(files);
    List<String> fileChecksums = Arrays.asList("123", "456");
    when(event.getFileChecksums()).thenReturn(fileChecksums);

    Map<String, String> partitionKeyValues = new HashMap<>();
    partitionKeyValues.put("load_date", "2013-03-24");
    partitionKeyValues.put("variant_code", "EN");
    when(event.getPartitionKeyValues()).thenReturn(partitionKeyValues);

    snsListener.onInsert(event);
    verify(snsClient).publish(requestCaptor.capture());
    PublishRequest publishRequest = requestCaptor.getValue();
    assertThat(publishRequest.getMessage(), is("{\"protocolVersion\":\""
        + PROTOCOL_VERSION
        + "\",\"eventType\":\"INSERT\",\"dbName\":\"some_db\",\"tableName\":\"some_table\",\"files\":[\"file:/a/b.txt\",\"file:/a/c.txt\"],"
        + "\"fileChecksums\":[\"123\",\"456\"],\"partitionKeyValues\":{\"load_date\":\"2013-03-24\",\"variant_code\":\"EN\"}}"));
  }

  @Test
  public void onAddPartition() throws MetaException {
    AddPartitionEvent event = mock(AddPartitionEvent.class);
    when(event.getStatus()).thenReturn(true);
    when(event.getTable()).thenReturn(table);

    List<Partition> partitions = new ArrayList<>();
    partitions
        .add(new Partition(PARTITION_VALUES, DB_NAME, TABLE_NAME, 0, 0,
            createStorageDescriptor(partitionKeys, PARTITION_LOCATION), ImmutableMap.of()));

    when(event.getPartitionIterator()).thenReturn(partitions.iterator());

    snsListener.onAddPartition(event);
    verify(snsClient).publish(requestCaptor.capture());
    PublishRequest publishRequest = requestCaptor.getValue();

    assertThat(publishRequest.getMessage(), is("{\"protocolVersion\":\""
        + PROTOCOL_VERSION
        + "\",\"eventType\":\"ADD_PARTITION\",\"dbName\":\"some_db\",\"tableName\":\"some_table\",\"tableLocation\":\"s3://table_location\",\"hkaasParameters\":\"{HKAAS_EXPIRY_DAYS=5, HKAAS_ENABLED=true}\",\"partitionKeys\":{\"column_1\":\"string\",\"column_2\":\"int\",\"column_3\":\"string\"},\"partitionValues\":[\"value_1\",\"1000\",\"value_2\"],\"partitionLocation\":\"s3://table_location/partition_location=2\"}"));

  }

  @Test
  public void onDropPartition() throws MetaException {
    DropPartitionEvent event = mock(DropPartitionEvent.class);
    when(event.getStatus()).thenReturn(true);
    when(event.getTable()).thenReturn(table);

    List<Partition> partitions = new ArrayList<>();
    partitions
        .add(new Partition(PARTITION_VALUES, DB_NAME, TABLE_NAME, 0, 0,
            createStorageDescriptor(partitionKeys, PARTITION_LOCATION), ImmutableMap.of()));

    when(event.getPartitionIterator()).thenReturn(partitions.iterator());

    snsListener.onDropPartition(event);
    verify(snsClient).publish(requestCaptor.capture());
    PublishRequest publishRequest = requestCaptor.getValue();

    assertThat(publishRequest.getMessage(), is("{\"protocolVersion\":\""
        + PROTOCOL_VERSION
        + "\",\"eventType\":\"DROP_PARTITION\",\"dbName\":\"some_db\",\"tableName\":\"some_table\",\"tableLocation\":\"s3://table_location\",\"hkaasParameters\":\"{HKAAS_EXPIRY_DAYS=5, HKAAS_ENABLED=true}\",\"partitionKeys\":{\"column_1\":\"string\",\"column_2\":\"int\",\"column_3\":\"string\"},\"partitionValues\":[\"value_1\",\"1000\",\"value_2\"],\"partitionLocation\":\"s3://table_location/partition_location=2\"}"));

  }

  @Test
  public void onDropTable() throws MetaException {
    DropTableEvent event = mock(DropTableEvent.class);
    when(event.getStatus()).thenReturn(true);
    when(event.getTable()).thenReturn(table);

    snsListener.onDropTable(event);
    verify(snsClient).publish(requestCaptor.capture());
    PublishRequest publishRequest = requestCaptor.getValue();

    assertThat(publishRequest.getMessage(), is("{\"protocolVersion\":\""
        + PROTOCOL_VERSION
        + "\",\"eventType\":\"DROP_TABLE\",\"dbName\":\"some_db\",\"tableName\":\"some_table\",\"tableLocation\":\"s3://table_location\",\"hkaasParameters\":\"{HKAAS_EXPIRY_DAYS=5, HKAAS_ENABLED=true}\"}"));
  }

  @Test
  public void onAlterPartitionUpdateLocation() throws MetaException {
    AlterPartitionEvent event = mock(AlterPartitionEvent.class);
    when(event.getStatus()).thenReturn(true);
    when(event.getTable()).thenReturn(table);

    Partition oldPartition = new Partition(PARTITION_VALUES, DB_NAME, TABLE_NAME, 0, 0,
        createStorageDescriptor(partitionKeys, OLD_PARTITION_LOCATION), ImmutableMap.of());
    Partition newPartition = new Partition(PARTITION_VALUES, DB_NAME, TABLE_NAME, 0, 0,
        createStorageDescriptor(partitionKeys, PARTITION_LOCATION), ImmutableMap.of());

    when(event.getOldPartition()).thenReturn(oldPartition);
    when(event.getNewPartition()).thenReturn(newPartition);

    snsListener.onAlterPartition(event);
    verify(snsClient).publish(requestCaptor.capture());
    PublishRequest publishRequest = requestCaptor.getValue();

    assertThat(publishRequest.getMessage(), is("{\"protocolVersion\":\""
        + PROTOCOL_VERSION
        + "\",\"eventType\":\"ALTER_PARTITION\",\"dbName\":\"some_db\",\"tableName\":\"some_table\",\"tableLocation\":\"s3://table_location\",\"hkaasParameters\":\"{HKAAS_EXPIRY_DAYS=5, HKAAS_ENABLED=true}\",\"partitionKeys\":{\"column_1\":\"string\",\"column_2\":\"int\",\"column_3\":\"string\"},\"partitionValues\":[\"value_1\",\"1000\",\"value_2\"],\"partitionLocation\":\"s3://table_location/partition_location=2\",\"oldPartitionValues\":[\"value_1\",\"1000\",\"value_2\"],\"oldPartitionLocation\":\"s3://table_location/partition_location=1\"}"));
  }

  @Test
  public void onAlterPartition() throws MetaException {
    AlterPartitionEvent event = mock(AlterPartitionEvent.class);
    when(event.getStatus()).thenReturn(true);
    when(event.getTable()).thenReturn(table);

    Partition oldPartition = new Partition(PARTITION_VALUES, DB_NAME, TABLE_NAME, 0, 0,
        createStorageDescriptor(partitionKeys, PARTITION_LOCATION), ImmutableMap.of());
    Partition newPartition = new Partition(NEW_PARTITION_VALUES, DB_NAME, TABLE_NAME, 0, 0,
        createStorageDescriptor(partitionKeys, PARTITION_LOCATION), ImmutableMap.of());

    when(event.getOldPartition()).thenReturn(oldPartition);
    when(event.getNewPartition()).thenReturn(newPartition);

    snsListener.onAlterPartition(event);
    verify(snsClient).publish(requestCaptor.capture());
    PublishRequest publishRequest = requestCaptor.getValue();

    assertThat(publishRequest.getMessage(), is("{\"protocolVersion\":\""
        + PROTOCOL_VERSION
        + "\",\"eventType\":\"ALTER_PARTITION\",\"dbName\":\"some_db\",\"tableName\":\"some_table\",\"tableLocation\":\"s3://table_location\",\"hkaasParameters\":\"{HKAAS_EXPIRY_DAYS=5, HKAAS_ENABLED=true}\",\"partitionKeys\":{\"column_1\":\"string\",\"column_2\":\"int\",\"column_3\":\"string\"},\"partitionValues\":[\"value_3\",\"2000\",\"value_4\"],\"partitionLocation\":\"s3://table_location/partition_location=2\",\"oldPartitionValues\":[\"value_1\",\"1000\",\"value_2\"],\"oldPartitionLocation\":\"s3://table_location/partition_location=2\"}"));
  }

  @Test
  public void onAlterTable() throws MetaException {
    Table newTable = new Table();
    newTable.setTableName("new_" + TABLE_NAME);
    newTable.setDbName(DB_NAME);
    newTable.setSd(newStorageDescriptor);
    newTable.setParameters(PARAMETERS);

    AlterTableEvent event = mock(AlterTableEvent.class);
    when(event.getStatus()).thenReturn(true);
    when(event.getOldTable()).thenReturn(table);
    when(event.getNewTable()).thenReturn(newTable);

    snsListener.onAlterTable(event);
    verify(snsClient).publish(requestCaptor.capture());
    PublishRequest publishRequest = requestCaptor.getValue();

    assertThat(publishRequest.getMessage(), is("{\"protocolVersion\":\""
        + PROTOCOL_VERSION
        + "\",\"eventType\":\"ALTER_TABLE\",\"dbName\":\"some_db\",\"tableName\":\"new_some_table\",\"tableLocation\":\"s3://table_location_1\",\"hkaasParameters\":\"{HKAAS_EXPIRY_DAYS=5, HKAAS_ENABLED=true}\",\"oldTableName\":\"some_table\",\"oldTableLocation\":\"s3://table_location\"}"));
  }

  private StorageDescriptor createStorageDescriptor(List<FieldSchema> partitionKeys, String tableLocation) {
    return new StorageDescriptor(partitionKeys, tableLocation, "ORC", "ORC", false, 2, new SerDeInfo(),
        Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
  }
  // TODO: test for setting ARN via environment variable
}
