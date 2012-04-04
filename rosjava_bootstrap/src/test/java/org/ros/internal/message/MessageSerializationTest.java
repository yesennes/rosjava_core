/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.message;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.ros.internal.message.topic.TopicDefinitionResourceProvider;
import org.ros.message.MessageFactory;

import java.nio.ByteBuffer;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageSerializationTest {

  private TopicDefinitionResourceProvider topicDefinitionResourceProvider;
  private MessageFactory messageFactory;

  @Before
  public void setUp() {
    topicDefinitionResourceProvider = new TopicDefinitionResourceProvider();
    messageFactory = new DefaultMessageFactory(topicDefinitionResourceProvider);
  }

  private void checkSerializeAndDeserialize(RawMessage rawMessage) {
    ByteBuffer buffer = rawMessage.serialize();
    DefaultMessageDeserializer<RawMessage> deserializer =
        new DefaultMessageDeserializer<RawMessage>(rawMessage.getIdentifier(), messageFactory);
    assertEquals(rawMessage, deserializer.deserialize(buffer));
  }

  @Test
  public void testInt32() {
    RawMessage rawMessage = messageFactory.newFromType("std_msgs/Int32");
    rawMessage.setInt32("data", 42);
    checkSerializeAndDeserialize(rawMessage);
  }

  @Test
  public void testString() {
    RawMessage rawMessage = messageFactory.newFromType("std_msgs/String");
    rawMessage.setString("data", "Hello, ROS!");
    checkSerializeAndDeserialize(rawMessage);
  }

  @Test
  public void testNestedMessage() {
    topicDefinitionResourceProvider.add("foo/foo", "std_msgs/String data");
    RawMessage fooMessage = messageFactory.newFromType("foo/foo");
    RawMessage stringMessage = messageFactory.newFromType("std_msgs/String");
    stringMessage.setString("data", "Hello, ROS!");
    fooMessage.setMessage("data", stringMessage);
    checkSerializeAndDeserialize(fooMessage);
  }

  @Test
  public void testNestedMessageArray() {
    topicDefinitionResourceProvider.add("foo/foo", "std_msgs/String[] data");
    RawMessage fooMessage = messageFactory.newFromType("foo/foo");
    RawMessage stringMessageA = messageFactory.newFromType("std_msgs/String");
    stringMessageA.setString("data", "Hello, ROS!");
    RawMessage stringMessageB = messageFactory.newFromType("std_msgs/String");
    stringMessageB.setString("data", "Goodbye, ROS!");
    fooMessage.setMessageList("data", Lists.<Message>newArrayList(stringMessageA, stringMessageB));
    checkSerializeAndDeserialize(fooMessage);
  }

  @Test
  public void testInt32Array() {
    topicDefinitionResourceProvider.add("foo/foo", "int32[] data");
    RawMessage rawMessage = messageFactory.newFromType("foo/foo");
    rawMessage.setInt32List("data", Lists.newArrayList(1, 2, 3, 4, 5));
    checkSerializeAndDeserialize(rawMessage);
  }
}
