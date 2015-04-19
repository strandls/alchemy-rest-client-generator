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

package com.strandls.alchemy.rest.client.request;

import javax.ws.rs.client.Invocation.Builder;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.google.inject.ImplementedBy;

/**
 * Filter to apply to {@link javax.ws.rs.client.Invocation.Builder} before
 * firing a client request. Can be used to configure credentials for http
 * authentication.
 *
 * {@link HttpAuthenticationFeature}.
 *
 * @author ashish
 *
 */
@ImplementedBy(NoOpRequestBuilderFilter.class)
public interface RequestBuilderFilter {
    public void apply(final Builder builder);
}
