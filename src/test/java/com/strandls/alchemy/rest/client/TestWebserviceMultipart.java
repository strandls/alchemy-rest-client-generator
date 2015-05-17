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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPart;

/**
 * A webservice for testing multipart for data based web services.
 *
 * @author Ashish Shinde
 *
 */
@Path("/testmultipart")
public class TestWebserviceMultipart {
    /**
     * Echoes a multipart request.
     *
     * @param multiPart
     * @return
     */
    @Path("/multipartEcho")
    @POST
    @Produces("multipart/mixed")
    public MultiPart multipartEcho(final FormDataMultiPart multiPart) {
        return multiPart;
    }

    /**
     * Echoes a multipart request including file content as a map of strings.
     *
     * @param multiPart
     * @return
     * @throws IOException
     */
    @Path("/multipartMapEcho")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Map<String, String> multipartMapEcho(
            @DefaultValue("true") @FormDataParam("enabled") final boolean enabled,
            @FormDataParam("secret") final String secret,
            @FormDataParam("file") final InputStream file,
            @FormDataParam("file") final FormDataContentDisposition fileDisposition)
                    throws IOException {
        final Map<String, String> echo = new HashMap<String, String>();
        echo.put("enabled", ObjectUtils.toString(enabled));
        echo.put("secret", secret);
        echo.put("file", IOUtils.toString(file));
        echo.put("filename", fileDisposition.getFileName());
        echo.put("filetype", fileDisposition.getType());
        return echo;
    }

    /**
     * Echoes a multipart request including file content as a map of strings.
     *
     * @throws IOException
     */
    @Path("/multipartMapEchoWithoutDisposition")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Map<String, String> multipartMapEchoWithoutDisposition(
            @DefaultValue("true") @FormDataParam("enabled") final boolean enabled,
            @FormDataParam("secret") final String secret,
            @FormDataParam("file") final InputStream file) throws IOException {
        final Map<String, String> echo = new HashMap<String, String>();
        echo.put("enabled", ObjectUtils.toString(enabled));
        echo.put("secret", secret);
        echo.put("file", IOUtils.toString(file));
        return echo;
    }

    /**
     * Echoes a multipart request as a map of strings.
     *
     * @throws IOException
     */
    @Path("/multipartMapEchoPrimitive")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Map<String, String> multipartMapEchoPrimitive(
            @DefaultValue("true") @FormDataParam("enabled") final boolean enabled,
            @FormDataParam("secret") final String secret) throws IOException {
        final Map<String, String> echo = new HashMap<String, String>();
        echo.put("enabled", ObjectUtils.toString(enabled));
        echo.put("secret", secret);
        return echo;
    }

}
