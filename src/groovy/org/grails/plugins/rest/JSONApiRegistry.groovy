package org.grails.plugins.rest

import org.grails.plugins.util.TextUtil

class JSONApiRegistry {
    static registry = [:]
    static singulars = [:]
    
    /**
     * Register a given entity name with its full domain classname
     * Will also automatically register the plural version of the entity name
     * @param name
     * @param className
     * @return
     */
    static register(def name, def className) {
        registry[name] = className
        // Register the plural, if not already
        registry[TextUtil.pluralize(name)] = className
        // Keep track of plural->singular mapping, and singular->singular for convenience
        singulars[name] = name
        singulars[TextUtil.pluralize(name)] = name
    }
    
    static getEntity(def token) {
        return registry[token]
    }
    
    /**
     * See if a singular form of the given token has been registered;
     * used in processing incoming URLs to identify the root form of the word
     * @param token
     * @return
     */
    static getSingular(def token) {
        singulars[token]
    }
    
}
