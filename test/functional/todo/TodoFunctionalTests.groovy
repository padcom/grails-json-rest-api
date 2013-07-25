package todo

import org.apache.commons.logging.LogFactory
import org.grails.plugins.test.GenericRestFunctionalTests

import com.grailsrocks.functionaltest.BrowserTestCase


@Mixin(GenericRestFunctionalTests)
class TodoFunctionalTests extends BrowserTestCase {

	def log = LogFactory.getLog(getClass())
    def messageSource

    void setUp() {
        super.setUp()
    }
    
    void tearDown() {
        super.tearDown()
    }

    
    void testList() {
        genericTestList(new Todo(title:"title.one"))
    }
    
    void testCreate() {
        genericTestCreate(new Todo(title:"title.one"))
    }
    
    void testShow() {
        genericTestShow(new Todo(title:"title.one"))
    }
    
    void testUpdate() {
        genericTestUpdate(new Todo(title:"title.one"), [title:"title.two"])
    }
    
    void testDelete() {
        genericTestDelete(new Todo(title:"title.one"))
    }
}
