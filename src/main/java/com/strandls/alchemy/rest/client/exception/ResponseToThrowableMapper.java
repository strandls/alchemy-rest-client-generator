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

package com.strandls.alchemy.rest.client.exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;

/**
 * Converts a {@link Response} to a {@link Throwable} object.
 *
 * @author ashish
 *
 */
@Singleton
public class ResponseToThrowableMapper implements Function<Response, Throwable> {
    /**
     * The object mapper to be used.
     */
    private final ObjectMapper exceptionObjectMapper;

    /**
     * From response to a {@link ProcessingException}.
     */
    private final ResponseToJaxRsExceptionMapper jaxRxExceptionMapper;

    /**
     * @param exceptionObjectMapper
     */
    @Inject
    public ResponseToThrowableMapper(
            @ThrowableObjectMapper final ObjectMapper exceptionObjectMapper,
            final ResponseToJaxRsExceptionMapper jaxRxExceptionMapper) {
        this.exceptionObjectMapper = exceptionObjectMapper;
        this.jaxRxExceptionMapper = jaxRxExceptionMapper;
    }

    /*
     * (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public Throwable apply(final Response input) {
        // Buffer and close entity input stream (if any) to prevent
        // leaking connections (see JERSEY-2157).
        input.bufferEntity();

        String payLoadStr = null;
        try {
            payLoadStr = IOUtils.toString(input.readEntity(InputStream.class));
        } catch (final IOException e1) {
            return jaxRxExceptionMapper.apply(input);
        }
        try {
            final ExceptionPayload payload =
                    exceptionObjectMapper.readValue(payLoadStr, ExceptionPayload.class);
            return payload.getException();
        } catch (final IOException e) {
            final String message = getMessage(payLoadStr);
            if (message != null) {
                return new WebApplicationException(message, input.getStatus());
            }
        }

        // falback to standard jersey conversion
        return jaxRxExceptionMapper.apply(input);
    }

    /**
     * Get message from the payload.
     *
     * @param payLoadStr
     * @return
     */
    private String getMessage(final String payLoadStr) {

        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> exceptionObject =
                    exceptionObjectMapper.readValue(payLoadStr, Map.class);
            if (exceptionObject.containsKey("exceptionMessage")) {
                return ObjectUtils.toString(exceptionObject.get("exceptionMessage"));
            }
        } catch (final IOException e) {
            // could not determine the message
        }
        return null;
    }
}
