/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.sling.scripting.esx;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.sling.scripting.api.AbstractScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

@Component(label = "ESX Scripting Engine Factory", description = "", metatype = true)
@Service
@Properties({
    @Property(name = Constants.SERVICE_VENDOR, value = "The Apache Software Foundation")
    ,
        @Property(name = Constants.SERVICE_DESCRIPTION, value = "Scripting Engine for Ecmas Script using Node JS like module loader")
    ,
        @Property(name = "compatible.javax.script.name", value = "esx")
})
public class EsxScriptEngineFactory extends AbstractScriptEngineFactory {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ScriptEngine nashornScriptEngine;
    
    @Reference
    ScriptModuleCache moduleCache;

    public EsxScriptEngineFactory() {
        setNames("esx", "ESX");
        setExtensions("esx", "ESX");
    }

    @Override
    public String getLanguageName() {
        return "ESX";
    }

    @Override
    public String getLanguageVersion() {
        return "1.0";
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new EsxScriptEngine(this);
    }

    /**
     * 
     * @return 
     */
    public ScriptModuleCache getModuleCache() {
        return moduleCache;        
    }
    
    /**
     *
     * @return
     */
    public ScriptEngine getNashornEngine() {
        return nashornScriptEngine;
    }

    @Deactivate
    private void deactivate() {
        log.debug("Deactivating Engine");
    }

    @Activate
    protected void activate(ComponentContext context) {
        log.debug("Starting Engine");
        // create one script engine
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        this.nashornScriptEngine = factory.getScriptEngine();
        log.debug("Engine started");
    }
}
