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

package com.strandls.alchemy.rest.client.stubgenerator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.Path;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.reflections.Reflections;
import org.stringtemplate.v4.STGroupDir;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.strandls.alchemy.rest.client.RestInterfaceAnalyzer;
import com.sun.codemodel.writer.FileCodeWriter;

/**
 * Ant {@link Task} that scan classpath for matching rest service
 * implementations and generates the stub, proxy implementation sources and
 * corresponding guice bindings.
 *
 * @author ashish
 *
 */
@Setter
public class RestProxyGenerator extends Task {
    /**
     * The suffix to be appended to the generated package. Can be
     * <code>null</code>.
     */
    private String classSuffix;

    /**
     * The destination package for generated classes. Can be <code>null</code>
     * or blank string for using the default package.
     */
    private String destinationPackage = "";

    /**
     * Comma separated regexes on canonical class names to exclude.
     */
    private String excludes;

    /**
     * Comma separated regexes on canonical class names to include, .
     */
    private String includes;

    /**
     * The output directory where generated stubs would be added.
     */
    private File outputDir;

    /**
     * The service classes.
     */
    @Getter(lazy = true, onMethod = @_({
        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = {
                "JLM_JSR166_UTILCONCURRENT_MONITORENTER",
        "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE" },
        justification = "Findbugs warnings on lombok generated code not critical."),
        @SuppressWarnings("unchecked") }))
    private final Set<Class<?>> serviceClasses = findServiceClasses();

    /*
     * (non-Javadoc)
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() {
        // validate parameters
        validate();

        log("Starting client code generation", Project.MSG_ERR);
        try {
            // generate service stubs.
            generateServiceStubs();

            // generate proxy implementations.
            generateProxyImplementations();

            // generate guice module with stub to proxy bindings.
            generateGuiceModule();
        } catch (final IOException e) {
            throw new BuildException(e);
        }

        log("Finished client code generation", Project.MSG_ERR);

    }

    /**
     * @return the rest webservice classes to process.
     * @throws MalformedURLException
     */
    private Set<Class<?>> findServiceClasses() {
        final List<Object> params = new ArrayList<Object>();
        final ClassLoader loader = getClass().getClassLoader();
        if (loader instanceof AntClassLoader) {
            final String[] path =
                    ((AntClassLoader) loader).getClasspath().split(
                            System.getProperty("path.separator"));
            for (final String string : path) {
                try {
                    params.add(new URL(string));
                } catch (final MalformedURLException e) {
                    try {
                        params.add(new URL("file://" + string));
                    } catch (final MalformedURLException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            }

        }

        final Reflections reflections = new Reflections(params.toArray(new Object[0]));
        final Set<Class<?>> allRestServices = reflections.getTypesAnnotatedWith(Path.class);
        log("Locating service classes", Project.MSG_ERR);

        final Set<Class<?>> filtered = Sets.filter(allRestServices, new Predicate<Class<?>>() {
            @Override
            public boolean apply(final Class<?> input) {
                return doesMatch(input, includes) && !doesMatch(input, excludes);
            }

            /**
             * Indicates if the input class matches any one of the comma
             * separated regex patterns.
             *
             * <code>null</code> or empty pattern implies no match.
             *
             * @param input
             *            the input class.
             * @param patterns
             *            comma separated list of patterns.
             *
             * @return <code>true</code> if any one of the pattern matches the
             *         canonical name of the class.
             */
            private boolean doesMatch(final Class<?> input, final String patterns) {
                boolean matches = false;
                if (patterns != null) {
                    for (final String include : patterns.split("\\s*,\\s*")) {
                        matches |= Pattern.matches(include, input.getCanonicalName());
                    }
                }
                return matches;
            }
        });

        for (final Class<?> klass : filtered) {
            log("Will process class: " + klass.getCanonicalName(), Project.MSG_ERR);
        }

        return filtered;
    }

    /**
     * Generate the guice module with bindings.
     *
     * @throws IOException
     */
    private void generateGuiceModule() throws IOException {
        final Set<Class<?>> classes = getServiceClasses();
        new STGroupDir(getClass().getPackage().getName().replaceAll("\\.", "/"));
        final File generatedSourceDirectory = getTargetSourceDirectory();

        final Map<String, String> stubProxyMap = new HashMap<>();
        for (final Class<?> klass : classes) {
            stubProxyMap.put(getStubClassName(klass), getProxyImplemenationName(klass));
        }

        log("Generating guice module", Project.MSG_ERR);

        final GuiceModuleGenerator moduleGenerator = new GuiceModuleGenerator();
        moduleGenerator.generateGuiceModule(destinationPackage, generatedSourceDirectory,
                stubProxyMap);
    }

    /**
     * Generate the proxy implementations for the services.
     *
     * @throws IOException
     */
    private void generateProxyImplementations() throws IOException {
        final Set<Class<?>> classes = getServiceClasses();
        new STGroupDir(getClass().getPackage().getName().replaceAll("\\.", "/"));
        final File generatedSourceDirectory = getTargetSourceDirectory();

        log("Generating proxies", Project.MSG_ERR);

        final ProxyImplementationGenerator proxyGenerator = new ProxyImplementationGenerator();
        for (final Class<?> klass : classes) {
            proxyGenerator.generateProxy(getStubClassName(klass), getProxyImplemenationName(klass),
                    destinationPackage, generatedSourceDirectory);
            log("Generated " + getProxyImplemenationName(klass), Project.MSG_INFO);
        }

    }

    /**
     * Generate service stubs.
     *
     * @throws IOException
     */
    private void generateServiceStubs() throws IOException {
        final ServiceStubGenerator stubGenerator =
                new ServiceStubGenerator(new RestInterfaceAnalyzer());

        final File generatedSourceDirectory = outputDir;

        final Set<Class<?>> classes = getServiceClasses();
        @Cleanup
        final FileCodeWriter codeWriter = new FileCodeWriter(generatedSourceDirectory);

        log("Generating service stubs", Project.MSG_ERR);

        for (final Class<?> klass : classes) {
            final String stubClassName = getStubClassName(klass);
            try {

                stubGenerator.generateStubInterface(klass, stubClassName, destinationPackage,
                        codeWriter);
                log("Generated " + stubClassName, Project.MSG_INFO);
            } catch (final Exception e) {
                log("Stub generation failed for " + klass.getCanonicalName(), Project.MSG_ERR);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * The name of the generated proxy implementation.
     *
     * @param klass
     *            the service class.
     * @return the name of the proxy implemenation
     */
    private String getProxyImplemenationName(final Class<?> klass) {
        return getStubClassName(klass) + "Proxy";
    }

    /**
     * Return the name of the generated stub class.
     *
     * @param klass
     *            the service class.
     * @return the name of the generated stub class.
     */
    private String getStubClassName(final Class<?> klass) {
        return klass.getSimpleName() + (!StringUtils.isBlank(classSuffix) ? classSuffix : "");
    }

    /**
     * @return the target generates source directory.
     */
    private File getTargetSourceDirectory() {
        final String packageFolder =
                !StringUtils.isBlank(destinationPackage) ? destinationPackage.replaceAll("\\.",
                        File.separator) : "";
                final File generatedSourceDirectory =
                        !StringUtils.isBlank(packageFolder) ? new File(outputDir, packageFolder)
                : outputDir;

                        if (!generatedSourceDirectory.isDirectory() && !generatedSourceDirectory.mkdirs()) {
                            throw new RuntimeException("Could not create " + generatedSourceDirectory);
                        }
                        return generatedSourceDirectory;
    }

    /**
     * Validate the arguments.
     */
    private void validate() {
        if (outputDir == null || (outputDir.exists() && !outputDir.isDirectory())) {
            throw new IllegalArgumentException("Not a valid directory : " + outputDir);
        }

        if (!outputDir.isDirectory() && !outputDir.mkdirs()) {
            throw new RuntimeException("Could not create " + outputDir);
        }
    }
}
