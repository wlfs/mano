/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.xml;

import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class XmlHelper {

    static DocumentBuilderFactory _factory;
    static DocumentBuilder _builder;
    private final Document _document;
    static XPath _xpath;

    static void init() throws ParserConfigurationException {
        if (_factory == null) {
            _factory = DocumentBuilderFactory.newInstance();
            _factory.setNamespaceAware(false);
        }
        if (_xpath == null) {
            _xpath = XPathFactory.newInstance().newXPath();
        }
        if (_builder == null) {
            _builder = _factory.newDocumentBuilder();
        }
    }

    public static XmlHelper load(String filename) throws XmlException {
        try {
            init();
            return new XmlHelper(_builder.parse(filename));
        } catch (ParserConfigurationException|SAXException|IOException ex) {
            throw new XmlException(ex.getMessage(),ex);
        }
    }

    private XmlHelper(Document document) {
        _document = document;
    }
    
    private Object xpathEvaluate(Node node,String xpath,QName returnType) throws XmlException{
        try {
            XPathExpression expr = _xpath.compile(xpath);
            return expr.evaluate(node, returnType);
        } catch (XPathExpressionException ex) {
            throw new XmlException(ex.getMessage(),ex);
        }
    }

    public Node selectNode(String xpath) throws XmlException {
        return selectNode(_document,xpath);
    }
    
    public Node selectNode(Node node,String xpath) throws XmlException {
        return (Node)xpathEvaluate(node,xpath,XPathConstants.NODE);
    }

    public NodeList selectNodes(String xpath) throws XmlException {
        return selectNodes(_document,xpath);
    }
    
    public NodeList selectNodes(Node node,String xpath) throws XmlException {
        return (NodeList)xpathEvaluate(node,xpath,XPathConstants.NODESET);
    }

    public String attr(NamedNodeMap attrs,String name){
        if(attrs==null || attrs.getNamedItem(name)==null){
            return null;
        }
        return attrs.getNamedItem(name).getNodeValue();
    }
    
}
