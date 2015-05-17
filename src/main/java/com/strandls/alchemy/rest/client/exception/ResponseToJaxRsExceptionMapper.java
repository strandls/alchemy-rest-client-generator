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

import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.internal.LocalizationMessages;

import com.google.common.base.Function;

/**
 * Maps {@link Response} to an {@link ProcessingException}.
 *
 * @author Ashish Shinde
 *
 */
@Singleton
public class ResponseToJaxRsExceptionMapper implements Function<Response, Exception> {

    /*
     * (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public Exception apply(final Response response) {
        try {
            WebApplicationException webAppException;
            final int statusCode = response.getStatus();
            final Response.Status status = Response.Status.fromStatusCode(statusCode);
            if (status == null) {
                final Response.Status.Family statusFamily = response.getStatusInfo().getFamily();
                webAppException = createExceptionForFamily(response, statusFamily);
            } else {
                switch (status) {
                case BAD_REQUEST:
                    webAppException = new BadRequestException(response);
                    break;
                case UNAUTHORIZED:
                    webAppException = new NotAuthorizedException(response);
                    break;
                case FORBIDDEN:
                    webAppException = new ForbiddenException(response);
                    break;
                case NOT_FOUND:
                    webAppException = new NotFoundException(response);
                    break;
                case METHOD_NOT_ALLOWED:
                    webAppException = new NotAllowedException(response);
                    break;
                case NOT_ACCEPTABLE:
                    webAppException = new NotAcceptableException(response);
                    break;
                case UNSUPPORTED_MEDIA_TYPE:
                    webAppException = new NotSupportedException(response);
                    break;
                case INTERNAL_SERVER_ERROR:
                    webAppException = new InternalServerErrorException(response);
                    break;
                case SERVICE_UNAVAILABLE:
                    webAppException = new ServiceUnavailableException(response);
                    break;
                default:
                    final Response.Status.Family statusFamily =
                    response.getStatusInfo().getFamily();
                    webAppException = createExceptionForFamily(response, statusFamily);
                }
            }

            return webAppException;
        } catch (final Throwable t) {
            return new ProcessingException(
                    LocalizationMessages.RESPONSE_TO_EXCEPTION_CONVERSION_FAILED(), t);
        }
    }

    /**
     * Convert an exception to an exception. Ripped off from
     * {@link JerseyInvocation}.
     *
     * @param response
     * @param statusFamily
     * @return
     */
    private WebApplicationException createExceptionForFamily(final Response response,
            final Response.Status.Family statusFamily) {
        WebApplicationException webAppException;
        switch (statusFamily) {
        case REDIRECTION:
            webAppException = new RedirectionException(response);
            break;
        case CLIENT_ERROR:
            webAppException = new ClientErrorException(response);
            break;
        case SERVER_ERROR:
            webAppException = new ServerErrorException(response);
            break;
        default:
            webAppException = new WebApplicationException(response);
        }
        return webAppException;
    }

}
