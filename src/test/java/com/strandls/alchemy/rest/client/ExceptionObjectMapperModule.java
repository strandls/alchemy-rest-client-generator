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

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.strandls.alchemy.rest.client.exception.ThrowableMaskMixin;
import com.strandls.alchemy.rest.client.exception.ThrowableObjectMapper;

/**
 * Binding for {@link ObjectMapper} used for server side error conversions.
 *
 * @author ashish
 *
 */
public class ExceptionObjectMapperModule extends AbstractModule {

    /*
     * (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
    }

    /**
     * Binding for throwable exception mapper.
     *
     * @param mapper
     * @return
     */
    @Provides
    @Singleton
    @ThrowableObjectMapper
    public ObjectMapper getExceptionObjectMapper(final ObjectMapper mapper) {
        // can't copy owing to bug -
        // https://github.com/FasterXML/jackson-databind/issues/245
        final ObjectMapper exceptionMapper = mapper;
        exceptionMapper.registerModule(new SimpleModule() {
            /**
             * The serial version id.
             */
            private static final long serialVersionUID = 1L;

            /*
             * (non-Javadoc)
             * @see
             * com.fasterxml.jackson.databind.module.SimpleModule#setupModule
             * (com.fasterxml.jackson.databind.Module.SetupContext)
             */
            @Override
            public void setupModule(final SetupContext context) {
                context.setMixInAnnotations(Exception.class, ThrowableMaskMixin.class);
                context.setMixInAnnotations(TestCustomException.class, ThrowableMaskMixin.class);
            }
        });
        exceptionMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return exceptionMapper;
    }

}
