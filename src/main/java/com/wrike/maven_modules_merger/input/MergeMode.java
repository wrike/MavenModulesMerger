package com.wrike.maven_modules_merger.input;

import com.wrike.maven_modules_merger.MavenModulesMerger;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Contains merge modes, which are supported by {@link MavenModulesMerger}.
 *
 * <p>
 * {@link #SOURCES} mode is used to run merger on source code.
 * </p>
 * <p>
 * {@link #TARGET} mode is used to run merger on already compiled project.
 * </p>
 *
 * @author daniil.shylko on 27.10.2022
 */
@Getter
@AllArgsConstructor
public enum MergeMode {

    SOURCES("sources", Set.of(Path.of("src"))),
    TARGET("target", getSubdirectories("target", Set.of("classes", "test-classes")));

    private final String mergeModeName;
    private final Set<Path> directoriesToMerge;

    private static final Map<String, MergeMode> mergeModeNamesToEnumValues = Arrays.stream(MergeMode.values())
            .collect(Collectors.toMap(MergeMode::getMergeModeName, Function.identity()));

    public static Optional<MergeMode> getMergeModeByName(String mergeModeName) {
        return Optional.ofNullable(mergeModeNamesToEnumValues.get(mergeModeName));
    }

    public static Set<String> getAllSupportedMergeModeNames() {
        return Set.copyOf(mergeModeNamesToEnumValues.keySet());
    }

    /**
     * @param directory      name of directory
     * @param subdirectories name of subdirectories of directory
     * @return target subdirectories path in the next format: "directory/subdirectoryName"
     */
    private static Set<Path> getSubdirectories(String directory, Set<String> subdirectories) {
        Path directoryPath = Path.of(directory);
        return subdirectories.stream()
                .map(directoryPath::resolve)
                .collect(Collectors.toSet());
    }

}
