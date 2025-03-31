package me.akashmaj.demomarketplaceservice;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

class MyOrdersHandler implements HttpHandler {

    public ActorSystem<Gateway.Command> system;
    public ActorRef<Gateway.Command> gateway;
    public Duration askTimeout;
    public Scheduler scheduler;

    public MyOrdersHandler(ActorRef<Gateway.Command> gateway, Duration askTimeout, Scheduler scheduler) {
        this.gateway = gateway;
        this.askTimeout = askTimeout;
        this.scheduler = scheduler;
    }

    public static List<Map<String, Integer>> processOrderItems(String requestBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse the JSON request body
        JsonNode jsonNode = objectMapper.readTree(requestBody);

        // Extract "user_id"
        int userId = jsonNode.get("user_id").asInt();

        // Extract "items" as a List of Maps
        List<Map<String, Integer>> itemsToOrder = objectMapper.readValue(
                jsonNode.get("items").toString(),
                new TypeReference<List<Map<String, Integer>>>() {
                }
        );

        // Print results
        System.out.println("User ID: " + userId);
        System.out.println("Items to Order: " + itemsToOrder);
        return itemsToOrder;

    }

    public static int processOrderUser(String requestBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse the JSON request body
        JsonNode jsonNode = objectMapper.readTree(requestBody);

        // Extract "user_id"
        int userId = jsonNode.get("user_id").asInt();
        return userId;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        String path = t.getRequestURI().getPath();
        String m = t.getRequestMethod();
        System.out.println("MyOrdersHandler PATH: " + path);

        String[] parts = path.split("/");
        System.out.println("MyOrdersHandler PATH Length: " + parts.length);

        if (parts.length == 2 && parts[1].equals("orders") && m.equals("POST")) {
            try {

                // read request body into a map
                String requestBody = new String(t.getRequestBody().readAllBytes());
                List<Map<String, Integer>> itemsToOrder = processOrderItems(requestBody);
                Integer userId = processOrderUser(requestBody);

                CompletionStage<Gateway.OrderInfo> compl = AskPattern.ask(
                        gateway,
                        (ActorRef<Gateway.OrderInfo> ref) -> new Gateway.PlaceOrder(userId, itemsToOrder, ref),
                        askTimeout,
                        scheduler
                );

                compl.thenAccept(response -> {

//                    String jsonResponse = String.format("{\"id\": %d, \"name\": \"%s\", \"price\": %d, \"stockQuantity\": %d}",
//                            response.productId, response.productName, response.productPrice, response.productStockQuantity);
                    String jsonResponse = null;
                    try {
                        jsonResponse = response.toJson();
                    } catch (Exception e) {
                        jsonResponse = String.format("{\"id\": %d, \"status\": \"%s\"}",
                                response.orderId, response.orderStatus);
                    }
                    int responseCode = 201;
                    if(response.orderStatus.equals("FAILED")){
                        responseCode = 400;
                    }

                    try {
                        t.getResponseHeaders().set("Content-Type", "application/json");
                        t.sendResponseHeaders(responseCode, jsonResponse.length());
                        OutputStream os = t.getResponseBody();
                        os.write(jsonResponse.getBytes());
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });

            } catch (NumberFormatException e) {
                t.sendResponseHeaders(400, 0);
                t.getResponseBody().close();
            }
        } else if (parts.length == 3 && parts[1].equals("orders") && m.equals("PUT")) {

            try {

                int orderId = Integer.parseInt(parts[2]);
                System.out.println(orderId);

                // read request body into a map
                String requestBody = new String(t.getRequestBody().readAllBytes());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(requestBody);
                String status = jsonNode.get("status").asText();
                CompletionStage<Gateway.OrderInfo> compl = AskPattern.ask(
                        gateway,
                        (ActorRef<Gateway.OrderInfo> ref) -> new Gateway.UpdateOrder(orderId, status , ref),
                        askTimeout,
                        scheduler
                );

                compl.thenAccept(response -> {
                    String jsonResponse = null;
                    try {
                        jsonResponse = response.toJson();
                        System.out.println(jsonResponse);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        t.getResponseHeaders().set("Content-Type", "application/json");
                        t.sendResponseHeaders(200, jsonResponse.length());

                        OutputStream os = t.getResponseBody();
                        os.write(jsonResponse.getBytes());
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }catch (Exception e) {
                t.sendResponseHeaders(400, 0);
                t.getResponseBody().close();
            }

        } else if (parts.length == 3 && parts[1].equals("orders") && m.equals("GET")) {
            try {

                int orderId = Integer.parseInt(parts[2]);
                System.out.println(orderId);

                CompletionStage<Gateway.OrderInfo> compl = AskPattern.ask(
                        gateway,
                        (ActorRef<Gateway.OrderInfo> ref) -> new Gateway.GetOrder(orderId, ref),
                        askTimeout,
                        scheduler
                );

                compl.thenAccept(response -> {
                    String jsonResponse = null;
                    try {
                        jsonResponse = response.toJson();
                        System.out.println(jsonResponse);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        t.getResponseHeaders().set("Content-Type", "application/json");
//                        t.sendResponseHeaders(200, jsonResponse.length());
                        t.getResponseHeaders().set("Content-Type", "application/json");
                        OutputStream os = t.getResponseBody();
                        os.write(jsonResponse.getBytes());
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                });
            } catch (Exception e) {
                t.sendResponseHeaders(400, 0);
                t.getResponseBody().close();
            }

        } else if(parts.length == 3 && parts[1].equals("orders") && m.equals("DELETE")) {
            try {

                int orderId = Integer.parseInt(parts[2]);
                System.out.println(orderId);

                CompletionStage<Gateway.OrderInfo> compl = AskPattern.ask(
                        gateway,
                        (ActorRef<Gateway.OrderInfo> ref) -> new Gateway.CancelOrder(orderId, null, ref),
                        askTimeout,
                        scheduler
                );

                compl.thenAccept(response -> {
                    String jsonResponse = null;
                    try {
                        jsonResponse = response.toJson();
                        System.out.println(jsonResponse);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        t.getResponseHeaders().set("Content-Type", "application/json");
                        t.sendResponseHeaders(200, jsonResponse.length());
//                        t.getResponseHeaders().set("Content-Type", "application/json");
                        OutputStream os = t.getResponseBody();
                        os.write(jsonResponse.getBytes());
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
            }catch (Exception e) {
                t.sendResponseHeaders(400, 0);
                t.getResponseBody().close();
            }
        }
        else {
            t.sendResponseHeaders(404, 0);
        }
    }
}

