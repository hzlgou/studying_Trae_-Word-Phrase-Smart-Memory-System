package com.hzlgou.service.impl;

import com.hzlgou.model.Phrase;
import com.hzlgou.model.Word;
import com.hzlgou.repository.PhraseRepository;
import com.hzlgou.repository.WordRepository;
import com.hzlgou.service.WordPhraseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class WordPhraseServiceImpl implements WordPhraseService {
    
    @Autowired
    private WordRepository wordRepository;
    
    @Autowired
    private PhraseRepository phraseRepository;
    
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
        String word = tokens.get(index).toLowerCase();
        if (wordCache.containsKey(word)) {
            Word w = wordCache.get(word);
            return buildWordResponse(w);
        }
        
        // 3. 兜底返回（实际项目中应该调用第三方API）
        return buildDefaultResponse(tokens.get(index));
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
    
    // 初始化数据库和缓存
    @PostConstruct
    @Override
    public void initDatabase() {
        // 初始化常用单词
        initCommonWords();
        // 初始化常用短语
        initCommonPhrases();
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
        List<Word> words = new ArrayList<>();
        
        // 添加一些常用单词示例
        Word word1 = new Word();
        word1.setWord("take");
        word1.setLemma("take");
        word1.setPronunciation("/teɪk/");
        word1.setDerivation("take → retake, uptake");
        word1.setTip("take表示拿取，take notes就是拿笔记下");
        words.add(word1);
        
        Word word2 = new Word();
        word2.setWord("place");
        word2.setLemma("place");
        word2.setPronunciation("/pleɪs/");
        word2.setDerivation("place → placement");
        word2.setTip("place是地方，in place就是在正确的地方");
        words.add(word2);
        
        Word word3 = new Word();
        word3.setWord("meeting");
        word3.setLemma("meet");
        word3.setPronunciation("/ˈmiːtɪŋ/");
        word3.setDerivation("meet → meeting");
        word3.setTip("meet是见面，meeting就是见面的地方");
        words.add(word3);
        
        Word word4 = new Word();
        word4.setWord("will");
        word4.setLemma("will");
        word4.setPronunciation("/wɪl/");
        word4.setDerivation("will → willing");
        word4.setTip("will表示将会，也可以作名词表示意志");
        words.add(word4);
        
        Word word5 = new Word();
        word5.setWord("tomorrow");
        word5.setLemma("tomorrow");
        word5.setPronunciation("/təˈmɒrəʊ/");
        word5.setDerivation("");
        word5.setTip("tomorrow是明天，由to + morrow组成");
        words.add(word5);
        
        // 保存到数据库和缓存
        for (Word word : words) {
            saveWord(word);
        }
    }
    
    // 初始化常用短语
    private void initCommonPhrases() {
        List<Phrase> phrases = new ArrayList<>();
        
        // 添加一些常用短语示例
        Phrase phrase1 = new Phrase();
        phrase1.setPhrase("take place");
        phrase1.setLen(2);
        phrase1.setMainIdx(0);
        phrase1.setPronunciation("/teɪk pleɪs/");
        phrase1.setDerivation("take → retake, uptake; place → placement");
        phrase1.setTip("take了place，活动才有场所");
        phrases.add(phrase1);
        
        Phrase phrase2 = new Phrase();
        phrase2.setPhrase("in place");
        phrase2.setLen(2);
        phrase2.setMainIdx(1);
        phrase2.setPronunciation("/ɪn pleɪs/");
        phrase2.setDerivation("in → inside; place → placement");
        phrase2.setTip("in place表示在正确的位置");
        phrases.add(phrase2);
        
        Phrase phrase3 = new Phrase();
        phrase3.setPhrase("look forward to");
        phrase3.setLen(3);
        phrase3.setMainIdx(0);
        phrase3.setPronunciation("/lʊk ˈfɔːwəd tuː/");
        phrase3.setDerivation("look → looking; forward → forwards; to → too");
        phrase3.setTip("look forward to表示期待");
        phrases.add(phrase3);
        
        // 保存到数据库和缓存
        for (Phrase phrase : phrases) {
            savePhrase(phrase);
        }
    }
    

}