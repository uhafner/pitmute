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
    <version>0.1.0</version>
</dependency>
```

2. To use the CSV filter activate the filter feature and specify the path to your CSV file

```xml
<configuration>
    <features>+FCSV(csvFile[src/main/resources/exclusions.csv])</features>
</configuration>
```

3. Create a CSV file with the following format

```
className, Mutator, startLine, endLine
```

Only `className` is required. All other fields may be left empty.

If multiple fields are provided, **all conditions must be true** for a mutation to be ignored.

4. Install the plugin
```
mvn install
```

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
        <features>+FCSV(csvFile[src/main/resources/exclusions.csv])</features>
      </configuration>
      <dependencies>
        <dependency>
          <groupId>edu.hm.hafner</groupId>
          <artifactId>pitmute</artifactId>
          <version>0.1.0</version>
        </dependency>
      </dependencies>
    </plugin>
  </plugins>
</build>
```