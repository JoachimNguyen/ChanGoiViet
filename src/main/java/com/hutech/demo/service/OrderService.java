package com.hutech.demo.service;
import com.hutech.demo.model.CartItem;
import com.hutech.demo.model.Order;
import com.hutech.demo.model.OrderDetail;
import com.hutech.demo.repository.OrderDetailRepository;
import com.hutech.demo.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private VNPayService vnPayService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public List<Order> getOrdersByPhoneNumber(String phoneNumber) {
        return orderRepository.findBySDTContaining(phoneNumber);
    }

    @Transactional
    public Order createOrder(String customerName, String diaChi, String SDT, String email, String ghiChu, String thanhToan, List<CartItem> cartItems, HttpServletRequest request)
    {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setDiaChi(diaChi);
        order.setEmail(email);
        order.setGhiChu(ghiChu);
        order.setSDT(SDT);
        order.setThanhToan(thanhToan);
        order = orderRepository.save(order);
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            orderDetailRepository.save(detail);
        }
        cartService.clearCart();
        return order;
    }
}
