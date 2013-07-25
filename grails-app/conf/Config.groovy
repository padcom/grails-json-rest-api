// configuration for plugin testing - will not be included in the plugin zip
 
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}

//
// This is how you change the root URL for this plugin:
//
// grails.'json-rest-api'.root = '/json'
//
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"

environments {
    test {
        grails.logging.jul.usebridge = false
        log4j = {
            appenders {
                rollingFile name:"plugin", maxFileSize:"10000KB", maxBackupIndex:10, file:"logs/json-rest-api.log",layout:pattern(conversionPattern: '%d{yyyy-MM-dd HH:mm:ss,SSS z} [%t] %-5p[%c]: %m%n')
                console name:'stacktrace'  // to get stacktraces out to the console
            }
            
            debug 'json.rest.api','grails.app','org.grails.plugins.rest',additivity = true
            warn  'org.codehaus.groovy','org.grails.plugin','grails.spring','net.sf.ehcache','grails.plugin',
                  'org.apache','com.gargoylesoftware.htmlunit','org.codehaus.groovy.grails.orm.hibernate','org.hibernate'
            
            root {
                debug 'plugin','stacktrace' 
                additivity = true
            }
        }
    }
}
