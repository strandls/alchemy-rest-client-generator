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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;

import lombok.Cleanup;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.reflections.ReflectionUtils;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.base.Predicate;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.strandls.alchemy.rest.client.reader.VoidMessageBodyReader;

/**
 * Unit tests for {@link AlchemyRestClientFactory}.
 *
 * @author Ashish Shinde
 *
 */
public class AlchemyRestClientFactoryTest extends JerseyTest {

    /**
     * The client side guice module.
     *
     * @author Ashish Shinde
     *
     */
    public class ClientModule extends AbstractModule {

        public ClientModule() {
            super();
        }

        /*
         * (non-Javadoc)
         * @see com.google.inject.AbstractModule#configure()
         */
        @Override
        protected void configure() {
            // bind the URI.
            bind(String.class).annotatedWith(
                    Names.named(AlchemyRestClientFactory.BASE_URI_NAMED_PARAM)).toInstance(
                            getBaseUri().toString());
        }

        /**
         * @return the jersey client to use behind the proxy.
         */
        @Provides
        Client getClient() {
            return client();
        }

    }

    /**
     * The client factory.
     */
    private AlchemyRestClientFactory clientFactory;

    /*
     * (non-Javadoc)
     * @see org.glassfish.jersey.test.JerseyTest#configure()
     */
    @Override
    protected Application configure() {
        final ResourceConfig application =
                new ResourceConfig(TestWebserviceWithPath.class, TestWebserviceWithPutDelete.class,
                        TestWebserviceMultipart.class, TestWebserviceExceptionHandling.class,
                        JacksonJsonProvider.class);
        final Injector injector =
                Guice.createInjector(new ClientModule(), new ExceptionObjectMapperModule());

        // register multi part feature.
        application.register(MultiPartFeature.class);

        // register the application mapper.
        application.register(injector.getInstance(TestExceptionMapper.class));
        return application;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.glassfish.jersey.test.JerseyTest#configureClient(org.glassfish.jersey
     * .client.ClientConfig)
     */
    @Override
    protected void configureClient(final ClientConfig config) {
        super.configureClient(config);
        config.register(VoidMessageBodyReader.class);
    }

    /**
     * @param testFile
     * @return
     * @throws FileNotFoundException
     */
    private FileInputStream getInputStream(final String testFile) throws FileNotFoundException {
        return new FileInputStream(new File(testFile));
    }

    /**
     * Convert multi part body into a map of key value pairs.
     *
     * @param multiPart
     * @return
     * @throws IOException
     */
    private Map<String, String> multipartToMap(final MultiPart multiPart) throws IOException {
        final Map<String, String> map = new HashMap<>();
        for (final BodyPart part : multiPart.getBodyParts()) {
            Object value = part.getEntity();

            if (value instanceof BodyPartEntity) {
                value = IOUtils.toString(((BodyPartEntity) value).getInputStream());
            }

            if (value instanceof File) {
                value = IOUtils.toString(new FileInputStream((File) value));
            }

            final String fieldName =
                    part.getHeaders().get("Content-Disposition").get(0)
                            .split("form-data;\\s*.*name=\"")[1].replaceFirst("\"$", "");
            map.put(fieldName, ObjectUtils.toString(value));
        }
        return map;
    }

    /**
     * Setup jackson as json provider.
     */
    @Before
    public void setup() {
        client().register(new JacksonJsonProvider());
        client().register(MultiPartFeature.class);
        final Injector injector =
                Guice.createInjector(new ClientModule(), new ExceptionObjectMapperModule());
        clientFactory = injector.getInstance(AlchemyRestClientFactory.class);
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.AlchemyRestClientFactory#getInstance(java.lang.Class, java.lang.String, javax.ws.rs.client.Client)}
     * .
     *
     * Test {@link ProcessingException}s are not touched.
     *
     * @throws Exception
     */
    @Test(expected = NotFoundException.class)
    public void testException404() throws Exception {
        final TestWebserviceExceptionHandling service =
                clientFactory.getInstance(TestWebserviceExceptionHandling.class);
        service.failInternal404();
        fail("Should have thrown an exception");

    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.AlchemyRestClientFactory#getInstance(java.lang.Class, java.lang.String, javax.ws.rs.client.Client)}
     * .
     *
     * Test generic exception is relayed correctly without loosing message.
     *
     * @throws Exception
     */
    @Test
    public void testExceptionMapping() throws Exception {
        final TestWebserviceExceptionHandling service =
                clientFactory.getInstance(TestWebserviceExceptionHandling.class);
        try {
            service.fail();
            fail("Should have thrown an exception");
        } catch (final Exception e) {
            assertEquals(TestWebserviceExceptionHandling.EXCEPTION_STRING, e.getMessage());

            // ensure the mixin got applied
            assertNull(e.getCause());
            // ensure server stack trace is masked.
            // will have local stack trace though.
            final StackTraceElement[] stackTrace = e.getStackTrace();
            for (final StackTraceElement stackTraceElement : stackTrace) {
                assertFalse(stackTraceElement.toString().contains(
                        TestWebserviceExceptionHandling.class.getName() + ".fail"));
            }
            assertEquals(0, e.getSuppressed().length);

        }

    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.AlchemyRestClientFactory#getInstance(java.lang.Class, java.lang.String, javax.ws.rs.client.Client)}
     * .
     *
     * Test custom exception is relayed correctly without loosing message and
     * internal fields.
     *
     * @throws Exception
     */
    @Test
    public void testExceptionMappingCustomException() throws Exception {
        final TestWebserviceExceptionHandling service =
                clientFactory.getInstance(TestWebserviceExceptionHandling.class);
        try {
            service.failWithACustomException();
            fail("Should have thrown an exception");
        } catch (final TestCustomException e) {
            assertEquals(10, e.getStatusCode());

            // ensure the mixin got applied
            assertNull(e.getCause());

            // ensure server stack trace is masked.
            // will have local stack trace though.
            final StackTraceElement[] stackTrace = e.getStackTrace();
            for (final StackTraceElement stackTraceElement : stackTrace) {
                assertFalse(stackTraceElement.toString().contains(
                        TestWebserviceExceptionHandling.class.getName()
                                + ".failWithACustomException"));
            }
            assertEquals(0, e.getSuppressed().length);
        }

    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.AlchemyRestClientFactory#getInstance(java.lang.Class, java.lang.String, javax.ws.rs.client.Client)}
     * .
     *
     * Test {@link FormDataParam} handling with upload and download of files.
     *
     * @throws Exception
     */
    @Test
    public void testGetInstanceFormDataParam() throws Exception {
        final TestWebserviceMultipart webserviceClient =
                clientFactory.getInstance(TestWebserviceMultipart.class);

        // test input stream upload with content disposition
        final boolean enabled = new SecureRandom().nextInt() % 2 == 0;
        final String secret = UUID.randomUUID().toString();
        final String testFile = "src/test/resources/UploadTest.txt";
        final FormDataContentDisposition disposition =
                FormDataContentDisposition.name("file").fileName(testFile).build();
        @Cleanup
        final FileInputStream inputStream = getInputStream(testFile);
        final Map<String, String> result =
                webserviceClient.multipartMapEcho(enabled, secret, inputStream, disposition);
        assertEquals(result.get("secret"), secret);
        assertEquals(result.get("enabled"), ObjectUtils.toString(enabled));

        @Cleanup
        final FileInputStream inputStream1 = getInputStream(testFile);
        assertEquals(result.get("file"), IOUtils.toString(inputStream1));
        assertEquals(result.get("filename"), testFile);
        assertEquals(result.get("filetype"), "form-data");

        // test input stream upload without content disposition
        final boolean enabled1 = new SecureRandom().nextInt() % 2 == 0;
        final String secret1 = UUID.randomUUID().toString();
        final String testFile1 = "src/test/resources/UploadTest.txt";
        final Map<String, String> result1 =
                webserviceClient.multipartMapEchoWithoutDisposition(enabled1, secret1,
                        getInputStream(testFile1));
        assertEquals(result1.get("secret"), secret1);
        assertEquals(result1.get("enabled"), ObjectUtils.toString(enabled1));

        @Cleanup
        final FileInputStream inputStream2 = getInputStream(testFile);
        assertEquals(result1.get("file"), IOUtils.toString(inputStream2));

        // test simple params
        final Map<String, String> result11 =
                webserviceClient.multipartMapEchoPrimitive(enabled1, secret1);
        assertEquals(result11.get("secret"), secret1);
        assertEquals(result11.get("enabled"), ObjectUtils.toString(enabled1));
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.AlchemyRestClientFactory#getInstance(java.lang.Class, java.lang.String, javax.ws.rs.client.Client)}
     * .
     *
     * Test get and post methods with a combination of parameter types (query,
     * path, header, cookie, matrix)
     *
     * @throws Exception
     */
    @Test
    public void testGetInstanceGetAndPost() throws Exception {
        final TestWebserviceWithPath service =
                clientFactory.getInstance(TestWebserviceWithPath.class);

        final Random r = new Random();

        final int[] args = new int[] { r.nextInt(), r.nextInt(), r.nextInt() };
        @SuppressWarnings("unchecked")
        final Set<Method> methods =
        ReflectionUtils.getAllMethods(TestWebserviceWithPath.class,
                new Predicate<Method>() {
            @Override
            public boolean apply(final Method input) {
                return Modifier.isPublic(input.getModifiers());
            }
        });

        for (final Method method : methods) {
            System.out.println("Invoking : " + method);
            if (method.getParameterTypes().length == 3) {
                assertArrayEquals("Invocation failed on " + method, args,
                        (int[]) method.invoke(service, args[0], args[1], args[2]));
            } else if (method.getParameterTypes().length == 0) {
                method.invoke(service);
            } else {
                assertArrayEquals("Invocation failed on " + method, args,
                        (int[]) method.invoke(service, args));
            }
            System.out.println("Done invoking : " + method);
        }
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.AlchemyRestClientFactory#getInstance(java.lang.Class, java.lang.String, javax.ws.rs.client.Client)}
     * .
     *
     * Test multipart form data handling with upload and download of files.
     *
     * @throws Exception
     */
    @Test
    public void testGetInstanceMultipartBody() throws Exception {
        final TestWebserviceMultipart webserviceClient =
                clientFactory.getInstance(TestWebserviceMultipart.class);
        final FormDataMultiPart multiPartEntity = new FormDataMultiPart();
        final String testFile = "src/test/resources/UploadTest.txt";
        multiPartEntity.field("enabled", "true").field("secret", UUID.randomUUID().toString())
                .bodyPart(new FileDataBodyPart("file", new File(testFile)));

        final MultiPart result = webserviceClient.multipartEcho(multiPartEntity);
        assertEquals(multipartToMap(multiPartEntity), multipartToMap(result));
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.AlchemyRestClientFactory#getInstance(java.lang.Class, java.lang.String, javax.ws.rs.client.Client)}
     * .
     *
     * @throws Exception
     */
    @Test(expected = NotRestInterfaceException.class)
    public void testGetInstanceOnBadClass() throws Exception {
        clientFactory.getInstance(Object.class);
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.AlchemyRestClientFactory#getInstance(java.lang.Class, java.lang.String, javax.ws.rs.client.Client)}
     * .
     *
     * @throws Exception
     */
    @Test
    public void testGetInstancePutAndDelete() throws Exception {
        final TestWebserviceWithPutDelete service =
                clientFactory.getInstance(TestWebserviceWithPutDelete.class);

        assertTrue(service.put("someURI"));
        assertTrue(service.delete("someURI"));
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.AlchemyRestClientFactory#getInstance(java.lang.Class, java.lang.String, javax.ws.rs.client.Client)}
     * .
     *
     * Test get and post methods with a combination of parameter types (query,
     * path, header, cookie, matrix)
     *
     * @throws Exception
     */
    @Test
    public void testGetInstanceStub() throws Exception {
        final TestWebserviceWithPathStub service =
                clientFactory.getInstance(TestWebserviceWithPathStub.class);

        final Random r = new Random();

        final int[] args = new int[] { r.nextInt(), r.nextInt(), r.nextInt() };
        @SuppressWarnings("unchecked")
        final Set<Method> methods =
        ReflectionUtils.getAllMethods(TestWebserviceWithPathStub.class,
                new Predicate<Method>() {
            @Override
            public boolean apply(final Method input) {
                return Modifier.isPublic(input.getModifiers());
            }
        });

        for (final Method method : methods) {
            System.out.println("Invoking : " + method);
            if (method.getParameterTypes().length == 3) {
                assertArrayEquals("Invocation failed on " + method, args,
                        (int[]) method.invoke(service, args[0], args[1], args[2]));
            } else {
                assertArrayEquals("Invocation failed on " + method, args,
                        (int[]) method.invoke(service, args));
            }
            System.out.println("Done invoking : " + method);
        }
    }
}
