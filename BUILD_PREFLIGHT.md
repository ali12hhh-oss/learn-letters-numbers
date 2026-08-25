# Build preflight

Gradle: 8.13
AGP: 8.11.1
Kotlin: 2.2.20
Java: 17

Present:
- gradlew
- gradlew.bat
- gradle-wrapper.properties
- GitHub Actions workflow
- .gitignore

gradle-wrapper.jar: not available in this environment; must be generated/committed with Gradle 8.13 before wrapper execution

Expected GitHub commands:
`./gradlew --version`
`./gradlew lintDebug`
`./gradlew assembleDebug`
`./gradlew bundleRelease`
