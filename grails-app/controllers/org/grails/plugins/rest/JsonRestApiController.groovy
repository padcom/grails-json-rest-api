package org.grails.plugins.rest

import grails.converters.*

import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.web.servlet.support.RequestContextUtils as RCU

class JsonRestApiController {

  def log = LogFactory.getLog(getClass())
  def messageSource
  def grailsApplication
  static def wrapResults
  static def useEntityRootName
  static def dataFieldsOnly
  static final String WRAP_RESULTS = 'wrapResults'
  static final String DEFAULT_ENTITY_ROOT = 'data'
  static final String DATA_FIELDS_ONLY = 'dataFieldsOnly'


  def list = {
    def result = [ success: true ]
    def entity = grailsApplication.getClassForName(params.entity)
    def entityRoot = resolveEntityRoot(params, true)  // we just need the property to be resolved
    
    if (useEntityRootName) {
        // in other words, mimic incoming URL in rendered list
        entityRoot = params.domain
    }
    
    if (entity) {
      def api = getCustomApi(entity)
      if (api?.list instanceof Closure)
        result[entityRoot] = api.list(params)
      else
        result[entityRoot] = entity.list(params)
      if (api?.count instanceof Closure)
        result.count = api.count(params)
      else
        result.count = entity.count()
    } else {
      result.success = false
      result.message = "Entity ${params.entity} not found"
    }
    render text: renderJSON(result, params, entityRoot, true), contentType: 'application/json', status: result.success ? 200 : 500
  }

  def show = {
      log.debug("Request params: $params")
      def entity = grailsApplication.getClassForName(params.entity)
      def entityRoot = resolveEntityRoot(params)
      def query = retrieveRecord(entity, entityRoot)
      if (query.result[entityRoot]) {
          def o = query.result[entityRoot]
          log.debug("returned result is type: ${o.class.name}")
      } else { log.debug("no query result #####") }
      
      render text: renderJSON(query.result, params, entityRoot), contentType: 'application/json', status: query.status
  }
  
  def create = {
    log.debug("*** Processing create() ***")
    def entity = grailsApplication.getClassForName(params.entity)
    def entityRoot = resolveEntityRoot(params)
    def wrapped = resolveWrapResults(params) // if wrapped then expect incoming data wrapped also
    
    def result = [ success: true ]
    def status = 200
    
    if (entity) {
      def obj = entity.newInstance()
      log.debug("Resolved entityRoot [$entityRoot] with JSON: ${request.JSON}")
      def json = wrapped ? request.JSON?.opt(entityRoot) : request.JSON
      if (json) {
         log.debug("creating from $json")
         bindFromJSON(obj, json)
      }
      else {
          log.debug("create(): no JSON request data available")
      }

      obj.validate()
      if (obj.hasErrors()) {
        status = 500
        result.message = extractErrors(obj).join(";")
        result.success = false
      } else {
        result[entityRoot] = obj.save(flush: true)
        log.debug("Returning saved object under root [${entityRoot}]: ${result[entityRoot]}")
      }
    } else {
      result.success = false
      result.message = "Entity ${params.entity} not found"
      status = 500
    }
    render text: renderJSON(result, params, entityRoot), contentType: 'application/json', status: status
  }
  
  def update = {
    def entity = grailsApplication.getClassForName(params.entity)
    def entityRoot = resolveEntityRoot(params)
    def wrapped = resolveWrapResults(params) // if wrapped then expect incoming data wrapped also
    
    def query = retrieveRecord(entity, entityRoot)
    def obj 
    if (query.result.success) {
      obj = query.result[entityRoot]
      log.debug("update: located object to update: $obj")
      def json = wrapped ? request.JSON?.opt(entityRoot) : request.JSON
      
      log.debug("update: binding input data: $json")
      if (json) {
         bindFromJSON(obj, json)
      }
      else {
          log.debug("update(): no JSON request data available")
      }

      obj.validate()
      if (obj.hasErrors()) {
        query.status = 500
        query.result.message = extractErrors(query.result[entityRoot]).join(";")
        query.result.success = false
      } else {
         log.debug("update: saving ")
         obj = obj.save(flush:true)
      }
    }
    render text: renderJSON(query.result, params, entityRoot), contentType: 'application/json', status: query.status
  }

  def delete = {
    def entity = grailsApplication.getClassForName(params.entity)
    def entityRoot = resolveEntityRoot(params)
  
    def query = retrieveRecord(entity, entityRoot)
    try {
      if (query.result.success) {
         log.debug("**** deleting entity: ${query.result[entityRoot].id} ****")
         query.result[entityRoot].delete(flush: true)
         query.result[entityRoot] = null // To return an empty value in response
      }
    } catch (Exception e) {
      query.result.success = false
      query.result.message = e.message
      query.status = 500
    }
    render text: renderJSON(query.result, params, entityRoot), contentType: 'application/json', status: query.status
  }

  private getCustomApi(clazz) {
    clazz.declaredFields.name.contains('api') ? clazz.api : null
  }

  private boolean resolveWrapResults(def params) {
      if (params.containsKey(WRAP_RESULTS)) {
          // prefer URl overrides, mainly for testing
          wrapResults = new Boolean(params.wrapResults)
      }
      else if (wrapResults == null) {
          wrapResults = resolveFromConfig(WRAP_RESULTS)
      }
      return wrapResults
  }
  

  private String resolveEntityRoot(def params, boolean multi=false) {
      def entityName = params?.domain
      log.debug("Resolving entityRoot for name [$entityName] and multi [$multi]")
      if (params.containsKey('useEntityRootName')) {
          // prefer URl overrides, mainly for testing
          useEntityRootName = new Boolean(params.useEntityRootName)
      }
      else if (useEntityRootName == null) {
          useEntityRootName = resolveFromConfig('useEntityRootName')
      }
      else {
          log.debug("using existing value for [useEntityRootName] ")
      }
      // entityRoot name must always be the singular - hence root
      def root = useEntityRootName ? (JSONApiRegistry.getSingular(entityName)) : DEFAULT_ENTITY_ROOT
      log.debug("entityRoot resolved as $root")
      return root
  }
  
  private boolean resolveDataFieldsOnly(def params) {
      if (params.containsKey(DATA_FIELDS_ONLY)) {
          // prefer URl overrides, mainly for testing
          dataFieldsOnly = new Boolean(params.dataFieldsOnly)
      }
      else if (dataFieldsOnly == null) {
          dataFieldsOnly = resolveFromConfig(DATA_FIELDS_ONLY)
      }
      return dataFieldsOnly
  }
  
  private def resolveFromConfig(def property) {
      assert grailsApplication != null
      def config = grailsApplication.config.grails.'json-rest-api'
      log.debug("looking up $property in config: ${config}")
      return config[property]
  }
  
  private String renderJSON (def obj, def params, String entityRoot=DEFAULT_ENTITY_ROOT, def renderAsList=false) {
      log.debug("Rendering domainClass as JSON under node '$entityRoot'")
      if (obj?.success == false) {
          log.debug("Result was error with message: ${obj.message}")
      }
      def json = new JSONObject()
      
      resolveWrapResults(params)        // resolve return style for results
      resolveDataFieldsOnly(params)     // make sure we know what to include

      if (!obj[entityRoot]) {
          log.debug("obj.$entityRoot is null, rendering as empty list")
          // Do not alter the response statuses, our only job here is to render into JSON;
          //   If there is no data and no message though, do give some indication why there was no JSON
          obj.message = obj.message ? obj.message : "No data available"
          obj[entityRoot] = []
      }

      JSONArray dcList = new JSONArray()  // this is for our domain classes
      def mc, dc
      def supportsToJson = false

      if (java.util.Collection.class.isAssignableFrom(obj[entityRoot].class)) {
          log.debug("domainClass node is a list")
          if (obj[entityRoot].size() > 0) {
              dc = obj[entityRoot][0]
              mc = obj[entityRoot][0].metaClass
          }
          else {
              // Empty list...do nothing
          }
      }
      else {
          log.debug("domainClass node is a domain class")
          dc = obj[entityRoot]
          mc = obj[entityRoot].metaClass
          // Normalize the single entity as a list
          obj[entityRoot] = [ obj[entityRoot] ]
      }

      // Does our domain class support the toJSON() method?
      supportsToJson = dc ? mc?.respondsTo(dc, 'toJSON') : false
      log.debug("obj.$entityRoot ${supportsToJson?'supports':'does not support'} toJSON")

      log.debug("rendering object list")
      obj[entityRoot].each() { dcObj ->
          if (supportsToJson) { 
              // Custom convert domain object
              dcList.add(dcObj.toJSON(messageSource))
          }
          else {
              // Use the registered ObjectMarshaller for the domain class
              // This is possibly the catch-all marshaller that this plugin registered, or the user could 
              //    have registered a custom marshaller for the domain class
              // Note this requires an extra step of parsing the rendered String back into a JSONObject, 
              //    but the ObjectMarshaller interface gives us little choice
              //    if we want the flexibility of using a JSONObject
              def dcObjConverter = (dcObj as JSON)
              dcList.add(new JSONObject(dcObjConverter.toString()))
          }
      }

      if (wrapResults) {
          // This output style allows for options of how to package results
          
          // Render full object without re-rendering domain class
          if (!dataFieldsOnly) {
               json.put("success",obj.success)
               json.put("message",obj.message)
          }
          if (renderAsList) {
              json.put(entityRoot, dcList)
              if (!dataFieldsOnly) {
                  json.put("count", dcList.size())
              }
          }
          else if (dcList.size() > 0){
              log.debug("Render single entity")
              json.put(entityRoot, dcList.getJSONObject(0))
          }
      }
      else {
          // This output style allows for ONLY DATA
          if (renderAsList) {
              json = dcList
          }
          else if (dcList.size() > 0){
              log.debug("Render single entity")
              json = dcList.getJSONObject(0)
          }
          else {
              log.warn("renderJSON has no single result to render for object: $obj")
          }
      }

      def jsonStr = json.toString()
      log.debug("Rendered as: ${jsonStr}")
      return jsonStr
  }

  private void bindFromJSON(def obj, def args) {
      if (obj?.metaClass?.respondsTo(obj, 'fromJSON')) {
          log.debug("bindFromJSON: binding fromJSON()")
          obj.fromJSON(args)
      }
      else {
          log.debug("bindFromJSON: no metaClass; binding from properties")
          obj.properties = args
      }
  }


  /*
  *  returns Map with:
  *      status  - [200|404|500]
  *      result[:] ==>
  *              success             - [true|false]
  *              message             - only if success == false
  *              [data|<entityName>] - the entity
  */
  private retrieveRecord(Class entity, String entityRoot) {
    def result = [ success: true ]
    def status = 200
    if (entity) {
      def obj = entity.get(params.id)
      if (obj) {
        result[entityRoot] = obj
      } else {
        result.success = false
        result.message = "Object with id=${params.id} not found"
        status = 404
      }
    } else {
      result.success = false
      result.message = "Entity ${params.entity} not found"
      status = 500
    }

    [ result: result, status: status ]
  }

  private extractErrors(model) {
    def locale = RCU.getLocale(request)
    model.errors.fieldErrors.collect { error ->
      messageSource.getMessage(error, locale)
    }
  }

}
