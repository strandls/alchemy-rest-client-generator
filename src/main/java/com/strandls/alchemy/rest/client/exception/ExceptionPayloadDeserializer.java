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

import java.io.IOException;

import javax.ws.rs.InternalServerErrorException;

import lombok.extern.slf4j.Slf4j;

import org.reflections.ReflectionUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Deserializer for {@link ExceptionPayload} that tries to recreate the original
 * exception.
 *
 * @author ashish
 *
 */
@Slf4j
public class ExceptionPayloadDeserializer extends JsonDeserializer<ExceptionPayload> {

    /*
     * (non-Javadoc)
     * @see
     * com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml
     * .jackson.core.JsonParser,
     * com.fasterxml.jackson.databind.DeserializationContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public ExceptionPayload deserialize(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final ObjectMapper sourceObjectMapper = ((ObjectMapper) jp.getCodec());

        final ObjectNode tree = jp.readValueAsTree();

        String message = null;
        if (tree.has("exceptionMessage")) {
            message = tree.get("exceptionMessage").asText();
        }

        Throwable exception = null;
        try {
            final String className = tree.get("exceptionClassFQN").asText();
            final Class<? extends Throwable> clazz =
                    (Class<? extends Throwable>) ReflectionUtils.forName(className);
            exception = sourceObjectMapper.treeToValue(tree.get("exception"), clazz);
        } catch (final Throwable t) {
            log.warn("Error deserializing exception class", t);
            exception = new InternalServerErrorException(message);
        }

        return new ExceptionPayload(exception.getClass().getName(), message, exception);
    }
}
