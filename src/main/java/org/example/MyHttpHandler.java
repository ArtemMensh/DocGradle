package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.Collectors;

public class MyHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String requestParamValue = null;

        if ("GET".equals(httpExchange.getRequestMethod())) {

            httpExchange.sendResponseHeaders(404, "Server Error".length());
            OutputStream outputStream = httpExchange.getResponseBody();

            outputStream.write("Server Error".getBytes());
            outputStream.flush();
            outputStream.close();
        } else if ("POST".equals(httpExchange.getRequestMethod())) {
            try {
                handleResponse(httpExchange, handlePostParam(httpExchange));
            } catch (Exception e) {
                System.out.println(e);
                httpExchange.sendResponseHeaders(404, e.toString().length());
                OutputStream outputStream = httpExchange.getResponseBody();

                outputStream.write(e.toString().getBytes());
                outputStream.flush();
                outputStream.close();
                throw new RuntimeException(e);
            }
        }

    }

    private String handlePostParam(HttpExchange httpExchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        return br.lines().collect(Collectors.joining());

    }

    private String handleGetRequest(HttpExchange httpExchange) {
        return httpExchange.
                getRequestURI()
                .toString()
                .split("\\?")[1]
                .split("=")[1];
    }

    private void handleResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {
        JSONObject o = (JSONObject) JSONValue.parse(requestParamValue);

        String pathDocFile = "C:/Users/men19/Downloads/weights268/Sample.docx";
//        String pathDocFile = "/dewt/Sampledoc/Sample.docx";

        String pathToNewFile = "C:/Users/men19/Downloads/weights268/";
//        String pathToNewFile = "/dewt/static/doc/";


        File file = new File("C:/Users/men19/Downloads/weights268/Sample.docx");
        FileInputStream fis = new FileInputStream(file.getAbsolutePath());
        XWPFDocument document = new XWPFDocument(fis); // Вот и объект описанного нами класса
        String documentLine = document.getDocument().toString();

        JSONObject data = (JSONObject)o.get("data");
        String nameDocument = data.get("name").toString();

        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER) ;
        XWPFRun run = title.createRun();
        run.setFontSize(18);
        run.setFontFamily("Times New Roman");
        run.setText(Decode(nameDocument));
        run.addBreak();


        JSONArray items = (JSONArray) data.get("items");

        for (Object item : items) {
            JSONObject content = (JSONObject)((JSONObject)item).get("content");
            if(content == null) continue;
            String txt = content.get("txt").toString();
            String txt2 = content.get("txt2").toString();

            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setIndentFromLeft(20);
            run = paragraph.createRun();
            run.setFontSize(12);
            run.setFontFamily("Times New Roman");
            run.setBold(true);
            run.setText(Decode("Этап " + txt));
            run.setBold(false);
            run.setText(txt2);
            run.addBreak();
        }

        Random rand = new Random();
        String nameNewFile = "Doc" + "-" + rand.nextInt(1000) + ".docx";
        document.write(new FileOutputStream(new File(pathToNewFile + nameNewFile)));

        httpExchange.sendResponseHeaders(200, (pathToNewFile + nameNewFile).length());
        OutputStream outputStream = httpExchange.getResponseBody();

        outputStream.write((pathToNewFile + nameNewFile).getBytes());
        outputStream.flush();
        outputStream.close();
        System.getProperties();
    }


    private String Decode(String string) throws UnsupportedEncodingException {
        return URLDecoder.decode(URLEncoder.encode(new String(string.getBytes(), StandardCharsets.UTF_8), "UTF-8"), "UTF-8");
    }

}
