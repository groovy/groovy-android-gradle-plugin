package groovyx.grooid

import org.gradle.api.JavaVersion
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
        } else {
            task.sourceCompatibility = JavaVersion.VERSION_1_6
            task.targetCompatibility = JavaVersion.VERSION_1_6
        }
    }
}
