package com.android.bingmaps;

import android.content.Context;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * KMLReader class responsible for reading and parsing KML files.
 */
public class KMLReader {
    private Context context;
    private Map<String, String> styleMap;

    /**
     * Constructor for KMLReader.
     *
     * @param context The application context.
     */
    public KMLReader(Context context) {
        this.context = context;
        this.styleMap = new HashMap<>();
    }

    /**
     * Reads and parses a KML file from the given InputStream.
     * Extracts placemark information and returns a list of Placemarks.
     *
     * @param inputStream The InputStream of the KML file.
     * @return A list of Placemarks parsed from the KML file.
     */
    public ArrayList<Placemark> readKMLFile(InputStream inputStream) {
        ArrayList<Placemark> result = new ArrayList<>();

        try {
            // Create a new DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Create a new DocumentBuilder
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the KML file
            Document document = builder.parse(inputStream);

            // Normalize the XML Structure
            document.getDocumentElement().normalize();

            // First pass: Collect styles
            NodeList styleList = document.getElementsByTagName("Style");
            for (int i = 0; i < styleList.getLength(); i++) {
                Node styleNode = styleList.item(i);
                if (styleNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element styleElement = (Element) styleNode;
                    String styleId = styleElement.getAttribute("id");
                    String color = "";
                    NodeList colorList = styleElement.getElementsByTagName("color");
                    if (colorList != null && colorList.getLength() > 0) {
                        Node colorNode = colorList.item(0);
                        if (colorNode != null) {
                            color = colorNode.getTextContent();
                        }
                    }
                    styleMap.put("#" + styleId, "#" + color);
                }
            }

            // Collect style maps
            NodeList styleMapList = document.getElementsByTagName("StyleMap");
            for (int i = 0; i < styleMapList.getLength(); i++) {
                Node styleMapNode = styleMapList.item(i);
                if (styleMapNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element styleMapElement = (Element) styleMapNode;
                    String styleMapId = styleMapElement.getAttribute("id");
                    NodeList pairList = styleMapElement.getElementsByTagName("Pair");
                    for (int j = 0; j < pairList.getLength(); j++) {
                        Node pairNode = pairList.item(j);
                        if (pairNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element pairElement = (Element) pairNode;
                            String key = pairElement.getElementsByTagName("key").item(0).getTextContent();
                            String styleUrl = pairElement.getElementsByTagName("styleUrl").item(0).getTextContent();
                            if ("normal".equals(key)) {
                                styleMap.put("#" + styleMapId, styleUrl);
                            }
                        }
                    }
                }
            }

            // Second pass: Collect placemarks
            NodeList folderList = document.getElementsByTagName("Folder");
            // Iterate through the folders
            for (int i = 0; i < folderList.getLength(); i++) {
                Node folderNode = folderList.item(i);
                if (folderNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element folderElement = (Element) folderNode;
                    String categoryName = folderElement.getElementsByTagName("name").item(0).getTextContent();

                    // Add category name to the result list
                    result.add(new Placemark(categoryName, "", "", ""));

                    // Get all Placemark elements within the folder
                    NodeList placemarkList = folderElement.getElementsByTagName("Placemark");
                    for (int j = 0; j < placemarkList.getLength(); j++) {
                        Node placemarkNode = placemarkList.item(j);
                        if (placemarkNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element placemarkElement = (Element) placemarkNode;
                            String name = (placemarkElement.getElementsByTagName("name").item(0) != null)
                                    ? placemarkElement.getElementsByTagName("name").item(0).getTextContent()
                                    : "";
                            String description = (placemarkElement.getElementsByTagName("description").item(0) != null)
                                    ? placemarkElement.getElementsByTagName("description").item(0).getTextContent()
                                    : "";
                            String coordinates = (placemarkElement.getElementsByTagName("coordinates").item(0) != null)
                                    ? placemarkElement.getElementsByTagName("coordinates").item(0).getTextContent()
                                    : "";
                            String styleUrl = (placemarkElement.getElementsByTagName("styleUrl").item(0) != null)
                                    ? placemarkElement.getElementsByTagName("styleUrl").item(0).getTextContent()
                                    : "";

                            // Resolve the styleUrl to an actual color
                            String resolvedColor = styleMap.get(styleUrl);
                            if (resolvedColor != null && resolvedColor.startsWith("#")) {
                                resolvedColor = styleMap.get(resolvedColor);
                            }

                            Placemark placemark = new Placemark(name, description, coordinates, resolvedColor);
                            Log.i("KML_LOG", "KML: " + placemark.getName() + "   **   " + placemark.getColor());
                            // Add placemark to the result list
                            result.add(placemark);
                        }
                    }
                }
            }

            // Close the input stream
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("KML_LOG", "KML Error: " + e.getMessage());
        }
        return result;
    }
}
