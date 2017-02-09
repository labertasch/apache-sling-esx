/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.engine;

import io.senol.esx.module.Module;
import io.senol.esx.module.ModuleScript;
import io.senol.esx.module.ScriptModuleCache;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.scripting.api.AbstractScriptEngineFactory;
import org.apache.sling.scripting.api.AbstractSlingScriptEngine;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stas
 */
@Component(label = "ESX Scripting Engine Factory", description = "", metatype = true)
@Service
@Properties({
    @Property(name = "service.vendor", value = "Adobe Systems")
    ,
        @Property(name = "service.description", value = "ESX Script Engine Factory")
    ,
        @Property(name = "compatible.javax.script.name", value = "esx")
})
public class EsxScriptEngineFactory extends AbstractScriptEngineFactory {

    private final String BABEL_SOURCE_CODE = "/resources/libs/esx/modules/babel/babel.min.js";
    private final String BABEL_POLYFILL =    "/resources/libs/esx/modules/node_modules/babel-polyfill/dist/polyfill.js";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Date date;
    private ScriptEngine scriptEngine;
    private ScriptContext esxGlobalContext;
    private EsxScriptEngine sengine;
    private String babelPolyfill;

    /*@Reference
    ScriptResolverService resolverService;*/

    @Reference
    ScriptModuleCache moduleCache;

    public EsxScriptEngineFactory() {
        setNames("esx", "ESX");
        setExtensions("esx");
    }

    @Override
    public String getLanguageName() {
        return "esx";
    }

    @Override
    public String getLanguageVersion() {
        return "1.0";
    }

    @Override
    public ScriptEngine getScriptEngine() {        
        return this.sengine;
    }

    public void printDate() {
        log.info(this.date.toString());
    }

    public ScriptContext getScriptContext() {
        return this.esxGlobalContext;
    }

    public ScriptEngine getGlobalEngine() {
        return this.scriptEngine;
    }

/*    public ScriptResolverService getResolverService() {
        return resolverService;
    }*/

    public ScriptModuleCache getModuleCache() {
        return this.moduleCache;
    }

    private Object babelOptions;
    private Object Babel;

    public Object getBabelOptions() {
        return babelOptions;
    }

    public Object getBabel() {
        return Babel;
    }

    public String getBabelPolyfill() {
        return babelPolyfill;
    }
    @Activate
    protected void activate(ComponentContext context) {
        log.info("activated a new osgi service esx");

        // create one script engine
        this.scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
        
        // create a new global context, merge with default context to hold
        // our global libraries
        this.esxGlobalContext = new SimpleScriptContext();

        esxGlobalContext.setBindings(scriptEngine.createBindings(), ScriptContext.ENGINE_SCOPE);
        
        this.sengine = new EsxScriptEngine(this);

        
        try {
            log.info("Loading babel");
            String babelPolyfill = new String(IOUtils.toByteArray(
                    context.getBundleContext().getBundle()
                            .getEntry(this.BABEL_POLYFILL).openStream()));
            
            this.babelPolyfill = babelPolyfill;
            this.scriptEngine.eval(babelPolyfill);

            String babel = new String(IOUtils.toByteArray(
                    context.getBundleContext().getBundle()
                            .getEntry(this.BABEL_SOURCE_CODE).openStream()));
            this.scriptEngine.eval(babel);
            
            
            Invocable inv = (Invocable) this.scriptEngine;
            Object json = this.scriptEngine.eval("JSON");
            this.babelOptions = inv.invokeMethod(json, "parse", "{ \"presets\": [ \"es2015\", \"react\"],   \"compact\": \"true\" }");
            log.debug("babel optinos == null == " + (this.babelOptions == null));
            this.Babel = this.scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).get("Babel");
            log.debug("babel library == null ==  " + (this.Babel == null));
            log.info("babel loaded into script context");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }   
    }

    class EsxScriptEngine extends AbstractSlingScriptEngine {

        private EsxScriptEngineFactory scriptEngineFactory;

        public EsxScriptEngine(EsxScriptEngineFactory scriptEngineFactory) {
            super(scriptEngineFactory);
            this.scriptEngineFactory = scriptEngineFactory;
        }

        private String readMainScript(Reader reader) throws IOException {
            log.debug("read main script:");
            BufferedReader esxScript = new BufferedReader(reader);
            StringBuilder buffer = new StringBuilder();
            String temp;
            while ((temp = esxScript.readLine()) != null) {
                buffer.append(temp).append("\r\n");
            }
            log.debug("returning buffer and buffer != null = " + (buffer != null));
            log.debug("buffer = " + buffer.toString());
            return buffer.toString();
        }

        @Override
        public Object eval(Reader reader, ScriptContext context) throws ScriptException {
            Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
            SlingScriptHelper scriptHelper = (SlingScriptHelper) bindings.get("sling");

            Resource scriptResource = scriptHelper.getScript().getScriptResource();
            
            Resource resource = scriptHelper.getRequest().getResource();
            String mainscript = null;
            try {
                mainscript = readMainScript(reader);
            } catch (IOException ex) {
                log.error("could not read main script", ex);
            }
            
            ModuleScript moduleScript = new ModuleScript(ModuleScript.JS_FILE, scriptResource);
            
            Module module = new Module(scriptEngineFactory, resource, moduleScript, scriptResource.getPath(),
                    null, mainscript);

            Object result = module.runMainScript();

            if (result != null) {
                                                
                String renderResult = (String) result;// scriptEngine.eval("exports.render()", module).toString();

                try {
                    context.getWriter().write(renderResult);
                } catch (IOException ex) {
                    log.error("couldnt write result to output", ex);
                }

            } else {
                log.error("result is null");
            }

            return null;
        }     

    }
}