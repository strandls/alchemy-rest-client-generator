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

package com.strandls.alchemy.rest.client.stubgenerator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.inject.Singleton;

import lombok.NonNull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

/**
 * Generates a guice module that binds the stubs to their proxy implementations.
 *
 * @author Ashish Shinde
 *
 */
@Singleton
public class GuiceModuleGenerator {
    /**
     * The name of the generated module.
     */
    static final String CLIENT_GUICE_MODULE_NAME = "ClientGuiceModule";
    /**
     * The template file name.
     */
    private static final String GUICE_IMPLEMENTATION_TEMPLATE = "GuiceModule";
    /**
     * The name of the template parameter for package.
     */
    private static final String PACKAGE = "package";
    /**
     * The name of the template parameter for stub to proxy class map.
     */
    private static final String STUB_PROXY_MAP = "stubProxyMap";

    /**
     * Generate a guice module with bindings from stubs to proxy classes.
     *
     * @param packageName
     *            the name of the destination package. Cane be <code>null</code>
     * @param destinationFolder
     *            the destination outputStream.
     * @param stubProxyMap
     *            the map from the stub name to the proxy class name.
     * @throws IOException
     */
    public void generateGuiceModule(final String packageName,
            @NonNull final File destinationFolder, @NonNull final Map<String, String> stubProxyMap)
                    throws IOException {
        final STGroup g =
                new STGroupFile(getClass().getPackage().getName().replaceAll("\\.", "/")
                        + "/StubGenerator.stg");
        final ST template = g.getInstanceOf(GUICE_IMPLEMENTATION_TEMPLATE);
        template.add(STUB_PROXY_MAP, stubProxyMap);
        if (!StringUtils.isBlank(packageName)) {
            template.add(PACKAGE, packageName);
        }
        FileUtils.write(new File(destinationFolder, CLIENT_GUICE_MODULE_NAME + ".java"),
                template.render(100));
    }
}
