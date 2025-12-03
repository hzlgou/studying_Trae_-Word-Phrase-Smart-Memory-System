package com.hzlgou.model;

import javax.persistence.*;

@Entity
@Table(name = "phrase")
public class Phrase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String phrase;
    
    @Column(nullable = false)
    private Integer len;
    
    @Column(nullable = false)
    private Integer mainIdx;
    
    private String pronunciation;
    
    private String derivation;
    
    private String tip;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public Integer getLen() {
        return len;
    }

    public void setLen(Integer len) {
        this.len = len;
    }

    public Integer getMainIdx() {
        return mainIdx;
    }

    public void setMainIdx(Integer mainIdx) {
        this.mainIdx = mainIdx;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getDerivation() {
        return derivation;
    }

    public void setDerivation(String derivation) {
        this.derivation = derivation;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }
}