package org.grails.plugins.test

import grails.converters.JSON

import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication


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
    
    void genericTestList(Class clazz) {
        // Backwards compat test
        this.doGenericTestList (clazz, false, false)
        // Extra capabilities mode
        this.doGenericTestList (clazz, true, true)
    }
    
    def doGenericTestList = { Class clazz, def useEntityRootName, def dataFieldsOnly ->
                
        def cname = clazz.name.substring(clazz.name.lastIndexOf('.')+1).toLowerCase()
        def dataNode = useEntityRootName ? cname : 'data'
        log.debug("------- ${cname}.testList() ---- [${useEntityRootName?'entityName node':'data node'}] [${dataFieldsOnly?'no ':''}status fields] ---------")
        
        def startCnt = clazz.count()

        clazz.build()
        //clazz.build()
        //clazz.build()
        //clazz.build()

        def cnt = clazz.count()
        assertEquals startCnt+1, cnt
        

        // *** Send REST Request ***
        get("/api/$cname?useEntityRootName=${useEntityRootName}&dataFieldsOnly=${dataFieldsOnly}")
        assertStatus 200

        // *** Examine Result ***
        def model = response.contentAsString

        log.debug("list() returned:: " + model)
        def map = JSON.parse(model)
        log.debug("Parsed map: " + map)
        

        // Our entry should be one and only
        def obj = map[dataNode][0]
        assertNotNull("returned data has no id", obj.id)
        
        
        if (dataFieldsOnly) {
            assertFalse "result contains success flag", map.containsKey('success')
        }
        else {
            assertEquals "success flag", true, map['success']
            assertEquals startCnt+4, map.count
        }
        
        if (useEntityRootName) {
            assertFalse "result contains data param", map.containsKey('data')
            assertNotNull "result does not contain entityName param", map[cname]
        }
        else {
            assertFalse "result contains entityName param", map.containsKey(cname)
            assertNotNull "result does not contain data", map.data
        }

    }

    
    /**
     * @param clazz
     * @param args  Map of args to include in created object; values get assert afterwards
     */
    void genericTestCreate(Class clazz, def args) {
        // Backwards compat test
        this.doGenericTestCreate (clazz, args, false, false)
        // Extra capabilities mode
        this.doGenericTestCreate (clazz, args, true, true)
    }
    
    def doGenericTestCreate = { Class clazz, def args, def useEntityRootName, def dataFieldsOnly ->
        
        def cname = clazz.name.substring(clazz.name.lastIndexOf('.')+1).toLowerCase()
        def dataNode = useEntityRootName ? cname : 'data'
        
        log.debug("------- ${cname}.testCreate() ---- [${useEntityRootName?'entityName node':'data node'}] [${dataFieldsOnly?'no ':''}status fields] ---------")
        
        def value = 'testKey'
        def json = [(dataNode) : args] as JSON
        
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
        log.debug("Parsed map: " + map)
        
        // Newly created object
        def obj = map[dataNode]
        
        assertNotNull obj.id
        
        // Ensure each value was saved
        args.each { entry ->
            assertEquals args[entry.key], obj[entry.key]
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
    }
    
    void genericTestShow(Class clazz, Map args) {
        // Backwards compat test
        this.doGenericTestShow (clazz, args, false, false)
        // Extra capabilities mode
        this.doGenericTestShow (clazz, args, true, true)
    }

    /**
     * @param clazz
     * @param args  Map of args to include in created object; object is retreieved with 'show' and values asserted
     */
    def doGenericTestShow = { Class clazz, def args, def useEntityRootName, def dataFieldsOnly ->
                
        def cname = clazz.name.substring(clazz.name.lastIndexOf('.')+1).toLowerCase()
        log.debug("------- ${cname}.testShow() ---- [${useEntityRootName?'entityName node':'data node'}] [${dataFieldsOnly?'no ':''}status fields] ---------")
        
        def startCnt = clazz.count()

        clazz.build(args)
        //clazz.build()
        //clazz.build()
        //clazz.build()
        
        def allObjs = clazz.getAll()
        assertTrue allObjs.size() == startCnt + 1

        // Show first list element
        def show = allObjs[0]

        // *** Send REST Request ***
        //   Note: wanted to use closure mode of passing args but was getting an ArrayIndexOutOfBoundsException - didn't figure out why
        get("/api/$cname/${show.id}?useEntityRootName=${useEntityRootName}&dataFieldsOnly=${dataFieldsOnly}") 
        assertStatus 200
 
        // *** Examine Result ***
        def model = response.contentAsString
        
        def dataNode = useEntityRootName ? cname : 'data'

        log.debug("show() returned:: " + model)
        def map = JSON.parse(model)
        log.debug("Parsed map: " + map)

        // Our entry should be one and only
        def id = map[dataNode]['id']

        assertNotNull(id)
        assertEquals id, show.id
        
        
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
    }
    
    /**
     * Invoke me with a domain class
     * @param clazz
     */
    void genericTestUpdate(Class clazz, Map args) {
        // Backwards compat test
        this.doGenericTestUpdate (clazz, args, false, false)
        // Extra capabilities mode
        this.doGenericTestUpdate (clazz, args, true, true)
    }
    
    def doGenericTestUpdate = { Class clazz, def args, def useEntityRootName, def dataFieldsOnly ->
            
        def cname = clazz.name.substring(clazz.name.lastIndexOf('.')+1).toLowerCase()
        def dataNode = useEntityRootName ? cname : 'data'
        
        log.debug("------- ${cname}.testUpdate() ---- [${useEntityRootName?'entityName node':'data node'}] [${dataFieldsOnly?'no ':''}status fields] ---------")
        
        def startCnt = clazz.count()

        clazz.build()
        //clazz.build()

        def allObjs = clazz.getAll()
        assertTrue allObjs.size() == startCnt + 1

        // Pull first list element for updating
        def show = allObjs[0]
        assertNotNull(show.id)

        // *** Apply user updates ***
        args.each { entry ->
            show[entry.key] = entry.value
        }
        log.debug("update: sending in data obj: $show")

        // Note: in reality we would never use Obj.toJSON(msgSource) here
        //     this would always come from the client; hence simple JSON convert
        def json = [(dataNode) : show] as JSON

        // *** Send REST Request ***
        put("/api/$cname/${show.id}?useEntityRootName=${useEntityRootName}&dataFieldsOnly=${dataFieldsOnly}") {
            headers['Content-type'] = 'application/json'
            body { json }
        }
        assertStatus 200

        // *** Examine Result ***
        def model = response.contentAsString
        
        
        log.debug("update() returned:: " + model)
        def map = JSON.parse(model)
        log.debug("Parsed map: " + map)
        
        // Verify Result
        //   Ensure each value was saved
        def obj = map[dataNode]
        args.each { entry ->
            assertEquals args[entry.key], obj[entry.key]
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
    }

    /**
     * Invoke me with a domain class
     * @param clazz
     */
    void genericTestDelete(Class clazz) {
        // Backwards compat test
        this.doGenericTestDelete (clazz, false, false)
        // Extra capabilities mode
        this.doGenericTestDelete (clazz, true, true)
    }
    
    def doGenericTestDelete = { Class clazz, def useEntityRootName, def dataFieldsOnly ->
        def cname = clazz.name.substring(clazz.name.lastIndexOf('.')+1).toLowerCase()
        log.debug("------- ${cname}.testDelete() ---- [${useEntityRootName?'entityName node':'data node'}] [${dataFieldsOnly?'no ':''}status fields] ---------")
        
        def startCnt = clazz.count()

        clazz.build()
        //clazz.build()

        def allObjs = clazz.getAll()
        assertTrue allObjs.size() == startCnt + 1

        // Delete first list element
        def id = allObjs[0].id
        assertNotNull "id of object to delete is null", id
        
        // *** Send REST Request ***
        delete("/api/$cname/${id}?useEntityRootName=${useEntityRootName}&dataFieldsOnly=${dataFieldsOnly}")
        assertStatus 200

        // *** Examine Result ***
        def model = response.contentAsString

        log.debug("delete() returned:: " + model)
        def map = JSON.parse(model)
        log.debug("Parsed map: " + map)

        // Verify Result
        assertFalse "record still exists", clazz.exists(id)
        
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
    }

    
}
