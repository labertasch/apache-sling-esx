/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.module.impl;

import io.senol.esx.module.ScriptModuleCache;
import java.util.HashMap;
import java.util.Map;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.api.resource.Resource;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service(value = {EventHandler.class, ScriptModuleCache.class})
/*@Property(name = org.osgi.service.event.EventConstants.EVENT_TOPIC,
        value = {SlingConstants.TOPIC_RESOURCE_CHANGED, SlingConstants.TOPIC_RESOURCE_REMOVED})*/
@Property(name=org.osgi.service.event.EventConstants.EVENT_TOPIC,
            value = {SlingConstants.TOPIC_RESOURCE_CHANGED, SlingConstants.TOPIC_RESOURCE_REMOVED})
public class RepositoryModuleCache implements ScriptModuleCache {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Map<String, ScriptObjectMirror> cache = new HashMap<String, ScriptObjectMirror>();

    @Override
    public void put(String module, ScriptObjectMirror script) {
        log.debug("putting module into cache" + module);
        cache.put(module, script);
    }

    @Override
    public ScriptObjectMirror get(String module) {
        return cache.get(module);
    }

    @Override
    public ScriptObjectMirror get(Resource resource) {
        return cache.get(resource.getPath());
    }

    @Override
    public boolean flush(String module) {
        Object res = cache.remove(module);
        if(res != null) {
            log.info(module + " flushed from cache");
        }
        return (true);
    }

    @Override
    public void handleEvent(Event event) {
        log.info("handle eevent esx");
        final String eventPath = (String) event.getProperty(SlingConstants.PROPERTY_PATH);
        log.info(eventPath);
        if (cache.remove(eventPath) != null) {
            log.info(eventPath + " was removed from cache");
        }

    }

   
}
