package org.grails.plugins.rest

import grails.converters.*
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.springframework.web.servlet.support.RequestContextUtils as RCU

class JsonRestApiController {
  def list = {
    def result = [ success: true ]
    def entity = grailsApplication.getClassForName(params.entity)
    if (entity) {
      def api = getCustomApi(entity)
      if (api?.list instanceof Closure)
        result.data = api.list(params)
      else
        result.data = entity.list(params)
      if (api?.count instanceof Closure)
        result.count = api.count(params)
      else
        result.count = entity.count()
    } else {
      result.success = false
      result.message = "Entity ${params.entity} not found"
    }
    render text: result as JSON, contentType: 'application/json', status: result.success ? 200 : 500
  }

  def show = {
    def data = retrieveRecord()
    render text: data.result as JSON, contentType: 'application/json', status: data.status
  }
  
  def create = {
    def result = [ success: true ]
    def status = 200
    def entity = grailsApplication.getClassForName(params.entity)
    if (entity) {
      def obj = entity.newInstance()
      obj.properties = request.JSON.data
      obj.validate()
      if (obj.hasErrors()) {
        status = 500
        result.message = extractErrors(obj).join(";")
        result.success = false
      } else {
        result.data = obj.save(flush: true)
      }
    } else {
      result.success = false
      result.message = "Entity ${params.entity} not found"
      status = 500
    }
    render text: result as JSON, contentType: 'application/json', status: status
  }
  
  def update = {
    def data = retrieveRecord()
    if (data.result.success) {
      data.result.data.properties = request.JSON.data
      data.result.data.validate()
      if (data.result.data.hasErrors()) {
        data.status = 500
        data.result.message = extractErrors(data.result.data).join(";")
        data.result.success = false
      } else {
      }
    }
    render text: data.result as JSON, contentType: 'application/json', status: data.status
  }

  def delete = {
    def data = retrieveRecord()
    if (data.result.success) {
      data.result.data.delete()
    }
    render text: data.result as JSON, contentType: 'application/json', status: data.status
  }

  private getCustomApi(clazz) {
    clazz.declaredFields.name.contains('api') ? clazz.api : null
  }

  private retrieveRecord() {
    def result = [ success: true ]
    def status = 200
    def entity = grailsApplication.getClassForName(params.entity)
    if (entity) {
      def obj = entity.get(params.id)
      if (obj) {
        result.data = obj
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

  def messageSource

  private extractErrors(model) {
    def locale = RCU.getLocale(request)
    model.errors.fieldErrors.collect { error ->
      messageSource.getMessage(error, locale)
    }
  }

}
