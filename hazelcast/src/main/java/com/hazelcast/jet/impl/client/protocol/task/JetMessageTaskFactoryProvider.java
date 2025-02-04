/*
 * Copyright (c) 2008-2023, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.jet.impl.client.protocol.task;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.MessageTaskFactory;
import com.hazelcast.client.impl.protocol.MessageTaskFactoryProvider;
import com.hazelcast.client.impl.protocol.codec.JetExistsDistributedObjectCodec;
import com.hazelcast.client.impl.protocol.codec.JetExportSnapshotCodec;
import com.hazelcast.client.impl.protocol.codec.JetGetJobAndSqlSummaryListCodec;
import com.hazelcast.client.impl.protocol.codec.JetGetJobConfigCodec;
import com.hazelcast.client.impl.protocol.codec.JetGetJobIdsCodec;
import com.hazelcast.client.impl.protocol.codec.JetGetJobMetricsCodec;
import com.hazelcast.client.impl.protocol.codec.JetGetJobStatusCodec;
import com.hazelcast.client.impl.protocol.codec.JetGetJobSubmissionTimeCodec;
import com.hazelcast.client.impl.protocol.codec.JetGetJobSummaryListCodec;
import com.hazelcast.client.impl.protocol.codec.JetGetJobSuspensionCauseCodec;
import com.hazelcast.client.impl.protocol.codec.JetIsJobUserCancelledCodec;
import com.hazelcast.client.impl.protocol.codec.JetJoinSubmittedJobCodec;
import com.hazelcast.client.impl.protocol.codec.JetResumeJobCodec;
import com.hazelcast.client.impl.protocol.codec.JetSubmitJobCodec;
import com.hazelcast.client.impl.protocol.codec.JetTerminateJobCodec;
import com.hazelcast.client.impl.protocol.codec.JetUploadJobMetaDataCodec;
import com.hazelcast.client.impl.protocol.codec.JetUploadJobMultipartCodec;
import com.hazelcast.client.impl.protocol.task.MessageTask;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.internal.nio.Connection;
import com.hazelcast.internal.util.collection.Int2ObjectHashMap;
import com.hazelcast.spi.impl.NodeEngine;
import com.hazelcast.spi.impl.NodeEngineImpl;

public class JetMessageTaskFactoryProvider implements MessageTaskFactoryProvider {
    private final Int2ObjectHashMap<MessageTaskFactory> factories = new Int2ObjectHashMap<>(50);
    private final Node node;

    public JetMessageTaskFactoryProvider(NodeEngine nodeEngine) {
        this.node = ((NodeEngineImpl) nodeEngine).getNode();
        initFactories();
    }

    public void initFactories() {
        factories.put(JetSubmitJobCodec.REQUEST_MESSAGE_TYPE, toFactory(JetSubmitJobMessageTask::new));
        factories.put(JetTerminateJobCodec.REQUEST_MESSAGE_TYPE, toFactory(JetTerminateJobMessageTask::new));
        factories.put(JetGetJobStatusCodec.REQUEST_MESSAGE_TYPE, toFactory(JetGetJobStatusMessageTask::new));
        factories.put(JetGetJobSuspensionCauseCodec.REQUEST_MESSAGE_TYPE, toFactory(JetGetJobSuspensionCauseMessageTask::new));
        factories.put(JetGetJobMetricsCodec.REQUEST_MESSAGE_TYPE, toFactory(JetGetJobMetricsMessageTask::new));
        factories.put(JetGetJobIdsCodec.REQUEST_MESSAGE_TYPE, toFactory(JetGetJobIdsMessageTask::new));
        factories.put(JetJoinSubmittedJobCodec.REQUEST_MESSAGE_TYPE, toFactory(JetJoinSubmittedJobMessageTask::new));
        factories.put(JetGetJobSubmissionTimeCodec.REQUEST_MESSAGE_TYPE, toFactory(JetGetJobSubmissionTimeMessageTask::new));
        factories.put(JetGetJobConfigCodec.REQUEST_MESSAGE_TYPE, toFactory(JetGetJobConfigMessageTask::new));
        factories.put(JetResumeJobCodec.REQUEST_MESSAGE_TYPE, toFactory(JetResumeJobMessageTask::new));
        factories.put(JetExportSnapshotCodec.REQUEST_MESSAGE_TYPE, toFactory(JetExportSnapshotMessageTask::new));
        factories.put(JetGetJobSummaryListCodec.REQUEST_MESSAGE_TYPE, toFactory(JetGetJobSummaryListMessageTask::new));
        factories.put(JetGetJobAndSqlSummaryListCodec.REQUEST_MESSAGE_TYPE,
                toFactory(JetGetJobAndSqlSummaryListMessageTask::new));
        factories.put(JetExistsDistributedObjectCodec.REQUEST_MESSAGE_TYPE,
                toFactory(JetExistsDistributedObjectMessageTask::new));
        factories.put(JetIsJobUserCancelledCodec.REQUEST_MESSAGE_TYPE,
                toFactory(JetIsJobUserCancelledMessageTask::new));
        factories.put(JetUploadJobMetaDataCodec.REQUEST_MESSAGE_TYPE, toFactory(JetUploadJobMetaDataTask::new));
        factories.put(JetUploadJobMultipartCodec.REQUEST_MESSAGE_TYPE, toFactory(JetUploadJobMultipartTask::new));
    }

    @Override
    public Int2ObjectHashMap<MessageTaskFactory> getFactories() {
        return factories;
    }

    private MessageTaskFactory toFactory(MessageTaskConstructor constructor) {
        return ((clientMessage, connection) -> constructor.construct(clientMessage, node, connection));
    }

    private interface MessageTaskConstructor {
        MessageTask construct(ClientMessage message, Node node, Connection connection);
    }
}

