package com.hzlgou.repository;

import com.hzlgou.model.WordBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordBookRepository extends JpaRepository<WordBook, Long> {
    Optional<WordBook> findByWordId(Long wordId);
    List<WordBook> findByIsMarkedTrue();
    List<WordBook> findAllByOrderByCreatedAtDesc();
}