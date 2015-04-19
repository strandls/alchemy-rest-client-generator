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

import lombok.Getter;

/**
 * Thrown when {@link RestInterfaceAnalyzer#analyze(Class)} find a class that
 * does not represent a rest interface.
 *
 * @author ashish
 *
 */
public class NotRestInterfaceException extends Exception {

    /**
     * The serial version ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The class which was found to not be a rest interface.
     */
    @Getter
    private final Class<?> klass;

    /**
     * Create the exception from the class.
     *
     * @param klass
     *            the class in question.
     */
    public NotRestInterfaceException(final Class<?> klass) {
        super(klass + "  not a rest interface");
        this.klass = klass;
    }

}
