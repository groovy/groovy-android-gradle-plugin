package me.champeau.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.internal.tasks.DefaultGroovySourceSet
import org.gradle.api.internal.tasks.DefaultSourceSet
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile

import javax.inject.Inject


/**
 * Adds support for building Android applications using the Groovy language.
 */
class GroovyAndroidPlugin implements Plugin<Project> {

    private static String LATEST_SUPPORTED="0.13.0"

    private static List RUNTIMEJARS_COMPAT = [
            { it.runtimeJars },
            { it.bootClasspath }
    ]

    void apply(Project project) {
        project.extensions.create("androidGroovy", GroovyAndroidPluginExtension)

        def plugin = project.plugins.findPlugin('android')?:project.plugins.findPlugin('android-library')
        if (!plugin) {
            throw new GradleException('You must apply the Android plugin or the Android library plugin before using the groovy-android plugin')
        }

        def groovyPlugin = this

        project.android {

            packagingOptions {
                // workaround for http://stackoverflow.com/questions/20673625/android-gradle-plugin-0-7-0-duplicate-files-during-packaging-of-apk
                exclude 'META-INF/LICENSE.txt'
                exclude 'META-INF/groovy-release-info.properties'
            }

            // Forces Android Studio to recognize groovy folder as code
            sourceSets {
              main.java.srcDir('src/main/groovy')
              androidTest.java.srcDir('src/androidTest/groovy')
            }

            def variants = plugin.class.name.endsWith('.LibraryPlugin')?libraryVariants:applicationVariants

            variants.all {
                 groovyPlugin.attachGroovyCompileTask(project, plugin, javaCompile, 'src/main')
                 if (testVariant) {
                     groovyPlugin.attachGroovyCompileTask(project, plugin, testVariant.javaCompile, 'src/androidTest')
                 }
            }
        }
        project.logger.info("Detected Android plugin version ${getAndroidPluginVersion(project)}")

    }

     private void attachGroovyCompileTask(Project project, Plugin plugin, JavaCompile javaCompile, String srcDir) {
         def groovyPlugin = this
         def taskName = javaCompile.name.replace("Java", "Groovy")
         def groovyCompile = project.task(taskName, type: GroovyCompile) {
             project.androidGroovy.configure(it)
             source = project.fileTree(new File(srcDir, 'groovy'))
             destinationDir = javaCompile.destinationDir
             classpath = javaCompile.classpath
             groovyClasspath = classpath
             doFirst {
                 def runtimeJars = groovyPlugin.getRuntimeJars(project, plugin)
                 classpath = project.files(runtimeJars) + classpath
             }
         }

         javaCompile.finalizedBy(groovyCompile)
         javaCompile.exclude { e ->
             // this is a dirty hack to work around the fact that we need to declare the source tree as Java sources
             // for this to be recognized by Android Studio, so here we just exclude the files !
             e.file.absolutePath.contains('src/main/groovy') || e.file.absolutePath.contains('src/androidTest/groovy')
         }
     }

    private String getAndroidPluginVersion(Project project) {

        def dependency = [project,project.rootProject].collect {
            it.buildscript.configurations.classpath.resolvedConfiguration.firstLevelModuleDependencies.find {
                it.moduleGroup == 'com.android.tools.build' && it.moduleName == 'gradle'
            }
        }.find()

        if (dependency) {
            return dependency.moduleVersion
        }
        project.logger.warn("Unable to determine Android build tools version from classpath. Falling back to default ($LATEST_SUPPORTED).")

        LATEST_SUPPORTED
    }

    def getRuntimeJars(Project project, plugin) {
        int index
        switch (getAndroidPluginVersion(project)) {
            case ~/0\.9\..*/:
                index = 0
                break
            case ~/0\.10\..*/:
                index = 1
                break
            case ~/0\.11\..*/:
                index = 1
                break
            case ~/0\.12\..*/:
                index = 1
                break
            case ~/0\.13\..*/:
                index = 1
                break
            default:
                index = RUNTIMEJARS_COMPAT.size()-1
        }
        def fun = RUNTIMEJARS_COMPAT[index]
        fun(plugin)
    }
}
