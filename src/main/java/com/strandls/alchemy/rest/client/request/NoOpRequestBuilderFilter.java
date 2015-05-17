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

package com.strandls.alchemy.rest.client.request;

import javax.ws.rs.client.Invocation.Builder;

/**
 * A do nothing / noop filter to serve as a default binding for
 * {@link RequestBuilderFilter}.
 *
 * @author Ashish Shinde
 *
 */
public class NoOpRequestBuilderFilter implements RequestBuilderFilter {

    /*
     * (non-Javadoc)
     * @see
     * com.strandls.alchemy.rest.client.request.RequestBuilderFilter#apply(javax
     * .ws.rs.client.Invocation.Builder)
     */
    @Override
    public void apply(final Builder builder) {

    }

}
