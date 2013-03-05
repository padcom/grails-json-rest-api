## Description of Changes in this Branch

### Upgraded to Grails 2.1.1

Grails update.

### Support for the JSON REST dialect used by EmberJS

Want to serve objects up to EmberJS?  It's default RESTAdapter is a little finicky. You will need to provide it with [something
that looks like this](http://kentbutlercs.blogspot.hu/2013/02/emberjs-notes-and-gotchas.html)

To accomplish this with the json-rest-api plugin, you need to set the following flags in your grails-app/conf/Config.groovy:

        // Set to 'true' to package JSON objects inside a node named for the object
        //   Value of 'false' will use 'data' as the node name instead 
        //   Default value is false
        //   Enable this for EmberJS
        grails.'json-rest-api'.useEntityRootName = true

        // Set to 'true' to exclude meta-data from the JSON containing the rendered object
        //   Value of 'false' will include meta-data fields such as 'count' and 'status' 
        //   Default value is false
        //   Enable this for EmberJS
        grails.'json-rest-api'.dataFieldsOnly = true

For example, for the 'show' action this will produce output like:

        {"todo":{"id":1,"title":"eat","completed":false}}

instead of:

        {"data":{"id":1,"title":"eat","completed":false},"success":true}

For the 'list' action, enabling these flags gives you:

        {"todo":[{"id":1,"title":"eat","completed":false},{"id":2,"title":"sleep","completed":false},{"id":3,"title":"work","completed":false},{"id":4,"title":"title","completed":false},{"id":5,"title":"title","completed":false},{"id":6,"title":"title","completed":false},{"id":7,"title":"title","completed":false},{"id":8,"title":"title","completed":false},{"id":9,"title":"title","completed":false},{"id":10,"title":"title","completed":false},{"id":11,"title":"title","completed":false}]}

and with the flags disabled:

        {"count":7,"data":[{"id":1,"title":"eat","completed":false},{"id":2,"title":"sleep","completed":false},{"id":3,"title":"work","completed":false},{"id":4,"title":"title","completed":false},{"id":5,"title":"title","completed":false},{"id":6,"title":"title","completed":false},{"id":7,"title":"title","completed":false}],"success":true}

##### Implementation

This is accomplished with changes to:

* `JsonRestApiController.groovy`
  - change to support knowledge of the name of the entity when creating JSON

* `JsonRestApiGrailsPlugin.groovy`
  - change to register the domain class using both singular and plural forms of the domain class name
  - necessary to support EmberJS requests using plural - [see this table](http://kentbutlercs.blogspot.hu/2013/02/emberjs-notes-and-gotchas.html)

* added `org.grails.plugins.util.TextUtil`
  - for pluralizing entity names

* added `org.grails.plugins.test.GenericRestFunctionalTests`
  - provides a way to test the REST interface apps which use this plugin offline
  - sample use of this located [in this project](https://github.com/kentbutler/todomvc-grails-emberjs.git)


### Rendering JSON

The branch adds another (optional) way to render the JSON for your domain classes. I did this because the Grails way wants to render the JSON immediately to a Writer, and I have often found cases where my domain class will get rolled into a larger JSON result, and/or I want to customize the rendering of the JSON to do things like i18n the text going out. I find it more flexible to produce a `JSONObject` from my domain class and let the user decide later when to actually render the JSON.

Following is the writeup I imagined for the plugin front page.

------------------------

This plugin offers 3 different ways to render your domain classes into JSON:

* Grails Standard Approach - register an `ObjectMarshaller` in Bootstrap or in your domain class directly

* Use the plugin's built-in `ObjectMarshaller`, which allows you to exclude fields using the 'custom api' object
- specify 'excludedFields' as a list of field names

* Create method  `JSONObject toJSON(def messageSource)`  in your domain class which will provide:
- ability to apply i18n conversion to object fields while rendering
- return a JSONObject for more flexibility in rendering of JSON throughout your app
- most efficient style for the plugin

See the ToDo domain class in the  [TodoMVC sample app](https://github.com/kentbutler/todomvc-grails-emberjs) for examples of these 3 approaches.


### Unmarshalling from JSON

I addded an optional analagous `fromJSON()` method to the domain class for more flexibility in receiving data from the web client.  Here is my imagined writeup:

-------------------------------------

This plugin offers 2 different ways to render your objects from encoded JSON:

* Grails Standard Approach - uses `JSON.parse()` to produce a `JSONObject`
- transformed into a domain class as         def myObj =  MyObj.class.newInstance()         myObj.properties = JSON.parse(inputString)

* Create method void fromJSON(JSONObject)  in your domain class for more control over transforming input


See the generic functional test class in the [TodoMVC sample app](https://github.com/kentbutler/todomvc-grails-emberjs) for examples of usage.

