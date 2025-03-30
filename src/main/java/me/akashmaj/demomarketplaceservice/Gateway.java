package me.akashmaj.demomarketplaceservice;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Gateway extends AbstractBehavior<Gateway.Command> {

    /* will store productId to product ActorRef mapping */
    public Map<Integer, ActorRef<Product.Command>> productsRef;

    /* will help to populate the product actors from excel file*/
    ProductsPopulator productsPopulator;

    public static Behavior<Gateway.Command> create() {
        return Behaviors.setup(Gateway::new);
    }

    private Gateway(ActorContext<Command> context) {
        super(context);

        productsRef = new HashMap<>();
        productsPopulator = new ProductsPopulator();
        productsPopulator.processExcelFile();

        for(int i = 0; i < productsPopulator.products; i++) {
            Integer productId = productsPopulator.productIds.get(i);
            String productName = productsPopulator.productNames.get(i);
            String productDescription = productsPopulator.productDescriptions.get(i);
            Integer productPrice = productsPopulator.productPrices.get(i);
            Integer productStockQuantity = productsPopulator.productStockQuantitys.get(i);

            ActorRef<Product.Command> productActor = context.spawn(Product.create(productId, productName, productDescription, productPrice, productStockQuantity), "Product-" + productId);
            productsRef.put(productId, productActor);
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetProduct.class, this::onGetProduct)
                .onMessage(ProductInfoResponse.class, this::onProductInfoResponse)
                .build();
    }

    private Behavior<Command> onGetProduct(GetProduct getProduct) {
        Optional<ActorRef<Product.Command>> product = Optional.ofNullable(productsRef.get(getProduct.productId));

//        if (product.isPresent()) {
//            System.out.println("Product with ID: " + getProduct.productId + " found");
//            product.get().tell(new Product.ProductInfoRequest(getContext().getSelf(), getProduct.productId));
//
//        } else {
//            getProduct.replyTo.tell(new ProductInfoResponse(null, -1, "Not Found", "Not Found", -1, -1 ));
//        }

        if (product.isPresent()) {
            System.out.println("TRU");

            // Ask the product actor for its details
            CompletionStage<ProductInfoResponse> productDetails = AskPattern.ask(
                    product.get(),
                    (ActorRef<ProductInfoResponse> replyTo) -> new Product.GetProductById(getProduct.productId, replyTo),
                    Duration.ofSeconds(3),
                    getContext().getSystem().scheduler()
            );

            productDetails.thenAccept(info -> {
                System.out.println("INSIDE");
                getProduct.replyTo.tell(new ProductInfoResponse(getContext().getSelf(),
                        info.productId, info.productName,
                        info.productDescription, info.productStockQuantity,
                        info.productPrice));
            });

        } else {
            getProduct.replyTo.tell(new ProductInfoResponse(getContext().getSelf(), -1, "Not Found", "Not Found", 1, 1));
        }

        return this;
    }


    private Behavior<Command> onProductInfoResponse(ProductInfoResponse productInfoResponse){
        System.out.println("Product ID: " + productInfoResponse.productId);
        System.out.println("Product Name: " + productInfoResponse.productName);
        System.out.println("Product Description: " + productInfoResponse.productDescription);
        System.out.println("Product Price: " + productInfoResponse.productPrice);
        System.out.println("Product Stock Quantity: " + productInfoResponse.productStockQuantity);
        return this;
    }

    public interface Command {}

    public static class GetProduct implements Command {
        public final int productId;
        public final ActorRef<ProductInfoResponse> replyTo;

        public GetProduct(int productId, ActorRef<ProductInfoResponse> replyTo) {
            this.productId = productId;
            this.replyTo = replyTo;
        }
    }


    public static class ProductInfoResponse implements Command {
        @JsonProperty("id")
        public  Integer productId;
        @JsonProperty("name")
        public  String productName;
        @JsonProperty("description")
        public  String productDescription;
        @JsonProperty("price")
        public  Integer productPrice;
        @JsonProperty("stockQuantity")
        public  Integer productStockQuantity;
        @JsonIgnore
        public  ActorRef<Gateway.Command> replyTo;

        public ProductInfoResponse(ActorRef<Gateway.Command> replyTo, Integer productId, String productName, String productDescription, Integer productPrice, Integer productStockQuantity) {
            this.productId = productId;
            this.productName = productName;
            this.productDescription = productDescription;
            this.productPrice = productPrice;
            this.productStockQuantity = productStockQuantity;
            this.replyTo = replyTo;
        }

        public String toJson() throws Exception {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        }

    }
}
