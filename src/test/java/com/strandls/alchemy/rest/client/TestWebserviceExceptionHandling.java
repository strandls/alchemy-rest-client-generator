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

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;

/**
 * A webservice for testing multipart for data based web services.
 *
 * @author ashish
 *
 */
@Path("/exception")
public class TestWebserviceExceptionHandling {
    /**
     * The exception string.
     */
    public static final String EXCEPTION_STRING = "A failing without a reason.";

    /**
     * Fails without a reason.
     *
     * @param multiPart
     * @return
     * @throws Exception
     */
    @Path("/fail")
    @GET
    public void fail() throws Exception {
        throw new Exception(EXCEPTION_STRING);
    }

    /**
     * Fails without a reason.
     *
     * @param multiPart
     * @return
     * @throws Exception
     */
    @Path("/failCustom")
    @GET
    public void failWithACustomException() throws Exception {
        throw new TestCustomException(10);
    }

    /**
     * Fails without a reason.
     *
     * @param multiPart
     * @return
     * @throws Exception
     */
    @Path("/fail404")
    @GET
    public void failInternal404() throws Exception {
        throw new NotFoundException();
    }

}
