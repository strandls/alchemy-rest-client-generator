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

package com.strandls.alchemy.rest.client.exception;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;

/**
 * Converts a {@link Throwable} to a {@link Response}.
 *
 * @author Ashish Shinde
 *
 */
@Slf4j
@Singleton
public class ThrowableToResponseMapper implements Function<Throwable, Response> {
    /**
     * The object mapper to be used.
     */
    private final ObjectMapper exceptionObjectMapper;

    /**
     * @param exceptionObjectMapper
     */
    @Inject
    public ThrowableToResponseMapper(@ThrowableObjectMapper final ObjectMapper exceptionObjectMapper) {
        this.exceptionObjectMapper = exceptionObjectMapper;
    }

    /*
     * (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public Response apply(final Throwable input) {

        final ExceptionPayload payLoad = new ExceptionPayload(input);
        try {
            final int statusCode =
                    input instanceof WebApplicationException ? ((WebApplicationException) input)
                            .getResponse().getStatus() : Status.INTERNAL_SERVER_ERROR
                            .getStatusCode();

            return Response.status(Status.fromStatusCode(statusCode))
                                    .entity(exceptionObjectMapper.writeValueAsString(payLoad)).build();
        } catch (final JsonProcessingException e) {
            log.warn("Error deserializing exception.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(input.getMessage()).build();
        }
    }
}
