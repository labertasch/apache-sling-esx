/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.engine;

import io.senol.esx.module.EsxScriptMonitor;
import io.senol.esx.module.Module;
import io.senol.esx.module.ModuleScript;
import io.senol.esx.module.SlingSandbox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;
import java.util.logging.Level;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.scripting.api.AbstractSlingScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * ESX Script Engine Implmeentation
 *
 * @author stas
 */
public class EsxScriptEngine extends AbstractSlingScriptEngine {

    private final Logger log = LoggerFactory.getLogger(getClass());
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
        log.debug("returning buffer and buffer != null = %s",  (buffer != null));
        log.debug("buffer = %s", buffer.toString());
        return buffer.toString();
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        log.debug("starting to eval something");
        Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        SlingScriptHelper scriptHelper = (SlingScriptHelper) bindings.get("sling");

        Resource scriptResource = scriptHelper.getScript().getScriptResource();

        Resource resource = scriptHelper.getRequest().getResource();

        EsxScriptMonitor scriptMonitor = new EsxScriptMonitor(Thread.currentThread());

        // this.scriptEngineFactory.moduleCache.monitorScript(scriptMonitor);
        String mainscript = null;
        try {
            mainscript = readMainScript(reader);
        } catch (IOException ex) {
            log.error("could not read main script", ex);
        }

        ModuleScript moduleScript = new ModuleScript(ModuleScript.JS_FILE, scriptResource);
        SlingSandbox sandbox = new SlingSandbox();
        Module module = new Module(scriptEngineFactory, resource, moduleScript, scriptResource.getPath(),
                null, mainscript, sandbox);
        Object result = null;
         class CancelTimer extends TimerTask {

            @Override            
            public void run() {            
                sandbox.stop();
                cancel();
            }

        }
        Timer timer = new Timer();
        timer.schedule(new CancelTimer(), 10*1000*5);
                
        result = module.runMainScript();
        
        
        if (result != null) {
            log.debug("result ist not null");
            String renderResult = (String) result;// scriptEngine.eval("exports.render()", module).toString();
            try {
                context.getWriter().write(renderResult);
            } catch (IOException ex) {
                log.error("couldnt write result to output", ex);
            }

        } else {
            log.error("result is null");
        }
         
        //this.scriptEngineFactory.moduleCache.stopMonitoringScript(scriptMonitor);
        return null;
    }

}
