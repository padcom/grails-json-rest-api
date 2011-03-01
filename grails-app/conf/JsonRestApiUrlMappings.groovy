import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.grails.plugins.rest.JSONApiRegistry

class JsonRestApiUrlMappings {

	static mappings = {
		def config = ConfigurationHolder.config.grails.'json-rest-api'
		def root = config.root ? config.root : '/api'

		"${root}/$domain" (controller: 'jsonRestApi') {
			entity = { JSONApiRegistry.registry[params.domain] }
			action = [ GET: 'list', POST: 'create' ]
		}

		"${root}/$domain/$id" (controller: 'jsonRestApi') {
			entity = { JSONApiRegistry.registry[params.domain] }
			action = [ GET: 'show', PUT: 'update', DELETE: 'delete' ]
		}
	}

}
