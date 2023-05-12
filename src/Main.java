import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {

        ArrayList<Integer> temp = new ArrayList<>();

        try {
            String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
            String serviceKey = "NhC%2FOgm81bAqynbgwtFbRfZESnH3%2FPsjF8RqxKIvooWfOrW5fVpRJB%2Bp7q6cFn8rpcEOapTpDvgq4KHUqjn3nA%3D%3D";
            String pageNo = "1";
            String numOfRows = "288";
            String dataType = "XML";
            String base_data = findBaseDay();
            String base_time = "2300";
            String nx = "55";
            String ny = "127";

            String final_url = url + "?serviceKey=" + serviceKey + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows + "&dataType=" + dataType + "&base_date=" + base_data + "&base_time=" + base_time + "&nx=" + nx + "&ny=" + ny;

            URL obj = new URL(final_url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = (Document) documentBuilder.parse(new InputSource(connection.getInputStream()));
            document.getDocumentElement().normalize();

            System.out.println(docToString(document));

            Element element = document.getDocumentElement();
            System.out.println(element.getNodeName());

            NodeList body_list = element.getChildNodes().item(1).getChildNodes();
            NodeList item_list = body_list.item(1).getChildNodes();
            System.out.println(item_list.getLength());

            if(item_list.getLength() > 0) {
                for (int i = 0; i < item_list.getLength(); i++) {
                    NodeList item = item_list.item(i).getChildNodes();
                    if(item.getLength() > 0) {
//                        for (int j = 0; j < item.getLength(); j++) {
//                            if(item.item(j).getNodeName().equals("#text")==false)
//                                System.out.println("\t xml tag name : " + item.item(j).getNodeName() + ", xml value : " + item.item(j).getTextContent());
//                        }
                        if(item.item(2).getTextContent().equals("TMP")) {
                            System.out.println("\t time : " + item.item(4).getTextContent() + "\t temp : " + item.item(5).getTextContent());
                            temp.add(Integer.parseInt(item.item(5).getTextContent()));
                        }
                    }
                }
            }

            int max_temp = Collections.max(temp);
            System.out.println("\t 최고기온 시간 : "+ max_temp + "\t 최고기온 : " + temp.indexOf(max_temp));

            int min_temp = Collections.min(temp);
            System.out.println("\t 최고기온 시간 : "+ min_temp + "\t 최고기온 : " + temp.indexOf(min_temp));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String findBaseDay() {
        LocalDate now = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formatedNow = now.format(formatter);
        return formatedNow;
    }

    public static String docToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.getBuffer().toString();
    }
}