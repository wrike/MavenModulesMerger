## Description

Maven module merger allows you to merge Maven modules into one big module.
For more information, please check out the generated JavaDoc.

This repository contains two projects:
- merger — The tool for merging
- example_project — The project, where you can try the Merger

## How to open JavaDoc

You can generate JavaDoc with Maven.

```shell
mvn -pl merger -P GenerateJavaDoc clean javadoc:javadoc
```

After that, you can open the generated JavaDoc.

```shell
open merger/target/site/apidocs/index.html #open the JavaDoc in browser
```

## How to build the project

You can build the project with Maven:
```shell
mvn -pl merger clean install
```

## How to create a standalone jar file

You can create a standalone executable jar, which will contain all dependencies.
```shell
mvn -pl merger -P JarWithDependencies clean install
```

## How to run tests

Firstly, run the jar file.
```shell
cd merger/target
java -jar merger-*-jar-with-dependencies.jar modulesList pathToProjectRoot pathToOutputFile mergeMode 
```

Then you can run tests by modules list from the pathToOutputFile
```shell
modules=$(cat pathToOutputFile) # Write new list of modules to the variable
cd pathToProjectRoot            # Go to directory with tests
mvn test -pl $modules           # Run tests by new list of modules
```

# Working with example_project

The example_project module is a separate project, where you can try the Merger.
To merge all modules in the example_project, run the following command:
```shell
cd merger/target
java -jar merger-*-jar-with-dependencies.jar \
    wrike_avatar,wrike_button,wrike_task_view,wrike_tooltip \
    ../../example_project \
    ../../example_project/target/modulesList.txt \
    sources
```
After this you will have merged_modules, created from all modules of example_project.
You can inspect created files in `example_project/merged_modules` directory and output modules in `example_project/target/modulesList.txt`.

Compare the speed of tests with the following commands:
```shell
cd example_project
mvn test -pl wrike_avatar,wrike_button,wrike_task_view,wrike_tooltip # running all tests without modules parallelism
mvn test -pl wrike_avatar,wrike_button,wrike_task_view,wrike_tooltip -T 5 # running all tests with modules parallelism
mvn test -pl merged_modules # running all tests after merging
```
Some modules have dependencies on other modules, so parallelism (`-T 5`) will not help a lot, but after merging all tests will be run in parallel fast.

Using unique names of directories in `src/test/java/resources` folders and unique package names guarantee
that we won't have conflicts between files during merging.
