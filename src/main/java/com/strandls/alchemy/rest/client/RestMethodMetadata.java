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

import java.lang.annotation.Annotation;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Metadata for a rest method class obtained from annotations on the method.
 *
 * @author ashish
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(onConstructor = @_(@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value = "EI_EXPOSE_REP", justification = "This is the best way of passing annotations.")))
public class RestMethodMetadata {
    /**
     * The path for the method.
     */
    private final String path;
    /**
     * The {@link javax.ws.rs.HttpMethod}.
     */
    @NonNull
    private final String httpMethod;

    /**
     * List of media types produced.
     */
    @NonNull
    private final List<String> produced;

    /**
     * List of media types consumed.
     */
    @NonNull
    private final List<String> consumed;

    /**
     * Annotations on parameters keyed by the index of the parameter.
     */
    @NonNull
    @Getter(
            onMethod = @_(@edu.umd.cs.findbugs.annotations.SuppressWarnings(
                    value = "EI_EXPOSE_REP",
                    justification = "This is the best way of passing annotations.")))
    private final Annotation[][] parameterAnnotations;
}
