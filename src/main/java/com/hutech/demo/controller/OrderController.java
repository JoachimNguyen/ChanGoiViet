package com.hutech.demo.controller;

import com.hutech.demo.model.CartItem;
import com.hutech.demo.model.Order;
import com.hutech.demo.service.CartService;
import com.hutech.demo.service.OrderService;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;

    @GetMapping("/checkout")
    public String checkout() {
        return "/cart/checkout";
    }

    @GetMapping("/search")
    public String searchOrderByPhoneNumber(@RequestParam("phoneNumber") String phoneNumber, Model model) {
        List<Order> orders = orderService.getOrdersByPhoneNumber(phoneNumber);
        model.addAttribute("orders", orders);
        return "/orders/order-list"; // Đổi lại view tương ứng của bạn
    }

    @PostMapping("/submit")
    public String submitOrder(String customerName, String diaChi, String SDT, String email, String ghiChu, String thanhToan ) {
        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            return "redirect:/cart"; // Redirect if cart is empty
        }
        orderService.createOrder(customerName, diaChi, SDT, email, ghiChu, thanhToan, cartItems);
        return "redirect:/order/confirmation";
    }

    @GetMapping
    public String showOrderList(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "/orders/order-list";
    }

    @GetMapping("/confirmation")
    public String orderConfirmation(Model model) {
        model.addAttribute("message", "Đơn Hàng Của Bạn Đã Được Đặt Thành Công");
        return "cart/order-confirmation";
    }

    @GetMapping("/view/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return "redirect:/order"; // Redirect if order not found
        }
        model.addAttribute("order", order);
        return "orders/order-details";
    }

}
