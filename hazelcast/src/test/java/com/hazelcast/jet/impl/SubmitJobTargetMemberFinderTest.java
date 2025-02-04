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

package com.hazelcast.jet.impl;

import com.hazelcast.cluster.Address;
import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.impl.MemberImpl;
import com.hazelcast.jet.JetException;
import com.hazelcast.version.MemberVersion;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertThrows;

public class SubmitJobTargetMemberFinderTest {

    @Test
    public void testGetRandomIdFromList() throws UnknownHostException {
        UUID uuid = UUID.randomUUID();
        MemberImpl member = new MemberImpl(
                new Address("hostname", InetAddress.getByName("192.168.8.112"), 0),
                new MemberVersion(0, 0, 0),
                true,
                uuid);

        ArrayList<Member> memberList = new ArrayList<>();
        memberList.add(member);

        SubmitJobTargetMemberFinder submitJobTargetMemberFinder = new SubmitJobTargetMemberFinder();
        UUID result = submitJobTargetMemberFinder.getRandomIdFromList(memberList);
        Assert.assertEquals(uuid, result);
    }

    @Test
    public void testGetRandomIdFromEmptyList() {
        ArrayList<Member> memberList = new ArrayList<>();

        SubmitJobTargetMemberFinder submitJobTargetMemberFinder = new SubmitJobTargetMemberFinder();
        assertThrows(JetException.class, () -> submitJobTargetMemberFinder.getRandomIdFromList(memberList));
    }
}
