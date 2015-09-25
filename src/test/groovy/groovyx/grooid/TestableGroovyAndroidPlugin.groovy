package groovyx.grooid

import groovy.transform.Canonical
import org.gradle.api.Project

/**
 * Class that overrides the getAndroidPluginVersion(Project) to make it testable
 */
@Canonical
class TestableGroovyAndroidPlugin extends GroovyAndroidPlugin {
    String androidPluginVersion

    @Override
    String getAndroidPluginVersion(Project project) {
        androidPluginVersion
    }

    int getRuntimeJarsIndex() {
        getRuntimeJarsIndex(androidPluginVersion)
    }
}
