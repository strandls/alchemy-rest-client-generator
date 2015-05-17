# Alchemy Rest Client Generator

 - [Overview](#overview)
 - [Recommended code organization](#recommended-code-organization)
 - [Usage](#usage)
   - [Adding gradle dependency](#adding-gradle-dependency)
   - [Adding maven dependency](#adding-maven-dependency)
   - [Unit tests for RESTful services](#unit-tests-for-restful-services)
   - [Client bindings](#client-bindings)
   - [The Jersey client provider](#the-jersey-client-provider)
   - [Basic Http authentication](#basic-http-authentication)
 - [Exception marshalling / demarshalling](#exception-marshalling--demarshalling)
   - [Common marshaller / demarshaller](#common-marshaller--demarshaller)
   - [Server side exception mapper](#server-side-exception-mapper)
 - [Demo](#demo)
 - [TODO](#todo)
 - [Contributing](#contributing)
     - [Setting up eclipse](#setting-up-eclipse)
 - [Copyright and license](#copyright-and-license)


## Overview
A java rest client auto generator with stubs and proxy code mimicking your webservices. 

Specifically, Alchemy rest client generator
 * generates a runtime proxy for a restful webservice class, useful for unit / integration testing.
 * has an ant task to generates webservice stubs and proxy implementations. This can be part of your build scripts, generating a new client every time your webservice code changes. This would constitute your client library.
 * uses guice for dependency injection, leading to cleaner and more loosely coupled code
 * supports query, path, matrix, header, cookie and form parameters for the rest methods
 * can marshall and demarshall exceptions thrown by services so that client can see the exception as if they were thrown by local methods.

Alchemy rest client **does not**
 * generate client side transfer objects as yet. For example if have a webservice as follows.

```
@GET
@PATH("/history")
public CommitInfo getHistory(String objectURI) {
	....
}
```
The generated client will not mimic / stub the CommitInfo object. The transfer object classes / jars will need to shared with the client. This seems reasonable at the moment.

## Recommended code organization

We recommend you split your service code into at least three components
 - common containing 
  - direct transfer objects / dependencies or dependencies to modules having these transfer objects.
  - exception marshalling / demarshlling code as discussed above
  - jackson json modules if any for using jackson

 - client containing
  - auto generated code
  - custom client bindings for server URL, credentials etc.
 
 - server containing
  - webservices and dependent server side module dependencies


## Usage

### Adding gradle dependency

```
compile 'com.strandls.alchemy:alchemy-rest-client-generator:0.9'

```

### Adding maven dependency

```
<dependency>
	<groupId>com.strandls.alchemy</groupId>
	<artifactId>alchemy-rest-client-generator</artifactId>
	<version>0.9</version>
</dependency>

```

### Unit tests for RESTful services

```
package com.strandls.alchemy.restclient.demo;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;

/**
 * A simpleton echo service.
 */
@Path("/echo")
@Slf4j
public class EchoService {
	/**
	 * Echoes back the input string.
	 * 
	 * @param input
	 *            the input string
	 * @return the input string
	 */
	@GET
	@Path("{toEcho}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String echo(@PathParam("toEcho") String input) {
		log.debug("Echo service invoked with input '{}'", input);
		return input;
	}
}
```

The following junit test EchoServiceTest  
* starts the EchoService in a Grizzly webcontainer.
* uses the client generator to create a runtime proxy for the webservice
* tests to ensure the service works

Code relevant to using the runtime proxy is show below. 


```	
public class EchoServiceTest extends JerseyTest {

	.
	.
	.

	/**
	 * Inject the client factory.
	 */
	private AlchemyRestClientFactory clientFactory;

	/**
	 * Test the echo service.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEcho() throws Exception {
		EchoService echoClient = clientFactory.getInstance(EchoService.class);
		String testinput = "Hello World !!!!";
		assertEquals(testinput, echoClient.echo(testinput));
	}

}
```

### Client bindings

We supply the base uri for the webservice to the generated client proxy via guice bindings. The follows Guice module is an example for how to do this.

	
```	
package com.strandls.alchemy.restclient.demo;

import javax.ws.rs.client.Client;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.strandls.alchemy.rest.client.AlchemyRestClientFactory;

/**
 * The client side guice module.
 */
public class ClientBindingModule extends AbstractModule {
    /*
     * (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
	// bind the URI.
	bind(String.class).annotatedWith(
		Names.named(AlchemyRestClientFactory.BASE_URI_NAMED_PARAM)).toInstance("http://localhost:9999");
	
	// bind the client.
	bind(Client.class).toProvider(JaxRsClientProvider.class);
    }


}
	
```	

### The Jersey client provider
The proxy code uses Jersey client behind the scenes to make http calls.

The following code set's up the Jersey client. Notice the use of [JacksonJsonProvider](http://mvnrepository.com/artifact/com.fasterxml.jackson.jaxrs/jackson-jaxrs-json-provider) to transparently convert to and from json input parameters and return values.


```	
package com.strandls.alchemy.restclient.demo;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import lombok.RequiredArgsConstructor;

import org.eclipse.net4j.util.security.ICredentialsProvider;
import org.eclipse.net4j.util.security.IPasswordCredentials;
import org.eclipse.net4j.util.security.PasswordCredentialsProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * Provides the {@link Client} after applying the jackson bindings.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @_(@Inject))
public class JaxRsClientProvider implements Provider<Client> {

    /**
     * The jackson json provider.
     */
    private final JacksonJsonProvider jsonProvider;

    /*
     * (non-Javadoc)
     * @see javax.inject.Provider#get()
     */
    @Override
    @Singleton
    public Client get() {
	final ClientConfig clientConfig = new ClientConfig();

	// register the json provider
	clientConfig.register(jsonProvider);

	return ClientBuilder.newClient(clientConfig);
    }
}
```	
	

Once you setup the client provider and a module to bind the base URI, all you need to do is inject AlchemyRestClientFactory into your code.

The rest client code generator requires the webservice classes  to be in its classpath. The generated client code however does not depend on the webservice classes.

**Note**: Client will still need in its classpath,  classes / jars for custom input paramter and return types.


### Basic Http authentication

You can setup basic http authentication on the server side by following [JerseyAuthDoc][Jersey documentation] or by looking up the [demo][ARCDemo] code.

The client needs to implement an the interface **com.strandls.alchemy.rest.client.request.RequestBuilderFilter** to plugin http authentication. The implementation could also be used to set other headers before the http request is made.

Example client request builder filter that sets up http credentials

```
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.client.Invocation.Builder;

import lombok.RequiredArgsConstructor;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.strandls.alchemy.rest.client.request.RequestBuilderFilter;
import com.strandls.alchemy.webservices.common.auth.Credentials;

/**
 * Request builder that sets autntication credentials.
 *
 * @author Ashish Shinde
 *
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @_(@Inject))
public class AuthRequestBuilderFilter implements RequestBuilderFilter {
    /**
     * The credentials provider.
     */
    private final Provider<Credentials> credentialsProvider;

    /*
     * (non-Javadoc)
     * @see
     * com.strandls.alchemy.rest.client.request.RequestBuilderFilter#apply(javax
     * .ws.rs.client.Invocation.Builder)
     */
    @Override
    public void apply(final Builder builder) {
        final Credentials credentials = credentialsProvider.get();
        String user = "James Bond";
        String password = "007";

        builder.property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, user)
        .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password);
    }
}

```

## Exception marshalling / demarshalling

Alchemy rest client enables the client to raise exceptions that were raised by the server as if they were generated locally. This can be setup by adding the following classed to both the server and client. Ideally you could create a common module and have the server and client depend on both

### Common marshaller / demarshaller

```
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.strandls.alchemy.inject.AlchemyModule;
import com.strandls.alchemy.inject.AlchemyModule.Environment;
import com.strandls.alchemy.reflect.JavaTypeQueryHandler;
import com.strandls.alchemy.rest.client.NotRestInterfaceException;
import com.strandls.alchemy.rest.client.RestInterfaceAnalyzer;
import com.strandls.alchemy.rest.client.exception.ThrowableMaskMixin;
import com.strandls.alchemy.rest.client.exception.ThrowableObjectMapper;

/**
 * Binding for {@link ObjectMapper} used for server side error conversions.
 *
 * @author Ashish Shinde
 *
 */
@Slf4j
@AlchemyModule(Environment.All)
public class ExceptionObjectMapperModule extends AbstractModule {

    /**
     * The jax rs package root.
     */
    private static final String JAVAX_WS_RS_PACKAGE = "javax.ws.rs";
    /**
     * Elixir web service package root.
     */
    private static final String ALCHEMY_SERVICE_PACKAGE = "com.strandls.alchemy";

    /*
     * (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
    }

    /**
     * Binding for throwable exception mapper.
     *
     * @param mapper
     * @return
     */
    @Provides
    @Singleton
    @ThrowableObjectMapper
    @Inject
    public ObjectMapper getExceptionObjectMapper(final ObjectMapper mapper,
            final RestInterfaceAnalyzer restInterfaceAnalyzer,
            final JavaTypeQueryHandler typeQueryHandler) {
        // can't copy owing to bug -
        // https://github.com/FasterXML/jackson-databind/issues/245
        final ObjectMapper exceptionMapper = mapper;
        exceptionMapper.registerModule(new SimpleModule() {
            /**
             * The serial version id.
             */
            private static final long serialVersionUID = 1L;

            /*
             * (non-Javadoc)
             * @see
             * com.fasterxml.jackson.databind.module.SimpleModule#setupModule
             * (com.fasterxml.jackson.databind.Module.SetupContext)
             */
            @Override
            public void setupModule(final SetupContext context) {
                // find exceptions thrown by webservices
                final Set<Class<?>> serviceClasses =
                        typeQueryHandler.getTypesAnnotatedWith(ALCHEMY_SERVICE_PACKAGE, Path.class);
                final Set<Class<?>> exceptionsUsed = new HashSet<Class<?>>();
                for (final Class<?> serviceClass : serviceClasses) {
                    // get hold of all rest methods and hence exception
                    try {
                        final Set<Method> restMethods =
                                restInterfaceAnalyzer.analyze(serviceClass).getMethodMetaData()
                                .keySet();
                        for (final Method method : restMethods) {
                            for (final Class<?> exceptionClass : method.getExceptionTypes()) {
                                exceptionsUsed.add(exceptionClass);
                            }
                        }
                    } catch (final NotRestInterfaceException e) {
                        log.error("Error geting exception classes for methods from {}",
                                serviceClass);
                        throw new RuntimeException(e);
                    }
                }

                // add the mixin to all jaxrs classes as well.
                exceptionsUsed.addAll(typeQueryHandler.getSubTypesOf(JAVAX_WS_RS_PACKAGE,
                        WebApplicationException.class));

                for (final Class<?> exceptionClass : exceptionsUsed) {
                    // add a mixin to prevent server stack trace from showing up
                    // to the client.
                    log.debug("Applied mixin mask to {}", exceptionClass);
                    context.setMixInAnnotations(exceptionClass, ThrowableMaskMixin.class);
                }

            }
        });
        exceptionMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return exceptionMapper;
    }

}

```
The code above also ensures that the stacktrace is not send to the client, which could be a security requirement.


### Server side exception mapper

You need some code on the server side to marshall exceptions raised by services into the response body. This is done by adding the class below to the server

```
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.strandls.alchemy.rest.client.exception.ThrowableToResponseMapper;

/**
 * Mapper for {@link Exception}s generated from the webservices.
 *
 * @author Ashish Shinde
 *
 */
@Provider
@RequiredArgsConstructor(onConstructor = @_(@Inject))
@Singleton
@Slf4j
public class AlchemyExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {
    /**
     * The response mapper.
     */
    private final ThrowableToResponseMapper responseMapper;

    /*
     * (non-Javadoc)
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
     */
    @Override
    public Response toResponse(final Exception exception) {
        log.error("{}", exception);
        return responseMapper.apply(exception);
    }

}
``` 

You might need to bind add a guice binding to your [Servlet module][GuiceServletModule] this class, to get the exception mapper to work.

```
 /*
     * (non-Javadoc)
     * @see com.google.inject.servlet.ServletModule#configureServlets()
     */
    @Override
    protected void configureServlets() {
        .
        .
        
        bind(AlchemyExceptionMapper.class);
        
        .
        .
    }
```
 
 
## Demo

The [Alchemy Rest Client Demo][ARCDemo] project is a good demostration of real life use of this module.

## TODO

Generate client from WADL.

## Contributing

Please refer to [Contribution Guidlines][Contrib] if you are not familiar with contributing to open source projects.

The gist for making a contibution is

1. [Fork]
2. Create a topic branch - `git checkout -b <your branch>`
3. Make your changes
4. Push to your branch - `git push origin <your branch>`
5. Create an [Issue] with a link to your branch

#### Setting up eclipse
Run
```
gradle eclipse
```

Import alchemy inject to eclipse using File > Import > Existing Projects into Workspace

The project has been setup to auto format the code via eclipse save actions. Please try not to disturb this.

## Copyright and license

Code and documentation copyright 2015 [Strand Life Sciences]. Code released under the [Apache License 2.0]. Docs released under Creative Commons.

[ARCDemo]:https://github.com/strandls/alchemy-rest-client-demo/
[Alchemy Inject]:https://github.com/strandls/alchemy-inject/
[Apache License 2.0]:http://www.apache.org/licenses/LICENSE-2.0.html
[Strand Life Sciences]:http://www.strandls.com/
[Fork]: http://help.github.com/forking/
[Issues]: https://github.com/strandls/alchemy-rest-client-demo/issues
[Contrib]: https://guides.github.com/activities/contributing-to-open-source/
[Jackson]: https://github.com/FasterXML/jackson
[JavaRegex]: http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
[GuiceServletModule]: https://github.com/google/guice/wiki/ServletModule
[JerseyAuthDoc]: https://jersey.java.net/documentation/latest/filters-and-interceptors.html#d0e9605

