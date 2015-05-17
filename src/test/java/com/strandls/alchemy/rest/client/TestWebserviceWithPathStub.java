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

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * Client side stub interface for
 * {@link com.strandls.alchemy.rest.client.TestWebserviceWithPath}.
 *
 */
@Path("/test")
public interface TestWebserviceWithPathStub {

    @Path("/echoMatrixParams")
    @Produces({ "application/json" })
    @Consumes({ "application/json" })
    @GET
    public int[] echoMatrixParams(@MatrixParam("param1") final int arg0,
            @MatrixParam("param2") final int arg1, @MatrixParam("param3") final int arg2);

    @Path("/echoPathParams/{param1}/{param2}/{param3}")
    @Produces({ "application/json" })
    @Consumes({ "application/json" })
    @GET
    public int[] echoPathParams(@PathParam("param1") final int arg0,
            @PathParam("param2") final int arg1, @PathParam("param3") final int arg2);

    @Path("/echoMixedParams/{param1}/")
    @Produces({ "application/json" })
    @Consumes({ "application/json" })
    @GET
    public int[] echoMixedParams(@PathParam("param1") final int arg0,
            @MatrixParam("param2") final int arg1, @QueryParam("param3") final int arg2);

    @Path("/echoQueryParams")
    @Produces({ "application/json" })
    @Consumes({ "application/json" })
    @GET
    public int[] echoQueryParams(@QueryParam("param1") final int arg0,
            @QueryParam("param2") final int arg1, @QueryParam("param3") final int arg2);

    @Path("/echoHeaderParams/{param1}/")
    @Produces({ "application/json" })
    @Consumes({ "application/json" })
    @GET
    public int[] echoHeaderParams(@PathParam("param1") final int arg0,
            @HeaderParam("param2") final int arg1, @CookieParam("param3") final int arg2);

    @Produces({ "application/json" })
    @Consumes({ "application/json" })
    @POST
    public int[] echo(final int[] arg0);
}
