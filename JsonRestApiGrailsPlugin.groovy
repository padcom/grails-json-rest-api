import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.grails.plugins.rest.JSONApiRegistry

class JsonRestApiGrailsPlugin {
    // the plugin version
    def version = "1.0.5"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Matthias Hryniszak"
    def authorEmail = "padcom@gmail.com"
    def title = "JSON RESTful API for GORM"
    def description = '''\\
This plugin provides effortless JSON API for GORM classes
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/json-rest-api"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        application.domainClasses.each { domainClass ->
            def resource = domainClass.getStaticPropertyValue('expose', String)
            if (resource) {
                JSONApiRegistry.registry[resource] = domainClass.fullName
            }
        }
    }

    def doWithApplicationContext = { applicationContext ->
        grails.converters.JSON.registerObjectMarshaller(new org.grails.plugins.rest.JSONDomainMarshaller())
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
