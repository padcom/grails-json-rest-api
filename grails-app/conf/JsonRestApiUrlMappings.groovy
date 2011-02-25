import org.grails.plugins.rest.JSONApiRegistry

class JsonRestApiUrlMappings {

	static mappings = {
		"/api/$domain" (controller: 'jsonRestApi') {
			entity = { JSONApiRegistry.registry[params.domain] }
			action = [ GET: 'list', POST: 'create' ]
		}

		"/api/$domain/$id" (controller: 'jsonRestApi') {
			entity = { normalize(params.domain) }
			action = [ GET: 'show', PUT: 'update', DELETE: 'delete' ]
		}
	}
}
