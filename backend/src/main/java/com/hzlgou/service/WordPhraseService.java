package com.hzlgou.service;

import com.hzlgou.model.Phrase;
import com.hzlgou.model.Word;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WordPhraseService {
    /**
     * 分词功能
     */
    List<String> tokenize(String text);
    
    /**
     * 获取最长匹配的短语或单词信息
     */
    Map<String, Object> getPhraseOrWordInfo(List<String> tokens, int index);
    
    /**
     * 保存单词
     */
    Word saveWord(Word word);
    
    /**
     * 保存短语
     */
    Phrase savePhrase(Phrase phrase);
    
    /**
     * 初始化词库和短语库
     */
    void initDatabase();
    
    Optional<Word> findByWord(String word);
}