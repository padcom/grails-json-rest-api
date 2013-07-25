package todo

import org.apache.commons.logging.LogFactory
import org.grails.plugins.test.GenericRestFunctionalTests

import com.grailsrocks.functionaltest.BrowserTestCase


@Mixin(GenericRestFunctionalTests)
class TodoUnderscoreFunctionalTests extends BrowserTestCase {

	def log = LogFactory.getLog(getClass())
    def messageSource

    void setUp() {
        super.setUp()
    }
    
    void tearDown() {
        super.tearDown()
    }

    
    void testList() {
        genericTestList(new Todo(title:"title.one", expose:'todo_underscore'))
    }
    
    void testCreate() {
        genericTestCreate(new Todo(title:"title.one", expose:'todo_underscore'))
    }
    
    void testShow() {
        genericTestShow(new Todo(title:"title.one", expose:'todo_underscore'))
    }
    
    void testUpdate() {
        genericTestUpdate(new Todo(title:"title.one", expose:'todo_underscore'), [title:"title.two"])
    }
    
    void testDelete() {
        genericTestDelete(new Todo(title:"title.one", expose:'todo_underscore'))
    }
}
