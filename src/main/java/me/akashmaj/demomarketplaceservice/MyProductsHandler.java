package me.akashmaj.demomarketplaceservice;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

class MyProductsHandler implements HttpHandler {

    public ActorSystem<Gateway.Command> system;
    public ActorRef<Gateway.Command> gateway;
    public Duration askTimeout;
    public Scheduler scheduler;

    public MyProductsHandler(ActorRef<Gateway.Command> gateway, Duration askTimeout, Scheduler scheduler) {
        this.gateway = gateway;
        this.askTimeout = askTimeout;
        this.scheduler = scheduler;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        String path = t.getRequestURI().getPath();
        String m = t.getRequestMethod();
        System.out.println("MyProductsHandler PATH: " + path);

        String[] parts = path.split("/");
        System.out.println("MyProductsHandler PATH Length: " + parts.length);

        if (parts.length == 3 && parts[1].equals("products") && m.equals("GET")) {
            try {

                int productId = Integer.parseInt(parts[2]);
                System.out.println(productId);

                CompletionStage<Gateway.ProductInfoResponse> compl = AskPattern.ask(
                        gateway,
                        (ActorRef<Gateway.ProductInfoResponse> ref) -> new Gateway.GetProduct(productId, ref),
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
                        jsonResponse = String.format("{\"id\": %d, \"name\": \"%s\", \"price\": %d, \"stockQuantity\": %d}",
                                response.productId, response.productName, response.productPrice, response.productStockQuantity);
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

            } catch (NumberFormatException e) {
                t.sendResponseHeaders(400, 0);
                t.getResponseBody().close();
            }
        }else  if (parts.length == 4 && parts[1].equals("products") && m.equals("PUT")) {
            try {

                int productId = Integer.parseInt(parts[2]);
                int stockQuantity = Integer.parseInt(parts[3]);
                System.out.println(productId);

                CompletionStage<Gateway.ProductInfoResponse> compl = AskPattern.ask(
                        gateway,
                        (ActorRef<Gateway.ProductInfoResponse> ref) -> new Gateway.UpdateProduct(ref, productId, stockQuantity),
                        askTimeout,
                        scheduler
                );

                compl.thenAccept(response -> {

                    String jsonResponse = null;
                    try {
                        jsonResponse = response.toJson();
                    } catch (Exception e) {
                        jsonResponse = String.format("{\"id\": %d, \"name\": \"%s\", \"price\": %d, \"stockQuantity\": %d}",
                                response.productId, response.productName, response.productPrice, response.productStockQuantity);
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

            }catch (NumberFormatException e) {
                t.sendResponseHeaders(400, 0);
                t.getResponseBody().close();
            }
        }
        else {
            t.sendResponseHeaders(404, 0);
            t.getResponseBody().close();
        }
    }
}

