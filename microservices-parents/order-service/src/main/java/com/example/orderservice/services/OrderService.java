package com.example.orderservice.services;

import com.example.orderservice.dto.OrderLineItemsDto;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderLineItems;
import com.example.orderservice.repository.OrderRepository;
import com.example.inventoryservice.dto.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.reflect.Array.*;
import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private  final OrderRepository orderRepository;
    /** Use the WebClient Bean in the Order Service*/
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                //.map(orderLineItemsDto -> mapToDto(orderLineItemsDto));
                .map(this::mapToDto)
                .collect(Collectors.toList());

        order.setOrderLineItemsList(orderLineItems);

        /** Collect all the sku-code as a List
         * enhance this in the API call */
        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .collect(Collectors.toList());

        /** Call Inventory Service, place order if product is in stock*/
        InventoryResponse[] inventoryResponseArray = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();
        boolean allProductInStock = stream(inventoryResponseArray)
                        .allMatch(InventoryResponse::isInStock);
        if(allProductInStock){
            orderRepository.save(order);
        }else{
            throw new IllegalStateException("Produce is not in stock");
        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
