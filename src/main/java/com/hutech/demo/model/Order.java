package com.hutech.demo.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    private String diaChi;
    private String SDT;
    private String email;
    private String ghiChu;
    private String thanhToan;
    @OneToMany(mappedBy = "order")
    private List<OrderDetail> orderDetails;
}