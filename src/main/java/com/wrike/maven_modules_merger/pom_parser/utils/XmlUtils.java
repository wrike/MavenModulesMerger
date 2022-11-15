package com.wrike.maven_modules_merger.pom_parser.utils;

import com.google.common.base.Suppliers;
import com.wrike.maven_modules_merger.pom_parser.PomParserException;
import lombok.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static javax.xml.xpath.XPathConstants.NODE;
import static javax.xml.xpath.XPathConstants.NODESET;
import static org.w3c.dom.Node.TEXT_NODE;

/**
 * Contains set of useful method for working with XML files
 *
 * @author daniil.shylko on 04.08.2022
 */
public class XmlUtils {

    private static final String PRETTIER_FILENAME = "/prettier.xsl";
    private static final Supplier<Templates> PRETTIFY_TEMPLATE_SUPPLIER = Suppliers.memoize(XmlUtils::getPrettifyTemplate);

    private XmlUtils() {
    }

    /**
     * Reads XML file from the given path
     *
     * @param filePath path to XML file
     * @return Document from file
     */
    public static Document readXml(Path filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile())) {
            return getDocumentBuilder().parse(fileInputStream);
        } catch (IOException | SAXException e) {
            throw new PomParserException(String.format("Unable to read xml from `%s` file", filePath), e);
        }
    }

    /**
     * Reads XML file from the given input stream
     *
     * @param inputStream input stream with XML data
     * @return Document from input stream
     */
    public static Document readXml(InputStream inputStream) {
        try (inputStream) {
            return getDocumentBuilder().parse(inputStream);
        } catch (IOException | SAXException e) {
            throw new PomParserException("Unable to read xml from input stream", e);
        }
    }

    /**
     * Creates parent directories for filePath to avoid {@link java.nio.file.NoSuchFileException}.
     *
     * @param filePath path to file for creating parent directories
     */
    private static void createParentDirectoryForFile(Path filePath) {
        Path parentDirectory = filePath.getParent();
        try {
            if (!Files.exists(parentDirectory)) {
                Files.createDirectories(parentDirectory);
            }
        } catch (IOException e) {
            throw new PomParserException(String.format("Unable to create parent directory `%s` for output file", parentDirectory), e);
        }
    }

    /**
     * Writes XML from document to file filePath
     *
     * @param document XML document
     * @param filePath path to output file
     * @param prettify this flag shows should we prettify XML or not
     */
    private static void writeXml(Document document, Path filePath, boolean prettify) {
        document.setXmlStandalone(true);
        createParentDirectoryForFile(filePath);
        try (FileOutputStream output = new FileOutputStream(filePath.toFile())) {
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(output);
            Transformer transformer = prettify ? getPrettifyTransformer() : getDefaultTransformer();
            transformer.transform(source, result);
        } catch (TransformerException | IOException e) {
            throw new PomParserException(String.format("Unable to write xml to `%s` file", filePath), e);
        }
    }

    /**
     * Writes XML from document to file filePath "as it is"
     *
     * @param document XML document
     * @param filePath path to output file
     */
    public static void writeXml(Document document, Path filePath) {
        writeXml(document, filePath, false);
    }

    /**
     * Writes prettified XML from document to file filePath
     *
     * @param document XML document
     * @param filePath path to output file
     */
    public static void writePrettifiedXml(Document document, Path filePath) {
        writeXml(document, filePath, true);
    }

    /**
     * Returns the node by given xPath
     *
     * @param document XML document
     * @param xPath    xPath to node
     * @return node by given Xpath
     */
    public static Node getNodeByXPath(Document document, String xPath) {
        try {
            return (Node) xPath().compile(xPath).evaluate(document, NODE);
        } catch (XPathExpressionException e) {
            throw new PomParserException(String.format("Unable to execute `%s` XPath", xPath), e);
        }
    }

    /**
     * Returns the node's text content by given xPath
     *
     * @param document XML document
     * @param xPath    xPath to node
     * @return node's text content by given Xpath
     */
    public static String getNodeByXPathTextContent(Document document, String xPath) {
        Node node = getNodeByXPath(document, xPath);
        if (node == null) {
            return null;
        }
        return node.getTextContent();
    }

    /**
     * Returns all nodes by given xPath
     *
     * @param document XML document
     * @param xPath    xPath to node
     * @return nodes by given Xpath
     */
    public static List<Node> getNodesByXPath(Document document, String xPath) {
        try {
            NodeList nodeList = (NodeList) xPath().compile(xPath).evaluate(document, NODESET);
            return convertNodeListToList(nodeList);
        } catch (XPathExpressionException e) {
            throw new PomParserException(String.format("Unable to execute `%s` XPath", xPath), e);
        }
    }

    /**
     * Removes all nodes by given xPath
     *
     * @param document XML document
     * @param xPath    xPath to node
     * @return number of removed nodes
     */
    public static int removeNodesByXPath(Document document, String xPath) {
        List<Node> nodesByXPath = getNodesByXPath(document, xPath);
        nodesByXPath.forEach(node -> node.getParentNode().removeChild(node));
        return nodesByXPath.size();
    }

    /**
     * Converts {@link NodeList} to {@link List} of {@link Node}
     *
     * @param nodeList XML nodeList
     * @return list of nodes from the nodeList
     */
    public static List<Node> convertNodeListToList(@NonNull NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item)
                .collect(Collectors.toList());
    }

    /**
     * Returns names(tags) of given nodes
     *
     * @param nodes list of nodes
     * @return list of nodes' names
     */
    public static List<String> getNodesNames(@NonNull List<Node> nodes) {
        return nodes.stream()
                .map(Node::getNodeName)
                .collect(Collectors.toList());
    }

    /**
     * Returns all children nodes of the given node
     *
     * @param node parent node
     * @return all children nodes
     */
    public static List<Node> getNodeChildren(@NonNull Node node) {
        return convertNodeListToList(node.getChildNodes()).stream()
                .filter(child -> TEXT_NODE != child.getNodeType())
                .collect(Collectors.toList());
    }

    /**
     * Returns not thread-safe {@link DocumentBuilder}.
     *
     * @return DocumentBuilder
     */
    private static DocumentBuilder getDocumentBuilder() {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            return builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new PomParserException("Unable to init DocumentBuilder", e);
        }
    }

    /**
     * Returns not thread-safe {@link Transformer}.
     *
     * @return Transformer
     */
    private static Transformer getDefaultTransformer() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            return transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new PomParserException("Unable to init default Transformer", e);
        }
    }

    /**
     * Returns thread-safe {@link Templates}.
     *
     * @return Templates
     */
    private static Templates getPrettifyTemplate() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        InputStream prettierStream = XmlUtils.class.getResourceAsStream(PRETTIER_FILENAME);
        try {
            return transformerFactory.newTemplates(new StreamSource(prettierStream));
        } catch (TransformerConfigurationException e) {
            throw new PomParserException("Unable to init prettify Transformer template", e);
        }
    }

    /**
     * Returns not thread-safe {@link Transformer}, which prettifies XML file.
     *
     * @return Transformer
     */
    private static Transformer getPrettifyTransformer() {
        try {
            return PRETTIFY_TEMPLATE_SUPPLIER.get().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new PomParserException("Unable to init prettify Transformer", e);
        }
    }

    /**
     * Returns not thread-safe {@link XPath}.
     *
     * @return XPath
     */
    private static XPath xPath() {
        return XPathFactory.newInstance().newXPath();
    }

}
