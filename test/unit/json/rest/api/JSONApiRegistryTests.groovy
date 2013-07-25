package json.rest.api

import grails.test.mixin.*
import grails.test.mixin.support.*

import org.apache.commons.logging.LogFactory
import org.grails.plugins.rest.JSONApiRegistry
import org.grails.plugins.util.TextUtil
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 * @author kent.butler@gmail.com
 */
@TestMixin(GrailsUnitTestMixin)
class JSONApiRegistryTests {
    
    def controller
    def log = LogFactory.getLog(getClass())
    
    void setUp() {
    }

    void tearDown() {
        // Tear down logic here
    }
    
    @Test
    void testBasic() {
        log.debug("------------ testBasic() -------------")
        
        JSONApiRegistry.register("horse", "dc.Horse")
        
        assertNotNull JSONApiRegistry.getEntity("horse")
        assertNotNull JSONApiRegistry.getEntity("horses")
        assertNotNull JSONApiRegistry.getSingular("horse")
        assertNotNull JSONApiRegistry.getSingular("horses")
    }
    
    @Test
    void testBasicS() {
        log.debug("------------ testBasicS() -------------")
        
        JSONApiRegistry.register("toads", "dc.Toads")
        
        assertNotNull JSONApiRegistry.getEntity("toads")
        assertNotNull JSONApiRegistry.getEntity("toadses")
        assertNotNull JSONApiRegistry.getSingular("toads")
        assertNotNull JSONApiRegistry.getSingular("toadses")
    }
}
