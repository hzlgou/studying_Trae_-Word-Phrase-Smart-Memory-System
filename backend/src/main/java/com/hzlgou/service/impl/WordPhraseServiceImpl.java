package com.hzlgou.service.impl;

import com.hzlgou.model.Phrase;
import com.hzlgou.model.Word;
import com.hzlgou.model.WordBook;
import com.hzlgou.repository.PhraseRepository;
import com.hzlgou.repository.WordBookRepository;
import com.hzlgou.repository.WordRepository;
import com.hzlgou.service.AIService;
import com.hzlgou.service.WordPhraseService;
import com.hzlgou.util.CSVUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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
    private WordBookRepository wordBookRepository;
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    // 缓存已通过Spring Cache + Caffeine实现，不再使用手动HashMap缓存
    
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
    @Cacheable(value = "wordPhraseCache", key = "#tokens.toString() + '_' + #index", unless = "#result == null")
    public Map<String, Object> getPhraseOrWordInfo(List<String> tokens, int index) {
        // 1. 尝试匹配最长短语（3-gram, 2-gram）
        String phrase3Gram = getNgram(tokens, index, 3);
        if (phrase3Gram != null) {
            Phrase phrase = getPhraseFromCache(phrase3Gram);
            if (phrase != null) {
                return buildPhraseResponse(phrase);
            }
        }
        
        String phrase2Gram = getNgram(tokens, index, 2);
        if (phrase2Gram != null) {
            Phrase phrase = getPhraseFromCache(phrase2Gram);
            if (phrase != null) {
                return buildPhraseResponse(phrase);
            }
        }
        
        // 2. 匹配单词
        String wordStr = tokens.get(index);
        Word word = getWordFromCache(wordStr);
        if (word != null) {
            return buildWordResponse(word);
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
                    // 安全地转换List，确保所有元素都是String类型
                    List<?> list = (List<?>) synonymsObj;
                    for (Object item : list) {
                        if (item instanceof String) {
                            synonyms.add((String) item);
                        }
                    }
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
        return saved;
    }
    
    @Override
    public Phrase savePhrase(Phrase phrase) {
        Phrase saved = phraseRepository.save(phrase);
        return saved;
    }
    
    @Override
    @Cacheable(value = "wordCache", key = "#word", unless = "#result == null")
    public Optional<Word> findByWord(String word) {
        return wordRepository.findByWord(word);
    }
    
    // 从缓存获取单词
    @Cacheable(value = "wordCache", key = "#word.toLowerCase()", unless = "#result == null")
    private Word getWordFromCache(String word) {
        // 先从数据库查找
        Optional<Word> optionalWord = wordRepository.findByWord(word);
        if (optionalWord.isPresent()) {
            return optionalWord.get();
        }
        
        // 尝试小写查找
        optionalWord = wordRepository.findByWord(word.toLowerCase());
        return optionalWord.orElse(null);
    }
    
    // 从缓存获取短语
    @Cacheable(value = "phraseCache", key = "#phrase.toLowerCase()", unless = "#result == null")
    private Phrase getPhraseFromCache(String phrase) {
        // 先从数据库查找
        Optional<Phrase> optionalPhrase = phraseRepository.findByPhrase(phrase);
        if (optionalPhrase.isPresent()) {
            return optionalPhrase.get();
        }
        
        // 尝试小写查找
        optionalPhrase = phraseRepository.findByPhrase(phrase.toLowerCase());
        return optionalPhrase.orElse(null);
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
    
    @Override
    public Map<String, Object> processArticle(String text) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. AI翻译全文
        String translatedText = aiService.translateText(text, "English", "Chinese");
        result.put("originalText", text);
        result.put("translatedText", translatedText);
        
        // 2. 分词
        List<String> tokens = tokenize(text);
        
        // 3. 分析每个单词，提取意思并保存到数据库
        Map<String, Map<String, Object>> wordDetailsMap = new HashMap<>();
        List<Map<String, Object>> uniqueWords = new ArrayList<>();
        
        for (String token : tokens) {
            // 过滤掉标点符号
            if (!token.matches("[a-zA-Z]+")) {
                continue;
            }
            
            // 如果已经处理过该单词，跳过
            if (wordDetailsMap.containsKey(token.toLowerCase())) {
                continue;
            }
            
            // 获取单词详细信息
            Map<String, Object> wordInfo;
            
            // 先从缓存/数据库查找
            Word existingWord = getWordFromCache(token);
            if (existingWord != null) {
                wordInfo = buildWordResponse(existingWord);
                // 检查是否在单词本中
                wordBookRepository.findByWordId(existingWord.getId()).ifPresent(wordBook -> {
                    wordInfo.put("inWordBook", wordBook.isMarked());
                });
                if (existingWord.getNote() != null) {
                    wordInfo.put("note", existingWord.getNote());
                }
            } else {
                // 调用AI获取单词信息
                try {
                    Map<String, Object> aiDetails = aiService.getWordDetails(token);
                    
                    // 保存到数据库
                    Word newWord = new Word();
                    newWord.setWord(token);
                    newWord.setLemma(aiDetails.getOrDefault("word", token).toString());
                    newWord.setPronunciation(aiDetails.getOrDefault("pronunciation", "/").toString());
                    
                    // 处理派生词
                    List<String> synonyms = new ArrayList<>();
                    if (aiDetails.containsKey("synonyms")) {
                        Object synonymsObj = aiDetails.get("synonyms");
                        if (synonymsObj instanceof List) {
                            // 安全地转换List，确保所有元素都是String类型
                            List<?> list = (List<?>) synonymsObj;
                            for (Object item : list) {
                                if (item instanceof String) {
                                    synonyms.add((String) item);
                                }
                            }
                        } else {
                            synonyms.add(synonymsObj.toString());
                        }
                    }
                    newWord.setDerivation(String.join(",", synonyms));
                    
                    // 设置记忆口诀（包含意思）
                    Object meaningObj = aiDetails.getOrDefault("meaning", "");
                    String meaningStr = "";
                    if (meaningObj instanceof List) {
                        // 安全地转换List，确保所有元素都是String类型
                        List<?> list = (List<?>) meaningObj;
                        StringBuilder sb = new StringBuilder();
                        for (Object item : list) {
                            if (item instanceof String) {
                                if (sb.length() > 0) {
                                    sb.append("; ");
                                }
                                sb.append((String) item);
                            }
                        }
                        meaningStr = sb.toString();
                    } else {
                        meaningStr = meaningObj.toString();
                    }
                    newWord.setTip("意思: " + meaningStr);
                    
                    // 保存到数据库
                    Word savedWord = saveWord(newWord);
                    wordInfo = buildWordResponse(savedWord);
                    wordInfo.put("meaning", meaningStr);
                    wordInfo.put("inWordBook", false);
                } catch (Exception e) {
                    log.error("Error processing word: {}", token, e);
                    wordInfo = buildDefaultResponse(token);
                    wordInfo.put("inWordBook", false);
                }
            }
            
            // 保存单词信息
            wordDetailsMap.put(token.toLowerCase(), wordInfo);
            uniqueWords.add(wordInfo);
        }
        
        result.put("tokens", tokens);
        result.put("uniqueWords", uniqueWords);
        result.put("wordCount", uniqueWords.size());
        
        return result;
    }
    
    @Override
    public Map<String, Object> toggleWordBookMark(Long wordId) {
        Map<String, Object> response = new HashMap<>();
        
        // 查找单词
        Optional<Word> wordOptional = wordRepository.findById(wordId);
        if (!wordOptional.isPresent()) {
            response.put("success", false);
            response.put("message", "单词不存在");
            return response;
        }
        
        Word word = wordOptional.get();
        
        // 查找或创建WordBook记录
        WordBook wordBook = wordBookRepository.findByWordId(wordId)
                .orElseGet(() -> {
                    WordBook newWordBook = new WordBook();
                    newWordBook.setWord(word);
                    return newWordBook;
                });
        
        // 切换标记状态
        wordBook.setMarked(!wordBook.isMarked());
        wordBook.setUpdatedAt(new Date());
        
        // 保存到数据库
        wordBookRepository.save(wordBook);
        
        // 构建响应
        response.put("success", true);
        response.put("wordId", wordId);
        response.put("inWordBook", wordBook.isMarked());
        response.put("message", wordBook.isMarked() ? "已添加到单词本" : "已从单词本移除");
        
        return response;
    }
    
    @Override
    public List<Map<String, Object>> getWordBook() {
        List<Map<String, Object>> wordBookList = new ArrayList<>();
        
        // 获取所有标记的单词
        List<WordBook> markedWords = wordBookRepository.findByIsMarkedTrue();
        
        // 构建响应
        for (WordBook wordBook : markedWords) {
            Word word = wordBook.getWord();
            Map<String, Object> wordInfo = buildWordResponse(word);
            
            // 添加单词本相关信息
            wordInfo.put("inWordBook", true);
            if (word.getNote() != null) {
                wordInfo.put("note", word.getNote());
            }
            wordInfo.put("addedAt", wordBook.getCreatedAt());
            
            wordBookList.add(wordInfo);
        }
        
        return wordBookList;
    }
    
    @Override
    public Map<String, Object> updateWordNote(Long wordId, String note) {
        Map<String, Object> response = new HashMap<>();
        
        // 查找单词
        Optional<Word> wordOptional = wordRepository.findById(wordId);
        if (!wordOptional.isPresent()) {
            response.put("success", false);
            response.put("message", "单词不存在");
            return response;
        }
        
        Word word = wordOptional.get();
        
        // 更新笔记
        word.setNote(note);
        wordRepository.save(word);
        
        // 构建响应
        response.put("success", true);
        response.put("wordId", wordId);
        response.put("note", note);
        response.put("message", "笔记已更新");
        
        return response;
    }
}