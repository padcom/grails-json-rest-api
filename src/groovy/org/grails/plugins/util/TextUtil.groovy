package org.grails.plugins.util

class TextUtil {

    static String pluralize (def token) {
        token?.size() > 0 ? (token[token?.size()-1] == "s" ? "${token}es" : "${token}s") : ""
    }
    
    static String emberPluralize (def token) {
        token?.size() > 0 ? "${token}s" : ""
    }
}
