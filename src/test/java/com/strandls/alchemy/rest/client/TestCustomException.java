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
 * A custom exception for testing.
 *
 * @author ashish
 *
 @SuppressWarnings("serial")
 */
@SuppressWarnings("serial")
@Getter
public class TestCustomException extends Exception {
    /**
     * A status code for error.
     */
    private final int statusCode;

    /**
     * @param statusCode
     */
    public TestCustomException(final int statusCode) {
        super("Failed with status code:" + statusCode);
        this.statusCode = statusCode;
    }

    /**
     * Dummy constructor for jackson.
     */
    @SuppressWarnings("unused")
    private TestCustomException() {
        this(0);
    }
}
