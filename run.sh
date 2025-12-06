#!/bin/bash

# Set JavaFX environment variables for Java 21 and JavaFX 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Compile and run
echo "Compiling..."
mvn clean compile dependency:copy-dependencies

echo "Running application..."
java --enable-native-access=javafx.graphics --module-path target/dependency --add-modules javafx.controls,javafx.fxml -cp target/classes:target/dependency/* app.Main