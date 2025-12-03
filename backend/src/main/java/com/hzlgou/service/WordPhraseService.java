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
    
    /**
     * 快速搜索单词
     * @param keyword 搜索关键词
     * @param searchType 搜索类型: prefix(前缀搜索), substring(子串搜索), exact(精确搜索)
     * @return 匹配的单词列表
     */
    List<Map<String, Object>> searchWords(String keyword, String searchType);
}