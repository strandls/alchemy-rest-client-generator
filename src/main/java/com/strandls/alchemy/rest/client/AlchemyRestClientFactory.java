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

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javassist.Modifier;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.objenesis.ObjenesisStd;

import com.strandls.alchemy.rest.client.exception.ResponseToThrowableMapper;
import com.strandls.alchemy.rest.client.request.RequestBuilderFilter;

/**
 * Factory for jersey based proxy clients.
 *
 * @author Ashish Shinde
 *
 */
@Singleton
@Slf4j
public class AlchemyRestClientFactory {
    /**
     * Handles rest method invocation for a single rest service.
     *
     * @author Ashish Shinde
     *
     */
    @RequiredArgsConstructor
    private static class RestMethodInvocationHandler implements MethodHandler {
        /**
         * The base URI.
         */
        private final String baseUri;

        /**
         * Jax rs client provider.
         */
        private final Provider<Client> clientProvider;

        /**
         * Rest interface metadata.
         */
        private final RestInterfaceMetadata restInterfaceMetadata;

        /**
         * Maps server side errors to local errors.
         */
        private final ResponseToThrowableMapper responseToThrowableMapper;

        /**
         * The request builder filter.
         */
        private final RequestBuilderFilter builderFilter;

        /**
         * Create the path for the rest method.
         *
         * @param methodMetaData
         *            the method meta data.
         * @param arguments
         *            the method arguments, used to add matrix and path
         *            parameters to the generated path.
         * @return the absolute remote rest path for this method invocation.
         */
        private String getPath(final RestMethodMetadata methodMetaData, final Object[] arguments) {
            final UriBuilder uriBuilder = UriBuilder.fromPath(baseUri);

            if (!StringUtils.isBlank(restInterfaceMetadata.getPath())) {
                uriBuilder.path(restInterfaceMetadata.getPath());
            }

            if (!StringUtils.isBlank(methodMetaData.getPath())) {
                uriBuilder.path(methodMetaData.getPath());
            }

            // add matrix parameters to the path
            final Annotation[][] parameterAnnotations = methodMetaData.getParameterAnnotations();
            final Map<String, Object> pathParamsMap = new LinkedHashMap<String, Object>();
            for (int i = 0; i < parameterAnnotations.length && i < arguments.length; i++) {
                final Annotation[] annotations = parameterAnnotations[i];

                final Object argument = arguments[i];
                for (final Annotation annotation : annotations) {
                    if (annotation instanceof MatrixParam) {
                        final String name = ((MatrixParam) annotation).value();
                        Object[] values = new Object[] {};
                        if (argument != null && argument.getClass().isArray()) {
                            values = (Object[]) argument;
                        } else if (argument instanceof Collection) {
                            @SuppressWarnings("unchecked")
                            final Collection<Object> collection = (Collection<Object>) argument;
                            values = collection.toArray();
                        } else {
                            values = new Object[] { argument };
                        }
                        uriBuilder.matrixParam(name, values);
                    } else if (annotation instanceof PathParam) {
                        pathParamsMap.put(((PathParam) annotation).value(), argument);
                    }
                }
            }

            // add path params to the path
            return uriBuilder.buildFromMap(pathParamsMap).toString();
        }

        /*
         * (non-Javadoc)
         * @see javassist.util.proxy.MethodHandler#invoke(java.lang.Object,
         * java.lang.reflect.Method, java.lang.reflect.Method,
         * java.lang.Object[])
         */
        @Override
        public Object invoke(final Object self, final Method thisMethod, final Method proceed,
                final Object[] arguments) throws Throwable {

            final RestMethodMetadata methodMetaData =
                    restInterfaceMetadata.getMethodMetaData().get(thisMethod);

            if (methodMetaData == null) {
                throw new NotRestMethodException(thisMethod);
            }

            final String path = getPath(methodMetaData, arguments);
            log.debug("Invoking rest service at {}", path);

            final Client client = clientProvider.get();

            WebTarget webTarget = client.target(path);
            Entity<?> entity = null;

            final Annotation[][] parameterAnnotations = methodMetaData.getParameterAnnotations();

            final String bodyParameterMediaType =
                    methodMetaData.getConsumed().isEmpty() ? null : methodMetaData.getConsumed()
                            .get(0);

            // set query params
            for (int i = 0; i < arguments.length; i++) {
                final Object argument = arguments[i];

                if (parameterAnnotations.length <= i) {
                    // body parameter without annotation
                    entity = toEntity(argument, bodyParameterMediaType);
                    continue;
                }

                final Annotation[] annotations = parameterAnnotations[i];

                if (annotations == null || annotations.length == 0) {
                    // body parameter without annotation
                    entity = toEntity(argument, bodyParameterMediaType);
                    continue;
                }

                for (final Annotation annotation : annotations) {
                    if (annotation instanceof QueryParam) {
                        final String key = ((QueryParam) annotation).value();
                        final String value = ObjectUtils.toString(argument);
                        webTarget = webTarget.queryParam(key, value);
                    }
                }

            }

            // create the request builder
            Builder webRequestBuilder = webTarget.request();
            webRequestBuilder.accept(methodMetaData.getProduced().toArray(new String[0]));
            webRequestBuilder =
                    webRequestBuilder.accept(methodMetaData.getConsumed().toArray(new String[0]));

            if (builderFilter != null) {
                builderFilter.apply(webRequestBuilder);
            }

            // for form params
            Form formParams = null;
            // process cookie and header params
            for (int i = 0; i < arguments.length; i++) {
                if (i >= parameterAnnotations.length) {
                    continue;
                }

                final Annotation[] annotations = parameterAnnotations[i];
                final Object argument = arguments[i];

                for (final Annotation annotation : annotations) {
                    if (annotation instanceof CookieParam) {
                        // add cookie to the request
                        Cookie cookie = null;
                        if (argument instanceof Cookie) {
                            cookie = (Cookie) argument;
                        } else {
                            cookie =
                                    new Cookie(((CookieParam) annotation).value(),
                                            ObjectUtils.toString(argument));
                        }
                        webRequestBuilder = webRequestBuilder.cookie(cookie);
                    } else if (annotation instanceof HeaderParam) {
                        // add header param
                        final String key = ((HeaderParam) annotation).value();
                        final String value = ObjectUtils.toString(argument);
                        webRequestBuilder = webRequestBuilder.header(key, value);
                    } else if (annotation instanceof FormParam) {
                        if (formParams == null) {
                            formParams = new javax.ws.rs.core.Form();
                        }
                        formParams.param(((FormParam) annotation).value(),
                                ObjectUtils.toString(argument));
                    }
                }
            }

            if (formParams != null) {
                // cannot have form parameters and body parameters without
                // annotation
                assert entity == null;

                // for form parameters the method should be post
                assert HttpMethod.POST.equals(methodMetaData.getHttpMethod());

                entity = toEntity(formParams, MediaType.APPLICATION_FORM_URLENCODED);
            }

            final FormDataMultiPart formDataMultiPart =
                    processFormDataParams(arguments, parameterAnnotations);

            if (formDataMultiPart != null) {
                // Cannot have form parameters and body parameters without
                // annotation
                assert entity == null;

                // for form parameters the method should be post
                assert HttpMethod.POST.equals(methodMetaData.getHttpMethod());

                entity = toEntity(formDataMultiPart, formDataMultiPart.getMediaType().toString());
            }

            // Get the return type.
            @SuppressWarnings("rawtypes")
            final GenericType<?> returnType = new GenericType(thisMethod.getGenericReturnType()) {
            };

            try {
                Object retval = null;
                final String httpMethod = methodMetaData.getHttpMethod();
                if (HttpMethod.GET.equals(httpMethod)) {
                    retval = webRequestBuilder.get(returnType);
                } else if (HttpMethod.PUT.equals(httpMethod)) {
                    retval = webRequestBuilder.put(entity, returnType);
                } else if (HttpMethod.POST.equals(httpMethod)) {
                    retval = webRequestBuilder.post(entity, returnType);
                } else if (HttpMethod.DELETE.equals(httpMethod)) {
                    retval = webRequestBuilder.delete(returnType);
                }
                return retval;
            } catch (final InternalServerErrorException e) {
                throw responseToThrowableMapper.apply(e.getResponse());
            }

        }

        /**
         * Process form data params.
         *
         * @param arguments
         *            the function call arguments.
         * @param parameterAnnotations
         *            function parameter annotations.
         * @return form data multipart object if the funtion contains form data
         *         elements.
         */
        private FormDataMultiPart processFormDataParams(final Object[] arguments,
                final Annotation[][] parameterAnnotations) {
            FormDataMultiPart formDataMultiPart = null;
            // map from param name to content disposition
            final Map<String, ContentDisposition> contentDispositions =
                    new HashMap<String, ContentDisposition>();

            // map from param name to input streams
            final Map<String, InputStream> inputstreams = new HashMap<String, InputStream>();
            for (int i = 0; i < arguments.length; i++) {
                if (i >= parameterAnnotations.length) {
                    continue;
                }

                final Annotation[] annotations = parameterAnnotations[i];
                for (final Annotation annotation : annotations) {
                    if (annotation instanceof FormDataParam) {
                        if (formDataMultiPart == null) {
                            formDataMultiPart = new FormDataMultiPart();
                        }

                        final Object argument = arguments[i];
                        final String paramName = ((FormDataParam) annotation).value();
                        if (argument instanceof File) {
                            formDataMultiPart.bodyPart(new FileDataBodyPart(paramName,
                                    (File) argument));
                        } else if (argument instanceof InputStream) {
                            inputstreams.put(paramName, (InputStream) argument);
                        } else if (argument instanceof FormDataContentDisposition) {
                            contentDispositions.put(paramName, (ContentDisposition) argument);
                        } else {
                            formDataMultiPart.field(paramName, ObjectUtils.toString(argument));
                        }

                    }
                }
            }

            if (formDataMultiPart != null && !inputstreams.isEmpty()) {
                // we have input streams that may have content dispositions
                for (final Entry<String, InputStream> streamEntry : inputstreams.entrySet()) {
                    final String paramName = streamEntry.getKey();
                    if (contentDispositions.containsKey(paramName)) {
                        // we have a content disposition for this input stream
                        final ContentDisposition contentDisposition =
                                contentDispositions.get(paramName);
                        final StreamDataBodyPart streamBodyPart =
                                new StreamDataBodyPart(paramName, streamEntry.getValue(),
                                        contentDisposition.getFileName());
                        formDataMultiPart.bodyPart(streamBodyPart);
                    } else {
                        final StreamDataBodyPart streamBodyPart =
                                new StreamDataBodyPart(paramName, streamEntry.getValue(), paramName);
                        formDataMultiPart.bodyPart(streamBodyPart);
                    }
                }
            }

            return formDataMultiPart;
        }

        /**
         * Converts an object to an entity.
         *
         * @param object
         *            the object.
         * @param bodyParameterMediaType
         *            the media type. If <code>null</code>
         *            {@link MediaType#MEDIA_TYPE_WILDCARD} will be used.
         * @return converted entity.
         */
        private Entity<Object> toEntity(final Object object, String bodyParameterMediaType) {

            if (object instanceof MultiPart) {
                bodyParameterMediaType = ((MultiPart) object).getMediaType().toString();
            }

            return !StringUtils.isBlank(bodyParameterMediaType) ? Entity.entity(object,
                    bodyParameterMediaType) : Entity.entity(object, MediaType.MEDIA_TYPE_WILDCARD);
        }
    }

    /**
     * The base uri named param name.
     */
    public static final String BASE_URI_NAMED_PARAM =
            "com.strandls.alchemy.rest.client.AlchemyRestClientFactory.baseURI";

    /**
     * The base URI.
     */
    @NonNull
    private final String baseUri;

    /**
     * Jax rs client provider.
     */
    @NonNull
    private final Provider<Client> clientProvider;

    /**
     * The rest interface analyzer.
     */
    private final RestInterfaceAnalyzer interfaceAnalyzer;

    /**
     * Object instantiator.
     */
    private final ObjenesisStd objenesis;

    /**
     * Maps {@link Response} to a {@link Throwable} object for server side
     * exceptions.
     */
    private final ResponseToThrowableMapper responseToThrowableMapper;

    /**
     * The request biulder filter.
     */
    private final RequestBuilderFilter builderFilter;

    /**
     * Creates the new factory.
     *
     * @param baseUri
     *            the base URI for the rest service.
     * @param clientProvider
     *            the {@link Client} provider.
     * @param interfaceAnalyzer
     *            the interface analyzer.
     */
    @Inject
    public AlchemyRestClientFactory(@Named(BASE_URI_NAMED_PARAM) final String baseUri,
            final Provider<Client> clientProvider, final RestInterfaceAnalyzer interfaceAnalyzer,
            final ResponseToThrowableMapper responseToThrowableMapper,
            final RequestBuilderFilter builderFilter) {
        this.baseUri = baseUri;
        this.clientProvider = clientProvider;
        this.interfaceAnalyzer = interfaceAnalyzer;
        this.objenesis = new ObjenesisStd();
        this.responseToThrowableMapper = responseToThrowableMapper;
        this.builderFilter = builderFilter;
    }

    /**
     * Get an instance of a rest proxy instance for the service class.
     *
     * @param serviceClass
     *            the service class.
     * @return the proxy implementation that invokes the remote service.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstance(@NonNull final Class<T> serviceClass) throws Exception {
        final ProxyFactory factory = new ProxyFactory();
        if (serviceClass.isInterface()) {
            factory.setInterfaces(new Class[] { serviceClass });
        } else {
            factory.setSuperclass(serviceClass);
        }
        factory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(final Method method) {
                return Modifier.isPublic(method.getModifiers());
            }
        });

        final Class<?> klass = factory.createClass();
        final Object instance = objenesis.getInstantiatorOf(klass).newInstance();
        ((ProxyObject) instance).setHandler(new RestMethodInvocationHandler(baseUri,
                clientProvider, interfaceAnalyzer.analyze(serviceClass), responseToThrowableMapper,
                builderFilter));
        return (T) instance;
    }
}
