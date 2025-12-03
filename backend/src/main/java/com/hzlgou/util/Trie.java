package com.hzlgou.util;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * 前缀树（Trie树）实现，用于高效的单词搜索
 */
public class Trie {
    // 前缀树节点
    private static class TrieNode {
        // 子节点映射
        Map<Character, TrieNode> children;
        // 是否是单词的结束
        boolean isEndOfWord;
        // 单词（如果是结束节点）
        String word;
        // 单词ID
        Long wordId;
        
        TrieNode() {
            children = new HashMap<>();
            isEndOfWord = false;
            word = null;
            wordId = null;
        }
    }
    
    // 根节点
    private final TrieNode root;
    
    /**
     * 构造函数
     */
    public Trie() {
        root = new TrieNode();
    }
    
    /**
     * 向Trie树中插入一个单词
     * @param word 要插入的单词
     * @param wordId 单词ID
     */
    public void insert(String word, Long wordId) {
        if (word == null || word.isEmpty()) {
            return;
        }
        
        TrieNode current = root;
        
        for (char c : word.toLowerCase().toCharArray()) {
            // 如果当前字符不存在，创建新节点
            current.children.putIfAbsent(c, new TrieNode());
            // 移动到下一个节点
            current = current.children.get(c);
        }
        
        // 标记单词结束
        current.isEndOfWord = true;
        current.word = word;
        current.wordId = wordId;
    }
    
    /**
     * 搜索单词是否存在于Trie树中
     * @param word 要搜索的单词
     * @return 如果存在返回true，否则返回false
     */
    public boolean search(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        
        TrieNode current = root;
        
        for (char c : word.toLowerCase().toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                return false;
            }
        }
        
        return current.isEndOfWord;
    }
    
    /**
     * 根据前缀搜索所有单词
     * @param prefix 前缀
     * @return 所有以该前缀开头的单词列表
     */
    public List<Map<String, Object>> searchByPrefix(String prefix) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (prefix == null || prefix.isEmpty()) {
            return result;
        }
        
        TrieNode current = root;
        
        // 先找到前缀的最后一个节点
        for (char c : prefix.toLowerCase().toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                return result;
            }
        }
        
        // 从该节点开始遍历所有单词
        collectWords(current, result);
        
        return result;
    }
    
    /**
     * 收集从当前节点开始的所有单词
     * @param node 当前节点
     * @param result 结果列表
     */
    private void collectWords(TrieNode node, List<Map<String, Object>> result) {
        if (node.isEndOfWord) {
            Map<String, Object> wordInfo = new HashMap<>();
            wordInfo.put("word", node.word);
            wordInfo.put("id", node.wordId);
            result.add(wordInfo);
        }
        
        for (TrieNode child : node.children.values()) {
            collectWords(child, result);
        }
    }
    
    /**
     * 搜索包含特定子串的所有单词
     * @param substring 子串
     * @return 包含该子串的所有单词列表
     */
    public List<Map<String, Object>> searchBySubstring(String substring) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (substring == null || substring.isEmpty()) {
            return result;
        }
        
        // 遍历所有单词，查找包含子串的单词
        collectAllWords(root, substring.toLowerCase(), result);
        
        return result;
    }
    
    /**
     * 收集所有包含特定子串的单词
     * @param node 当前节点
     * @param substring 要查找的子串
     * @param result 结果列表
     */
    private void collectAllWords(TrieNode node, String substring, List<Map<String, Object>> result) {
        if (node.isEndOfWord && node.word.toLowerCase().contains(substring)) {
            Map<String, Object> wordInfo = new HashMap<>();
            wordInfo.put("word", node.word);
            wordInfo.put("id", node.wordId);
            result.add(wordInfo);
        }
        
        for (TrieNode child : node.children.values()) {
            collectAllWords(child, substring, result);
        }
    }
    
    /**
     * 获取Trie树中所有的单词
     * @return 所有单词列表
     */
    public List<Map<String, Object>> getAllWords() {
        List<Map<String, Object>> result = new ArrayList<>();
        collectWords(root, result);
        return result;
    }
}