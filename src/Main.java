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
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    private static String filePath = "location_name_table.csv";
    private static BufferedReader br = null;
    private static String line;


    public static void main(String[] args) throws IOException {


        ArrayList<Integer> temp = new ArrayList<>();

        GpsToLocal gpsToLocal = new GpsToLocal();
        GpsToLocal.LatXLngY gridLocation = gpsToLocal.convertGRID_GPS( 36.351914, 127.3007893);



        try{
            br = new BufferedReader(new FileReader(filePath));
            line = br.readLine();
            System.out.println(line);
            while((line = br.readLine()) != null) {
                String[] line_temp = line.split(",");
                if(Integer.parseInt(line_temp[0]) == gridLocation.x && Integer.parseInt(line_temp[1]) == gridLocation.y) {
                    System.out.println(line_temp[2]);
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }





        try {
            String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
            String serviceKey = "NhC%2FOgm81bAqynbgwtFbRfZESnH3%2FPsjF8RqxKIvooWfOrW5fVpRJB%2Bp7q6cFn8rpcEOapTpDvgq4KHUqjn3nA%3D%3D";
            String pageNo = "1";
            String numOfRows = "290";
            // 12 * 24 + 2(최고,최저기온상태) = 290
            String dataType = "XML";
            String base_data = findBaseTime()[0];
            String base_time = findBaseTime()[1];
            String nx = String.valueOf(gridLocation.x);
            String ny = String.valueOf(gridLocation.y);

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
                        int hour_temp = 0;
                        int hour_pop = 0;
                        int hour_reh = 0;
                        int hour = Integer.parseInt(item.item(4).getTextContent());
                        if(item.item(2).getTextContent().equals("TMP")) {
                            hour_temp = Integer.parseInt(item.item(5).getTextContent());
                            System.out.print("\t 시간 : " + hour + "\t 기온 : " + hour_temp);
                            temp.add(hour_temp);
                        }
                        if(item.item(2).getTextContent().equals("POP")) {
                            hour_pop = Integer.parseInt(item.item(5).getTextContent());
                            System.out.print("\t 강수확률 : " + hour_pop + "%");
                        }
                        if(item.item(2).getTextContent().equals("REH")) {
                            hour_reh = Integer.parseInt(item.item(5).getTextContent());
                            System.out.print("\t 습도 : " + hour_reh + "%\n");
                        }

                    }
                }
            }

            int max_temp = Collections.max(temp);
            System.out.println("\t 최고기온 시간 : "+ temp.indexOf(max_temp) + "\t 최고기온 : " + max_temp);

            int min_temp = Collections.min(temp);
            System.out.println("\t 최저기온 시간 : "+ temp.indexOf(min_temp) + "\t 최저기온 : " + min_temp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] findBaseTime() {
        long milli_now = System.currentTimeMillis();
        Date date_now = new Date(milli_now);
        SimpleDateFormat yearMonthDay = new SimpleDateFormat("yyyyMMdd");
        String formatYearMonthDay = yearMonthDay.format(date_now);
        SimpleDateFormat hour = new SimpleDateFormat("HH00");
        String formatHour = hour.format(date_now);

        String[] formatedNow = {formatYearMonthDay, formatHour};

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

    // 불쾌지수 구하는거 다시 해야됨

//    public static int findDI(int temp, int reh) {
//        System.out.println(temp + reh);
//        String str_roundReh = String.format("%.2f", reh/100.0);
//        double db_roundReh = Double.parseDouble(str_roundReh);
//        System.out.println("\n습도 / 100 = "+db_roundReh);
//        double temp18 = 1.8*temp;
//        System.out.println("온도 * 1.8 = "+temp18);
//        double di = (temp18-0.55*(1.0-db_roundReh)*(temp18-26))+32;
//        System.out.print(di);
//        int integerDI = (int) Math.round(di);
//        return integerDI;
//    }
}