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

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * @author ashish
 *
 */
@Path("/test")
public class TestWebserviceWithPath {
    /**
     * Echo with a single body parameter as json.
     *
     * @param params
     *            the input int array.
     * @return the parameters as an integer array in the order of parameters.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public int[] echo(final int[] params) {
        return params;
    }

    /**
     * Echo Service method with mix of cookie and header params.
     *
     * @param param1
     *            the first int.
     * @param param2
     *            the second int.
     * @param param3
     *            the third int.
     * @return the parameters as an integer array in the order of parameters.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/echoHeaderParams/{param1}/")
    public int[] echoHeaderParams(@PathParam("param1") final int param1,
            @HeaderParam("param2") final int param2, @CookieParam("param3") final int param3) {
        return new int[] { param1, param2, param3 };
    }

    /**
     * Echo Service method with matrix params.
     *
     * @param param1
     *            the first int.
     * @param param2
     *            the second int.
     * @param param3
     *            the third int.
     * @return the parameters as an integer array in the order of parameters.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/echoMatrixParams")
    public int[] echoMatrixParams(@MatrixParam("param1") final int param1,
            @MatrixParam("param2") final int param2, @MatrixParam("param3") final int param3) {
        return new int[] { param1, param2, param3 };
    }

    /**
     * Echo Service method with mix of matrix, path and query params.
     *
     * @param param1
     *            the first int.
     * @param param2
     *            the second int.
     * @param param3
     *            the third int.
     * @return the parameters as an integer array in the order of parameters.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/echoMixedParams/{param1}/")
    public int[] echoMixedParams(@PathParam("param1") final int param1,
            @MatrixParam("param2") final int param2, @QueryParam("param3") final int param3) {
        return new int[] { param1, param2, param3 };
    }

    /**
     * Echo Service method with path params.
     *
     * @param param1
     *            the first int.
     * @param param2
     *            the second int.
     * @param param3
     *            the third int.
     * @return the parameters as an integer array in the order of parameters.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/echoPathParams/{param1}/{param2}/{param3}")
    public int[] echoPathParams(@PathParam("param1") final int param1,
            @PathParam("param2") final int param2, @PathParam("param3") final int param3) {
        return new int[] { param1, param2, param3 };
    }

    /**
     * Echo Service method with query params.
     *
     * @param param1
     *            the first int.
     * @param param2
     *            the second int.
     * @param param3
     *            the third int.
     * @return the parameters as an integer array in the order of parameters.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/echoQueryParams")
    public int[] echoQueryParams(@QueryParam("param1") final int param1,
            @QueryParam("param2") final int param2, @QueryParam("param3") final int param3) {
        return new int[] { param1, param2, param3 };
    }

    /**
     * Echo Service method with mix of query and form params.
     *
     * @param param1
     *            the first int.
     * @param param2
     *            the second int.
     * @param param3
     *            the third int.
     * @return the parameters as an integer array in the order of parameters.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/echoFormParams")
    public int[] echoFormParams(@FormParam("param1") final int param1,
            @FormParam("param2") final int param2, @QueryParam("param3") final int param3) {
        return new int[] { param1, param2, param3 };
    }

    /**
     * Noop webservice.
     */
    @POST
    @Path("/noOp")
    public void noOp() {
        System.out.println("");
    }

}
