package me.akashmaj.demomarketplaceservice;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;
import java.util.Map;

public class Order extends AbstractBehavior<Order.Command> {

   Integer orderId;
   Integer userId;
   String status;
   Integer totalPrice;
   List<Map<String, Integer>> itemsToOrder;

   public Order(ActorContext<Command> context, Integer orderId, Integer userId, String status, Integer totalPrice, List<Map<String, Integer>> itemsToOrder) {
        super(context);
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.itemsToOrder = itemsToOrder;
       System.out.println("INIT");
    }

    public static Behavior<Command> create(Integer orderId, Integer userId, String placed, Integer integer, List<Map<String, Integer>> itemsToOrder) {
        return Behaviors.setup(context -> new Order(context, orderId, userId, placed, integer, itemsToOrder));
   }

    public interface Command{

    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetOrder.class, this::onGetOrder)
                .onMessage(UpdateOrder.class, this::onUpdateOrder)
                .build();
    }

    private Behavior<Command> onUpdateOrder(UpdateOrder updateOrder) {
        Integer orderId = updateOrder.orderId;
        String status = updateOrder.status;
        ActorRef<Gateway.OrderInfo> replyTo = updateOrder.replyTo;
        if(this.status.equals("DELIVERED") || this.status.equals("CANCELLED")){

        }else if(this.status.equals("PLACED") && (status.equals("CANCELLED") || status.equals("DELIVERED"))){
            this.status = status;
        }
        if(replyTo != null)
            replyTo.tell(new Gateway.OrderInfo(null, orderId, this.userId, this.status, this.totalPrice, this.itemsToOrder, getContext().getSelf()));
        return this;
    }


    public static class UpdateOrder implements Command{
        public Integer orderId;
        public String status;
        public ActorRef<Gateway.OrderInfo> replyTo;

        public UpdateOrder(Integer orderId, String status, ActorRef<Gateway.OrderInfo> replyTo){
            this.orderId = orderId;
            this.status = status;
            this.replyTo = replyTo;
        }
    }

    public static class GetOrder implements Command{
        public Integer orderId;
        public ActorRef<Gateway.OrderInfo> replyTo;

        public GetOrder(Integer orderId, ActorRef<Gateway.OrderInfo> replyTo){
            this.orderId = orderId;
            this.replyTo = replyTo;
        }
    }

    private Behavior<Command> onGetOrder(GetOrder getOrder) {
        Integer orderId = getOrder.orderId;
        ActorRef<Gateway.OrderInfo> replyTo = getOrder.replyTo;

        replyTo.tell(new Gateway.OrderInfo(null, orderId, this.userId, this.status, this.totalPrice, this.itemsToOrder, getContext().getSelf()));
        return this;
    }


}
