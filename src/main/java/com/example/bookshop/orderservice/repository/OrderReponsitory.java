package com.example.bookshop.orderservice.repository;

import com.example.bookshop.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderReponsitory extends JpaRepository<Order, Long> {
    Page<Order> findAllByNguoiDungId(Long accountId, Pageable pageable);

//    Page<Order> findAll(Pageable pageable);
    Page<Order> findAllByNguoiDungIdAndTrangThai_TrangThaiId(Long accountId, String trangThaiId, Pageable pageable);

    Page<Order> findAllByTrangThai_TrangThaiId(String trangThaiId, Pageable pageable);

    Optional<Order> findByNguoiDungIdAndDonhangId(Long accountId, Long donHangId);
}
