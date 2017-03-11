/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.engine;

import java.time.Instant;

/**
 *
 * @author stas
 */
public interface ScriptMonitor {
    public void terminate();
    public Instant getStartTime();
    public Thread getThread();
}
