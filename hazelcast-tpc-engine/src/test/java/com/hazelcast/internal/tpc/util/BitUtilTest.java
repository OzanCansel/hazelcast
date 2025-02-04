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

package com.hazelcast.internal.tpc.util;

import org.junit.Test;

import static com.hazelcast.internal.tpc.util.BitUtil.nextPowerOfTwo;
import static org.junit.Assert.assertEquals;

public class BitUtilTest {

    @Test
    public void testNextPowerOfTwo() {
        assertEquals(1, nextPowerOfTwo(-9999999));
        assertEquals(1, nextPowerOfTwo(-1));
        assertEquals(1, nextPowerOfTwo(0));
        assertEquals(1, nextPowerOfTwo(1));
        assertEquals(2, nextPowerOfTwo(2));
        assertEquals(1024, nextPowerOfTwo(999));
        assertEquals(1 << 23, nextPowerOfTwo((1 << 23) - 1));
        assertEquals(1 << 23, nextPowerOfTwo(1 << 23));
    }
}
