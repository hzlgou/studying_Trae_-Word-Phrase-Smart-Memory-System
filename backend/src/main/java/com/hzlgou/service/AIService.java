package com.hzlgou.service;

import com.hzlgou.model.Phrase;
import com.hzlgou.model.Word;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 */
public interface AIService {
    
    /**
     * 使用AI构建高频词库
     * @param text 输入文本用于分析高频词
     * @param limit 生成单词的数量
     * @return 生成的单词列表
     */
    List<Word> buildHighFrequencyWordList(String text, int limit);
    
    /**
     * 使用AI构建高频短语库
     * @param text 输入文本用于分析高频短语
     * @param limit 生成短语的数量
     * @return 生成的短语列表
     */
    List<Phrase> buildHighFrequencyPhraseList(String text, int limit);
    
    /**
     * 生成单词的语音合成音频
     * @param word 单词
     * @return 音频字节数组
     */
    byte[] generateWordPronunciation(String word);
    
    /**
     * 生成短语的语音合成音频
     * @param phrase 短语
     * @return 音频字节数组
     */
    byte[] generatePhrasePronunciation(String phrase);
    
    /**
     * 处理连词功能
     * @param sentence 句子
     * @return 包含连词分析的结果
     */
    Map<String, Object> analyzeConjunctions(String sentence);
    
    /**
     * 翻译文本
     * @param text 要翻译的文本
     * @param fromLang 源语言
     * @param toLang 目标语言
     * @return 翻译结果
     */
    String translateText(String text, String fromLang, String toLang);
    
    /**
     * 获取单词的详细信息
     * @param word 单词
     * @return 包含单词详细信息的Map
     */
    Map<String, Object> getWordDetails(String word);
    
    /**
     * 获取短语的详细信息
     * @param phrase 短语
     * @return 包含短语详细信息的Map
     */
    Map<String, Object> getPhraseDetails(String phrase);
}