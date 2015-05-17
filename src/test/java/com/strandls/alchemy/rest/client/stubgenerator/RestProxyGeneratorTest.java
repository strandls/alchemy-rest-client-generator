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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.Test;

/**
 * Unit tests for {@link RestProxyGenerator}.
 *
 * @author Ashish Shinde
 *
 */
public class RestProxyGeneratorTest {

    /**
     * Run the task.
     *
     * @param outputDir
     *            the output directory.
     * @param destinationPackage
     *            the destination package name.
     * @throws IOException
     */
    private void runProxyGenerateTask(final File outputDir, final String destinationPackage)
            throws IOException {
        final BuildLogger logger = new DefaultLogger();
        logger.setMessageOutputLevel(Project.MSG_INFO);
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.out);
        logger.setEmacsMode(true);

        final ProjectHelper ph = ProjectHelper.getProjectHelper();
        final Project p = new Project();
        p.addBuildListener(logger);
        p.init();
        p.addReference("ant.projectHelper", ph);

        final URL[] urls =
                ((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs();
        final StringBuffer classPath = new StringBuffer();
        for (final URL url : urls) {
            classPath.append(new File(url.getPath()));
            classPath.append(System.getProperty("path.separator"));
        }
        classPath.append("src/test/resources/test-webservices.jar");

        // set properties
        p.setUserProperty("client.src.dir", outputDir.getAbsolutePath());
        p.setUserProperty("task.classpath", classPath.toString());

        if (destinationPackage != null) {
            p.setUserProperty("destinationPackage", destinationPackage);
        }

        ph.parse(p, new File("src/test/resources/restProxyGenBuild.xml"));
        p.executeTarget("restProxyGen");

        // ensure all files are generated
        final Set<String> expectedFiles =
                new HashSet<>(FileUtils.readLines(new File(
                        "src/test/resources/test-weservices-files.txt")));
        final File packageDir =
                new File(outputDir, destinationPackage.replaceAll("\\.", File.separator));
        final Set<String> actualFiles = new HashSet<String>();
        for (final File file : packageDir.listFiles()) {
            if (file.isFile()) {
                actualFiles.add(file.getName());
            }
        }

        assertEquals(expectedFiles, actualFiles);
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.stubgenerator.RestProxyGenerator#execute()}
     * .
     *
     * @throws Exception
     */
    @Test
    public void testExecute() throws Exception {
        final File tempDir = File.createTempFile("test", "test");
        FileUtils.deleteQuietly(tempDir);

        if (!tempDir.mkdirs()) {
            throw new IOException("Cannot create temporary directory.");
        }
        try {
            runProxyGenerateTask(tempDir, "com.strandls.alchemy.webservices.client");
        } finally {
            FileUtils.deleteQuietly(tempDir);
        }
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.stubgenerator.RestProxyGenerator#execute()}
     * .
     *
     * @throws Exception
     */
    @Test(expected = BuildException.class)
    public void testExecuteInvalidDirectory() throws Exception {
        final File tempDir = File.createTempFile("test", "test");
        try {
            runProxyGenerateTask(tempDir, "");
        } finally {
            FileUtils.deleteQuietly(tempDir);
        }
    }

    /**
     * Test method for
     * {@link com.strandls.alchemy.rest.client.stubgenerator.RestProxyGenerator#execute()}
     * .
     *
     * @throws Exception
     */
    @Test
    public void testExecuteWithNoPackage() throws Exception {
        final File tempDir = File.createTempFile("test", "test");
        FileUtils.deleteQuietly(tempDir);

        if (!tempDir.mkdirs()) {
            throw new IOException("Cannot create temporary directory.");
        }
        try {
            runProxyGenerateTask(tempDir, "");
        } finally {
            FileUtils.deleteQuietly(tempDir);
        }
    }
}
