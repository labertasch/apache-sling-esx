/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.module;

import io.senol.esx.engine.ScriptMonitor;
import java.time.Instant;

/**
 *
 * @author stas
 */
public class EsxScriptMonitor implements ScriptMonitor {
    private Instant start;
    private Thread scriptThread;
    
    public EsxScriptMonitor(Thread thread) {
        this.start = Instant.now();
        this.scriptThread = thread;
    }
    
    @Override
    public void terminate() {       
      //  this.scriptThread.stop();
    }

    @Override
    public Instant getStartTime() {
        return this.start;
    }

    @Override
    public Thread getThread() {
        return this.scriptThread;
    }
    
    
}
