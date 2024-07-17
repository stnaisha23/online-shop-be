package com.siti.orderitem.Services;

import com.siti.orderitem.Models.Customer;
import com.siti.orderitem.Models.Items;
import com.siti.orderitem.Models.Orders;
import com.siti.orderitem.Repositories.CustomerRepository;
import com.siti.orderitem.Repositories.ItemRepository;
import com.siti.orderitem.Repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class OrdersService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public Page<Orders> getAllOrders(int page, int size) {
        return orderRepository.findAllWithCustomerAndItems(PageRequest.of(page, size));
    }


    @Transactional(readOnly = true)
    public Optional<Orders> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional
    public Orders addOrder(Orders newOrder, Long customerId, Long itemsId) throws Exception {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new Exception("Customer not found with id: " + customerId));

        Items item = itemRepository.findById(itemsId)
                .orElseThrow(() -> new Exception("Item not found with id: " + itemsId));

        newOrder.setCustomer(customer);
        newOrder.setItems(item);

        // Calculate total price and update stock
        Double totalPrice = item.getPrice() * newOrder.getQuantity();
        newOrder.setTotalPrice(totalPrice);
        Double updatedStock = item.getStock() - newOrder.getQuantity();
        if (updatedStock < 0) {
            throw new Exception("Insufficient stock for item: " + itemsId);
        }
        item.setStock(updatedStock);
        itemRepository.save(item);

        Orders savedOrder = orderRepository.save(newOrder);
        savedOrder.setOrderCode("Ord-" + savedOrder.getOrderId());
        return orderRepository.save(savedOrder);
    }

    @Transactional
    public Orders updateOrder(Long orderId, Orders updatedOrder) throws Exception {
        Orders existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception("Order not found with id: " + orderId));

        Double quantityDifference = updatedOrder.getQuantity() - existingOrder.getQuantity();
        Items item = existingOrder.getItems();
        Double updatedStock = item.getStock() - quantityDifference;
        if (updatedStock < 0) {
            throw new Exception("Insufficient stock for item: " + item.getItemsId());
        }
        item.setStock(updatedStock);
        itemRepository.save(item);

        existingOrder.setQuantity(updatedOrder.getQuantity());
        existingOrder.setTotalPrice(item.getPrice() * updatedOrder.getQuantity());

        return orderRepository.save(existingOrder);
    }

    @Transactional
    public void deleteOrder(Long orderId) throws Exception {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception("Order not found with id: " + orderId));

        Items item = order.getItems();
        item.setStock(item.getStock() + order.getQuantity());
        itemRepository.save(item);

        orderRepository.delete(order);
    }
}

