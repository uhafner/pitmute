# PIT suppressions plugin

[![CI on all platforms](https://github.com/uhafner/pit-suppressions-plugin/workflows/GitHub%20CI/badge.svg)](https://github.com/uhafner/pit-suppressions-plugin/actions/workflows/ci.yml)
[![CodeQL](https://github.com/uhafner/pit-suppressions-plugin/workflows/CodeQL/badge.svg)](https://github.com/uhafner/pit-suppressions-plugin/actions/workflows/codeql.yml)
[![Line Coverage](https://raw.githubusercontent.com/uhafner/pit-suppressions-plugin/main/badges/line-coverage.svg)](https://github.com/uhafner/pit-suppressions-plugin/actions/workflows/quality-monitor-comment.yml)
[![Branch Coverage](https://raw.githubusercontent.com/uhafner/pit-suppressions-plugin/main/badges/branch-coverage.svg)](https://github.com/uhafner/pit-suppressions-plugin/actions/workflows/quality-monitor-comment.yml)
[![Mutation Coverage](https://raw.githubusercontent.com/uhafner/pit-suppressions-plugin/main/badges/mutation-coverage.svg)](https://github.com/uhafner/pit-suppressions-plugin/actions/workflows/quality-monitor-comment.yml)

This PIT plugin provides several mutation filters to suppress PIT mutations. 

All source code is licensed under the MIT license. Contributions to this library are welcome! 

### Requirements
- Java 21+
- Maven
- Tests
- PIT Mutation Testing

### Installation

1. Install the jar file
```
mvn install:install-file -Dfile=PATH_TO_JAR\pit-suppressions-plugin-0.1.0.jar -DgroupId=edu.hm.hafner -DartifactId=pit-suppressions-plugin -Dversion=0.1.0 -Dpackaging=jar
```

2. Add the plugin as a dependency inside the PIT plugin section in your `pom.xml`

```xml

<dependency>
    <groupId>edu.hm.hafner</groupId>
    <artifactId>pit-suppressions-plugin</artifactId>
    <version>0.1.0</version>
</dependency>
```

3. To use the CSV filter activate the filter feature and specify the path to your CSV file

```xml

<configuration>
    <features>+FCSV(csvFile[src/main/resources/exclusions.csv])</features>
</configuration>
```

4. Create a CSV file with the following format

```
className, Mutator, startLine, endLine
```

Only `className` is required. All other fields may be left empty.

If multiple fields are provided, **all conditions must be true** for a mutation to be ignored.

### CSV Field Explanation
| Field                | Description                                                                                                                                                                                                                      | Examples                                                                                 |
|:---------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------|
| className (required) | Defines the class where mutations should be ignored.<br/>- Fully qualified name (recommended)<br>- File name<br/>- Class name <br/> ```Caution! If multiple classes share the same name, the filter applies to _all of them!_``` | <br>com.example.Main<br>Main.java<br>Main                                                |
| mutator (optional)   | Mutations created by this mutator are suppressed.<br/>- Fully qualified name<br/>-Class name<br/>-Mutator name shortened (without "Mutator")                                                                                     | <br/>org.pitest.mutationtest.engine.gregor.mutators.MathMutator<br/>MathMutator<br/>Math |
| startLine (optional) | Mutations at or after this line number are suppressed.                                                                                                                                                                           | number >= 1                                                                              |
| endLine (optional)   | Mutations at or before this line number are suppressed.                                                                                                                                                                          | number <= line numbers in file                                                           |

### Example CSV

| Examples                           | Description                                                                         |
|:-----------------------------------|:------------------------------------------------------------------------------------|
| com.example.Main,,,                | ignore all mutations in Main                                                        |
| com.example.Main, MathMutator,,    | ignore all MathMutator mutations in Main                                            |
| com.example.Main, MathMutator,5,   | ignore all MathMutator mutations in Main from line 5 onward                         |
| com.example.Main, MathMutator,5,10 | ignore all MathMutator mutations in Main between line 5 and 10 (including 5 and 10) |


### Minimal Example pom.xml

- PIT plugin
- Feature with path to CSV file, to use CSV filter
- Plugin dependency
- Test framework (JUnit in this example)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>your.groupId</groupId>
    <artifactId>your-artifactId</artifactId>
    <version>1.0.0</version>
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.pitest</groupId>
                <artifactId>pitest-maven</artifactId>
                <version>1.20.2</version>
                <configuration>
                    <features>+FCSV(csvFile[src/main/resources/exclusions.csv])</features>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.pitest</groupId>
                        <artifactId>pitest-junit5-plugin</artifactId>
                        <version>1.2.2</version>
                    </dependency>
                    <dependency>
                        <groupId>edu.hm.hafner</groupId>
                        <artifactId>pit-suppressions-plugin</artifactId>//todo
                        <version>0.1.0</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```