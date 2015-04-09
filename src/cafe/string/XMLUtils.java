/**
 * Copyright (c) 2014-2015 by Wen Yu.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Any modifications to this file must keep this entire header intact.
 * 
 * Change History - most recent changes go on top of previous changes
 *
 * StringUtils.java
 *
 * Who   Date       Description
 * ====  =========  =====================================================
 * WY    09Apr2015  Added null check to findAttribute()
 * WY    03Mar2015  Added serializeToString() and serializeToByteArray()
 * WY    27Feb2015  Added findAttribute() and removeAttribute()
 * WY    23Jan2015  Initial creation - moved XML related methods to here
 */


package cafe.string;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class XMLUtils {
	public static void addChild(Node parent, Node child) {
		parent.appendChild(child);
	}
	
	public static void addText(Document doc, Node parent, String data) {
		parent.appendChild(doc.createTextNode(data));
	}
	
	// Create an empty Document node
	public static Document createDocumentNode() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = null;
	    
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return builder.newDocument();
	}
	
	public static Node createElement(Document doc, String tagName) {
		return doc.createElement(tagName);
	}
	
	public static Document createXML(byte[] xml) {
		//Get the DOM Builder Factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//Get the DOM Builder
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		//Load and Parse the XML document
		//document contains the complete XML as a Tree.
		Document document = null;
		try {
			document = builder.parse(new ByteArrayInputStream(xml));			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return document;
	}
	
	public static Document createXML(String xml) {
		//Get the DOM Builder Factory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//Get the DOM Builder
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		//Load and Parse the XML document
		//document contains the complete XML as a Tree.
		Document document = null;
		InputSource source = new InputSource(new StringReader(xml));
		try {
			document = builder.parse(source);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return document;		 
	}
	
	public static String escapeXML(String input) {
		Iterator<Character> itr = StringUtils.stringIterator(input);
		StringBuilder result = new StringBuilder();		
		
		while (itr.hasNext())
		{
			Character c = itr.next();
			
			switch (c)
			{
				case '"':
					result.append("&quot;");
					break;
				case '\'':
					result.append("&apos;");
					break;
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				case '&':
					result.append("&amp;");
					break;
				default:
					result.append(c);
			}
		}
		
		return result.toString();
	}
	
	// Retrieve the first non-empty, non-null attribute value for the attribute name
	public static String findAttribute(Document doc, String tagName, String attribute) {
		// Sanity check
		if(doc == null || tagName == null || attribute == null)	return "";
		
		NodeList nodes = doc.getElementsByTagName(tagName);
		
		for(int i = 0; i < nodes.getLength(); i++) {
			String attr = ((Element)nodes.item(i)).getAttribute(attribute);
			if(!StringUtils.isNullOrEmpty(attr))
				return attr;
		}
		
		return "";
	}
	
	public static void insertLeadingPI(Document doc, String target, String data) {
		Element element = doc.getDocumentElement();
	    ProcessingInstruction pi = doc.createProcessingInstruction(target, data);
	    element.getParentNode().insertBefore(pi, element);
	}
	
	public static void insertTrailingPI(Document doc, String target, String data) {
		Element element = doc.getDocumentElement();
	    ProcessingInstruction pi = doc.createProcessingInstruction(target, data);
	    element.getParentNode().appendChild(pi);
	}
		
	public static void printNode(Node node, String indent) {
		if(node != null) {
			if(indent == null) indent = "";
			switch(node.getNodeType()) {
		        case Node.DOCUMENT_NODE: {
		            Node child = node.getFirstChild();
		            while(child != null) {
		            	printNode(child, indent);
		            	child = child.getNextSibling();
		            }
		            break;
		        } 
		        case Node.DOCUMENT_TYPE_NODE: {
		            DocumentType doctype = (DocumentType) node;
		            System.out.println("<!DOCTYPE " + doctype.getName() + ">");
		            break;
		        }
		        case Node.ELEMENT_NODE: { // Element node
		            Element ele = (Element) node;
		            System.out.print(indent + "<" + ele.getTagName());
		            NamedNodeMap attrs = ele.getAttributes(); 
		            for(int i = 0; i < attrs.getLength(); i++) {
		                Node a = attrs.item(i);
		                System.out.print(" " + a.getNodeName() + "='" + 
		                          escapeXML(a.getNodeValue()) + "'");
		            }
		            System.out.println(">");
	
		            String newindent = indent + "    ";
		            Node child = ele.getFirstChild();
		            while(child != null) {
		            	printNode(child, newindent);
		            	child = child.getNextSibling();
		            }
	
		            System.out.println(indent + "</" + ele.getTagName() + ">");
		            break;
		        }
		        case Node.TEXT_NODE: {
		            Text textNode = (Text)node;
		            String text = textNode.getData().trim();
		            if ((text != null) && text.length() > 0)
		                System.out.println(indent + escapeXML(text));
		            break;
		        }
		        case Node.PROCESSING_INSTRUCTION_NODE: {
		            ProcessingInstruction pi = (ProcessingInstruction)node;
		            System.out.println(indent + "<?" + pi.getTarget() +
		                               " " + pi.getData() + "?>");
		            break;
		        }
		        case Node.ENTITY_REFERENCE_NODE: {
		            System.out.println(indent + "&" + node.getNodeName() + ";");
		            break;
		        }
		        case Node.CDATA_SECTION_NODE: {           // Output CDATA sections
		            CDATASection cdata = (CDATASection)node;
		            System.out.println(indent + "<" + "![CDATA[" + cdata.getData() +
		                        "]]" + ">");
		            break;
		        }
		        case Node.COMMENT_NODE: {
		        	Comment c = (Comment)node;
		            System.out.println(indent + "<!--" + c.getData() + "-->");
		            break;
		        }
		        default:
		            System.err.println("Unknown node: " + node.getClass().getName());
		            break;
			}
		}
	}
	
	// Retrieve and remove the first non-empty, non-null attribute value for the attribute name
	public static String removeAttribute(Document doc, String tagName, String attribute) {
		NodeList nodes = doc.getElementsByTagName(tagName);
		String retVal = "";
		
		for(int i = 0; i < nodes.getLength(); i++) {
			Element ele = (Element)nodes.item(i);
			String attr = ele.getAttribute(attribute);
			
			if(!StringUtils.isNullOrEmpty(attr)) {
				retVal = attr;
				ele.removeAttribute(attribute);
				break;
			}
		}
		
		return retVal;		
	}
	
	public static byte[] serializeToByteArray(Document doc) throws IOException {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new IOException("Unable to serialize XML document");
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		String encoding = doc.getInputEncoding();
		if(encoding == null) encoding = "UTF-8";
		transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
		DOMSource source = new DOMSource(doc);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Result result = new StreamResult(out);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new IOException("Unable to serialize XML document");
		}
		
		return out.toByteArray();
	}
	
	/**
	 * Serialize XML Document to string using DOM Level 3 Load/Save
	 * 
	 * @param doc XML Document
	 * @return String representation of the Document
	 * @throws IOException
	 */
	public static String serializeToStringLS(Document doc) throws IOException {
		return serializeToStringLS(doc, doc);
	}
	
	/**
	 * Serialize XML Document to string using DOM Level 3 Load/Save
	 * 
	 * @param doc XML Document
	 * @param node the Node to serialize
	 * @return String representation of the Document
	 * @throws IOException
	 */
	public static String serializeToStringLS(Document doc, Node node) throws IOException {
		String encoding = doc.getInputEncoding();
        if(encoding == null) encoding = "UTF-8";
        
        return serializeToStringLS(doc, node, encoding);
	}
	
	/**
	 * Serialize XML Node to string
	 * <p>
	 * Note: this method is supposed to be faster than the Transform version but the output control
	 * is limited. If node is Document node, it will output XML PI which sometimes we want to avoid.
	 * 
	 * @param doc XML document
	 * @param node Node to be serialized
	 * @param encoding encoding for the output
	 * @return String representation of the Document
	 * @throws IOException
	 */
	public static String serializeToStringLS(Document doc, Node node, String encoding) throws IOException {
		DOMImplementationLS domImpl = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImpl.createLSSerializer();
        LSOutput output = domImpl.createLSOutput();
        output.setEncoding(encoding);
        StringWriter writer = new StringWriter();
        output.setCharacterStream(writer);
        lsSerializer.write(node, output);
        writer.flush();
        
        return writer.toString();
	}
	
	public static String serializeToString(Document doc) throws IOException {
		String encoding = doc.getInputEncoding();
		if(encoding == null) encoding = "UTF-8";
		
		return serializeToString(doc, encoding);
	}
	
	/**
	 * Serialize XML Document to string using Transformer
	 * 
	 * @param doc XML Document
	 * @return String representation of the the Document
	 * @throws IOException
	 */
	public static String serializeToString(Node node, String encoding) throws IOException {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new IOException("Unable to serialize XML document");
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
		DOMSource source = new DOMSource(node);
		StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new IOException("Unable to serialize XML document");
		}		
	    writer.flush();
	    
        return writer.toString();
	}
	
	public static void showXML(Document document) {
		printNode(document,"");
	}
}