package com.wrike.merger;

import com.wrike.merger.filter.ModulesFilter;
import com.wrike.merger.input.InputParser;
import com.wrike.merger.input.MergeMode;
import com.wrike.merger.pom.FilePomParser;
import com.wrike.merger.pom.InputStreamPomParser;
import com.wrike.merger.pom.PomParser;
import com.wrike.merger.pom.bean.Dependency;
import com.wrike.merger.pom.bean.Parent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wrike.merger.ExitCode.MERGING_FAILED;
import static com.wrike.merger.FileUtils.copyDirectoryWithCheckingConflicts;
import static com.wrike.merger.pom.bean.DependencyScope.COMPILE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * {@link MavenModulesMerger} merges given modules together and stores the result into the separate module.
 *
 * @author daniil.shylko on 26.08.2022
 */
public class MavenModulesMerger {

    private static final Logger LOG = LogManager.getLogger(MavenModulesMerger.class);
    private static final String POM_FILENAME = "pom.xml";
    private static final String MERGED_MODULES_TEMPLATE_POM = "merged_modules_template_pom.xml";
    private static final String MERGED_MODULES = "merged_modules";

    private final ExceptionHandler exceptionHandler;
    private final List<ModulesFilter> modulesFilters;

    private Set<String> modulesNames;
    private Set<Path> modulesPaths;
    private Path pathToProjectRoot;
    private Path pathToOutputFile;
    private MergeMode mergeMode;

    /**
     * Default constructor without filters
     */
    public MavenModulesMerger() {
        this(new ExceptionHandler(), List.of());
    }

    /**
     * Default constructor with {@link ModulesFilter}
     *
     * @param modulesFilters Is used to understand which modules we should merge
     */
    public MavenModulesMerger(List<ModulesFilter> modulesFilters) {
        this(new ExceptionHandler(), modulesFilters);
    }

    /**
     * Package-private constructor, should be called directly only for testing purposes
     *
     * @param exceptionHandler object, which handle exceptions. Can be mocked for testing purposes
     * @param modulesFilters   Is used to understand which modules we should merge
     */
    MavenModulesMerger(ExceptionHandler exceptionHandler, List<ModulesFilter> modulesFilters) {
        this.exceptionHandler = exceptionHandler;
        this.modulesFilters = modulesFilters;
    }

    /**
     * The stateless method, which does merging for provided arguments.
     * <p>
     * It receives the following arguments in the order:
     * </p>
     * <ol>
     *     <li>modulesList</li>
     *     <li>pathToProjectRoot</li>
     *     <li>pathToOutputFile</li>
     *     <li>mergeMode</li>
     * </ol>
     *
     * @param args arguments list, which will be parsed by {@link InputParser}
     * @see InputParser
     */
    public void merge(String... args) {
        try {
            parseArguments(args);
            Map<Boolean, Set<Path>> modulesPathsByMergingAbility = separateModulesByMergingAbility();
            Set<Path> modulesToMerge = modulesPathsByMergingAbility.get(TRUE);
            LOG.info("Starting merging. Modules to merge = `{}`", modulesToMerge);
            if (modulesToMerge.size() < 2) {
                if (modulesToMerge.isEmpty()) {
                    LOG.info("Finishing merging. Modules won't be merged because there are no modules for merging");
                } else {
                    LOG.info("Finishing merging. Modules won't be merged because there is only 1 module for merging");
                }
                outputModules(modulesNames);
                return;
            }
            mergeModules(modulesToMerge);
            Stream<String> nonMergedModules = modulesPathsByMergingAbility.get(FALSE).stream()
                    .map(pathToProjectRoot::relativize)
                    .map(Path::toString);
            Set<String> modulesAfterMerging = Stream.concat(Stream.of(MERGED_MODULES), nonMergedModules)
                    .collect(Collectors.toSet());
            outputModules(modulesAfterMerging);
        } catch (Exception e) {
            exceptionHandler.onException(e, MERGING_FAILED);
        }
    }

    /**
     * Parsing of arguments with {@link InputParser}
     *
     * @param args arguments for parsing
     */
    private void parseArguments(String... args) {
        InputParser inputParser = new InputParser(args);
        this.modulesNames = inputParser.getModulesNames();
        this.pathToProjectRoot = inputParser.getPathToProjectRoot();
        this.pathToOutputFile = inputParser.getPathToOutputFile();
        this.mergeMode = inputParser.getMergeMode();
        this.modulesPaths = getModulesPaths(modulesNames);
    }

    /**
     * Gets absolute paths for modules in projects
     *
     * @param modules names of modules
     * @return absolute paths for modules in projects
     */
    private Set<Path> getModulesPaths(Set<String> modules) {
        return modules.stream()
                .map(this::getProjectRootRelatedPath)
                .collect(Collectors.toSet());
    }

    /**
     * Merges all modules and sets up both {@link #MERGED_MODULES} pom.xml and root pom.xml files
     *
     * @param modulesPaths modules paths to merge
     */
    private void mergeModules(Set<Path> modulesPaths) {
        Path mergedModulesDirectory = createMergedModulesDirectory();
        modulesPaths.forEach(moduleToMerge -> {
            LOG.info("Merging `{}` module", pathToProjectRoot.relativize(moduleToMerge));
            Set<Path> directoriesToMerge = filterExistingModuleDirectories(moduleToMerge, mergeMode.getDirectoriesToMerge());
            LOG.info("`{}` directories content will be merged", directoriesToMerge);
            directoriesToMerge.forEach(subdirectoryPath -> {
                try {
                    copyDirectoryWithCheckingConflicts(moduleToMerge.resolve(subdirectoryPath), mergedModulesDirectory.resolve(subdirectoryPath));
                } catch (IOException e) {
                    throw new MavenModulesMergerException(String.format("Can't copy files of `%s` module", pathToProjectRoot.relativize(moduleToMerge)), e);
                } catch (FilesContentConflictException e) {
                    throw new MavenModulesMergerException("Unable to merge files due to conflict in files content.", e);
                }
            });
        });
        Set<Dependency> mergedModulesDependencies = collectMergedModulesDependencies(modulesPaths);
        FilePomParser rootPomParser = new FilePomParser(getProjectRootRelatedPath(POM_FILENAME));
        createMergedModulesPomFile(rootPomParser, mergedModulesDependencies, mergedModulesDirectory);
        addMergedModulesToRootPom(rootPomParser);
    }

    /**
     * Collects all dependencies for {@link #MERGED_MODULES} modules.
     * <p>
     * The set of dependencies is collected by this algorithm:
     * </p>
     * <ol>
     *     <li>Collecting all dependencies of all modules, which were merged</li>
     *     <li>Set scope COMPILE for all dependencies to avoid conflicts
     *     between the same dependencies, but with the different scope</li>
     *     <li>All modules were merged together and they don't need to have dependency to each other,
     *     so we can remove dependencies to themselves</li>
     * </ol>
     *
     * @param modulesPaths paths to all modules for collecting dependencies
     * @return set of dependency for {@link #MERGED_MODULES} module
     */
    private Set<Dependency> collectMergedModulesDependencies(Set<Path> modulesPaths) {
        List<PomParser> modulePomParsers = modulesPaths.stream()
                .map(modulePath -> modulePath.resolve(POM_FILENAME))
                .map(FilePomParser::new)
                .collect(Collectors.toList());
        Map<Dependency, List<Dependency>> modulesDependenciesMap = modulePomParsers.stream()
                .map(PomParser::getAllDependencies)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Function.identity()));
        checkAllDependenciesHaveTheSameVersion(modulesDependenciesMap);
        Set<Dependency> modulesDependencies = modulesDependenciesMap.keySet();
        modulesDependencies.forEach(dependency -> dependency.setScope(COMPILE));
        Set<Dependency> dependenciesToExclude = modulePomParsers.stream()
                .map(pomParser -> Dependency.builder()
                        .groupId(pomParser.getEffectiveGroupId())
                        .artifactId(pomParser.getArtifactId())
                        .build()
                )
                .collect(Collectors.toSet());
        modulesDependencies.removeAll(dependenciesToExclude);
        return modulesDependencies;
    }

    /**
     * Checks that all dependencies with the same groupId and artifactId have the same version
     *
     * @param modulesDependenciesMap dependencies grouped by artifactId and groupId
     */
    private static void checkAllDependenciesHaveTheSameVersion(Map<Dependency, List<Dependency>> modulesDependenciesMap) {
        modulesDependenciesMap.values().forEach(dependencies -> {
            if (dependencies.stream().map(Dependency::getVersion).distinct().count() != 1L) {
                throw new MavenModulesMergerException(String.format("Dependencies have different versions: %s", dependencies));
            }
        });
    }

    /**
     * Copies {@link #MERGED_MODULES_TEMPLATE_POM} to {@code mergedModulesDirectory}
     * and sets its dependencies to {@code dependencies}
     *
     * @param rootPomParser          parser for root pom-file
     * @param dependencies           list of dependencies of {@link #MERGED_MODULES} modules
     * @param mergedModulesDirectory path to {@link #MERGED_MODULES} directory
     */
    private void createMergedModulesPomFile(FilePomParser rootPomParser, Set<Dependency> dependencies, Path mergedModulesDirectory) {
        PomParser mergedModulesTemplatePomParser = new InputStreamPomParser(
                getClass().getResourceAsStream(File.separator + MERGED_MODULES_TEMPLATE_POM));
        mergedModulesTemplatePomParser.setDependencies(dependencies);
        mergedModulesTemplatePomParser.setParent(Parent.builder()
                .groupId(rootPomParser.getEffectiveGroupId())
                .artifactId(rootPomParser.getArtifactId())
                .version(rootPomParser.getVersion())
                .build());
        Path mergedModulesPomPath = mergedModulesDirectory.resolve(POM_FILENAME);
        mergedModulesTemplatePomParser.writeToFile(mergedModulesPomPath);
        LOG.info("Merged modules pom was created");
    }

    /**
     * Adds the {@link #MERGED_MODULES} modules as a child modules in a root pom file
     *
     * @param rootPomParser parser for root pom-file
     */
    private void addMergedModulesToRootPom(FilePomParser rootPomParser) {
        rootPomParser.addChildModuleIfDoesNotExist(MERGED_MODULES);
        rootPomParser.writeToOriginFile();
        LOG.info("Merged module was added to root pom as a child module");
    }

    /**
     * We merge only existent directories. At least one directory should exist.
     *
     * @param moduleToMerge path to module
     * @param directories   list of paths, relative to module
     * @return list of existent directories in modulePath
     */
    private Set<Path> filterExistingModuleDirectories(Path moduleToMerge, Set<Path> directories) {
        Set<Path> existentDirectories = directories.stream()
                .map(moduleToMerge::resolve)
                .filter(Files::exists)
                .collect(Collectors.toSet());
        if (existentDirectories.isEmpty()) {
            throw new MavenModulesMergerException(String.format("No directories found from set %s to merge in `%s` module", directories, pathToProjectRoot.relativize(moduleToMerge)));
        }
        return existentDirectories.stream()
                .map(moduleToMerge::relativize)
                .collect(Collectors.toSet());
    }

    /**
     * Creates {@link #MERGED_MODULES} directory
     *
     * @return path to created {@link #MERGED_MODULES} directory
     */
    private Path createMergedModulesDirectory() {
        Path mergedModulesPath = getProjectRootRelatedPath(MERGED_MODULES);
        try {
            Files.createDirectory(mergedModulesPath);
        } catch (IOException e) {
            throw new MavenModulesMergerException("Can't create directory for merged modules", e);
        }
        return mergedModulesPath;
    }

    /**
     * Returns project root related path to file.
     * <p>
     * For example, for file "123.txt" and path to root "/root/" it returns "/root/123.txt" path.
     * </p>
     *
     * @param fileName name of file in project root
     * @return fileName path, relative to {@link #pathToProjectRoot}
     */
    private Path getProjectRootRelatedPath(String fileName) {
        return pathToProjectRoot.resolve(fileName);
    }

    /**
     * <p>
     * Separates all modules by the merging ability based on {@link #modulesFilters}.
     * </p>
     *
     * @return map of modules sets. The map has the next structure -
     * {true=[List of modules, which should be merged], false=[List of modules, which should not be merged]}
     */
    private Map<Boolean, Set<Path>> separateModulesByMergingAbility() {
        return modulesPaths.stream()
                .collect(Collectors.partitioningBy(
                        module -> modulesFilters.stream().allMatch(modulesFilter -> modulesFilter.moduleMatches(module)),
                        Collectors.toSet()));
    }

    /**
     * Writes to {@link #pathToOutputFile} set of modules, which should be run after merging. Values are separated by comma.
     *
     * @param outputModulesList set of modules
     */
    private void outputModules(Set<String> outputModulesList) {
        createParentDirectoriesForOutputFile();
        try {
            String outputModulesListString = String.join(",", outputModulesList);
            Files.writeString(pathToOutputFile, outputModulesListString);
            LOG.info("Modules list `{}` was written to file `{}`", outputModulesListString, pathToOutputFile);
        } catch (IOException e) {
            throw new MavenModulesMergerException("Can't write modules list to file " + pathToOutputFile, e);
        }
    }

    /**
     * Creates parent directories for {@link #pathToOutputFile}
     */
    private void createParentDirectoriesForOutputFile() {
        try {
            Path parentDirectory = pathToOutputFile.getParent();
            Files.createDirectories(parentDirectory);
        } catch (IOException e) {
            throw new MavenModulesMergerException(String.format("Can't create parent directories `%s` for output file", pathToOutputFile), e);
        }
    }

}
