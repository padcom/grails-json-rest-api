package org.grails.plugins.rest;

import java.beans.PropertyEditorSupport;

public class NumberToDomainInstanceEditor extends PropertyEditorSupport {
    private final Class domainClass
    
    public NumberToDomainInstanceEditor(Class domainClass) {
        this.domainClass = domainClass
    }

    @Override
    public void setValue(Object value) {
        if (value.class == domainClass)
            super.setValue(value)
        else if (value instanceof Number) {
            def instance = domainClass.get(value)
            super.setValue(instance)
        }
    }
}
