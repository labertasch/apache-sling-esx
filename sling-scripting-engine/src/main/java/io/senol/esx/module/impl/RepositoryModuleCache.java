/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.module.impl;

import io.senol.esx.engine.ScriptMonitor;
import io.senol.esx.module.ScriptModuleCache;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
@Property(name = org.osgi.service.event.EventConstants.EVENT_TOPIC,
        value = {SlingConstants.TOPIC_RESOURCE_CHANGED, SlingConstants.TOPIC_RESOURCE_REMOVED})
public class RepositoryModuleCache implements ScriptModuleCache {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Map<String, ScriptObjectMirror> cache = new HashMap<String, ScriptObjectMirror>();

    public ArrayList<ScriptMonitor> runningScripts = new ArrayList<ScriptMonitor>();

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
        if (res != null) {
            log.debug(module + " flushed from cache");
        }
        return (true);
    }

    @Override
    public void handleEvent(Event event) {
        final String eventPath = (String) event.getProperty(SlingConstants.PROPERTY_PATH);
        if (cache.remove(eventPath) != null) {
            log.info(eventPath + " was removed from cache");
        }

    }


    @Override
    public void monitorScript(ScriptMonitor monitor) {
        runningScripts.add(monitor);
    }

    @Override
    public void stopMonitoringScript(ScriptMonitor monitor) {
        try {
            int position = runningScripts.indexOf(monitor);
            if (position != -1) {
                ScriptMonitor script = runningScripts.get(runningScripts.indexOf(monitor));
                Instant start = script.getStartTime();
                log.info("size: " + runningScripts.size());
                runningScripts.remove(monitor);
                script.getThread().stop();
                log.info("Script execution: " + Duration.between(start, Instant.now()).toMillis());
                log.info("size: " + runningScripts.size());
                                     
            } else {
                log.info("ScriptMonitor not found ");
            }

        } catch (IndexOutOfBoundsException e) {
            log.info("adsf", e);
        }

        runningScripts.remove(monitor);
    }

    @Override
    public ArrayList<ScriptMonitor> getRunningScripts() {
        return runningScripts;
    }

}
