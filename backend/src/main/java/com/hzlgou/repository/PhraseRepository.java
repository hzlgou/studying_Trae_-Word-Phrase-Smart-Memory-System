package com.hzlgou.repository;

import com.hzlgou.model.Phrase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhraseRepository extends JpaRepository<Phrase, Long> {
    Optional<Phrase> findByPhrase(String phrase);
}