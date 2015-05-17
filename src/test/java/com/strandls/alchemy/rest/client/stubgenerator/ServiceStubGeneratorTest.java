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

package com.strandls.alchemy.rest.client.stubgenerator;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;

import javax.inject.Inject;

import lombok.Cleanup;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;

import com.google.guiceberry.junit4.GuiceBerryRule;
import com.strandls.alchemy.rest.client.NotRestInterfaceException;
import com.strandls.alchemy.rest.client.TestWebserviceWithGenericTypes;
import com.strandls.alchemy.rest.client.TestWebserviceWithPath;
import com.sun.codemodel.writer.SingleStreamCodeWriter;

/**
 * Unit tests for {@link ServiceStubGenerator}.
 *
 * @author Ashish Shinde
 *
 */
public class ServiceStubGeneratorTest {
    /**
     * Setup guice berry.
     */
    @Rule
    public final GuiceBerryRule guiceBerry = new GuiceBerryRule(StubTestModule.class);

    /**
     * The stub generator.
     */
    @Inject
    private ServiceStubGenerator stubGenerator;

    /**
     * Test method for
     * {@link ServiceStubGenerator#generateStubInterface(Class, String, String, com.sun.codemodel.CodeWriter)}
     * .
     *
     * @throws NotRestInterfaceException
     * @throws Exception
     */
    @Test
    public void test() throws NotRestInterfaceException, Exception {
        @Cleanup
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final SingleStreamCodeWriter writer = new SingleStreamCodeWriter(out);
        stubGenerator.generateStubInterface(TestWebserviceWithPath.class,
                TestWebserviceWithPath.class.getSimpleName() + "Stub", TestWebserviceWithPath.class
                        .getPackage().getName() + ".stub", writer);
        writer.close();

        assertEquals(FileUtils.readFileToString(
                new File("src/test/resources/com/strandls/alchemy/rest/client/stubgenerator"
                        + "/TestWebserviceWithPathStub.txt")).trim(), new String(out.toByteArray(),
                Charset.defaultCharset()).trim());
    }

    /**
     * Test method for
     * {@link ServiceStubGenerator#generateStubInterface(Class, String, String, com.sun.codemodel.CodeWriter)}
     * .
     *
     * Ensures generic types are handled correctly.
     *
     * @throws NotRestInterfaceException
     * @throws Exception
     */
    @Test
    public void testGenericTypes() throws NotRestInterfaceException, Exception {
        @Cleanup
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final SingleStreamCodeWriter writer = new SingleStreamCodeWriter(out);
        stubGenerator.generateStubInterface(TestWebserviceWithGenericTypes.class,
                TestWebserviceWithGenericTypes.class.getSimpleName() + "Stub",
                TestWebserviceWithGenericTypes.class.getPackage().getName() + ".stub", writer);
        writer.close();
        System.out.println(new String(out.toByteArray(), Charset.defaultCharset()).trim());
        assertEquals(FileUtils.readFileToString(
                new File("src/test/resources/com/strandls/alchemy/rest/client/stubgenerator"
                        + "/TestWebserviceWithGenericTypes.txt")).trim(),
                new String(out.toByteArray(), Charset.defaultCharset()).trim());

    }

}
