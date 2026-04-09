package com.example.my_api_server.repo;

import com.example.my_api_server.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {
    //동일한 레코드에 대해서
    @Lock(LockModeType.PESSIMISTIC_WRITE) // PG 에서는FOR NO KEY UPDATE 레코드 락, My SQL에서는 For Update
    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id")
//JPQL(자바 객체로 쿼리문 작성)
    List<Product> findAllByIdsWithXLock(List<Long> ids);
}
