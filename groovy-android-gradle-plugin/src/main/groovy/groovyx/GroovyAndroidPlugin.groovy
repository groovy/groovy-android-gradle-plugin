/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovyx

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.SourceKind
import com.android.builder.model.SourceProvider
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.HasConvention
import org.gradle.api.tasks.GroovySourceSet
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject
/**
 * Adds support for building Android applications using the Groovy language.
 */
@Slf4j
class GroovyAndroidPlugin implements Plugin<Project> {

  public static final String ANDROID_GROOVY_EXTENSION_NAME = 'androidGroovy'

  private final Instantiator instantiator

  @Inject
  GroovyAndroidPlugin(Instantiator instantiator) {
    this.instantiator = instantiator
  }

  void apply(Project project) {
    project.extensions.create(ANDROID_GROOVY_EXTENSION_NAME, GroovyAndroidExtension, project, instantiator, project.fileResolver)
    handleProject(project)
  }

  static void handleProject(Project project) {
    def androidPlugin = ['android', 'com.android.application', 'android-library', 'com.android.library',
                         'com.android.test', 'com.android.feature']
      .collect { project.plugins.findPlugin(it) as BasePlugin }
      .find { it != null }

    log.debug('Found Plugin: {}', androidPlugin)

    if (androidPlugin == null) {
      throw new GradleException('You must apply the Android plugin or the Android library plugin before using the groovy-android plugin')
    }

    def extension = project.extensions.getByType(GroovyAndroidExtension)

    def androidExtension = project.extensions.getByName("android") as BaseExtension

    androidExtension.sourceSets.all { AndroidSourceSet sourceSet ->
      if (!(sourceSet instanceof HasConvention)) {
        return
      }

      def sourceSetName = sourceSet.name
      def sourceSetPath = project.file("src/$sourceSetName/groovy")

      if (!sourceSetPath.exists()) {
        log.debug('SourceSet path does not exists for {} {}', sourceSetName, sourceSetPath)
        return
      }

      // add so Android Studio will recognize groovy files can see these
      sourceSet.java.srcDir(sourceSetPath)

      // create groovy source set so we can access it later
      GroovySourceSet groovySourceSet = extension.sourceSetsContainer.maybeCreate(sourceSetName)
      sourceSet.convention.plugins['groovy'] = groovySourceSet
      def groovyDirSet = groovySourceSet.groovy
      groovyDirSet.srcDir(sourceSetPath)

      log.debug('Created groovy sourceDirectorySet at {}', groovyDirSet.srcDirs)
    }

    project.afterEvaluate {
      forEachVariant(project){ BaseVariant variant ->
        processVariant(variant, project, androidExtension, androidPlugin, extension)
      }
    }
  }

  private static void forEachVariant(Project project, Closure<BaseVariant> action) {
    def androidExtension = project.extensions.getByName("android")

    log.debug('Project has {} as an extension', androidExtension)

    if (androidExtension instanceof AppExtension) {
      androidExtension.applicationVariants.all(action)
    }

    if (androidExtension instanceof LibraryExtension) {
      androidExtension.libraryVariants.all(action)
      if (androidExtension instanceof FeatureExtension) {
        androidExtension.featureVariants.all(action)
      }
    }

    if (androidExtension instanceof TestExtension) {
      androidExtension.applicationVariants.all(action)
    }

    if (androidExtension instanceof TestedExtension) {
      androidExtension.testVariants.all(action)
      androidExtension.unitTestVariants.all(action)
    }
  }

  private static void processVariant(BaseVariant variantData, Project project, BaseExtension androidExtension, BasePlugin androidPlugin, GroovyAndroidExtension extension) {
    def variantName = getVariantName(variantData)
    log.debug('Processing variant {}', variantName)

    def javaTask = getJavaTask(variantData)
    if (javaTask == null) {
      log.info('javaTask it null for {}', variantName)
      return
    }

    def taskName = javaTask.name.replace('Java', 'Groovy')
    def groovyTask = project.tasks.create(taskName, GroovyCompile)

    // do before configuration so users can override / don't break backwards compatibility
    groovyTask.targetCompatibility = javaTask.targetCompatibility
    groovyTask.sourceCompatibility = javaTask.sourceCompatibility
    extension.configure(groovyTask)

    groovyTask.destinationDir = javaTask.destinationDir
    groovyTask.classpath = javaTask.classpath
    groovyTask.dependsOn = javaTask.dependsOn
    groovyTask.groovyClasspath = javaTask.classpath

    def providers = getSourceProviders(variantData)
    providers.each { SourceProvider provider ->
      def groovySourceSet = provider.convention.plugins['groovy'] as GroovySourceSet
      if (groovySourceSet == null) {
        return
      }

      def groovySourceDirectorySet = groovySourceSet.groovy
      groovyTask.source(groovySourceDirectorySet)

      // Exclude any java files that may be included in both java and groovy source sets
      javaTask.exclude { file ->
        file.file in groovySourceSet.groovy.files
      }

      if (extension.skipJavaC) {
        groovyTask.source(*(javaTask.source.files as List))
        javaTask.exclude {
          true
        }
      }
    }

    // no sources for groovy to compile skip the groovy task
    if (groovyTask.source.empty) {
      log.debug('no groovy sources found for {} removing groovy task', variantName)
      project.tasks.remove(groovyTask)
      return
    }
    log.debug('groovy sources for {}: {}', variantName, groovyTask.source.files)

    def additionalSourceFiles = getGeneratedSourceDirs(variantData)
    log.debug('additional source files found at {}', additionalSourceFiles)
    groovyTask.source(*additionalSourceFiles)

    groovyTask.doFirst { GroovyCompile task ->
      def androidRunTime = project.files(getRuntimeJars(androidPlugin, androidExtension))
      task.classpath = androidRunTime + javaTask.classpath
      task.groovyClasspath = task.classpath
      task.options.compilerArgs += getJavaTaskCompilerArgs(javaTask, extension.skipJavaC)
      task.groovyOptions.javaAnnotationProcessing = true
      task.options.annotationProcessorPath = javaTask.options.annotationProcessorPath
      log.debug('Java annotationProcessorPath {}', javaTask.options.annotationProcessorPath)
      log.debug('Groovy compiler args {}', task.options.compilerArgs)
    }

    log.debug('Groovy classpath: {}', groovyTask.classpath.files)

    javaTask.finalizedBy(groovyTask)
  }

  private static getRuntimeJars(BasePlugin plugin, BaseExtension extension) {
    if (plugin.metaClass.getMetaMethod('getRuntimeJarList')) {
      return plugin.runtimeJarList
    }

    if (extension.metaClass.getMetaMethod('getBootClasspath')) {
      return extension.bootClasspath
    }

    return plugin.bootClasspath
  }

  private static List<String> getJavaTaskCompilerArgs(JavaCompile javaTask, boolean skipJavaC) {
    def compilerArgs = javaTask.options.compilerArgs
    log.debug('javaTask.options.compilerArgs = {}', compilerArgs)

    if (skipJavaC) {
      // if we skip java c the java compiler will still look for the annotation processor directory
      // we should create it for it.
      compilerArgs.findAll { !it.startsWith('-') }
        .each { new File(it).mkdirs() }
    }

    return compilerArgs
  }


  private static String getVariantName(BaseVariant variant) {
    return variant.name
  }

  private static JavaCompile getJavaTask(BaseVariant variantData) {
    // just get actual javac we don't support jack.
    return variantData.getJavaCompile()
  }

  private static Iterable<SourceProvider> getSourceProviders(BaseVariant variantData) {
    return variantData.sourceSets
  }

  private static List<File> getGeneratedSourceDirs(BaseVariant variantData) {
    return variantData.getSourceFolders(SourceKind.JAVA).collect { it.dir }
  }
}
