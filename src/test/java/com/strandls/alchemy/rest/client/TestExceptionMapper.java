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

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import lombok.RequiredArgsConstructor;

import com.strandls.alchemy.rest.client.exception.ThrowableToResponseMapper;

/**
 * Mapper for testing exceptions.
 *
 * @author ashish
 *
 */
@Provider
@RequiredArgsConstructor(onConstructor = @_(@Inject))
public class TestExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {
    /**
     * The response mapper.
     */
    private final ThrowableToResponseMapper responseMapper;

    /*
     * (non-Javadoc)
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
     */
    @Override
    public Response toResponse(final Exception exception) {
        return responseMapper.apply(exception);
    }

}
