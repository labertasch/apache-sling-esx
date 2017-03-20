/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.sling.scripting.esx.plugins;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

/**
 *
 * @author stas
 */
public class SimpleResource {

    private final Resource resource;
    private final ValueMap valueMap;

    public SimpleResource(Resource resource) {
        this.resource = resource;
        this.valueMap = resource.getValueMap();
    }

    public String getPath() {
        return resource.getPath();
    }

    public String getResourceType() {
        return resource.getResourceType();
    }

    public String getResourceSuperType() {
        return resource.getResourceSuperType();
    }

    public SimpleResource getParent() {
        return resource.getParent().adaptTo(SimpleResource.class);
    }

    public boolean hasChildren() {
        return resource.hasChildren();
    }

    public boolean isResourceType(String resourceType) {
        return resource.isResourceType(resourceType);
    }

    public ValueMap getValueMap() {
        return valueMap;
    }
    
    public ValueMap getProperties() {
        return getValueMap();
    }
    
    public String getStringProperty(String key) {
        return valueMap.get(key, String.class);
    }
    
    public long getDateTimeProperty(String key) {
        return ((Calendar) valueMap.get(key, Calendar.class)).getTime().getTime();
    }
    
    public Object[] getArray(String key) {
        return (Object[]) valueMap.get(key, Object[].class);
    }

    public SimpleResource getChild(String childName) {
        return resource.getChild(childName).adaptTo(SimpleResource.class);
    }
    
    public ResourceResolver getResourceResolver() {
        return resource.getResourceResolver();
    }
    public List<SimpleResource> getChildren() {
        Iterator<Resource> resChildren = resource.listChildren();
        ArrayList<SimpleResource> children = new ArrayList<SimpleResource>();
        resChildren.forEachRemaining(resource -> children.add(resource.adaptTo(SimpleResource.class)));
        return children;
    }

}
