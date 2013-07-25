package json.rest.api

import grails.test.mixin.*
import grails.test.mixin.support.*

import org.apache.commons.logging.LogFactory
import org.grails.plugins.util.TextUtil
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 * @author kent.butler@gmail.com
 */
@TestMixin(GrailsUnitTestMixin)
class TextUtilTests {
    
    def controller
    def log = LogFactory.getLog(getClass())
    
    void setUp() {
    }

    void tearDown() {
        // Tear down logic here
    }
    
    @Test
    void testPluralNonS() {
        log.debug("------------ testPluralNonS() -------------")
        
        def result = TextUtil.pluralize "duck"
        
        assertEquals "ducks", result
    }
    
    @Test
    void testPluralS() {
        log.debug("------------ testPluralS() -------------")
        
        def result = TextUtil.pluralize "moss"
        
        assertEquals "mosses", result
    }

    @Test
    void testNull() {
        log.debug("------------ testNull() -------------")
        
        def result = TextUtil.pluralize null
        
        assertEquals "", result
    }

    @Test
    void testEmpty() {
        log.debug("------------ testEmpty() -------------")
        
        def result = TextUtil.pluralize ''
        
        assertEquals "", result
    }
}
