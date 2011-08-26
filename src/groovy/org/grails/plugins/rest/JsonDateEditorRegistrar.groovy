package org.grails.plugins.rest;

// Enables JSON serialized dates to be properly deserialized
// 
// Ref: http://stackoverflow.com/questions/2871977/binding-a-grails-date-from-params-in-a-controller

import org.springframework.beans.PropertyEditorRegistrar
import org.springframework.beans.PropertyEditorRegistry
import org.springframework.beans.propertyeditors.CustomDateEditor
import java.text.SimpleDateFormat

public class JsonDateEditorRegistrar implements PropertyEditorRegistrar {

    public void registerCustomEditors(PropertyEditorRegistry registry) {

        registry.registerCustomEditor(Date, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"), true))
    }
}
