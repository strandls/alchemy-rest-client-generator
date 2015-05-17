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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Analyzes {@link Class} to extract {@link RestInterfaceMetadata}.
 *
 * @author Ashish Shinde
 *
 */
@Singleton
public class RestInterfaceAnalyzer {
    /**
     * The cache from class to rest metadata.
     */
    private final LoadingCache<Class<?>, RestInterfaceMetadata> metadataCache;

    /**
     * Creates a new instance.
     */
    public RestInterfaceAnalyzer() {
        metadataCache =
                CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, RestInterfaceMetadata>() {

                    @Override
                    public RestInterfaceMetadata load(final Class<?> klass) throws Exception {
                        return doAnalyze(klass);
                    }
                });
    }

    /**
     * Analyze the class and build the meta information.
     *
     * @param klass
     *            the class.
     * @return analyzed metadata.
     */
    public RestInterfaceMetadata analyze(final Class<?> klass) throws NotRestInterfaceException {
        try {
            return metadataCache.getUnchecked(klass);
        } catch (final Exception e) {
            final Throwable cause = e.getCause();
            if (cause != null && cause instanceof NotRestInterfaceException) {
                throw (NotRestInterfaceException) cause;
            }
            throw e;
        }
    }

    /**
     * Get rest meta data for the method.
     *
     * @param method
     *            the method to analyze.
     * @return the metadata for a method.
     */
    private RestMethodMetadata analyzeMethod(final Method method) {
        String path = "";
        String httpMethod = null;
        final List<String> produced = new ArrayList<String>();
        final List<String> consumed = new ArrayList<String>();

        final Annotation[] annotations = method.getDeclaredAnnotations();
        for (final Annotation annotation : annotations) {
            if (annotation instanceof Path) {
                path = ((Path) annotation).value();
            } else if (annotation instanceof GET) {
                httpMethod = HttpMethod.GET;
            } else if (annotation instanceof PUT) {
                httpMethod = HttpMethod.PUT;
            } else if (annotation instanceof POST) {
                httpMethod = HttpMethod.POST;
            } else if (annotation instanceof DELETE) {
                httpMethod = HttpMethod.DELETE;
            } else if (annotation instanceof Produces) {
                final Produces produces = (Produces) annotation;
                produced.addAll(Arrays.asList(produces.value()));
            } else if (annotation instanceof Consumes) {
                final Consumes consumes = (Consumes) annotation;
                consumed.addAll(Arrays.asList(consumes.value()));
            }
        }

        if (StringUtils.isBlank(httpMethod)) {
            // no http method specified.
            return null;
        }
        return new RestMethodMetadata(path, httpMethod, produced, consumed,
                method.getParameterAnnotations());
    }

    /**
     * Analyze the class and build the meta information.
     *
     * @param klass
     *            the class.
     * @return analyzed metadata.
     * @throws NotRestInterfaceException
     *             if the class analyzed is not a rest interface.
     */
    protected RestInterfaceMetadata doAnalyze(final Class<?> klass)
            throws NotRestInterfaceException {
        final List<String> produced = new ArrayList<String>();
        final List<String> consumed = new ArrayList<String>();
        String path = null;

        @SuppressWarnings("unchecked")
        final Set<Annotation> declaredAnnotationList = ReflectionUtils.getAllAnnotations(klass);
        for (final Annotation annotation : declaredAnnotationList) {
            if (annotation instanceof Path) {
                path = ((Path) annotation).value();
            } else if (annotation instanceof Produces) {
                final Produces produces = (Produces) annotation;
                final String[] values = produces.value();
                produced.addAll(Arrays.asList(values));
            } else if (annotation instanceof Consumes) {
                final Consumes consumes = (Consumes) annotation;
                final String[] values = consumes.value();
                consumed.addAll(Arrays.asList(values));
            }
        }

        final Map<Method, RestMethodMetadata> methodMetadataMap =
                new LinkedHashMap<Method, RestMethodMetadata>();

        @SuppressWarnings("unchecked")
        final Set<Method> methods = ReflectionUtils.getAllMethods(klass, new Predicate<Method>() {
            @Override
            public boolean apply(final Method input) {
                // return only public methods
                return Modifier.isPublic(input.getModifiers());
            }
        });

        for (final Method method : methods) {
            final RestMethodMetadata methodMetadata = analyzeMethod(method);
            if (methodMetadata != null) {
                methodMetadataMap.put(method, methodMetadata);
            }
        }

        if (methodMetadataMap.isEmpty()) {
            // not a valid rest interface.
            throw new NotRestInterfaceException(klass);
        }

        return new RestInterfaceMetadata(path, produced, consumed, methodMetadataMap);
    }
}
