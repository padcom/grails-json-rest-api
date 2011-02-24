class JsonRestApiUrlMappings {

	static mappings = {
		"/api/$domain" (controller: 'jsonRestApi') {
			entity = { normalize(params.domain) }
			action = [ GET: 'list', POST: 'create' ]
		}

		"/api/$domain/$id" (controller: 'jsonRestApi') {
			entity = { normalize(params.domain) }
			action = [ GET: 'show', PUT: 'update', DELETE: 'delete' ]
		}
	}

	private static normalize(name) {
		def parts = name.split("-")
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].size() > 0)
				parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1)
		}
		return parts.join()
	}
}
