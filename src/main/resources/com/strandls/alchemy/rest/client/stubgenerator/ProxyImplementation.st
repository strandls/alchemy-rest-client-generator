// Template to generate delegating proxy implmentations of stubs.

ProxyImplementation(package, stubName, proxyName ) ::= <<

<if(package)>
package <package>;
<endif>

import javax.inject.Inject;
import javax.inject.Singleton;

import com.strandls.alchemy.rest.client.AlchemyRestClientFactory;

import lombok.Delegate;

/**
 * Proxy implementation for {@link <stubName>}.
 */
@Singleton
public class <proxyName> implements <stubName> {
    /**
     * The proxy implementation.
     */
    @Delegate
    private final <stubName> implementation;

    /**
     * Create the proxy instance
     */
    @Inject
    public <proxyName>(final AlchemyRestClientFactory clientFactory) {
        try {
            // generate real proxy implemetation.
            implementation = clientFactory.getInstance(<stubName>.class);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}

>>
