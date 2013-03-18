package json.rest.api

import grails.converters.JSON
import grails.test.mixin.*
import grails.test.mixin.support.*
import org.codehaus.groovy.grails.web.json.JSONObject

import org.apache.commons.logging.LogFactory
import org.grails.plugins.rest.JsonRestApiController
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 * @author kent.butler@gmail.com
 */
@TestMixin(GrailsUnitTestMixin)
class JsonRestApiControllerTests {
    
    def controller
    def log = LogFactory.getLog(getClass())
    def obj
    
    void setUp() {
        controller = new JsonRestApiController()
        // By default we want to include the usual status parameters in generated output JSON
        obj = [(JsonRestApiController.DATA_FIELDS_ONLY): false]
    }

    void tearDown() {
        // Tear down logic here
    }
    
    
    /**
     * we need a real class as our domain class; doesn't need HBM instrumenting 
     *     at this level of testing though
     */
    class Person {
        int id
        String firstName
        String lastName
        
        // Have the controller use this method for rendering JSON since
        //   the Grails converter plugin will not be loaded for testing
        JSONObject toJSON(def msgSource) {
            def json = new JSONObject()
            json.put("id", id)
            json.put("firstName", firstName)
            json.put("lastName", lastName)
            return json
        }
    }
    

    void testRenderJsonNoData() {
        log.debug("------------ testRenderJsonNoData() -------------")
        assertNotNull controller
        
        obj << [success: false, message: 'Could not find record']
        
        def result = controller.renderJSON(obj, obj)
        assertNotNull result
        assertTrue "Result was instead: $result", result.indexOf("Could not find record") >= 0
        
        def parsed = JSON.parse(result)
        assertNotNull parsed
        assertNotNull "success field not found in $result", parsed.success
        assertNotNull "message field not found in $result", parsed.message
        assertEquals false, parsed.success 
        assertEquals 'Could not find record', parsed.message 
    }
    
    void testRenderJsonWithClass() {
        log.debug("------------ testRenderJsonWithClass() -------------")
        assertNotNull controller
        
        def dc = new Person(id: 1, firstName:'Bob', lastName:'Bear')
        obj << [success: true, data: dc]
        
        def result = controller.renderJSON(obj, obj)
        assertNotNull result
        assertTrue "Result was instead: $result", result.indexOf("Bear") > 0
        
        def parsed = JSON.parse(result)
        assertNotNull parsed
        assertNotNull "success field not found in $result", parsed.success
        assertEquals true, parsed.success 

        assertNotNull "data field not found in $result", parsed.data
        assertNotNull parsed.data.id
        assertEquals 1, parsed.data.id 
        assertNotNull parsed.data.firstName
        assertEquals 'Bob', parsed.data.firstName
        assertNotNull parsed.data.lastName
        assertEquals 'Bear', parsed.data.lastName
    }
    
    void testRenderJsonWithClassEntityRoot() {
        log.debug("------------ testRenderJsonWithClassEntityRoot() -------------")
        assertNotNull controller
        
        def dc = new Person(id: 1, firstName:'Bob', lastName:'Bear')
        obj << [success: true, person: dc]
        
        def result = controller.renderJSON(obj, obj, 'person')
        assertNotNull result
        assertTrue "Result was instead: $result", result.indexOf("Bear") > 0
        
        def parsed = JSON.parse(result)
        assertNotNull parsed
        assertNotNull "success field not found in $result", parsed.success
        assertEquals true, parsed.success

        assertNotNull "person field not found in $result", parsed.person
        assertNotNull parsed.person.id
        assertEquals 1, parsed.person.id
        assertNotNull parsed.person.firstName
        assertEquals 'Bob', parsed.person.firstName
        assertNotNull parsed.person.lastName
        assertEquals 'Bear', parsed.person.lastName
    }
    
    void testRenderJsonWithEmptyList() {
        log.debug("------------ testRenderJsonWithEmptyList() -------------")
        assertNotNull controller
        
        obj << [success: true, data: [], message:'No results found']
        
        def result = controller.renderJSON(obj, obj)
        assertNotNull result
        assertTrue "Result was instead: $result", result.indexOf("No results found") >= 0
        
        def parsed = JSON.parse(result)
        assertNotNull parsed
        assertNotNull "success field not found in $result", parsed.success
        assertNotNull "message field not found in $result", parsed.message
        assertEquals true, parsed.success 
        assertEquals 'No results found', parsed.message 

    }

    void testRenderJsonWithEmptyListNoMessage() {
        log.debug("------------ testRenderJsonWithEmptyListNoMessage() -------------")
        assertNotNull controller
        
        obj << [success: true, data: []]
        
        def result = controller.renderJSON(obj, obj)
        assertNotNull result
        assertTrue "Result was instead: $result", result.indexOf("No data available") >= 0
        
        def parsed = JSON.parse(result)
        assertNotNull parsed
        assertNotNull "success field not found in $result", parsed.success
        assertNotNull "message field not found in $result", parsed.message
        assertEquals true, parsed.success
        assertEquals 'No data available', parsed.message 
    }
    
    void testRenderJsonWithDataList() {
        log.debug("------------ testRenderJsonWithDataList() -------------")
        assertNotNull controller
        
        def dc1 = new Person(id: 1, firstName:'Bilbo', lastName:'Baggins')
        def dc2 = new Person(id: 2, firstName:'Frodo', lastName:'Baggins')
        def dc3 = new Person(id: 3, firstName:'Mrs.', lastName:'Baggins')
        
        obj << [success: true, data: [dc1,dc2,dc3]]
        
        def result = controller.renderJSON(obj, obj, 'data', true)  // <-- note we render as a List
        assertNotNull result
        assertTrue result.indexOf("Baggins") > 0
        
        def parsed = JSON.parse(result)
        assertNotNull parsed
        assertNotNull "success field not found in $result", parsed.success
        assertEquals true, parsed.success 
        
        assertNotNull "count field not found in $result", parsed.count
        assertEquals 3, parsed.count 
        
        assertNotNull "data field not found in $result", parsed.data
        assertNotNull "data[0] field not found in $result", parsed.data[0]
        assertNotNull parsed.data[0].id
        assertEquals 1, parsed.data[0].id 
        assertNotNull parsed.data[0].firstName
        assertEquals 'Bilbo', parsed.data[0].firstName 
        assertNotNull parsed.data[0].lastName
        assertEquals 'Baggins', parsed.data[0].lastName
    }
    
    void testRenderJsonWithDataListEntityRoot() {
        log.debug("------------ testRenderJsonWithDataListEntityRoot() -------------")
        assertNotNull controller
        
        def dc1 = new Person(id: 1, firstName:'Bilbo', lastName:'Baggins')
        def dc2 = new Person(id: 2, firstName:'Frodo', lastName:'Baggins')
        def dc3 = new Person(id: 3, firstName:'Mrs.', lastName:'Baggins')
        
        obj << [success: true, persons: [dc1,dc2,dc3]]
        
        def result = controller.renderJSON(obj, obj, 'persons', true)  // <-- note we render as a List
        assertNotNull result
        assertTrue "Result contains unexpected data: $result", result.indexOf("count") > 0
        
        def parsed = JSON.parse(result)
        assertNotNull parsed
        assertNotNull "success field not found in $result", parsed.success
        assertEquals true, parsed.success 
        
        assertNotNull "count field not found in $result", parsed.count
        assertEquals 3, parsed.count
        
        assertNotNull "persons field not found in $result", parsed.persons
        assertNotNull "persons[0] field not found in $result", parsed.persons[0]
        assertNotNull parsed.persons[0].id
        assertEquals 1, parsed.persons[0].id
        assertNotNull parsed.persons[0].firstName
        assertEquals 'Bilbo', parsed.persons[0].firstName 
        assertNotNull parsed.persons[0].lastName
        assertEquals 'Baggins', parsed.persons[0].lastName
    }
    

}
