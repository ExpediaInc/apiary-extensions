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
package com.expedia.apiary.extensions.receiver.common;

import java.io.Closeable;
import java.util.Optional;

import com.expedia.apiary.extensions.receiver.common.event.ListenerEvent;

/**
 * A {@code MessageReader} is in charge of retrieving events from the messaging infrastructure.
 */
public interface MessageReader extends Closeable {

  Optional<ListenerEvent> next();

}
