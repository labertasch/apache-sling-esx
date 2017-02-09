/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stas
 */
public class ConsoleLog {
        Logger log;
        
    public ConsoleLog(String scriptId) {
        this.log = LoggerFactory.getLogger(scriptId);        
    }
    public void log(String msg) {
        log.info(msg);
    }
    public void info(String msg) {
        log.info(msg);
    }
    
    public void warn(String msg) {
        log.warn(msg);
    }
    
    public void error(String msg) {
        log.error(msg);
    }
    public Logger getLogger() {
        return log;
    }
}
