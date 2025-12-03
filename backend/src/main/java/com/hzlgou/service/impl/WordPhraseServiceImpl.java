package com.hzlgou.service.impl;

import com.hzlgou.model.Phrase;
import com.hzlgou.model.Word;
import com.hzlgou.repository.PhraseRepository;
import com.hzlgou.repository.WordRepository;
import com.hzlgou.service.AIService;
import com.hzlgou.service.WordPhraseService;
import com.hzlgou.util.CSVUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class WordPhraseServiceImpl implements WordPhraseService {
    private static final Logger log = LoggerFactory.getLogger(WordPhraseServiceImpl.class);
    
    @Autowired
    private WordRepository wordRepository;
    
    @Autowired
    private PhraseRepository phraseRepository;
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    // 本地缓存，提高查询性能
    private Map<String, Word> wordCache = new HashMap<>();
    private Map<String, Phrase> phraseCache = new HashMap<>();
    
    // 分词正则表达式
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\w+|[.,!?;:'\"()\\[\\]{}\\-]");
    
    @Override
    public List<String> tokenize(String text) {
        // 移除多余空格和换行
        text = text.replaceAll("\\s+", " ").trim();
        // 按空格分割，同时保留标点符号
        List<String> tokens = new ArrayList<>();
        var matcher = TOKEN_PATTERN.matcher(text);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }
    
    @Override
    public Map<String, Object> getPhraseOrWordInfo(List<String> tokens, int index) {
        // 1. 尝试匹配最长短语（3-gram, 2-gram）
        String phrase3Gram = getNgram(tokens, index, 3);
        if (phrase3Gram != null && phraseCache.containsKey(phrase3Gram.toLowerCase())) {
            Phrase phrase = phraseCache.get(phrase3Gram.toLowerCase());
            return buildPhraseResponse(phrase);
        }
        
        String phrase2Gram = getNgram(tokens, index, 2);
        if (phrase2Gram != null && phraseCache.containsKey(phrase2Gram.toLowerCase())) {
            Phrase phrase = phraseCache.get(phrase2Gram.toLowerCase());
            return buildPhraseResponse(phrase);
        }
        
        // 2. 匹配单词
        String wordStr = tokens.get(index);
        String wordLower = wordStr.toLowerCase();
        if (wordCache.containsKey(wordLower)) {
            Word w = wordCache.get(wordLower);
            return buildWordResponse(w);
        }
        
        // 3. 如果数据库中没有，调用AI获取单词信息并保存到数据库
        try {
            Map<String, Object> aiDetails = aiService.getWordDetails(wordStr);
            Word newWord = new Word();
            newWord.setWord(wordStr);
            newWord.setLemma(aiDetails.getOrDefault("word", wordStr).toString());
            newWord.setPronunciation(aiDetails.getOrDefault("pronunciation", "/").toString());
            
            // 处理派生词（synonyms）
            List<String> synonyms = new ArrayList<>();
            if (aiDetails.containsKey("synonyms")) {
                Object synonymsObj = aiDetails.get("synonyms");
                if (synonymsObj instanceof List) {
                    synonyms = (List<String>) synonymsObj;
                } else {
                    synonyms.add(synonymsObj.toString());
                }
            }
            newWord.setDerivation(String.join(",", synonyms));
            
            // 设置记忆口诀
            newWord.setTip("AI生成: " + aiDetails.getOrDefault("meaning", "").toString());
            
            // 保存到数据库和缓存
            Word savedWord = saveWord(newWord);
            return buildWordResponse(savedWord);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果AI调用失败，返回默认信息
            return buildDefaultResponse(wordStr);
        }
    }
    
    @Override
    public Word saveWord(Word word) {
        Word saved = wordRepository.save(word);
        wordCache.put(saved.getWord().toLowerCase(), saved);
        return saved;
    }
    
    @Override
    public Phrase savePhrase(Phrase phrase) {
        Phrase saved = phraseRepository.save(phrase);
        phraseCache.put(saved.getPhrase().toLowerCase(), saved);
        return saved;
    }
    
    @Override
    public Optional<Word> findByWord(String word) {
        return wordRepository.findByWord(word);
    }
    
    // 初始化数据库和缓存
    @PostConstruct
    @Override
    public void initDatabase() {
        log.info("initDatabase start");
        // 初始化常用单词
        initCommonWords();
        // 初始化常用短语
        initCommonPhrases();
        // 使用AI生成高频词库和短语库的示例
        // String sampleText = "Sample text for AI analysis"; // 这里可以使用更长的文本
        // List<Word> aiWords = aiService.buildHighFrequencyWordList(sampleText, 50);
        // List<Phrase> aiPhrases = aiService.buildHighFrequencyPhraseList(sampleText, 50);
        // aiWords.forEach(this::saveWord);
        // aiPhrases.forEach(this::savePhrase);
        log.info("initDatabase end");
    }
    
    // 获取N-gram短语
    private String getNgram(List<String> tokens, int index, int n) {
        if (n == 2) {
            // 2-gram：检查当前位置和下一个位置
            if (index + 1 < tokens.size()) {
                return tokens.get(index) + " " + tokens.get(index + 1);
            }
            // 或者当前位置和前一个位置
            if (index - 1 >= 0) {
                return tokens.get(index - 1) + " " + tokens.get(index);
            }
        } else if (n == 3) {
            // 3-gram：检查当前位置前后
            if (index - 1 >= 0 && index + 1 < tokens.size()) {
                return tokens.get(index - 1) + " " + tokens.get(index) + " " + tokens.get(index + 1);
            }
        }
        return null;
    }
    
    // 构建短语响应
    private Map<String, Object> buildPhraseResponse(Phrase phrase) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "phrase");
        response.put("text", phrase.getPhrase());
        response.put("pronunciation", phrase.getPronunciation());
        response.put("derivation", Arrays.asList(phrase.getDerivation().split(";")));
        response.put("tip", phrase.getTip());
        return response;
    }
    
    // 构建单词响应
    private Map<String, Object> buildWordResponse(Word word) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "word");
        response.put("text", word.getWord());
        response.put("pronunciation", word.getPronunciation());
        response.put("derivation", Arrays.asList(word.getDerivation().split(",")));
        response.put("tip", word.getTip());
        return response;
    }
    
    // 构建默认响应
    private Map<String, Object> buildDefaultResponse(String text) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "unknown");
        response.put("text", text);
        response.put("pronunciation", "未知");
        response.put("derivation", Collections.emptyList());
        response.put("tip", "暂无记忆口诀");
        return response;
    }
    
    // 初始化常用单词
    private void initCommonWords() {
        try {
            // 从CSV文件加载单词数据
            Resource resource = resourceLoader.getResource("file:c:/Trae/English/data/words.csv");
            List<Word> words = CSVUtil.loadWordsFromCSV(resource.getFile().getPath());
            
            // 保存到数据库和缓存
            for (Word word : words) {
                saveWord(word);
            }
            
            log.info("Loaded {} words from CSV file", words.size());
        } catch (Exception e) {
            log.error("Failed to load words from CSV file: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 初始化常用短语
    private void initCommonPhrases() {
        try {
            // 从CSV文件加载短语数据
            Resource resource = resourceLoader.getResource("file:c:/Trae/English/data/phrases.csv");
            List<Phrase> phrases = CSVUtil.loadPhrasesFromCSV(resource.getFile().getPath());
            
            // 保存到数据库和缓存
            for (Phrase phrase : phrases) {
                savePhrase(phrase);
            }
            
            log.info("Loaded {} phrases from CSV file", phrases.size());
        } catch (Exception e) {
            log.error("Failed to load phrases from CSV file: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    

}