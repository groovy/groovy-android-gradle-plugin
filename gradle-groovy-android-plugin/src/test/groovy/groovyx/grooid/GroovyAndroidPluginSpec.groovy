package groovyx.grooid

import spock.lang.Specification
import spock.lang.Unroll

class GroovyAndroidPluginSpec extends Specification {

    @Unroll
    def "plugin version #version should return #index"() {
        expect:
        GroovyAndroidPlugin.getRuntimeJarsIndex(version) == index

        //index 0: runtimeJars
        //index 1: bootClasspath
        //index 2: androidBuilder.bootClasspath
        //index 3: androidBuilder.getBootClasspath(boolean)
        where:
        index | version
        0 | '0.9.0'
        0 | '0.9.1'
        0 | '0.9.2'
        1 | '0.10.0'
        1 | '0.10.1'
        1 | '0.10.2'
        1 | '0.10.4'
        1 | '0.11.0'
        1 | '0.11.1'
        1 | '0.11.2'
        1 | '0.12.0'
        1 | '0.12.1'
        1 | '0.12.2'
        1 | '0.13.0'
        1 | '0.13.1'
        1 | '0.13.2'
        1 | '0.13.3'
        1 | '0.14.0'
        1 | '0.14.1'
        1 | '0.14.2'
        1 | '0.14.3'
        1 | '0.14.4'
        1 | '1.0.0'
        1 | '1.0.0-rc1'
        1 | '1.0.0-rc2'
        1 | '1.0.0-rc3'
        1 | '1.0.0-rc4'
        1 | '1.0.1'
        //In version 1.1, runtime jars changed from bootClasspath to androidBuilder.bootClasspath
        2 | '1.1.0'
        2 | '1.1.0-rc1'
        2 | '1.1.0-rc2'
        2 | '1.1.0-rc3'
        2 | '1.1.1'
        2 | '1.1.2'
        2 | '1.1.3'
        2 | '1.2.0'
        2 | '1.2.0-beta1'
        2 | '1.2.0-beta2'
        2 | '1.2.0-beta3'
        2 | '1.2.0-beta4'
        2 | '1.2.0-rc1'
        2 | '1.2.1'
        2 | '1.2.2'
        2 | '1.2.3'
        2 | '1.3.0'
        2 | '1.3.0-beta1'
        2 | '1.3.0-beta2'
        2 | '1.3.0-beta3'
        2 | '1.3.0-beta4'
        2 | '1.3.1'
        2 | '1.4.0-beta1'
        //In version 1.4.0-beta2, runtime jars changed from androidBuilder.getBootClasspath()
        //to androidBuilder.getBootClasspath(boolean)
        //The boolean parameter seems to indicate the inclusion of legacy jars (HTTP)
        3 | '1.4.0-beta2'
        3 | '1.4.0-beta3'
        3 | '1.5.0'
        3 | '1.5.0-beta1'
        3 | '1.5.0-rc1'
    }
}

