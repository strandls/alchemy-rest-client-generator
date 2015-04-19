/*
 * Copyright (C) 2015 Alchemy Rest Client Generator Authors
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

package com.strandls.alchemy.rest.client.stubgenerator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Unit tests for {@link ProxyImplementationGenerator}.
 *
 * @author ashish
 *
 */
public class ProxyImplementationGeneratorTest {

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.stubgenerator.ProxyImplementationGenerator#generateProxy(java.lang.String, java.lang.String, java.lang.String, java.io.File)}
     * .
     *
     * @throws IOException
     */
    @Test
    public void testGenerateProxy() throws IOException {
        final ProxyImplementationGenerator generator = new ProxyImplementationGenerator();
        final File tempDir = File.createTempFile("test", "test");
        FileUtils.deleteQuietly(tempDir);

        if (!tempDir.mkdirs()) {
            throw new IOException("Cannot create temporary directory.");
        }
        try {
            generator.generateProxy("TestStub", "TestStubProxy", "", tempDir);
            assertEquals(FileUtils.readFileToString(
                    new File("src/test/resources/com/strandls/alchemy/rest/client/stubgenerator"
                            + "/TestStubProxy.txt")).trim(),
                            FileUtils.readFileToString(new File(tempDir, "TestStubProxy.java")).trim());
        } finally {
            FileUtils.deleteQuietly(tempDir);
        }
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.stubgenerator.ProxyImplementationGenerator#generateProxy(java.lang.String, java.lang.String, java.lang.String, java.io.File)}
     * .
     *
     * @throws IOException
     */
    @Test
    public void testGenerateProxyWithPackage() throws IOException {
        final ProxyImplementationGenerator generator = new ProxyImplementationGenerator();
        final File tempDir = File.createTempFile("test", "test");
        FileUtils.deleteQuietly(tempDir);

        if (!tempDir.mkdirs()) {
            throw new IOException("Cannot create temporary directory.");
        }
        try {
            generator.generateProxy("TestStub", "TestStubProxyWithPackage",
                    "com.strandls.alchemy.client", tempDir);
            assertEquals(FileUtils.readFileToString(
                    new File("src/test/resources/com/strandls/alchemy/rest/client/stubgenerator"
                            + "/TestStubProxyWithPackage.txt")).trim(),
                            FileUtils.readFileToString(new File(tempDir, "TestStubProxyWithPackage.java"))
                            .trim());
        } finally {
            FileUtils.deleteQuietly(tempDir);
        }
    }
}
