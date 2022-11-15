package com.wrike.maven_modules_merger.pom_parser;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wrike.maven_modules_merger.pom_parser.utils.XmlUtils.readXml;

/**
 * Author: Daniil Shylko
 * Date: 17.08.2022
 */
public class FilePomParser extends AbstractPomParser {

    @Getter
    private final Path originPath;

    public FilePomParser(@NonNull Path originPath) {
        super(readXml(originPath));
        this.originPath = originPath;
    }

    public void writeToOriginFile() {
        writeToFile(originPath);
    }

    public static List<FilePomParser> collectAllPomFiles(Path root) {
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(file -> POM_FILENAME.equals(file.getFileName().toString()))
                    .map(FilePomParser::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new PomParserException(String.format("Unable to collect pom files from `%s` root", root), e);
        }
    }

}
