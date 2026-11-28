package de.jobst.resulter.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Removes active content from uploaded SVG files (scripts, event handlers, external references).
 * shortcut: blocklist of elements plus allowlist for links, no CSS parsing; upgrade to a full sanitizer library if
 * untrusted users may upload SVG.
 */
final class SvgSanitizer {

    private static final Set<String> FORBIDDEN_ELEMENTS =
            Set.of("script", "foreignobject", "iframe", "object", "embed", "animate", "set", "handler", "use");
    private static final Pattern SAFE_DATA_URI = Pattern.compile("^data:image/(png|jpeg|gif|webp);base64,[A-Za-z0-9+/=\\s]*$");
    private static final Pattern UNSAFE_CSS = Pattern.compile("@import|javascript:|expression\\(|url\\(\\s*['\"]?\\s*(?!#)", Pattern.CASE_INSENSITIVE);

    private static final String SVG_NS = "http://www.w3.org/2000/svg";

    private SvgSanitizer() {}

    static byte[] sanitize(byte[] svg) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(svg));
            Element root = doc.getDocumentElement();
            if (!"svg".equalsIgnoreCase(root.getLocalName())) {
                throw new IllegalArgumentException("Not an SVG document");
            }
            // drop comments and processing instructions outside the root (e.g. xml-stylesheet)
            for (Node n = doc.getFirstChild(), next; n != null; n = next) {
                next = n.getNextSibling();
                if (n != root) {
                    doc.removeChild(n);
                }
            }
            if (!SVG_NS.equals(root.getNamespaceURI())) {
                throw new IllegalArgumentException("Not an SVG document");
            }
            clean(root);

            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(out));
            return out.toByteArray();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SVG: " + e.getMessage(), e);
        }
    }

    private static void clean(Element element) {
        NamedNodeMap attrs = element.getAttributes();
        List<Attr> remove = new ArrayList<>();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            String name = a.getName().toLowerCase(Locale.ROOT);
            String value = a.getValue().trim();
            boolean link = name.equals("href") || name.endsWith(":href");
            if (name.startsWith("on")
                    || (link && !value.startsWith("#") && !SAFE_DATA_URI.matcher(value).matches())
                    || (name.equals("style") && UNSAFE_CSS.matcher(value).find())) {
                remove.add(a);
            }
        }
        remove.forEach(element::removeAttributeNode);

        List<Node> children = new ArrayList<>();
        for (Node c = element.getFirstChild(); c != null; c = c.getNextSibling()) {
            children.add(c);
        }
        for (Node c : children) {
            if (c.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE || c.getNodeType() == Node.COMMENT_NODE) {
                element.removeChild(c);
            } else if (c instanceof Element child) {
                String local = child.getLocalName().toLowerCase(Locale.ROOT);
                boolean unsafeStyle = local.equals("style") && UNSAFE_CSS.matcher(child.getTextContent()).find();
                if (!SVG_NS.equals(child.getNamespaceURI()) || FORBIDDEN_ELEMENTS.contains(local) || unsafeStyle) {
                    element.removeChild(child);
                } else {
                    clean(child);
                }
            }
        }
    }
}
