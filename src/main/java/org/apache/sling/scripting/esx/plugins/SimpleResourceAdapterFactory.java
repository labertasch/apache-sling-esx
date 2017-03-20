/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.sling.scripting.esx.plugins;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;

@Component
@Service
public class SimpleResourceAdapterFactory implements AdapterFactory {

    @Property(name = "adapters")
    public static final String[] ADAPTER_CLASSES = {
        SimpleResource.class.getName()
    };
    @Property(name = "adaptables")
    public static final String[] ADAPTABLE_CLASSES = {
        Resource.class.getName()
    };

    @Override
    public <AdapterType> AdapterType getAdapter(Object adaptableObject, Class<AdapterType> type) {
        if (type.equals(SimpleResource.class)
                && adaptableObject instanceof Resource) {

            SimpleResource simpeResource = new SimpleResource((Resource) adaptableObject);

            return (AdapterType) simpeResource;
        } else {
            return null;
        }
    }

}
