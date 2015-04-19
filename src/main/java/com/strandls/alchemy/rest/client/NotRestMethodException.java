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

import java.lang.reflect.Method;

import lombok.Getter;

/**
 * Thrown when {@link RestInterfaceAnalyzer#analyze(Class)} find a method that
 * does not represent a rest method.
 *
 * @author ashish
 *
 */
public class NotRestMethodException extends Exception {

    /**
     * The serial version ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The method which was not found to be a rest method.
     */
    @Getter
    private final Method method;

    /**
     * Create the exception from the method.
     *
     * @param method
     *            the method in question.
     */
    public NotRestMethodException(final Method method) {
        super(method + "  not a rest method");
        this.method = method;
    }

}
