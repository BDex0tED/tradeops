package com.tradeops.service;

import com.tradeops.model.entity.Order;
import com.tradeops.model.entity.OrderStatus;
import com.tradeops.model.request.CreateOrderRequest;

public interface OrderService {

    // FR-023: Создание заказа с витрины (Резервирует склад, считает сумму, ставит статус NEW)
    Order createOrder(CreateOrderRequest request);

    // FR-026, FR-027: Перевод заказа по статусам (NEW -> ASSIGNED -> ON_PROGRESS -> COMPLETED)
    Order changeOrderStatus(Long orderId, OrderStatus newStatus, Long actorId);

    // В будущем тут добавим методы для получения списка заказов для админов
}