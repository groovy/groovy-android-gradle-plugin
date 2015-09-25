package groovyx.grooid

import org.junit.Test

/**
 * Tests for GroovyAndroidPlugin
 */
class GroovyAndroidPluginTest {

    //index 0: runtimeJars
    //index 1: bootClasspath
    //index 2: androidBuilder.bootClasspath
    //index 3: androidBuilder.getBootClasspath(boolean)

    @Test
    void testRuntimeJarsFromVersion0() {
        assert 0 == getRuntimeJarsIndex('0.9.0')
        assert 0 == getRuntimeJarsIndex('0.9.1')
        assert 0 == getRuntimeJarsIndex('0.9.2')

        //it seems that in version 0.10.0, runtime jars changed from runtimeJars to bootClasspath
        assert 1 == getRuntimeJarsIndex('0.10.0')
        assert 1 == getRuntimeJarsIndex('0.10.1')
        assert 1 == getRuntimeJarsIndex('0.10.2')
        assert 1 == getRuntimeJarsIndex('0.10.4')
        assert 1 == getRuntimeJarsIndex('0.11.0')
        assert 1 == getRuntimeJarsIndex('0.11.1')
        assert 1 == getRuntimeJarsIndex('0.11.2')
        assert 1 == getRuntimeJarsIndex('0.12.0')
        assert 1 == getRuntimeJarsIndex('0.12.1')
        assert 1 == getRuntimeJarsIndex('0.12.2')
        assert 1 == getRuntimeJarsIndex('0.13.0')
        assert 1 == getRuntimeJarsIndex('0.13.1')
        assert 1 == getRuntimeJarsIndex('0.13.2')
        assert 1 == getRuntimeJarsIndex('0.13.3')
        assert 1 == getRuntimeJarsIndex('0.14.0')
        assert 1 == getRuntimeJarsIndex('0.14.1')
        assert 1 == getRuntimeJarsIndex('0.14.2')
        assert 1 == getRuntimeJarsIndex('0.14.3')
        assert 1 == getRuntimeJarsIndex('0.14.4')
    }

    @Test
    void testRuntimeJarsFromVersion1_0() {
        assert 1 == getRuntimeJarsIndex('1.0.0')
        assert 1 == getRuntimeJarsIndex('1.0.0-rc1')
        assert 1 == getRuntimeJarsIndex('1.0.0-rc2')
        assert 1 == getRuntimeJarsIndex('1.0.0-rc3')
        assert 1 == getRuntimeJarsIndex('1.0.0-rc4')
        assert 1 == getRuntimeJarsIndex('1.0.1')
    }

    @Test
    void testRuntimeJarsFromVersion1_1() {
        //In version 1.1, runtime jars changed from bootClasspath to androidBuilder.bootClasspath
        assert 2 == getRuntimeJarsIndex('1.1.0')
        assert 2 == getRuntimeJarsIndex('1.1.0-rc1')
        assert 2 == getRuntimeJarsIndex('1.1.0-rc2')
        assert 2 == getRuntimeJarsIndex('1.1.0-rc3')
        assert 2 == getRuntimeJarsIndex('1.1.1')
        assert 2 == getRuntimeJarsIndex('1.1.2')
        assert 2 == getRuntimeJarsIndex('1.1.3')
    }

    @Test
    void testRuntimeJarsFromVersion1_2() {
        assert 2 == getRuntimeJarsIndex('1.2.0')
        assert 2 == getRuntimeJarsIndex('1.2.0-beta1')
        assert 2 == getRuntimeJarsIndex('1.2.0-beta2')
        assert 2 == getRuntimeJarsIndex('1.2.0-beta3')
        assert 2 == getRuntimeJarsIndex('1.2.0-beta4')
        assert 2 == getRuntimeJarsIndex('1.2.0-rc1')
        assert 2 == getRuntimeJarsIndex('1.2.1')
        assert 2 == getRuntimeJarsIndex('1.2.2')
        assert 2 == getRuntimeJarsIndex('1.2.3')
    }

    @Test
    void testRuntimeJarsFromVersion1_3() {
        assert 2 == getRuntimeJarsIndex('1.3.0')
        assert 2 == getRuntimeJarsIndex('1.3.0-beta1')
        assert 2 == getRuntimeJarsIndex('1.3.0-beta2')
        assert 2 == getRuntimeJarsIndex('1.3.0-beta3')
        assert 2 == getRuntimeJarsIndex('1.3.0-beta4')
        assert 2 == getRuntimeJarsIndex('1.3.1')
    }

    @Test
    void testRuntimeJarsFromVersion1_4() {
        assert 2 == getRuntimeJarsIndex('1.4.0-beta1')

        //In version 1.4.0-beta2, runtime jars changed from androidBuilder.getBootClasspath()
        //to androidBuilder.getBootClasspath(boolean)
        //The boolean parameter seems to indicate the inclusion of legacy jars (HTTP)
        assert 3 == getRuntimeJarsIndex('1.4.0-beta2')
        assert 3 == getRuntimeJarsIndex('1.4.0-beta3')
    }

    @Test
    void testRuntimeJarsFromExpectedVersions() {
        assert 3 == getRuntimeJarsIndex('1.5.0')
        assert 3 == getRuntimeJarsIndex('1.5.0-beta1')
        assert 3 == getRuntimeJarsIndex('1.5.0-rc1')
    }

    private static int getRuntimeJarsIndex(String version) {
        new TestableGroovyAndroidPlugin(version).runtimeJarsIndex
    }
}

