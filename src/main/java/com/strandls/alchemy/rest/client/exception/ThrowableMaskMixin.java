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

package com.strandls.alchemy.rest.client.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A mixin to ensure sensitive stacktrace and root cause information is not
 * leaked to the client.
 *
 * @author ashish
 *
 */
public abstract class ThrowableMaskMixin {
    @JsonIgnore
    public abstract Throwable getCause();

    @JsonIgnore
    public abstract StackTraceElement[] getStackTrace();

    @JsonIgnore
    public abstract Throwable[] getSuppressed();
}
