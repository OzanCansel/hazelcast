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

package com.hazelcast.spring;

import com.hazelcast.config.AdvancedNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EndpointConfig;
import com.hazelcast.config.MemberAddressProviderConfig;
import com.hazelcast.config.RestServerEndpointConfig;
import com.hazelcast.config.ServerSocketEndpointConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.WanBatchPublisherConfig;
import com.hazelcast.config.WanReplicationConfig;
import com.hazelcast.config.alto.AltoSocketConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.EndpointQualifier;
import com.hazelcast.instance.ProtocolType;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.Arrays;

import static com.hazelcast.config.RestEndpointGroup.CLUSTER_READ;
import static com.hazelcast.config.RestEndpointGroup.HEALTH_CHECK;
import static com.hazelcast.test.HazelcastTestSupport.assertContains;
import static com.hazelcast.test.HazelcastTestSupport.assertContainsAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(CustomSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"advancedNetworkConfig-applicationContext-hazelcast.xml"})
@Category(QuickTest.class)
public class TestAdvancedNetworkApplicationContext {

    @Resource(name = "instance")
    private HazelcastInstance instance;

    @BeforeClass
    @AfterClass
    public static void start() {
        HazelcastInstanceFactory.terminateAll();
    }


    @Test
    public void testAdvancedNetworkConfig() {
        Config config = instance.getConfig();
        AdvancedNetworkConfig advancedNetworkConfig = config.getAdvancedNetworkConfig();
        assertTrue(advancedNetworkConfig.isEnabled());

        TcpIpConfig tcpIpConfig = advancedNetworkConfig.getJoin().getTcpIpConfig();
        assertTrue(tcpIpConfig.isEnabled());
        assertEquals("127.0.0.1:5700", tcpIpConfig.getRequiredMember());
        assertFalse(advancedNetworkConfig.getJoin().getMulticastConfig().isEnabled());
        assertFalse(advancedNetworkConfig.getJoin().getAutoDetectionConfig().isEnabled());

        MemberAddressProviderConfig addressProviderConfig = advancedNetworkConfig.getMemberAddressProviderConfig();
        assertFalse(addressProviderConfig.isEnabled());

        AltoSocketConfig expectedAltoSocketConfig = new AltoSocketConfig()
                .setPortRange("14000-16000")
                .setReceiveBufferSizeKB(256)
                .setSendBufferSizeKB(256);

        ServerSocketEndpointConfig memberEndpointConfig = (ServerSocketEndpointConfig) advancedNetworkConfig
                .getEndpointConfigs().get(EndpointQualifier.MEMBER);

        assertEquals(5700, memberEndpointConfig.getPort());
        assertEquals(99, memberEndpointConfig.getPortCount());
        assertFalse(memberEndpointConfig.isPortAutoIncrement());
        assertTrue(memberEndpointConfig.getInterfaces().isEnabled());
        assertContains(memberEndpointConfig.getInterfaces().getInterfaces(), "127.0.0.1");
        assertTrue(memberEndpointConfig.isReuseAddress());
        assertTrue(memberEndpointConfig.getSocketInterceptorConfig().isEnabled());
        assertEquals("com.hazelcast.SocketInterceptor",
                memberEndpointConfig.getSocketInterceptorConfig().getClassName());
        assertTrue(memberEndpointConfig.isSocketBufferDirect());
        assertTrue(memberEndpointConfig.isSocketKeepAlive());
        assertEquals(2, memberEndpointConfig.getSocketKeepCount());
        assertEquals(120, memberEndpointConfig.getSocketKeepIdleSeconds());
        assertEquals(5, memberEndpointConfig.getSocketKeepIntervalSeconds());
        assertFalse(memberEndpointConfig.isSocketTcpNoDelay());
        assertEquals(expectedAltoSocketConfig, memberEndpointConfig.getAltoSocketConfig());

        EndpointConfig wanConfig = advancedNetworkConfig.getEndpointConfigs().get(
                EndpointQualifier.resolve(ProtocolType.WAN, "wan-tokyo"));
        assertFalse(wanConfig.getInterfaces().isEnabled());
        assertTrue(wanConfig.getSymmetricEncryptionConfig().isEnabled());
        assertEquals("PBEWithMD5AndDES", wanConfig.getSymmetricEncryptionConfig().getAlgorithm());
        assertEquals("thesalt", wanConfig.getSymmetricEncryptionConfig().getSalt());
        assertEquals("thepass", wanConfig.getSymmetricEncryptionConfig().getPassword());
        assertEquals(19, wanConfig.getSymmetricEncryptionConfig().getIterationCount());
        assertEquals(expectedAltoSocketConfig, wanConfig.getAltoSocketConfig());

        ServerSocketEndpointConfig clientEndpointConfig = (ServerSocketEndpointConfig) advancedNetworkConfig
                .getEndpointConfigs().get(EndpointQualifier.CLIENT);
        assertEquals(9919, clientEndpointConfig.getPort());
        assertEquals(10, clientEndpointConfig.getPortCount());
        assertFalse(clientEndpointConfig.isPortAutoIncrement());
        assertTrue(clientEndpointConfig.isReuseAddress());
        assertEquals(expectedAltoSocketConfig, clientEndpointConfig.getAltoSocketConfig());

        RestServerEndpointConfig restServerEndpointConfig = advancedNetworkConfig.getRestEndpointConfig();
        assertEquals(9999, restServerEndpointConfig.getPort());
        assertTrue(restServerEndpointConfig.isPortAutoIncrement());
        assertContainsAll(restServerEndpointConfig.getEnabledGroups(),
                Arrays.asList(HEALTH_CHECK, CLUSTER_READ));
        assertEquals(expectedAltoSocketConfig, restServerEndpointConfig.getAltoSocketConfig());

        ServerSocketEndpointConfig memcacheEndpointConfig = (ServerSocketEndpointConfig) advancedNetworkConfig
                .getEndpointConfigs().get(EndpointQualifier.MEMCACHE);
        assertEquals(9989, memcacheEndpointConfig.getPort());
        assertEquals(expectedAltoSocketConfig, memcacheEndpointConfig.getAltoSocketConfig());

        ServerSocketEndpointConfig wanSSEndpointConfig = (ServerSocketEndpointConfig) advancedNetworkConfig
                .getEndpointConfigs().get(EndpointQualifier.resolve(ProtocolType.WAN, "wan-server-socket-config"));
        assertEquals(9979, wanSSEndpointConfig.getPort());
        assertEquals(expectedAltoSocketConfig, wanSSEndpointConfig.getAltoSocketConfig());

        WanReplicationConfig testWan = config.getWanReplicationConfig("testWan");
        WanBatchPublisherConfig tokyoWanPublisherConfig =
                testWan.getBatchPublisherConfigs()
                        .stream()
                        .filter(pc -> pc.getPublisherId().equals("tokyoPublisherId"))
                        .findFirst()
                        .get();

        assertNotNull(tokyoWanPublisherConfig);
        assertEquals("wan-tokyo", tokyoWanPublisherConfig.getEndpoint());
    }
}
