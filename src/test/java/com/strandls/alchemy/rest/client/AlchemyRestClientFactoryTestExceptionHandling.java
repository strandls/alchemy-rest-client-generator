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

package com.strandls.alchemy.rest.client;

import static org.junit.Assert.fail;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ExceptionMapper;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Names;

/**
 * Unit tests for {@link AlchemyRestClientFactory} with no
 * {@link ExceptionMapper} configured.
 *
 * @author ashish
 *
 */
public class AlchemyRestClientFactoryTestExceptionHandling extends JerseyTest {
    /**
     * The client side guice module.
     *
     * @author ashish
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

        // register multi part feature.
        application.register(MultiPartFeature.class);

        return application;
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
        } catch (final Exception e) {
            // nothing much to test here.
        }

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
}
