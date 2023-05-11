import java.io.*;
import java.net.*;
import java.net.http.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import org.w3c.dom.Node;

/**
 * This approach uses the java.net.http.HttpClient classes, which
 * were introduced in Java11.
 */
public class Client {
    
    static String host;
    static int PORT; 
    public static void main(String... args) throws Exception {
        host = args[0];
        PORT = Integer.parseInt(args[1]);

        // Base cases
        System.out.println(add() == 0);
        System.out.println(add(1, 2, 3, 4, 5) == 15);
        System.out.println(add(2, 4) == 6);
        System.out.println(subtract(12, 6) == 6);
        System.out.println(multiply(3, 4) == 12);
        System.out.println(multiply(1, 2, 3, 4, 5) == 120);
        System.out.println(divide(10, 5) == 2);
        System.out.println(modulo(10, 5) == 0);

        // Error cases
        try{
            System.out.println(add(999999999, 1000000000));
        }catch(Exception e) {
            throw new ArithmeticException("Error: Overflow!");
        }
        try {
            multiply(999999999, 1000000);
        }catch(Exception e) {
            throw new ArithmeticException("Error: Overflow!");
        }

        // Wouldn't compile
        // try {
        //     subtract("abc", "def");
        // } catch (Exception e) {
        //     throw new IllegalArgumentException("Error: Can't subtract strings")
        // }

        try {
            divide(1, 0);
        }catch(Exception e) {
            throw new ArithmeticException("Error: Divide by zero");
        }

    }
    public static int add(int lhs, int rhs) throws Exception {
        return makeRequest("add", lhs, rhs);
    }
    public static int add(Integer... params) throws Exception {
        return makeRequest("add", params);
    }
    public static int subtract(int lhs, int rhs) throws Exception {
        return makeRequest("subtract", lhs, rhs);
    }
    public static int multiply(int lhs, int rhs) throws Exception {
        return makeRequest("multiply", lhs, rhs);
    }
    public static int multiply(Integer... params) throws Exception {
        return makeRequest("multiply", params);
    }
    public static int divide(int lhs, int rhs) throws Exception {
        return makeRequest("divide", lhs, rhs);
    }
    public static int modulo(int lhs, int rhs) throws Exception {
        return makeRequest("modulo", lhs, rhs);
    }


    private static int makeRequest(String method, Integer... params) {
        
        HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build();

        String requestBody = "<?xml version=\"1.0\"?>" + buildRequestBody(method, params);

        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://" + host + ":" + PORT + "/RPC"))
        .header("Content-Type", "text/xml")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();

        String answerXML = "";
        int i4Value = 0;
        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            answerXML = response.body();
            int start = answerXML.indexOf("<i4>");
            int end = answerXML.indexOf("</i4>");
            i4Value = Integer.parseInt(answerXML.substring(start + 4, end)); 

        } catch (Exception e) {
            e.printStackTrace();
        }
        return i4Value;
    }

    private static String buildRequestBody(String method, Integer... params) {
        String requestBody = "<methodCall>";
        requestBody += "<methodName>" + method + "</methodName>";
        requestBody += "<params>";
        for (int i = 0; i < params.length; i++) {
            requestBody += "<param><value><i4>" + params[i] + "</i4></value></param>";
        }
        requestBody += "</params>";
        requestBody += "</methodCall>";
        return requestBody;
    }
}
