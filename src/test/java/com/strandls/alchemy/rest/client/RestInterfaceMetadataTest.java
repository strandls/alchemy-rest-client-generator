/*
 * Copyright (C) 2015 Strand Life Sciences.
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

package com.strandls.alchemy.rest.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link RestInterfaceMetadata}.
 *
 * @author Ashish Shinde
 *
 */
public class RestInterfaceMetadataTest {
    private RestInterfaceMetadata methodData1;
    private RestInterfaceMetadata methodData1Copy;
    private RestInterfaceMetadata methodData2;

    /**
     * Generate test data.
     *
     * @throws NotRestInterfaceException
     */
    @Before
    public void setup() throws NotRestInterfaceException {
        methodData1 = new RestInterfaceAnalyzer().analyze(TestWebserviceWithPutDelete.class);
        methodData2 = new RestInterfaceAnalyzer().analyze(TestWebserviceWithPath.class);
        methodData1Copy = new RestInterfaceAnalyzer().analyze(TestWebserviceWithPutDelete.class);
        ;
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.RestInterfaceMetadata#hashCode()}
     * .
     */
    @Test
    public void testHashCode() {
        assertNotNull(methodData1.hashCode());
        assertNotEquals(methodData1.hashCode(), methodData2.hashCode());
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.RestInterfaceMetadata#equals(java.lang.Object)}
     * .
     */
    @Test
    public void testEqualsObject() {
        assertEquals(methodData1, methodData1);
        assertNotSame(methodData1, methodData1Copy);
        assertEquals(methodData1, methodData1Copy);
        assertNotEquals(methodData1.hashCode(), methodData2.hashCode());
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.RestInterfaceMetadata#canEqual(java.lang.Object)}
     * .
     */
    @Test
    public void testCanEqual() {
        assertTrue(methodData1.canEqual(methodData2));
        assertFalse(methodData1.canEqual(new Object()));
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.RestInterfaceMetadata#toString()}
     * .
     */
    @Test
    public void testToString() {
        assertNotNull(methodData1.toString());
    }

}
