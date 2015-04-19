// Template to generate guice module binding stubs to the proxies.

GuiceModule(package, stubProxyMap ) ::= <<

<if(package)>
package <package>;
<endif>

import com.google.inject.AbstractModule;

/**
 * Binds client stubs to the proxy implementations.
 */
public class ClientGuiceModule extends AbstractModule {

    /*
     * (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        <stubProxyMap.keys:{k | bind(<k>.class).to(<stubProxyMap.(k)>.class);
        }>
    }

}

>>