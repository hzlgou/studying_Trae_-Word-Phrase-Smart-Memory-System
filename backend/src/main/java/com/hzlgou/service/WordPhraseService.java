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
    
    Map<String, Object> processArticle(String text);
    
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
    
    // 单词本相关方法
    Map<String, Object> toggleWordBookMark(Long wordId);
    List<Map<String, Object>> getWordBook();
    Map<String, Object> updateWordNote(Long wordId, String note);
    
    Optional<Word> findByWord(String word);
}