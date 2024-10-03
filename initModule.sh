#!/bin/bash
name=$1

capitalized_name="$(tr '[:lower:]' '[:upper:]' <<< "${name:0:1}")${name:1}"

# Create main package
mkdir -p "$name/src/main/java/$name"
touch "$name/src/main/java/$name/${capitalized_name}Application.java"

# Create resources folder
mkdir -p "$name"/src/main/resources
touch "$name"/src/main/resources/application.yaml

# Create test package
mkdir -p "$name/src/test/java/$name"
cat <<EOF > "$name/src/main/java/$name/${capitalized_name}Application.java"
package $name;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ${capitalized_name}Application {

    public static void main(String[] args) {
        SpringApplication.run(${capitalized_name}Application.class, args);
    }

}
EOF

mkdir -p "$name/src/test/resources"
touch "$name/src/test/resources/application.yaml"

# Create integrationTest package
mkdir -p "$name/src/integrationTest/java/$name"
mkdir -p "$name/src/integrationTest/resources"
touch "$name/src/integrationTest/resources/application.yaml"

# Create .gitignore and build.gradle files
touch "$name/.gitignore"
touch "$name/build.gradle"
cat <<EOF > "$name/build.gradle"
plugins {
    id 'buildlogic.spring-boot-conventions'
}

dependencies {

}

EOF

settings="settings.gradle"
include="include('$name')"
if ! grep -Fxq "$include" "$settings"; then
    echo "$include" >> "$settings"
fi