package todo

import org.codehaus.groovy.grails.web.json.JSONObject



class Todo {
    
    String title
    boolean isCompleted

    static constraints = {
        title(blank:false, nullable:false,maxSize:64)
        isCompleted(default:false)
    }
    
    
    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append("\n    id:    ").append(id)
        sb.append("\n    Title:    ").append(title)
        sb.append("\n    Completed:    ").append(isCompleted)
        sb.toString()
    }
    
    
    // --- json-rest-api artifacts --- 
    
    static expose = 'todo' // Expose as REST API using json-rest-api plugin
                           //   this will be the entity name on the URL
    static api = [
        // If allowing json-rest-api to use 'as JSON' to render, you may exclude
        //    unwanted fields here (done with its registered ObjectMarshaller)
        excludedFields: [ "attached", "errors", "properties" ],
        // You may override how the list() operation performs its search here
        list : { params -> Todo.list(params) },
        count: { params -> Todo.count() }
    ]

    
    /*
    // This is the standard way to override JSON marshalling for a class
    //    It uses a ClosureOjectMarshaller[sic] to select fields for marshalling 
    //    It is less efficient for the plugin which is based on JSONObject, but this will be
    //    used if you do not define a 'toJSON' method.
    // NOTE: if using this approach, the json-rest-api marshaller will NOT be used, hence the
    //      api.excludedFields if defined will be ignored
    // Example taken from http://grails.org/Converters+Reference
    static {
        grails.converters.JSON.registerObjectMarshaller(Todo) {
           // you can filter here the key-value pairs to output:
           return it.properties.findAll {k,v -> k != 'passwd'}
          }
    }
    */
    
    
    /**
     * Rending this object into a JSONObject; allows more flexibility and efficiency in how
     * the object is eventually included in larger JSON structures before ultimate rendering;
     * MessageSource offered for i18n conversion before exporting for user audience.
     * @param messageSource
     * @return
     */
    JSONObject toJSON(def messageSource) {
        JSONObject json = new JSONObject()
        json.put('id', id)
        json.put('title', title)
        json.put('isCompleted', isCompleted)
        return json
    }

    /**
     * Custom bind from JSON; this has efficiency since the grails request.JSON object offers
     *    a JSONObject directly
     * @param json
     */
    void fromJSON (JSONObject json) {
        [
            "title"
        ].each(optStr.curry(json, this))
       [
            "isCompleted"
        ].each(optBoolean.curry(json, this))
    }

    
    static Closure optStr = {json, obj, prop ->
        if(json?.has(prop)) {
            String propVal = (json?.isNull(prop)) ? null : json?.getString(prop)?.trim()
            obj[prop] = propVal
        }
    }

    static Closure optInt = {json, obj, prop ->
        if(json?.has(prop))
            obj[prop] = (json?.isNull(prop)) ? null : json?.getInt(prop)
    }

    static Closure optBoolean = {json, obj, prop ->
        if(json?.has(prop))
            obj[prop] = (json?.isNull(prop)) ? null : json?.getBoolean(prop)
    }

}
