package edu.nju.ws.spatialie.utils;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;

public class XmlUtil {

    public static Element getRootElement(String xmlPath) {
        File xmlFile = new File(xmlPath);
        return getDocument(xmlFile).getRootElement();
    }

    private static Document getDocument(File xmlFile) {
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(xmlFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

}
