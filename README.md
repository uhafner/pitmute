# PitMute

[![CI on all platforms](https://github.com/uhafner/pitmute/workflows/GitHub%20CI/badge.svg)](https://github.com/uhafner/pitmute/actions/workflows/ci.yml)
[![CodeQL](https://github.com/uhafner/pitmute/workflows/CodeQL/badge.svg)](https://github.com/uhafner/pitmute/actions/workflows/codeql.yml)
[![Line Coverage](https://raw.githubusercontent.com/uhafner/pitmute/main/badges/line-coverage.svg)](https://github.com/uhafner/pitmute/actions/workflows/quality-monitor-comment.yml)
[![Branch Coverage](https://raw.githubusercontent.com/uhafner/pitmute/main/badges/branch-coverage.svg)](https://github.com/uhafner/pitmute/actions/workflows/quality-monitor-comment.yml)
[![Mutation Coverage](https://raw.githubusercontent.com/uhafner/pitmute/main/badges/mutation-coverage.svg)](https://github.com/uhafner/pitmute/actions/workflows/quality-monitor-comment.yml)

This PIT plugin provides several mutation filters to suppress PIT mutations. 

All source code is licensed under the MIT license. Contributions to this library are welcome! 

### Requirements
- Java 21+
- Maven
- Tests
- PIT Mutation Testing

### Installation

1. Add the plugin as a dependency inside the PIT plugin section in your `pom.xml`
    ```xml
    <dependency>
      <groupId>edu.hm.hafner</groupId>
      <artifactId>pitmute</artifactId>
      <version>1.0.0</version>
    </dependency>
    ```

2. Activate the features

    Enable one or both features as needed.<br><br>

    2.1 Filter Mutations by Annotation

    To enable this feature, add the following to your configuration:
    ```xml
    <configuration>
      <features>+FANNOT</features>
    </configuration>
    ```

    The `@SuppressMutation` annotation can be applied to methods or classes to suppress mutations within that scope. This feature also supports repeated annotations. 
    If no parameters are provided, `@SuppressMutation` suppresses all mutations in the scope. If multiple parameters are provided, **all conditions must be met** for a mutation to be ignored.<br><br>

    All annotation parameters are optional. If both `mutator` (enum) and `mutatorName` (string with mutator class name or fqcn) are provided, `mutatorName` is ignored.<br><br>

    2.2 Filter Mutations by CSV File

    To use the CSV filter, activate the feature and specify the path to your CSV file.
    ```xml
    <configuration>
      <features>+FCSV(csvFile[src/main/resources/exclusions.csv])</features>
    </configuration>
    ```

    Optionally, add `allowMissingFile[true]` to suppress errors when the CSV file is not found and receive an info log instead.
    ```xml
    <configuration>
      <features>+FCSV(csvFile[src/main/resources/exclusions.csv] allowMissingFile[true])</features>
    </configuration>
    ```
    Create a CSV file with the following format:
    
    ```
    className, Mutator, startLine, endLine
    ```
    
    Only `className` is required. All other fields may be left empty.
    
    If multiple fields are provided, **all conditions must be true** for a mutation to be ignored.


3. Install the plugin
    ```
    mvn install
    ```

### Annotation Details
| Element                | Description                                                                                                                                                                                                                   | Examples                                                                                                                                                                                       |
|:-----------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| mutator (optional)     | Suppresses mutations created by mutator matching this enum (recommended). <br>If both mutator and mutatorName are provided, mutatorName is ignored.                                                                           | @SuppressMutation(mutator = MATH)                                                                                                                                                               |
| mutatorName (optional) | Suppresses mutations created by mutator with this name. If mutator is set, this value is ignored.<br/>- Mutator class name shortened (without "Mutator")<br/>- Mutator class name<br/>- Fully qualified name of mutator class | <br/>@SuppressMutation(mutatorName = "Math")<br/>@SuppressMutation(mutatorName = "MathMutator")<br/>@SuppressMutation(mutatorName = "org.pitest.mutationtest.engine.gregor.mutators.MathMutator") |
| line (optional)        | Restricts suppression to a specific line number, if it is in scope.                                                                                                                                                           | @SuppressMutation(line = 5)                                                                                                                                                                     |


### Example Annotations

| Examples                                        | Description                                                                                           |
|:------------------------------------------------|:------------------------------------------------------------------------------------------------------|
| @SuppressMutation                               | ignore all mutations in the scope (class or method)                                                   |
| @SuppressMutation(mutator = MATH)               | ignore all MathMutator mutations in the scope                                                         |
| @SuppressMutation(mutator = MATH, line = 5)     | ignore all MathMutator mutations on line 5 (only if the line is within the annotated method or class) |
| @SuppressMutation(mutatorName = "Math")         | ignore all MathMutator mutations in the scope (fallback if enum is missing)                           |

### CSV Field Explanation
| Field                | Description                                                                                                                                                                                                                     | Examples                                                                                 |
|:---------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------|
| className (required) | Defines the class where mutations should be ignored.<br/>- Fully qualified name (recommended)<br>- File name<br/>- Class name <br/> ```Caution! If multiple classes share the same name, the filter applies to all of them!```  | <br>com.example.Main<br>Main.java<br>Main                                                |
| mutator (optional)   | Mutations created by this mutator are suppressed.<br/>- Fully qualified name<br/>-Class name<br/>-Mutator name shortened (without "Mutator")                                                                                    | <br/>org.pitest.mutationtest.engine.gregor.mutators.MathMutator<br/>MathMutator<br/>Math |
| startLine (optional) | Mutations at or after this line number are suppressed.                                                                                                                                                                          | number >= 1                                                                              |
| endLine (optional)   | Mutations at or before this line number are suppressed.                                                                                                                                                                         | number <= line numbers in file                                                           |

### Example CSV

| Examples                           | Description                                                                         |
|:-----------------------------------|:------------------------------------------------------------------------------------|
| com.example.Main,,,                | ignore all mutations in Main                                                        |
| com.example.Main, MathMutator,,    | ignore all MathMutator mutations in Main                                            |
| com.example.Main, MathMutator,5,   | ignore all MathMutator mutations in Main from line 5 onward                         |
| com.example.Main, MathMutator,5,10 | ignore all MathMutator mutations in Main between line 5 and 10 (including 5 and 10) |


### Minimal Example pom.xml

- PIT plugin
- Annotation filter feature
- CSV filter feature with path to CSV file (optionally, add `allowMissingFile[true]` to suppress errors when the CSV file is not found)
- Plugin dependency

```xml
<properties>
  <maven.compiler.source>21</maven.compiler.source>
  <maven.compiler.target>21</maven.compiler.target>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
<build>
  <plugins>
    <plugin>
      <groupId>org.pitest</groupId>
      <artifactId>pitest-maven</artifactId>
      <version>1.20.2</version>
      <configuration>
        <features>+FANNOT</features>
        <features>+FCSV(csvFile[src/main/resources/exclusions.csv])</features>
      </configuration>
      <dependencies>
        <dependency>
          <groupId>edu.hm.hafner</groupId>
          <artifactId>pitmute</artifactId>
          <version>1.0.0</version>
        </dependency>
      </dependencies>
    </plugin>
  </plugins>
</build>
```