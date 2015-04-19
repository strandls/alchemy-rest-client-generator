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

package com.strandls.alchemy.rest.client.stubgenerator;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;

import com.strandls.alchemy.rest.client.NotRestInterfaceException;
import com.strandls.alchemy.rest.client.RestInterfaceAnalyzer;
import com.strandls.alchemy.rest.client.RestInterfaceMetadata;
import com.strandls.alchemy.rest.client.RestMethodMetadata;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

/**
 * Generate a stub interface for a restful service. All the service class and
 * method jaxrs {@link Annotation}s are preserved.
 *
 * @author ashish
 *
 */
@RequiredArgsConstructor(onConstructor = @_(@Inject))
@Singleton
public class ServiceStubGenerator {
    /**
     * Annotation value parameter name.
     */
    private static final String ANNOTATION_VALUE_PARAM_NAME = "value";

    /**
     * The rest interface analyzer.
     */
    private final RestInterfaceAnalyzer interfaceAnalyzer;

    /**
     * Add annotation to the annotatable.
     *
     * @param jAnnotatable
     *            the annotatable.
     * @param annotation
     *            the annotation class.
     */
    private void addAnnotation(final JAnnotatable jAnnotatable,
            final Class<? extends Annotation> annotation) {
        jAnnotatable.annotate(annotation);
    }

    /**
     * Add a list valued annotation the annotatable.
     *
     * @param jAnnotatable
     *            the annotatable
     * @param annotation
     *            the annotation
     * @param param
     *            the name of the parameter
     * @param values
     *            the values.
     */
    private void addListAnnotation(final JAnnotatable jAnnotatable,
            final Class<? extends Annotation> annotation, final String param,
            final Collection<String> values) {
        if (!values.isEmpty()) {
            final JAnnotationArrayMember annotationValues =
                    jAnnotatable.annotate(annotation).paramArray(param);
            for (final String value : values) {
                annotationValues.param(value);
            }
        }
    }

    /**
     * Add a rest method to the parent class.
     *
     * @param jCodeModel
     *            the code model.
     * @param jParentClass
     *            the parent class.
     * @param method
     *            the method.
     * @param methodMetaData
     *            the method metadata.
     */
    private void addMethod(final JCodeModel jCodeModel, final JDefinedClass jParentClass,
            final Method method, final RestMethodMetadata methodMetaData) {
        final String mehtodName = method.getName();

        final JType result =
                typeToJType(method.getReturnType(), method.getGenericReturnType(), jCodeModel);

        final JMethod jMethod = jParentClass.method(JMod.PUBLIC, result, mehtodName);

        @SuppressWarnings("unchecked")
        final Class<? extends Throwable>[] exceptionTypes =
                (Class<? extends Throwable>[]) method.getExceptionTypes();

        for (final Class<? extends Throwable> exceptionCType : exceptionTypes) {
            jMethod._throws(exceptionCType);
        }

        addSingleValueAnnotation(jMethod, Path.class, ANNOTATION_VALUE_PARAM_NAME,
                methodMetaData.getPath());

        addListAnnotation(jMethod, Produces.class, ANNOTATION_VALUE_PARAM_NAME,
                methodMetaData.getProduced());
        addListAnnotation(jMethod, Consumes.class, ANNOTATION_VALUE_PARAM_NAME,
                methodMetaData.getConsumed());

        final String httpMethod = methodMetaData.getHttpMethod();
        Class<? extends Annotation> httpMethodAnnotation = null;
        if (HttpMethod.GET.equals(httpMethod)) {
            httpMethodAnnotation = GET.class;
        } else if (HttpMethod.PUT.equals(httpMethod)) {
            httpMethodAnnotation = PUT.class;
        } else if (HttpMethod.POST.equals(httpMethod)) {
            httpMethodAnnotation = POST.class;
        } else if (HttpMethod.DELETE.equals(httpMethod)) {
            httpMethodAnnotation = DELETE.class;
        }

        addAnnotation(jMethod, httpMethodAnnotation);

        final Annotation[][] parameterAnnotations = methodMetaData.getParameterAnnotations();

        final Type[] argumentTypes = method.getGenericParameterTypes();
        final Class<?>[] argumentClasses = method.getParameterTypes();
        for (int i = 0; i < argumentTypes.length; i++) {
            final JType jType = typeToJType(argumentClasses[i], argumentTypes[i], jCodeModel);
            // we have lost the actual names, use generic arg names
            final String name = "arg" + i;
            final JVar param = jMethod.param(jType, name);
            if (parameterAnnotations.length > i) {
                for (final Annotation annotation : parameterAnnotations[i]) {
                    final JAnnotationUse jAnnotation = param.annotate(annotation.annotationType());
                    final String value = getValue(annotation);
                    if (value != null) {
                        jAnnotation.param(ANNOTATION_VALUE_PARAM_NAME, value);
                    }
                }
            }
        }
    }

    /**
     * Add a single valued annotation to the annotatable.
     *
     * @param jAnnotatable
     *            the annotatable.
     * @param annotation
     *            the annotation class.
     * @param param
     *            the name of the param
     * @param value
     *            the value of the param
     */
    private void addSingleValueAnnotation(final JAnnotatable jAnnotatable,
            final Class<? extends Annotation> annotation, final String param, final String value) {
        if (!StringUtils.isBlank(value)) {
            jAnnotatable.annotate(annotation).param(param, value);
        }
    }

    /**
     * Generate a stub interface for a rest web service implemented by the input
     * service class.
     *
     * <p>
     * The code writer is not close to allow for appends to same code writer.
     * The caller should close the code writer.
     * </p>
     *
     * @param serviceClass
     *            the input rest service class.
     * @param destinationInterfaceName
     *            the name of the destination interface
     * @param destinationPackage
     *            the destination package name.
     * @param codeWriter
     *            the writer to output the source to.
     * @throws NotRestInterfaceException
     *             if the service class is not a rest service.
     * @throws Exception
     *             if code generation fails.
     */
    public void generateStubInterface(final Class<?> serviceClass,
            final String destinationInterfaceName, final String destinationPackage,
            final CodeWriter codeWriter) throws NotRestInterfaceException, Exception {
        final RestInterfaceMetadata metaData = interfaceAnalyzer.analyze(serviceClass);

        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jPackage = jCodeModel._package(destinationPackage);
        final JDefinedClass jInterface = jPackage._interface(destinationInterfaceName);
        final String path = metaData.getPath();
        addSingleValueAnnotation(jInterface, Path.class, ANNOTATION_VALUE_PARAM_NAME, path);

        // add consumes annotation
        addListAnnotation(jInterface, Consumes.class, ANNOTATION_VALUE_PARAM_NAME,
                metaData.getConsumed());

        // add produces annotation
        addListAnnotation(jInterface, Produces.class, ANNOTATION_VALUE_PARAM_NAME,
                metaData.getProduced());
        final JDocComment jDocComment = jInterface.javadoc();
        jDocComment.add(String.format("Client side stub interface for {@link %s}.",
                serviceClass.getCanonicalName()));

        final List<Entry<Method, RestMethodMetadata>> methodEntries =
                new ArrayList<>(metaData.getMethodMetaData().entrySet());

        // sort methods alphabetically to give consistent order.
        Collections.sort(methodEntries, new Comparator<Entry<Method, RestMethodMetadata>>() {

            @Override
            public int compare(final Entry<Method, RestMethodMetadata> o1,
                    final Entry<Method, RestMethodMetadata> o2) {
                return o1.getKey().toGenericString().compareTo(o2.getKey().toGenericString());
            }
        });

        // generate methods.
        for (final Entry<Method, RestMethodMetadata> methodEntry : methodEntries) {
            final Method method = methodEntry.getKey();
            final RestMethodMetadata methodMetaData = methodEntry.getValue();
            addMethod(jCodeModel, jInterface, method, methodMetaData);

        }

        jCodeModel.build(codeWriter);
    }

    /**
     * Return {@link #ANNOTATION_VALUE_PARAM_NAME} for given annotation, if that
     * is a field.
     *
     * @param annotation
     *            the annotation
     * @return the string value if it exists, else null.
     */
    private String getValue(final Annotation annotation) {
        final Class<? extends Annotation> klass = annotation.getClass();
        Method method;
        try {
            method = klass.getDeclaredMethod(ANNOTATION_VALUE_PARAM_NAME);
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
        try {
            return (String) method.invoke(annotation);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
    }

    /**
     * Convert a generic {@link Type} to {@link JType}.
     *
     * @param rawType
     *            the raw type
     * @param type
     *            the generic type
     * @param jCodeModel
     *            the code model
     *
     * @return converted {@link JType}.
     */
    private JType typeToJType(final Class<?> rawType, final Type type, final JCodeModel jCodeModel) {
        final JType jType = jCodeModel._ref(rawType);
        if (jType instanceof JPrimitiveType) {
            return jType;
        }
        JClass result = (JClass) jType;
        if (type instanceof ParameterizedType) {
            for (final Type typeArgument : ((ParameterizedType) type).getActualTypeArguments()) {
                if (typeArgument instanceof WildcardType) {
                    result = result.narrow(jCodeModel.wildcard());
                } else if (typeArgument instanceof Class) {
                    result = result.narrow(jCodeModel._ref((Class<?>) typeArgument));
                }
            }
        }
        return result;
    }
}
