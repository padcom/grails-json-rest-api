package org.grails.plugins.test

import grails.converters.JSON

import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass


/**
 * Mixin to a project functional test and provide the domain class to test.
 * 
 * @author kent.butler@gmail.com
 *
 */
class GenericRestFunctionalTests {

    def log = LogFactory.getLog(getClass())
    
    protected void setUp() {
        // Cannot access the grailsApplication from a functional test
        // See http://jira.codehaus.org/browse/GEB-175
    }
    
    
    def cloneObject(def obj) {
        if (!obj) return null
        
        def clazz = obj.class
        def d = new DefaultGrailsDomainClass(clazz)
        
        log.trace("Cloning ${clazz?.name} from ====> ${d.persistantProperties}")
        
        def newObj = clazz.newInstance()
        d.persistantProperties.each {
            log.trace("copying ${it.name} ${it.association ? '(association)':''}")
            newObj[it.name] = obj[it.name]
        }
        return newObj
    }


    /**
     * Tests the REST list() action for the class of the given object.  <br/>
     * Example:  GET http://localhost:8080/cook/api/rawIngredient
     * <br/>Tests both plugin modes:<ul>
     * <li>default mode - result objects are returned in JSON under a node called 'data' with supporting 
     * data like 'count' and 'success'</li>
     * <li>bare mode - entity contained in JSON node is named for the entity and with no supporting data</li>
     * 
     * @param populated instance of object to be tested; must not be in persisted state;
     * used as the list content. 
     */
    void genericTestList(def obj) {
        // Backwards compat test
        this.doGenericTestList (obj, false, false)
        // Extra capabilities mode
        this.doGenericTestList (obj, true, true)
    }
    private def doGenericTestList = { def obj, def useEntityRootName, def dataFieldsOnly ->
        assertNotNull obj
        assertNotNull obj.class
        assertNull "Identifier found; given object must not be persisted", obj.id
        
        def clazz = obj.class
        def cname = clazz.expose
        def dataNode = useEntityRootName ? cname : 'data'
        log.debug("------- ${cname}.testList() ---- [${useEntityRootName?'entityName node':'data node'}] [${dataFieldsOnly?'no ':''}status fields] ---------")
        
        
        // Construct as a cheap clone so we can re-use this object
        def newObj = cloneObject(obj)
        log.debug("Saving COPY:: $newObj")
        newObj.save(flush:true)

        assertNotNull 'Object to list is not persisted',newObj.id

        def startCnt = clazz.count()
        
        log.debug "=========== start records =============="
        def recs = clazz.getAll()
        recs.each { log.debug it }
                
        // *** Send REST Request ***
        get("/api/$cname?useEntityRootName=${useEntityRootName}&dataFieldsOnly=${dataFieldsOnly}")
        assertStatus 200

        // *** Examine Result ***
        def model = response.contentAsString

        log.debug("list() returned:: " + model)
        def map = JSON.parse(model)
        log.debug("Parsed response into map: " + map)
        
        def resultObj = map[dataNode][0]
        assertNotNull("returned data has no id", resultObj?.id)
        
        
        if (dataFieldsOnly) {
            assertFalse "result contains success flag", map.containsKey('success')
        }
        else {
            assertEquals "success flag failed", true, map['success']
            assertEquals "count field incorrect", startCnt, map.count
        }
        
        if (useEntityRootName) {
            assertFalse "result contains data param", map.containsKey('data')
            assertNotNull "result does not contain entityName param", map[cname]
        }
        else {
            assertFalse "result contains entityName param", map.containsKey(cname)
            assertNotNull "result does not contain data", map.data
        }
        // Remove created record so this method is runnable again
        clazz.withSession { session ->
            session.evict(newObj)
        }
        
        newObj = clazz.get(newObj.id)
        newObj.delete(flush:true)    
    }

    
    /**
     * Tests the REST create() action for the class of the given object.   <br/>
     * Example:  POST http://localhost:8080/cook/api/rawIngredient
     * <br/> with JSON payload:  {"rawIngredient":{"nameKey":"ingredient.meat.beef","foodGroupId":1}}
     * <br/>Tests both plugin modes:<ul>
     * <li>default mode - object to create and result object are created in JSON under a node called 'data'; result JSON will
     * contain supporting data 'success'</li>
     * <li>bare mode - entity contained in JSON node is named for the entity and with no supporting data</li>
     * 
     * @param populated, unpersisted instance of object to be tested; will be used to create JSON using the object's toJSON(messageSource) 
     *    renderer if defined, or using the object's registered JSON Marshaller (see http://grails.org/Converters+Reference).<br/>
     *    NOTE: do not persist this object, it must arrive with no identifier defined. If containing related entities, those 
     *    *should* be persisted if the object is not configured to create the related(s) via cascade.
     */
    void genericTestCreate(def obj) {
        // Backwards compat test
        this.doGenericTestCreate (obj, false, false)
        // Extra capabilities mode
        this.doGenericTestCreate (obj, true, true)
    }
    def doGenericTestCreate = { def obj, def useEntityRootName, def dataFieldsOnly ->        
        assertNotNull obj
        assertNotNull obj.class
        assertNull "Identifier found; given object must not be persisted", obj.id
        
        def cname = obj.class.expose
        def dataNode = useEntityRootName ? cname : 'data'
                
        log.debug("------- ${cname}.testCreate() ---- [${useEntityRootName?'entityName node':'data node'}] [${dataFieldsOnly?'no ':''}status fields] ---------")
        
        def supportsToJson = obj?.respondsTo('toJSON')
        def json = (supportsToJson ? [(dataNode) : obj?.toJSON(messageSource)] as JSON : [(dataNode) : obj] as JSON )
        
        log.debug("Creating with JSON: " + json)
        
        // *** Send REST Request ***
        post("/api/${cname}?useEntityRootName=${useEntityRootName}&dataFieldsOnly=${dataFieldsOnly}") {
        headers['Content-type'] = 'application/json'
            body { json }
        }
        assertStatus 200
        
        // *** Examine Result ***
        def model = response.contentAsString
                
        log.debug("create() returned:: " + model)
        def map = JSON.parse(model)
        log.debug("Parsed response into map: " + map)
        
        // Newly created object
        def newObj = map[dataNode]
        
        assertNotNull newObj.id
                
        if (dataFieldsOnly) {
            assertFalse "result contains success flag", map.containsKey('success')
        }
        else {
            assertEquals "success flag", true, map['success']
        }
        
        if (useEntityRootName) {
            assertFalse "result contains data param", map.containsKey('data')
            assertNotNull "result does not contain entityName param", map[cname]
        }
        else {
            assertFalse "result contains entityName param", map.containsKey(cname)
            assertNotNull "result does not contain data", map.data
        }
        
        // Clean up by deleting object, else unique constraints may be violated
        obj.class.get(newObj.id).delete(flush:true)
    }
    
    /**
     * Tests the REST show() action for the class of the given object.  <br/>
     * Example:   GET http://localhost:8080/cook/api/rawIngredient/1
     * <br/>Tests both plugin modes:<ul>
     * <li>default mode - result object is returned in JSON under a node called 'data' with supporting
     * data 'success'</li>
     * <li>bare mode - entity contained in JSON node is named for the entity and with no supporting data</li>
     * 
     * @param populated instance of object to be tested; object must not be in persisted state;
     * used as the object whose content is to be shown by the query.
     */
    void genericTestShow(def obj) {
        // Backwards compat test
        this.doGenericTestShow (obj, false, false)
        // Extra capabilities mode
        this.doGenericTestShow (obj, true, true)
    }
    def doGenericTestShow = { def obj, def useEntityRootName, def dataFieldsOnly ->
        assertNotNull obj
        assertNotNull obj.class
        assertNull "Identifier found; given object must not be persisted", obj.id
        
        def clazz = obj.class
        def cname = obj.class.expose
        def dataNode = useEntityRootName ? cname : 'data'
        
        log.debug("------- ${cname}.testShow() ---- [${useEntityRootName?'entityName node':'data node'}] [${dataFieldsOnly?'no ':''}status fields] ---------")
        
        // Construct as a cheap clone so we can re-use this object
        def newObj = cloneObject(obj)
        newObj.save(flush:true)
        
        assertNotNull 'Object to show is not persisted',newObj.id
        
        // *** Send REST Request ***
        //   Note: wanted to use closure mode of passing args but was getting an ArrayIndexOutOfBoundsException - didn't figure out why
        get("/api/$cname/${newObj.id}?useEntityRootName=${useEntityRootName}&dataFieldsOnly=${dataFieldsOnly}") 
        assertStatus 200
 
        // *** Examine Result ***
        def model = response.contentAsString
        
        log.debug("show() returned:: " + model)
        def map = JSON.parse(model)
        log.debug("Parsed response into map: " + map)

        // Our entry should be one and only
        def id = map[dataNode]['id']

        assertNotNull(id)
        assertEquals "Result id is not the same id", newObj.id, id 
        
        
        if (dataFieldsOnly) {
            assertFalse "result contains success flag", map.containsKey('success')
        }
        else {
            assertEquals "success flag", true, map['success']
        }
        
        if (useEntityRootName) {
            assertFalse "result contains data param", map.containsKey('data')
            assertNotNull "result does not contain entityName param", map[cname]
        }
        else {
            assertFalse "result contains entityName param", map.containsKey(cname)
            assertNotNull "result does not contain data", map.data
        }
        // Remove created record so this method is runnable again
        clazz.withSession { session ->
            session.evict(newObj)
        }
        
        newObj = clazz.get(newObj.id)
        newObj.delete(flush:true)    
    }
    
    /**
     * Tests the REST update() action for the class of the given object.   <br/>
     * Example:   PUT http://localhost:8080/cook/api/rawIngredient/1
     * <br/> with JSON payload:  {"data":{"id":1,"nameKey":"ingredient.meat.chicken","foodGroupId":1}}
     * <br/>Tests both plugin modes:<ul>
     * <li>default mode - object to update and result object are created in JSON under a node called 'data'; result JSON will
     * contain supporting data 'success'</li>
     * <li>bare mode - entity contained in JSON node is named for the entity and with no supporting data</li>
     * 
     * @param populated, unpersisted instance of object to be tested; will be used to create JSON using the object's toJSON(messageSource) 
     *    renderer if defined, or using the object's registered JSON Marshaller (see http://grails.org/Converters+Reference).<br/>
     *    NOTE: do not persist this object, it must arrive with no identifier defined. If containing related entities, those 
     *    *should* be persisted if the object is not configured to create the related(s) via cascade.
     *    
     * @param Map of args containing fields and new values to update on the object. Will iterate the args and apply values
     *     to the object before updating, and assert updated values against the returned object.
     */
    void genericTestUpdate(def obj, def args) {
        // Backwards compat test
        this.doGenericTestUpdate (obj, args, false, false)
        // Extra capabilities mode
        this.doGenericTestUpdate (obj, args, true, true)
    }
    def doGenericTestUpdate = { def obj, def args, def useEntityRootName, def dataFieldsOnly ->
        assertNotNull obj
        assertNotNull obj.class
        assertNotNull args
        assertNull "Identifier found; given object must not be persisted", obj.id
        
        def clazz = obj.class
        def cname = clazz.expose
        def dataNode = useEntityRootName ? cname : 'data'
        
        log.debug("------- ${cname}.testUpdate() ---- [${useEntityRootName?'entityName node':'data node'}] [${dataFieldsOnly?'no ':''}status fields] ---------")

        def startCnt = clazz.count()
        
        // Construct as a cheap clone so we can re-use this object
        def newObj = cloneObject(obj)
        newObj.save(flush:true)
        
        assertNotNull(newObj.id)
        assertEquals startCnt+1, clazz.count()

        log.debug("Num existing objects: ${startCnt+1}")
        
        // *** Apply user updates ***
        args.each { entry ->
            newObj[entry.key] = entry.value
        }

        // Note: in reality we would never use Obj.toJSON(msgSource) here
        //     this would always come from the client; hence simple JSON convert
        //def json = [(dataNode) : newObj] as JSON
        def supportsToJson = obj?.respondsTo('toJSON')
        def json = (supportsToJson ? [(dataNode) : newObj?.toJSON(messageSource)] as JSON : [(dataNode) : newObj] as JSON )

        log.debug("Updating with JSON: " + json)
        
        // *** Send REST Request ***
        put("/api/$cname/${newObj.id}?useEntityRootName=${useEntityRootName}&dataFieldsOnly=${dataFieldsOnly}") {
            headers['Content-type'] = 'application/json'
            body { json }
        }
        assertStatus 200

        // *** Examine Result ***
        def model = response.contentAsString
        
        
        log.debug("update() returned:: " + model)
        def map = JSON.parse(model)
        log.debug("Parsed response into map: " + map)
        
        // Verify Result
        //   Ensure each value was saved
        def resultObj = map[dataNode]
        args.each { entry ->
            assertEquals args[entry.key], resultObj[entry.key]
        }
        
        if (dataFieldsOnly) {
            assertFalse "result contains success flag", map.containsKey('success')
        }
        else {
            assertEquals "success flag", true, map['success']
        }
        
        if (useEntityRootName) {
            assertFalse "result contains data param", map.containsKey('data')
            assertNotNull "result does not contain entityName param", map[cname]
        }
        else {
            assertFalse "result contains entityName param", map.containsKey(cname)
            assertNotNull "result does not contain data", map.data
        }
        
        // Remove created record so this method is runnable again
        clazz.withSession { session ->
            session.evict(newObj)
        }
        
        newObj = clazz.get(newObj.id)
        newObj.delete(flush:true)
    }

    /**
     * Tests the REST delete() action for the class of the given object.  <br/>
     * Example:   DELETE http://localhost:8080/cook/api/rawIngredient/1
     * <br/>Tests both plugin modes:<ul>
     * <li>default mode - result object is returned in JSON under a node called 'data' with supporting
     * data 'success'</li>
     * <li>bare mode - entity contained in JSON node is named for the entity and with no supporting data</li>
     * 
     * @param populated, unpersisted instance of object to be tested; used as the object whose content is 
     *    to be shown by the query.
     */
    void genericTestDelete(def obj) {
        // Backwards compat test
        this.doGenericTestDelete (obj, false, false)
        // Extra capabilities mode
        this.doGenericTestDelete (obj, true, true)
    }
    def doGenericTestDelete = { def obj, def useEntityRootName, def dataFieldsOnly ->
        assertNotNull obj
        assertNotNull obj.class
        assertNull "Identifier found; given object must not be persisted", obj.id
        
        def clazz = obj.class
        def cname = clazz.expose
        assertNotNull "Class is not exposed for plugin", cname
        log.debug("------- ${cname}.testDelete() ---- [${useEntityRootName?'entityName node':'data node'}] [${dataFieldsOnly?'no ':''}status fields] ---------")

        // Construct as a cheap clone so we can re-use this object
        def newObj = cloneObject(obj)
        newObj.save(flush:true)
        
        def startCnt = clazz.count()
        
        log.debug("Start count: $startCnt")

        def allObjs = clazz.getAll()
        assertEquals  startCnt, allObjs.size()

        def id = newObj.id
        assertNotNull "id of object to delete is null", id
        
        // *** Send REST Request ***
        delete("/api/${cname}/${id}?useEntityRootName=${useEntityRootName}&dataFieldsOnly=${dataFieldsOnly}")
        assertStatus 200

        // *** Examine Result ***
        def model = response.contentAsString

        log.debug("delete() returned:: " + model)
        def map = JSON.parse(model)
        log.debug("Parsed response into map: " + map)

        // Verify Result
        assertFalse "record still exists", clazz.exists(id)
        
        if (dataFieldsOnly) {
            assertFalse "result contains success flag", map.containsKey('success')
        }
        else {
            assertEquals "success flag", true, map['success']
        }
        // In the delete case, there should be no returned data .. CHECK that this is compliant with original grails-json-rest-api
/*
        if (useEntityRootName) {
            assertFalse "result contains data param", map.containsKey('data')
            assertNotNull "result does not contain entityName param", map[cname]
        }
        else {
            assertFalse "result contains entityName param", map.containsKey(cname)
            assertNotNull "result does not contain data", map.data
        }
*/
    }

    
}
