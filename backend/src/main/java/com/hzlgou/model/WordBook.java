package com.hzlgou.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "word_book")
public class WordBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;
    
    @Column(name = "is_marked")
    private boolean isMarked = false;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Word getWord() {
        return word;
    }
    
    public void setWord(Word word) {
        this.word = word;
    }
    
    public boolean isMarked() {
        return isMarked;
    }
    
    public void setMarked(boolean marked) {
        isMarked = marked;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}