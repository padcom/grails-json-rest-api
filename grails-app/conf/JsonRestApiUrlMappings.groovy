import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.grails.plugins.rest.JSONApiRegistry


class JsonRestApiUrlMappings  {
    

	static mappings = {
        //TODO Replace if a real solution is offered to these
        //   http://jira.grails.org/browse/GRAILS-8508
        //   http://jira.grails.org/browse/GRAILS-8616
        //   http://jira.grails.org/browse/GRAILS-8598
        //   
        // otherwise let's not suffer goofy workarounds unless forced to
		def config = ConfigurationHolder.config.grails.'json-rest-api'
		def root = config.root ? config.root : '/api'

		"${root}/$domain" (controller: 'jsonRestApi') {
			entity = { JSONApiRegistry.getEntity(params.domain) }  // Registry recognizes plural form
			action = [ GET: 'list', POST: 'create' ]
		}

		"${root}/$domain/$id" (controller: 'jsonRestApi') {
			entity = { JSONApiRegistry.getEntity(params.domain) } // Registry recognizes plural form
			action = [ GET: 'show', PUT: 'update', DELETE: 'delete' ]
		}
	}

}
