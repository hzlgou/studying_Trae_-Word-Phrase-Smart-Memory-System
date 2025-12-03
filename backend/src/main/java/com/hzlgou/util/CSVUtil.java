package com.hzlgou.util;

import com.hzlgou.model.Phrase;
import com.hzlgou.model.Word;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV工具类，用于从CSV文件加载单词和短语数据
 */
public class CSVUtil {

    /**
     * 从CSV文件加载单词数据
     * @param filePath CSV文件路径
     * @return 单词列表
     */
    public static List<Word> loadWordsFromCSV(String filePath) {
        List<Word> words = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // 跳过表头
                    continue;
                }
                
                String[] parts = parseCSVLine(line);
                if (parts.length >= 5) {
                    Word word = new Word();
                    word.setWord(parts[0].trim());
                    word.setLemma(parts[1].trim());
                    word.setPronunciation(parts[2].trim());
                    word.setDerivation(parts[3].trim());
                    word.setTip(parts[4].trim());
                    words.add(word);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return words;
    }

    /**
     * 从CSV文件加载短语数据
     * @param filePath CSV文件路径
     * @return 短语列表
     */
    public static List<Phrase> loadPhrasesFromCSV(String filePath) {
        List<Phrase> phrases = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // 跳过表头
                    continue;
                }
                
                String[] parts = parseCSVLine(line);
                if (parts.length >= 6) {
                    Phrase phrase = new Phrase();
                    phrase.setPhrase(parts[0].trim());
                    phrase.setLen(Integer.parseInt(parts[1].trim()));
                    phrase.setMainIdx(Integer.parseInt(parts[2].trim()));
                    phrase.setPronunciation(parts[3].trim());
                    phrase.setDerivation(parts[4].trim());
                    phrase.setTip(parts[5].trim());
                    phrases.add(phrase);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return phrases;
    }

    /**
     * 解析CSV行，处理包含逗号的字段（用引号括起来的字段）
     * @param line CSV行
     * @return 解析后的字段数组
     */
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }
}
