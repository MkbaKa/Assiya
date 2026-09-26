package jdk.nashorn.api.scripting;

import javax.script.ScriptEngine;

/**
 * Compile-time stub of the JDK 8-14 built-in Nashorn factory.
 * Never shipped: consumed as compileOnly, the real class comes from the server JDK at runtime.
 */
public class NashornScriptEngineFactory {

    public ScriptEngine getScriptEngine(String[] args, ClassLoader loader) {
        throw new IllegalStateException("fake api");
    }

}
