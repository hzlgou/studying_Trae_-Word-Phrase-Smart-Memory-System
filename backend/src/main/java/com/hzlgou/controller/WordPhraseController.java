package com.hzlgou.controller;

import com.hzlgou.model.Phrase;
import com.hzlgou.model.Word;
import com.hzlgou.service.WordPhraseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class WordPhraseController {
    
    @Autowired
    private WordPhraseService wordPhraseService;
    
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
    
    /**
     * 获取单词/短语信息接口
     */
    @PostMapping("/phrase")
    public Map<String, Object> getPhraseInfo(@RequestBody Map<String, Object> request) {
        List<String> tokens = (List<String>) request.get("tokens");
        Integer index = (Integer) request.get("index");
        return wordPhraseService.getPhraseOrWordInfo(tokens, index);
    }
    
    /**
     * 添加单词
     */
    @PostMapping("/word")
    public Word addWord(@RequestBody Word word) {
        return wordPhraseService.saveWord(word);
    }
    
    /**
     * 添加短语
     */
    @PostMapping("/phrase/add")
    public Phrase addPhrase(@RequestBody Phrase phrase) {
        return wordPhraseService.savePhrase(phrase);
    }
}