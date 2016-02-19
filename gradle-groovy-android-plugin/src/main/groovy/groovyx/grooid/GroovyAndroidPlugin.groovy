/*
 * Copyright 2016 the original author or authors.
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

package groovyx.grooid

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.internal.VariantManager
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.internal.variant.BaseVariantOutputData
import com.android.builder.model.SourceProvider
import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.HasConvention
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.tasks.DefaultGroovySourceSet
import org.gradle.api.tasks.GroovySourceSet
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.compile.GroovyCompile

/**
 * Adds support for building Android applications using the Groovy language.
 */
class GroovyAndroidPlugin implements Plugin<Project> {

  public static final String ANDROID_GROOVY_EXTENSION_NAME = 'androidGroovy'

  private Project project

  @CompileStatic
  void apply(Project project) {
    this.project = project

    BasePlugin basePlugin = getAndroidBasePlugin(project)
    if (!basePlugin) {
        throw new GradleException('You must apply the Android plugin or the Android library plugin before using the groovy-android plugin')
    }

    project.extensions.create(ANDROID_GROOVY_EXTENSION_NAME, GroovyAndroidPluginExtension)

    androidExtension.sourceSets.all { AndroidSourceSet sourceSet ->
      if (sourceSet instanceof HasConvention) {
        def sourceSetName = sourceSet.name

        def sourceSetPath = project.file("src/$sourceSetName/groovy")

        // add so Android Studio will recognize groovy files can see these
        sourceSet.java.srcDirs.add(sourceSetPath)

        // create groovy source set so we can access it later
        def groovySourceSet = new DefaultGroovySourceSet(sourceSetName, fileResolver)
        sourceSet.convention.plugins.put('groovy', groovySourceSet)
        def groovyDirSet = groovySourceSet.groovy
        groovyDirSet.srcDir(sourceSetPath)

        project.logger.debug("Created groovy sourceDirectorySet at ${groovyDirSet.srcDirs}")
      }
    }

    project.afterEvaluate { Project afterProject ->
      def androidPlugin = getAndroidBasePlugin(afterProject)
      def variantManager = getVariantManager(androidPlugin)

      processVariantData(variantManager.variantDataList, androidExtension, androidPlugin)
    }
  }

  private void processVariantData(
      List<BaseVariantData<? extends BaseVariantOutputData>> variantDataList,
      BaseExtension androidExtension, BasePlugin androidPlugin) {

    variantDataList.each { variantData ->
      def variantDataName = variantData.name
      project.logger.debug("Process variant [$variantDataName]")

      def javaTask = getJavaTask(variantData)
      if (javaTask == null) {
        project.logger.info("GROOVY: javaTask is missing for $variantDataName, so Groovy files won't be compiled for it")
        return
      }

      def taskName = javaTask.name.replace('Java', 'Groovy')
      def groovyTask = project.tasks.create(taskName, GroovyCompile)

      // do before configuration so users can override
      groovyTask.targetCompatibility = javaTask.targetCompatibility
      groovyTask.sourceCompatibility = javaTask.sourceCompatibility
      project.extensions.getByName(ANDROID_GROOVY_EXTENSION_NAME).configure(groovyTask)

      groovyTask.destinationDir = javaTask.destinationDir
      groovyTask.description = "Compiles the $variantDataName in groovy."
      groovyTask.classpath = javaTask.classpath
      groovyTask.setDependsOn(javaTask.dependsOn)
      groovyTask.groovyClasspath = javaTask.classpath

      def providers = variantData.variantConfiguration.sortedSourceProviders
      providers.each { SourceProvider provider ->
        def javaSrcDirs = (provider as AndroidSourceSet).java.srcDirs
        def groovySourceSet = (provider as HasConvention).convention.plugins['groovy'] as GroovySourceSet
        def groovySourceDirectorySet = groovySourceSet.groovy
        groovyTask.source(groovySourceDirectorySet)

        groovySourceDirectorySet.srcDirs(*(javaSrcDirs as List))
      }


      def additionalSourceFiles = getGeneratedSourceDirs(variantData)
      groovyTask.source(*additionalSourceFiles)

      groovyTask.doFirst { GroovyCompile task ->
        def androidRunTime = project.files(getRuntimeJars(androidPlugin, androidExtension))
        task.classpath = androidRunTime + javaTask.classpath
        task.groovyClasspath = task.classpath
      }

      javaTask.finalizedBy(groovyTask)
    }
  }

  @CompileStatic
  private static BasePlugin getAndroidBasePlugin(Project project) {
    def plugin = project.plugins.findPlugin('android') ?:
        project.plugins.findPlugin('android-library')

    return plugin as BasePlugin
  }

  @CompileStatic
  private BaseExtension getAndroidExtension() {
    return project.extensions.getByName('android') as BaseExtension
  }

  private static getRuntimeJars(BasePlugin plugin, BaseExtension extension) {
    if (plugin.metaClass.getMetaMethod('getRuntimeJarList')) {
      return plugin.runtimeJarList
    } else if (extension.metaClass.getMetaMethod('getBootClasspath')) {
      return extension.bootClasspath
    }

    return plugin.bootClasspath
  }

  private static SourceTask getJavaTask(BaseVariantData baseVariantData) {
    if (baseVariantData.metaClass.getMetaProperty('javaCompileTask')) {
      return baseVariantData.javaCompileTask
    } else if (baseVariantData.metaClass.getMetaProperty('javaCompilerTask')) {
      return baseVariantData.javaCompilerTask
    }
    return null
  }

  @CompileStatic
  private static List<File> getGeneratedSourceDirs(BaseVariantData variantData) {
    def getJavaSourcesMethod = variantData.metaClass.getMetaMethod('getJavaSources')
    if (getJavaSourcesMethod.returnType.metaClass == objectArrayMetaClass) {
      return variantData.javaSources.findAll { it instanceof File } as List<File>
    }

    List<File> result = []

    if (variantData.scope.generateRClassTask != null) {
      result << variantData.scope.RClassSourceOutputDir
    }

    if (variantData.scope.generateBuildConfigTask != null) {
      result << variantData.scope.buildConfigSourceOutputDir
    }

    if (variantData.scope.getAidlCompileTask() != null) {
      result << variantData.scope.aidlSourceOutputDir
    }

    if (variantData.scope.globalScope.extension.dataBinding.enabled) {
      result << variantData.scope.classOutputForDataBinding
    }

    if (!variantData.variantConfiguration.renderscriptNdkModeEnabled
        && variantData.scope.renderscriptCompileTask != null) {
      result << variantData.scope.renderscriptSourceOutputDir
    }

    return result
  }

  private FileResolver getFileResolver() {
    return project.fileResolver
  }

  private static VariantManager getVariantManager(BasePlugin plugin) {
    return plugin.variantManager
  }

  private static MetaClass getObjectArrayMetaClass() {
    return Object[].metaClass
  }
}
