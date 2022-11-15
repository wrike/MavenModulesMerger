package com.wrike.maven_modules_merger.pom_parser;

import com.wrike.maven_modules_merger.utils.TestFileUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.wrike.maven_modules_merger.pom_parser.utils.XmlUtils.*;
import static com.wrike.maven_modules_merger.utils.TestFileUtils.checkFilesContentIsEqualIgnoringWhitespace;
import static com.wrike.maven_modules_merger.utils.TestFileUtils.getTempTestPomPath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Author: Daniil Shylko
 * Date: 04.10.2022
 */
public class XmlUtilConcurrentTest {

    //don't change these numbers, they are the best ones (probably)
    private static final int NUMBER_OF_THREADS = 2;
    private static final int NUMBER_OF_PARALLEL_EXECUTIONS = 100;

    private void executeConcurrently(Runnable runnable) {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        Queue<Future<?>> tasks = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < NUMBER_OF_PARALLEL_EXECUTIONS; i++) {
            tasks.add(executorService.submit(runnable));
        }
        tasks.forEach(task -> {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        });
        executorService.shutdownNow();
    }

    @Test
    void checkXmlCanBeReadConcurrently() {
        Path tempFileForWriting = getTempTestPomPath();
        Path actualTempTestPomPath = getTempTestPomPath();

        List<Path> tempFilesForReading = Stream.generate(TestFileUtils::getTempTestPomPath)
                .limit(NUMBER_OF_PARALLEL_EXECUTIONS)
                .collect(Collectors.toList());
        Collection<Document> readDocuments = new ConcurrentLinkedQueue<>();
        AtomicInteger nextTempFile = new AtomicInteger(0);
        executeConcurrently(() -> readDocuments.add(readXml(tempFilesForReading.get(nextTempFile.getAndIncrement()))));

        assertThat(readDocuments)
                .as("Check all jobs are executed successfully")
                .hasSize(NUMBER_OF_PARALLEL_EXECUTIONS);
        readDocuments.forEach(readDocument -> {
            writeXml(readDocument, tempFileForWriting);
            try {
                checkFilesContentIsEqualIgnoringWhitespace(tempFileForWriting, actualTempTestPomPath);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Test
    void checkXmlCanBeWrittenConcurrently() {
        Path tempFileForReading = getTempTestPomPath();
        Path actualTempTestPomPath = getTempTestPomPath();

        List<Path> tempFilesForWriting = Stream.generate(TestFileUtils::getTempTestPomPath)
                .limit(NUMBER_OF_PARALLEL_EXECUTIONS)
                .peek(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .collect(Collectors.toList());
        List<Document> documentsToWrite = IntStream.range(0, NUMBER_OF_PARALLEL_EXECUTIONS)
                .mapToObj(i -> readXml(tempFileForReading))
                .collect(Collectors.toList());
        AtomicInteger nextTempFile = new AtomicInteger(0);
        AtomicInteger nextDocumentToWrite = new AtomicInteger(0);
        executeConcurrently(() -> writeXml(
                documentsToWrite.get(nextDocumentToWrite.getAndIncrement()),
                tempFilesForWriting.get(nextTempFile.getAndIncrement())));

        tempFilesForWriting.forEach(tempFileForWriting -> {
            try {
                checkFilesContentIsEqualIgnoringWhitespace(tempFileForWriting, actualTempTestPomPath);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Test
    void checkXmlCanBePrettyWrittenConcurrently() {
        Path tempFileForReading = getTempTestPomPath();
        Path actualTempTestPomPath = getTempTestPomPath();

        List<Path> tempFilesForWriting = Stream.generate(TestFileUtils::getTempTestPomPath)
                .limit(NUMBER_OF_PARALLEL_EXECUTIONS)
                .peek(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .collect(Collectors.toList());
        List<Document> documentsToWrite = IntStream.range(0, NUMBER_OF_PARALLEL_EXECUTIONS)
                .mapToObj(i -> readXml(tempFileForReading))
                .collect(Collectors.toList());
        AtomicInteger nextTempFile = new AtomicInteger(0);
        AtomicInteger nextDocumentToWrite = new AtomicInteger(0);
        executeConcurrently(() -> writePrettifiedXml(
                documentsToWrite.get(nextDocumentToWrite.getAndIncrement()),
                tempFilesForWriting.get(nextTempFile.getAndIncrement())));

        tempFilesForWriting.forEach(tempFileForWriting -> {
            try {
                checkFilesContentIsEqualIgnoringWhitespace(tempFileForWriting, actualTempTestPomPath);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Test
    void checkXPathOperationsWorkConcurrently() {
        Path tempFileForReading = getTempTestPomPath();
        String xpath = "/project/artifactId";
        String expectedXPathResult = "test_pom";

        List<Document> documents = IntStream.range(0, NUMBER_OF_PARALLEL_EXECUTIONS)
                .mapToObj(i -> readXml(tempFileForReading))
                .collect(Collectors.toList());

        Collection<Node> xpathResults = new ConcurrentLinkedQueue<>();
        AtomicInteger nextDocument = new AtomicInteger(0);
        executeConcurrently(() -> xpathResults.add(getNodeByXPath(documents.get(nextDocument.getAndIncrement()), xpath)));

        assertThat(xpathResults)
                .as("Check all jobs are executed successfully")
                .hasSize(NUMBER_OF_PARALLEL_EXECUTIONS)
                .as("Check all nodes have correct data")
                .extracting(Node::getTextContent)
                .containsOnly(expectedXPathResult);
    }

}
