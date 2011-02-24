package org.grails.plugins.rest

import grails.converters.*

class JsonRestApiController {
  def list = {
    def result = [ success: true ]
    def entity = grailsApplication.getClassForName(params.entity)
    if (entity) {
      result.count = entity.count()
      result.data = entity.list(params)
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
        result.status = 500
        result.message = 'Invalid object' // TODO: define an error message that makes sense
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
        data.result.data = null
        data.result.message = 'Invalid object' // TODO: define an error message that makes sense
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
}
