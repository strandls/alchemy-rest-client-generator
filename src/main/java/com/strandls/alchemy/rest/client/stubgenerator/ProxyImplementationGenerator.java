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

import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

/**
 * Generates proxies for generates stubs.
 *
 * @author Ashish Shinde
 *
 */
@Singleton
public class ProxyImplementationGenerator {
    /**
     * The name of the template parameter for package.
     */
    private static final String PACKAGE = "package";
    /**
     * The name of the template parameter for proxy class name.
     */
    private static final String PROXY_NAME = "proxyName";
    /**
     * The name of the template parameter for stub class name.
     */
    private static final String STUB_NAME = "stubName";
    /**
     * The template file name.
     */
    private static final String PROXY_IMPLEMENTATION_TEMPLATE = "ProxyImplementation";

    /**
     * Generate the proxy implementation file.
     *
     * @param stubName
     *            the name of the stub
     * @param proxyName
     *            the name of the generated proxy class
     * @param packageName
     *            the name of the destination package
     * @param destinationFolder
     *            the destination outputStream.
     * @throws IOException
     */
    public void generateProxy(final String stubName, final String proxyName,
            final String packageName, final File destinationFolder) throws IOException {
        final STGroup g =
                new STGroupFile(getClass().getPackage().getName().replaceAll("\\.", "/")
                        + "/StubGenerator.stg");
        final ST template = g.getInstanceOf(PROXY_IMPLEMENTATION_TEMPLATE);
        template.add(STUB_NAME, stubName);
        template.add(PROXY_NAME, proxyName);
        if (!StringUtils.isBlank(packageName)) {
            template.add(PACKAGE, packageName);
        }
        FileUtils.write(new File(destinationFolder, proxyName + ".java"), template.render(100));
    }
}
