package edu.uw.info314.xmlrpc.server;

import java.util.*;
import java.util.logging.*;

import static spark.Spark.*;

class Call {
    public String name;
    public List<Object> args = new ArrayList<Object>();
}

public class App {
    public static final Logger LOG = Logger.getLogger(App.class.getCanonicalName());
    public static void main(String[] args) {
        LOG.info("Starting up on port 8080");
        port(8080);

        before((req, res) -> {
            if (!req.uri().equals("/RPC")) {
                halt(404, "Not found");
            }
            if (!req.requestMethod().equals("POST")) {
                halt(405, "Method not supported");
            }
            res.header("Host", req.host());
        });

        post("/RPC", (request, response) -> { 
            try {
                response.status(200); 
                response.type("text/xml");

                String xml = request.body();
                LOG.info("Received XML: " + xml);
    
                Call call = extractXMLRPCCall(xml);
                int[] params = call.args.stream().mapToInt(arg -> Integer.parseInt(arg.toString())).toArray();

                if(!xml.contains("<i4>") && !call.name.equals("add") && !call.name.equals("multiply")) {
                    return createFaultXML(3, "illegal argument type");
                }

                Calc calculator = new Calc();
                switch(call.name) {
                    case "add":
                        return buildResponseBody(calculator.add(params));
                    case "subtract":
                        return buildResponseBody(calculator.subtract(params[0], params[1]));
                    case "multiply":
                        return buildResponseBody(calculator.multiply(params));
                    case "divide":
                        if (params[1] == 0) {
                            return createFaultXML(1, "divide by zero");
                        }
                        return buildResponseBody(calculator.divide(params[0], params[1]));
                    case "modulo":
                        if (params[1] == 0) {
                            return createFaultXML(1, "divide by zero");
                        }
                        return buildResponseBody(calculator.modulo(params[0], params[1]));
                    default:
                        return 0;
                }
            } catch (Exception e) {
                LOG.warning("Error handling XML-RPC call: " + e.getMessage());
                response.status(500);
                response.body("Internal Server Error");
                return "";
            }
        });
    }

    public static Call extractXMLRPCCall(String xml) throws Exception {
        Call call = new Call();

        // extract method name
        int start = xml.indexOf("<methodName>") + 12; 
        int end = xml.indexOf("</methodName>");
        String methodName = xml.substring(start, end);
        call.name = methodName;

        // extract i4 values
        start = xml.indexOf("<i4>");
        end = xml.indexOf("</i4>");
        while (start >= 0 && end >= 0) {
            String i4Value = xml.substring(start + 4, end); 
            call.args.add(i4Value);
            start = xml.indexOf("<i4>", end); // find next occurrence of <i4>
            end = xml.indexOf("</i4>", end + 4);
        }
        return call;
    }

    private static String buildResponseBody(int answer) {
        String requestBody = "<?xml version=\"1.0\"?><methodResponse>";
        requestBody += "<params><param><value><i4>" + answer + "</i4></value></param></params></methodResponse>";
        return requestBody;
    }

    private static String createFaultXML(int faultCode, String faultString) {
        String xml = "<?xml version=\"1.0\"?><methodResponse><fault><value><struct>" +
                     "<member><name>faultCode</name><value><int>" + faultCode + "</int></value></member>" +
                     "<member><name>faultString</name><value><string>" + faultString + "</string></value></member>" +
                     "</struct></value></fault></methodResponse>";
        return xml;
    }
}
