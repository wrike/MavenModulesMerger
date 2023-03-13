## Description

Maven module merger allows you to merge Maven modules into one big module.
For more information, please check out the generated JavaDoc.

## How to open JavaDoc

You can generate JavaDoc with Maven.

```shell
mvn -P GenerateJavaDoc clean javadoc:javadoc
```

After that, you can open the generated JavaDoc.

```shell
open target/site/apidocs/index.html #open the JavaDoc in browser
```

## How to build the project

You can build the project with Maven:
```shell
mvn clean install
```

## How to create a standalone jar file

You can create a standalone executable jar, which will contain all dependencies.
```shell
mvn -P JarWithDependencies clean install
```

## How to run tests

Firstly, run the jar file.
```shell
cd target
java -jar maven_modules_merger-*-jar-with-dependencies.jar modulesList pathToProjectRoot pathToOutputFile mergeMode 
```

Then you can run tests by modules list from the pathToOutputFile
```shell
modules=$(cat pathToOutputFile) # Write new list of modules to the variable
mvn test -pl $modules           # Run tests by new list of modules
```
