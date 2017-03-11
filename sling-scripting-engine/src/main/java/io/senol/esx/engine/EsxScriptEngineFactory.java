/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.engine;

import io.senol.esx.module.ScriptModuleCache;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.scripting.api.AbstractScriptEngineFactory;
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

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ScriptEngine scriptEngine;
    private ScriptContext esxGlobalContext;
    private EsxScriptEngine sengine;
    private String babelPolyfill;
    private String babelSource;
    /**
     *
     */
    private final String BABEL_SOURCE_CODE = "/resources/libs/esx/modules/babel/babel.min.js";
    
    private final String SLING_BABEL_SOURCE_CODE = "/resources/libs/esx/sling-babel.js";

    /**
     *
     */
    private final String BABEL_POLYFILL = "/resources/libs/esx/modules/node_modules/babel-polyfill/dist/polyfill.js";

    @Reference
    ScriptModuleCache moduleCache;

    private Object babelOptions;
    private Object Babel;

    /**
     *
     */
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
        if (sengine == null) {
            log.info("sengine is null, loadin gnew one");
            this.sengine = new EsxScriptEngine(this);
            loadBabelScriptingEngine();
        }
        return this.sengine;
    }

    /**
     *
     * @return
     */
    public ScriptContext getScriptContext() {
        return this.esxGlobalContext;
    }

    /**
     *
     * @return
     */
    public ScriptEngine getGlobalEngine() {
        return this.scriptEngine;
    }

    /**
     *
     * @return
     */
    public ScriptModuleCache getModuleCache() {
        return this.moduleCache;
    }

    /**
     *
     * @return
     */
    public Object getBabelOptions() {
        return babelOptions;
    }

    /**
     *
     * @return
     */
    public Object getBabel() {
        return Babel;
    }

    /**
     *
     * @return
     */
    public String getBabelPolyfill() {
        return babelPolyfill;
    }

    public void clearEngine() {        
        this.sengine = null;
    }
    
    public void stop() {
        Thread.currentThread().stop();
    }
    /**
     *
     * @param context
     */
    private void loadBabelScriptingEngine() {
        try {
            log.info("Loading babel");
            // thread pool
            this.scriptEngine.eval(babelPolyfill);

            this.scriptEngine.eval(babelSource);
            
            
            

            Invocable inv = (Invocable) this.scriptEngine;
            
            String options = "{ \n" +
"    \"presets\": [\"es2015\"], \n" +
"    \"compact\": \"true\",\n" +
"    \"plugins\": [\"SlingSandbox\", [\"transform-react-jsx\", { \"pragma\" : \"SlingEsx.createElement\"}]] \n" +
//"     \"plugins\": [[\"transform-react-jsx\", { \"pragma\" : \"SlingEsx.createElement\"}]] \n" +
"    }";
            
            // "{ \"presets\": [ \"es2015\", \"react\"], "
                        //+ "\"compact\": \"true\", \"plugins\": [\"SlingSandbox\"]}"
            
            Object json = this.scriptEngine.eval("JSON");
            this.babelOptions = inv.invokeMethod(json, "parse",options);

            
            
            //["SlingSandbox", ["transform-react-jsx", { "pragma" : "SlingEsx.createElement"}]] 
            this.Babel = this.scriptEngine.
                    getBindings(ScriptContext.ENGINE_SCOPE).get("SlingBabel");
            
            log.info("Babael loaded");

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }    
    
    @Deactivate
    protected void deactivate() {
        log.info("deactiavted esx");

    }

    /**
     *
     * @param context
     */
    @Activate
    protected void activate(ComponentContext context) {

        log.info("activated a new osgi service esx");

         NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        //ScriptEngine engine = factory.getScriptEngine(new String[]{"-strict", "--no-java", "--no-syntax-extensions"});
        // create one script engine (change to threadpool)
        this.scriptEngine = factory.getScriptEngine();//new ScriptEngineManager().getEngineByName("nashorn");
        if(scriptEngine == null)  {
            log.error("could not load script engine?");
        }
        log.info("scritpengine != null == %s");
        
        try {
            this.babelPolyfill = new String(IOUtils.toByteArray(
                    context.getBundleContext().getBundle()
                            .getEntry(this.BABEL_POLYFILL).openStream()));

            this.babelSource = "//@sourceURL=" + getClass().getCanonicalName() + "\n load( { name : \"" + getClass().getCanonicalName() + "\", script: \""
                    + StringEscapeUtils.escapeEcmaScript(new String(IOUtils.toByteArray(
                            context.getBundleContext().getBundle()
                                    .getEntry(this.SLING_BABEL_SOURCE_CODE).openStream())))
                    + "\"} )";

            this.babelPolyfill = babelPolyfill;

            // create a new global context, merge with default context to hold
            // our global libraries
            this.esxGlobalContext = new SimpleScriptContext();

            esxGlobalContext.setBindings(scriptEngine.createBindings(),
                    ScriptContext.ENGINE_SCOPE);
            
            scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE).put("superGlobal", getClass().getCanonicalName());
            this.sengine = new EsxScriptEngine(this);

            loadBabelScriptingEngine();
                        
                    
        } catch (Exception e) {
            log.error("actiavet", e);
        }

    }
}
