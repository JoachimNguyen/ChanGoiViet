package com.hutech.demo.controller;
import com.hutech.demo.model.CartItem;
import com.hutech.demo.model.Order;
import com.hutech.demo.model.OrderDetail;
import com.hutech.demo.service.CartService;
import com.hutech.demo.service.OrderService;
import com.hutech.demo.service.VNPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.io.ByteArrayOutputStream;
import java.io.IOException;



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

    @Autowired
    private VNPayService vnPayService;

    @GetMapping("/search")
    public String searchOrderByPhoneNumber(@RequestParam("phoneNumber") String phoneNumber, Model model) {
        List<Order> orders = orderService.getOrdersByPhoneNumber(phoneNumber);
        model.addAttribute("orders", orders);
        return "/orders/order-list"; // Đổi lại view tương ứng của bạn
    }

    @PostMapping("/submit")
    public String submitOrder(String customerName, String diaChi, String SDT, String email, String ghiChu, String thanhToan,HttpServletRequest request ) {
        List<CartItem> cartItems = cartService.getCartItems();
        BigDecimal totalPrice = cartService.getTotalPrice();
        if (cartItems.isEmpty()) {
            return "redirect:/cart"; // Redirect if cart is empty
        }
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        orderService.createOrder(customerName, diaChi, SDT, email, ghiChu, thanhToan, cartItems,request);
        String redirectUrl = "redirect:" + vnPayService.createOrder_vnpay(totalPrice.intValue(), "VN PAY", baseUrl);
        return redirectUrl;
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
        // Tính tổng giá trị đơn hàng
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderDetail detail : order.getOrderDetails()) {
            // Chuyển đổi giá sản phẩm từ double sang BigDecimal
            BigDecimal price = BigDecimal.valueOf(detail.getProduct().getPrice());
            // Lấy số lượng và nhân với giá sản phẩm
            int quantity = detail.getQuantity();
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
            totalPrice = totalPrice.add(itemTotal);
        }
        model.addAttribute("order", order);
        model.addAttribute("totalPrice", totalPrice);
        return "orders/order-details";
    }

    @GetMapping("/vnpay-payment")
    public String GetMapping(HttpServletRequest request, Model model){
        int paymentStatus =vnPayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        model.addAttribute("orderId", orderInfo);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        return paymentStatus == 1 ? "ordersuccess" : "orderfail";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel() throws IOException {
        List<Order> orders = orderService.getAllOrders();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Orders");

        // Header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Tên Khách Hàng");
        headerRow.createCell(2).setCellValue("Địa Chỉ");
        headerRow.createCell(3).setCellValue("SĐT");
        headerRow.createCell(4).setCellValue("Thanh Toán");
        headerRow.createCell(5).setCellValue("Sản Phẩm");
        headerRow.createCell(6).setCellValue("Ghi Chúuuuuuu");

        // Data rows
        int rowNum = 1;
        for (Order order : orders) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getId());
            row.createCell(1).setCellValue(order.getCustomerName());
            row.createCell(2).setCellValue(order.getDiaChi());
            row.createCell(3).setCellValue(order.getSDT());
            row.createCell(4).setCellValue(order.getThanhToan());

            StringBuilder products = new StringBuilder();
            for (OrderDetail detail : order.getOrderDetails()) {
                products.append(detail.getQuantity()).append(" x ").append(detail.getProduct().getName()).append("\n");
            }
            row.createCell(5).setCellValue(products.toString());
            row.createCell(6).setCellValue(order.getGhiChu());
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        byte[] content = bos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }


}
