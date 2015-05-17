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

import static org.junit.Assert.assertEquals;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

/**
 * Unit tests for {@link ResponseToJaxRsExceptionMapper}.
 *
 * @author Ashish Shinde
 *
 */
public class ResponseToJaxRsExceptionMapperTest {

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.exception.ResponseToJaxRsExceptionMapper#apply(javax.ws.rs.core.Response)}
     * .
     */
    @Test
    public void testApply() {
        final ResponseToJaxRsExceptionMapper mapper = new ResponseToJaxRsExceptionMapper();
        for (final Status status : Response.Status.values()) {
            final Response response = Response.status(status).build();
            assertEquals(status.getStatusCode(), ((WebApplicationException) mapper.apply(response))
                    .getResponse().getStatus());
        }
    }
}
