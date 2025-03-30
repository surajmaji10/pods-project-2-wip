package me.akashmaj.demomarketplaceservice;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class Product extends AbstractBehavior<Product.Command> {

    Integer productId;
    String productName;
    String productDescription;
    Integer productPrice;
    Integer productStockQuantity;

    public static Behavior<Product.Command> create(Integer productId,
                                                   String productName,
                                                    String productDescription,
                                                   Integer productPrice,
                                                   Integer productStockQuantity) {
        System.out.println("Created Product ID: " + productId);
        System.out.println("Product Stock: " + productStockQuantity);
        return Behaviors.setup(context -> new Product(context, productId, productName, productDescription, productPrice, productStockQuantity));
    }

    public Product(ActorContext<Command> context) {
        super(context);
    }

    public Product(ActorContext<Command> context,
                   Integer productId,
                   String productName,
                   String productDescription,
                   Integer productPrice,
                   Integer productStockQuantity) {
        super(context);
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productPrice = productPrice;
        this.productStockQuantity = productStockQuantity;
    }

    @Override
    public Receive<Product.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetProductById.class, this::onGetProductById)
                .onMessage(ProductUpdateRequest.class, this::onProductUpdateRequest)
                .build();
    }

    public interface Command {

    }

    public static class GetProductById implements Command {
        public Integer productId;
        public ActorRef<Gateway.ProductInfoResponse> replyTo;

        public GetProductById(Integer productId, ActorRef<Gateway.ProductInfoResponse> replyTo) {
            this.productId = productId;
            this.replyTo = replyTo;
        }
    }

    private Behavior<Product.Command> onGetProductById(GetProductById getProductById) {
        Integer productId = getProductById.productId;
        ActorRef<Gateway.ProductInfoResponse> replyTo = getProductById.replyTo;

        replyTo.tell(new Gateway.ProductInfoResponse(null, productId, this.productName, this.productDescription, this.productPrice, this.productStockQuantity));
        return this;
    }

    public static class ProductUpdateRequest implements Command {
        public int productId;
        public int productStockQuantity;
        public ActorRef<Gateway.ProductInfoResponse> replyTo;

        public ProductUpdateRequest(ActorRef<Gateway.ProductInfoResponse> replyTo,
                                    Integer productId,
                                    Integer productStockQuantity) {
            this.productId = productId;

            this.productStockQuantity = productStockQuantity;
            this.replyTo = replyTo;
        }
    }

    private Behavior<Command> onProductUpdateRequest(ProductUpdateRequest productUpdateRequest) {
        System.out.println(this.productStockQuantity);
        this.productStockQuantity += productUpdateRequest.productStockQuantity;
        System.out.println(this.productStockQuantity);
        ActorRef<Gateway.ProductInfoResponse> replyTo = productUpdateRequest.replyTo;
        if(replyTo != null)
            replyTo.tell(new Gateway.ProductInfoResponse(null, productId, this.productName, this.productDescription, this.productPrice, this.productStockQuantity));
        return this;
    }


    public static class ProductInfoRequest implements Command {
        public final int productId;
        public final ActorRef<Gateway.Command> replyTo;

        public ProductInfoRequest(ActorRef<Gateway.Command> replyTo, Integer productId) {
            this.productId = productId;
            this.replyTo = replyTo;
        }
    }

}
