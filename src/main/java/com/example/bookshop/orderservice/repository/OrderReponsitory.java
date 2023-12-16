package com.example.bookshop.orderservice.repository;

import com.example.bookshop.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderReponsitory extends JpaRepository<Order, Long> {
    Page<Order> findAllByNguoiDungId(Long accountId, Pageable pageable);

//    Page<Order> findAll(Pageable pageable);
    Page<Order> findAllByNguoiDungIdAndTrangThai_TrangThaiId(Long accountId, String trangThaiId, Pageable pageable);

    Page<Order> findAllByTrangThai_TrangThaiId(String trangThaiId, Pageable pageable);

    Page<Order> findAllByTrangThai_TrangThaiIdAndTenNguoiNhanLikeIgnoreCase(String trangThaiId, String tenNguoiNhan,Pageable pageable);

    Page<Order> findAllByTenNguoiNhanLikeIgnoreCase(String tenNguoiNhan, Pageable pageable);

    Optional<Order> findByNguoiDungIdAndDonhangId(Long accountId, Long donHangId);

    default List<Order> findAllByThoiGianDat(LocalDate date) {
        return findAllByThoiGianDatBetween(date.atStartOfDay(), date.plusDays(1L).atStartOfDay());
    }
    List<Order> findAllByThoiGianDatBetween(LocalDateTime start, LocalDateTime end);
}
