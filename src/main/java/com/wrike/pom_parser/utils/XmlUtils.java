package com.wrike.pom_parser.utils;

import com.google.common.base.Suppliers;
import com.wrike.pom_parser.PomParserException;
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
 * Author: Daniil Shylko
 * Date: 04.08.2022
 */
public class XmlUtils {

    private static final String PRETTIER_FILENAME = "/prettier.xsl";
    private static final Supplier<Templates> PRETTIFY_TEMPLATE_SUPPLIER = Suppliers.memoize(XmlUtils::getPrettifyTemplate);

    private XmlUtils() {
    }

    public static Document readXml(Path filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile())) {
            return getDocumentBuilder().parse(fileInputStream);
        } catch (IOException | SAXException e) {
            throw new PomParserException(String.format("Unable to read xml from `%s` file", filePath), e);
        }
    }

    public static Document readXml(InputStream inputStream) {
        try (inputStream) {
            return getDocumentBuilder().parse(inputStream);
        } catch (IOException | SAXException e) {
            throw new PomParserException("Unable to read xml from input stream", e);
        }
    }

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

    public static void writeXml(Document document, Path filePath) {
        writeXml(document, filePath, false);
    }

    public static void writePrettifiedXml(Document document, Path filePath) {
        writeXml(document, filePath, true);
    }

    public static Node getNodeByXPath(Document document, String xPath) {
        try {
            return (Node) xPath().compile(xPath).evaluate(document, NODE);
        } catch (XPathExpressionException e) {
            throw new PomParserException(String.format("Unable to execute `%s` XPath", xPath), e);
        }
    }

    public static List<Node> getNodesByXPath(Document document, String xPath) {
        try {
            NodeList nodeList = (NodeList) xPath().compile(xPath).evaluate(document, NODESET);
            return convertNodeListToList(nodeList);
        } catch (XPathExpressionException e) {
            throw new PomParserException(String.format("Unable to execute `%s` XPath", xPath), e);
        }
    }

    public static int removeNodesByXPath(Document document, String xPath) {
        List<Node> nodesByXPath = getNodesByXPath(document, xPath);
        nodesByXPath.forEach(node -> node.getParentNode().removeChild(node));
        return nodesByXPath.size();
    }

    public static List<Node> convertNodeListToList(@NonNull NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item)
                .collect(Collectors.toList());
    }

    public static List<String> getNodesNames(@NonNull List<Node> nodes) {
        return nodes.stream()
                .map(Node::getNodeName)
                .collect(Collectors.toList());
    }

    public static List<Node> getNodeChildren(@NonNull Node node) {
        return convertNodeListToList(node.getChildNodes()).stream()
                .filter(child -> TEXT_NODE != child.getNodeType())
                .collect(Collectors.toList());
    }

    private static DocumentBuilder getDocumentBuilder() {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            return builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new PomParserException("Unable to init DocumentBuilder", e);
        }
    }

    private static Transformer getDefaultTransformer() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            return transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new PomParserException("Unable to init default Transformer", e);
        }
    }

    private static Templates getPrettifyTemplate() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        InputStream prettierStream = XmlUtils.class.getResourceAsStream(PRETTIER_FILENAME);
        try {
            return transformerFactory.newTemplates(new StreamSource(prettierStream));
        } catch (TransformerConfigurationException e) {
            throw new PomParserException("Unable to init prettify Transformer template", e);
        }
    }

    private static Transformer getPrettifyTransformer() {
        try {
            return PRETTIFY_TEMPLATE_SUPPLIER.get().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new PomParserException("Unable to init prettify Transformer", e);
        }
    }

    private static XPath xPath() {
        return XPathFactory.newInstance().newXPath();
    }

}
