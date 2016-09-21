# How to Release

1) Prepare for release `./gradlew prepareRelease`
1) Update Version Number (remove -SNAPSHOT)
1) Run all tests `./gradlew clean check fullTest`
1) Run release task `./gradlew release`
1) Update Version Number in build.gradle and README.adoc



