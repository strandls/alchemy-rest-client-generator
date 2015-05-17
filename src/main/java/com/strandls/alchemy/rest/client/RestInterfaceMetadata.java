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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;

/**
 * Metadata for the rest interface / implementing class obtained from
 * annotations on the rest interface / class.
 *
 * @author Ashish Shinde
 *
 */
@Data
public class RestInterfaceMetadata {
    /**
     * The base path.
     */
    private final String path;

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
     * Metadata for methods.
     */
    @NonNull
    private final Map<Method, RestMethodMetadata> methodMetaData;
}
