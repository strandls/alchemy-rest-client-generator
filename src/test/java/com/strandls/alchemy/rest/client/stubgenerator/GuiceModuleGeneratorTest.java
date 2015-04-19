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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Unit tests for {@link GuiceModuleGenerator}.
 *
 * @author ashish
 *
 */
public class GuiceModuleGeneratorTest {

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.stubgenerator.GuiceModuleGenerator#generateGuiceModule(java.lang.String, java.io.File, java.util.Map)}
     * .
     *
     * @throws Exception
     */
    @Test
    public void testGenerateModuleWithPackage() throws Exception {
        final GuiceModuleGenerator generator = new GuiceModuleGenerator();
        final File tempDir = File.createTempFile("test", "test");
        FileUtils.deleteQuietly(tempDir);

        if (!tempDir.mkdirs()) {
            throw new IOException("Cannot create temporary directory.");
        }
        try {
            final Map<String, String> map = new LinkedHashMap<String, String>();
            map.put("testStub1", "testStub1Proxy");
            map.put("testStub2", "testStub2Proxy");
            generator.generateGuiceModule("com.strandls.alchemy", tempDir, map);

            String actualGuiceStub = FileUtils.readFileToString(
                    new File(tempDir, GuiceModuleGenerator.CLIENT_GUICE_MODULE_NAME
                            + ".java")).trim();
            assertEquals(
                    FileUtils.readFileToString(
                            new File(
                                    "src/test/resources/com/strandls/alchemy/rest/client/stubgenerator/"
                                            + GuiceModuleGenerator.CLIENT_GUICE_MODULE_NAME
                                            + "WithPackage.txt")).trim(),
                                            actualGuiceStub);
        } finally {
            FileUtils.deleteQuietly(tempDir);
        }
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.stubgenerator.GuiceModuleGenerator#generateGuiceModule(java.lang.String, java.io.File, java.util.Map)}
     * .
     *
     * @throws Exception
     */
    @Test
    public void testGenerateModuleWithOutPackage() throws Exception {
        final GuiceModuleGenerator generator = new GuiceModuleGenerator();
        final File tempDir = File.createTempFile("test", "test");
        FileUtils.deleteQuietly(tempDir);

        if (!tempDir.mkdirs()) {
            throw new IOException("Cannot create temporary directory.");
        }
        try {
            final Map<String, String> map = new LinkedHashMap<String, String>();
            map.put("testStub1", "testStub1Proxy");
            map.put("testStub2", "testStub2Proxy");
            generator.generateGuiceModule(null, tempDir, map);

            String actualGuicemodule = FileUtils.readFileToString(
                    new File(tempDir, GuiceModuleGenerator.CLIENT_GUICE_MODULE_NAME
                            + ".java")).trim();
            String actualGuiceModule = actualGuicemodule;
            assertEquals(
                    FileUtils.readFileToString(
                            new File(
                                    "src/test/resources/com/strandls/alchemy/rest/client/stubgenerator/"
                                            + GuiceModuleGenerator.CLIENT_GUICE_MODULE_NAME
                                            + ".txt")).trim(),
                                            actualGuiceModule);
        } finally {
            FileUtils.deleteQuietly(tempDir);
        }
    }
}
