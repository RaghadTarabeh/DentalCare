package Control;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import Entity.InventoryItem;
import Entity.Supplier;
import Entity.XMLData;

public class XMLImporter {

    public static List<InventoryItem> loadInventoryItemsFromXML(String filePath) {
        List<InventoryItem> items = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(filePath));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("InventoryItem");

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    InventoryItem item = new InventoryItem(
                        Integer.parseInt(e.getElementsByTagName("InventoryItemID").item(0).getTextContent()),
                        e.getElementsByTagName("ItemName").item(0).getTextContent(),
                        e.getElementsByTagName("Description").item(0).getTextContent(),
                        Integer.parseInt(e.getElementsByTagName("CategoryID").item(0).getTextContent()),
                        Integer.parseInt(e.getElementsByTagName("QuantityInStock").item(0).getTextContent()),
                        Integer.parseInt(e.getElementsByTagName("SupplierID").item(0).getTextContent()),
                        LocalDate.parse(e.getElementsByTagName("ExpirationDate").item(0).getTextContent()),
                        e.getElementsByTagName("SerialNumber").item(0).getTextContent()
                    );
                    items.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public static List<Supplier> loadSuppliersFromXML(String filePath) {
        List<Supplier> suppliers = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(filePath));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Supplier");

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    Supplier supplier = new Supplier(
                        Integer.parseInt(e.getElementsByTagName("SupplierID").item(0).getTextContent()),
                        e.getElementsByTagName("SupplierName").item(0).getTextContent(),
                        e.getElementsByTagName("ContactInformation").item(0).getTextContent()
                    );
                    suppliers.add(supplier);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return suppliers;
    }
}
