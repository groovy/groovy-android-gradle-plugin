package me.champeau.gradle

import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.util.ConfigureUtil

/**
 * Configuration specific to the Groovy+Android plugin.
 */
class GroovyAndroidPluginExtension {
    private Closure<Void> configClosure
    void options(Closure<Void> config) {
        configClosure = config
    }

    void configure(GroovyCompile task) {
        if (configClosure) {
            ConfigureUtil.configure(configClosure, task)
        }
    }
}
