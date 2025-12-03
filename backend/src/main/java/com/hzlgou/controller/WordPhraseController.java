package com.hzlgou.controller;

import com.hzlgou.model.Phrase;
import com.hzlgou.model.Word;
import com.hzlgou.service.AIService;
import com.hzlgou.service.WordPhraseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class WordPhraseController {
    
    @Autowired
    private WordPhraseService wordPhraseService;
    
    @Autowired
    private AIService aiService;
    
    /**
     * 分词接口
     */
    @PostMapping("/tokenize")
    public Map<String, List<Map<String, String>>> tokenize(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        List<String> tokens = wordPhraseService.tokenize(text);
        
        // 转换为前端期望的格式
        List<Map<String, String>> formattedTokens = new ArrayList<>();
        for (String token : tokens) {
            Map<String, String> tokenObj = new HashMap<>();
            tokenObj.put("text", token);
            tokenObj.put("type", "word"); // 默认类型为word
            formattedTokens.add(tokenObj);
        }
        
        Map<String, List<Map<String, String>>> response = new HashMap<>();
        response.put("tokens", formattedTokens);
        return response;
    }
    
    @PostMapping("/process-article")
    public Map<String, Object> processArticle(@RequestBody Map<String, String> request) {
        String text = request.getOrDefault("text", "");
        return wordPhraseService.processArticle(text);
    }
    
    // 单词本相关接口
    @PostMapping("/wordbook/toggle/{wordId}")
    public Map<String, Object> toggleWordBookMark(@PathVariable Long wordId) {
        return wordPhraseService.toggleWordBookMark(wordId);
    }
    
    @GetMapping("/wordbook")
    public List<Map<String, Object>> getWordBook() {
        return wordPhraseService.getWordBook();
    }
    
    @PostMapping("/wordbook/note/{wordId}")
    public Map<String, Object> updateWordNote(@PathVariable Long wordId, @RequestBody Map<String, String> request) {
        String note = request.getOrDefault("note", "");
        return wordPhraseService.updateWordNote(wordId, note);
    }
    
    /**
     * 获取单词/短语信息接口
     */
    @PostMapping("/phrase")
    public Map<String, Object> getPhraseInfo(@RequestBody Map<String, Object> request) {
        // 安全地转换tokens为List<String>
        List<String> tokens = new ArrayList<>();
        Object tokensObj = request.get("tokens");
        if (tokensObj instanceof List) {
            List<?> list = (List<?>) tokensObj;
            for (Object item : list) {
                if (item instanceof String) {
                    tokens.add((String) item);
                }
            }
        }
        
        // 安全地转换index为Integer
        Integer index = 0;
        Object indexObj = request.get("index");
        if (indexObj instanceof Integer) {
            index = (Integer) indexObj;
        } else if (indexObj != null) {
            try {
                index = Integer.parseInt(indexObj.toString());
            } catch (NumberFormatException e) {
                // 使用默认值0
            }
        }
        
        return wordPhraseService.getPhraseOrWordInfo(tokens, index);
    }
    
    /**
     * 添加单词
     */
    @PostMapping("/word")
    public ResponseEntity<Word> saveWord(@RequestBody Word word) {
        Word savedWord = wordPhraseService.saveWord(word);
        return ResponseEntity.ok(savedWord);
    }

    @PutMapping("/word")
    public ResponseEntity<Word> updateWord(@RequestBody Word word) {
        // 先尝试通过单词内容查找现有记录
        Optional<Word> existingWordOpt = wordPhraseService.findByWord(word.getWord());
        
        if (existingWordOpt.isPresent()) {
            // 更新现有记录
            Word existingWord = existingWordOpt.get();
            existingWord.setLemma(word.getLemma());
            existingWord.setPronunciation(word.getPronunciation());
            existingWord.setDerivation(word.getDerivation());
            existingWord.setTip(word.getTip());
            
            Word updatedWord = wordPhraseService.saveWord(existingWord);
            return ResponseEntity.ok(updatedWord);
        } else {
            // 如果不存在，创建新记录
            Word savedWord = wordPhraseService.saveWord(word);
            return ResponseEntity.ok(savedWord);
        }
    }
    
    /**
     * 添加短语
     */
    @PostMapping("/phrase/add")
    public Phrase addPhrase(@RequestBody Phrase phrase) {
        return wordPhraseService.savePhrase(phrase);
    }
    
    // AI功能API
    
    @GetMapping("/ai/pronunciation/word")
    public ResponseEntity<byte[]> getWordPronunciation(@RequestParam String word) {
        byte[] audioData = aiService.generateWordPronunciation(word);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "inline; filename=\"" + word + ".mp3\"")
                .body(audioData);
    }

    @GetMapping("/ai/pronunciation/phrase")
    public ResponseEntity<byte[]> getPhrasePronunciation(@RequestParam String phrase) {
        byte[] audioData = aiService.generatePhrasePronunciation(phrase);
        String filename = phrase.replaceAll("\\s+", "-") + ".mp3";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "inline; filename=\"" + filename + ".mp3\"")
                .body(audioData);
    }
    
    @PostMapping("/ai/conjunctions")
    public Map<String, Object> analyzeConjunctions(@RequestBody Map<String, String> request) {
        String sentence = request.get("sentence");
        return aiService.analyzeConjunctions(sentence);
    }
    
    @PostMapping("/ai/translate")
    public Map<String, String> translateText(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String fromLang = request.getOrDefault("fromLang", "en");
        String toLang = request.getOrDefault("toLang", "zh");
        String translation = aiService.translateText(text, fromLang, toLang);
        Map<String, String> response = new java.util.HashMap<>();
        response.put("original", text);
        response.put("translation", translation);
        return response;
    }
    
    @PostMapping("/ai/build-word-list")
    public List<Word> buildWordList(@RequestBody Map<String, Object> request) {
        // 安全地获取text参数
        String text = "";
        Object textObj = request.get("text");
        if (textObj instanceof String) {
            text = (String) textObj;
        }
        
        // 安全地获取limit参数
        int limit = 50;
        Object limitObj = request.getOrDefault("limit", "50");
        if (limitObj instanceof Integer) {
            limit = (Integer) limitObj;
        } else {
            try {
                limit = Integer.parseInt(limitObj.toString());
            } catch (NumberFormatException e) {
                // 使用默认值50
            }
        }
        
        return aiService.buildHighFrequencyWordList(text, limit);
    }
    
    @PostMapping("/ai/build-phrase-list")
    public List<Phrase> buildPhraseList(@RequestBody Map<String, Object> request) {
        // 安全地获取text参数
        String text = "";
        Object textObj = request.get("text");
        if (textObj instanceof String) {
            text = (String) textObj;
        }
        
        // 安全地获取limit参数
        int limit = 50;
        Object limitObj = request.getOrDefault("limit", "50");
        if (limitObj instanceof Integer) {
            limit = (Integer) limitObj;
        } else {
            try {
                limit = Integer.parseInt(limitObj.toString());
            } catch (NumberFormatException e) {
                // 使用默认值50
            }
        }
        
        return aiService.buildHighFrequencyPhraseList(text, limit);
    }
    
    @GetMapping("/ai/word-details")
    public Map<String, Object> getWordDetails(@RequestParam String word) {
        return aiService.getWordDetails(word);
    }
}