package com.wrike.merger.input;

import com.wrike.merger.MavenModulesMerger;
import com.wrike.merger.MavenModulesMergerException;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wrike.merger.input.MergeMode.getAllSupportedMergeModeNames;

/**
 * {@link InputParser} parses arguments for the {@link MavenModulesMerger}.
 *
 * <p>
 * It receives the following arguments in the constructor in the order:
 * </p>
 *     <ol>
 *         <li>modulesList - the list of modules, separated by comma.</li>
 *         <li>pathToProjectRoot - path to the project root.</li>
 *         <li>pathToOutputFile - path to file, where will store the modules list after merging.</li>
 *         <li>mergeMode - merge mode from {@link MergeMode#getAllSupportedMergeModeNames} set.</li>
 *     </ol>
 * <p>
 * After parsing the next values will be returned:
 * </p>
 *     <ol>
 *         <li>modulesNames - set of modules names from modulesList</li>
 *         <li>pathToProjectRoot - string pathToProjectRoot from the input as {@link Path}</li>
 *         <li>pathToOutputFile - string pathToOutputFile from the input as {@link Path}</li>
 *         <li>mergeMode - {@link MergeMode} value, matched to given input</li>
 *     </ol>
 * <p>
 * It throws an exception in the following cases:
 * </p>
 *     <ul>
 *         <li>The number of arguments != 4</li>
 *         <li>One of arguments is null or empty</li>
 *         <li>pathToProjectRoot does not exist</li>
 *         <li>{@link MergeMode#getAllSupportedMergeModeNames} set does not contain given mergeMode</li>
 *     </ul>
 *
 * @author daniil.shylko on 26.08.2022
 * @see MavenModulesMerger
 */
@Getter
public class InputParser {

    private static final Logger LOG = LogManager.getLogger(InputParser.class);

    private final Set<String> modulesNames;
    private final Path pathToProjectRoot;
    private final Path pathToOutputFile;
    private final MergeMode mergeMode;

    public InputParser(String... args) {
        if (args.length != 4) {
            throw new MavenModulesMergerException("You should pass only four arguments" +
                    " - modulesList, pathToProjectRoot, pathToOutputFile and mergeMode");
        }
        String modulesListInput = args[0];
        String pathToProjectRootInput = args[1];
        String pathToOutputFileInput = args[2];
        String mergeModeInput = args[3];
        LOG.info("Arguments parsing. modulesList = `{}`, pathToProjectRoot = `{}`, pathToOutputFile = `{}`, mergeMode = `{}`",
                modulesListInput, pathToProjectRootInput, pathToOutputFileInput, mergeModeInput);
        modulesNames = parseModulesNames(modulesListInput);
        pathToProjectRoot = parsePathToProjectRoot(pathToProjectRootInput);
        pathToOutputFile = parsePathToOutputFile(pathToOutputFileInput);
        mergeMode = parseMergeMode(mergeModeInput);
    }

    /**
     * <p>
     * Parse the list of modules, separated by comma.
     * For example, for input "module1,module2/module3,module1" it returns the next set: ["module2/module3", "module1"]
     * </p>
     *
     * @param modulesList - list of modules, separated by comma.
     * @return set of modules names.
     */
    private static Set<String> parseModulesNames(@NonNull String modulesList) {
        if (modulesList.isEmpty()) {
            throw new MavenModulesMergerException("modulesList can't be empty");
        }
        return Arrays.stream(modulesList.split(","))
                .collect(Collectors.toSet());
    }

    private static Path parsePathToProjectRoot(@NonNull String pathToProjectRoot) {
        if (pathToProjectRoot.isEmpty()) {
            throw new MavenModulesMergerException("pathToProjectRoot can't be empty");
        }
        Path pathToRoot = Path.of(pathToProjectRoot);
        if (!Files.exists(pathToRoot)) {
            throw new MavenModulesMergerException("pathToProjectRoot directory doesn't exist");
        }
        return pathToRoot;
    }

    private static Path parsePathToOutputFile(@NonNull String pathToOutputFile) {
        if (pathToOutputFile.isEmpty()) {
            throw new MavenModulesMergerException("pathToOutputFile can't be empty");
        }
        return Path.of(pathToOutputFile);
    }

    private static MergeMode parseMergeMode(@NonNull String mergeMode) {
        return MergeMode.getMergeModeByName(mergeMode)
                .orElseThrow(() -> new MavenModulesMergerException(
                        String.format("Only %s merge modes are supported, but you provided `%s`", getAllSupportedMergeModeNames(), mergeMode)));
    }

}
