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

import lombok.Getter;
import lombok.ToString;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * The json payload sent across along with the exception.
 *
 * @author Ashish Shinde
 *
 */
@Getter
@ToString
@JsonDeserialize(using = ExceptionPayloadDeserializer.class)
public class ExceptionPayload {
    /**
     * The fully qualified name of the exception class.
     */
    private final String exceptionClassFQN;

    /**
     * The exception message.
     */
    private final String exceptionMessage;

    /**
     * The actual exception.
     */
    private final Throwable exception;

    /**
     * @param exceptionClassFQN
     * @param exception
     */
    public ExceptionPayload(final Throwable exception) {
        this.exceptionClassFQN = exception.getClass().getName();
        this.exception = exception;
        this.exceptionMessage = exception.getMessage();
    }

    /**
     * Dummy constructor for jackson.
     */
    @SuppressWarnings("unused")
    private ExceptionPayload() {
        this(null, null, null);
    }

    /**
     * Create the payload.
     *
     * @param exceptionClassFQN
     * @param exceptionMessage
     * @param exception
     */
    ExceptionPayload(final String exceptionClassFQN, final String exceptionMessage,
            final Throwable exception) {
        this.exceptionClassFQN = exceptionClassFQN;
        this.exceptionMessage = exceptionMessage;
        this.exception = exception;
    }

}
