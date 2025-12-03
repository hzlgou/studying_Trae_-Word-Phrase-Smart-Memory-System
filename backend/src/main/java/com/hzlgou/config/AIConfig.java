package com.hzlgou.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI配置类
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AIConfig {
    
    // OpenAI配置
    private OpenAIConfig openai = new OpenAIConfig();
    
    // 语音合成配置
    private TTSConfig tts = new TTSConfig();
    
    // 翻译服务配置
    private TranslationConfig translation = new TranslationConfig();
    
    // DeepSeek配置
    private DeepSeekConfig deepseek = new DeepSeekConfig();

    public OpenAIConfig getOpenai() {
        return openai;
    }

    public void setOpenai(OpenAIConfig openai) {
        this.openai = openai;
    }

    public TTSConfig getTts() {
        return tts;
    }

    public void setTts(TTSConfig tts) {
        this.tts = tts;
    }

    public TranslationConfig getTranslation() {
        return translation;
    }

    public void setTranslation(TranslationConfig translation) {
        this.translation = translation;
    }

    public DeepSeekConfig getDeepseek() {
        return deepseek;
    }

    public void setDeepseek(DeepSeekConfig deepseek) {
        this.deepseek = deepseek;
    }

    public static class OpenAIConfig {
        private String apiKey;
        private String apiUrl = "https://api.openai.com/v1";
        private String model = "gpt-3.5-turbo";
        
        // Getters and Setters
        public String getApiKey() {
            return apiKey;
        }
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        public String getApiUrl() {
            return apiUrl;
        }
        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
        public String getModel() {
            return model;
        }
        public void setModel(String model) {
            this.model = model;
        }
    }

    public static class TTSConfig {
        private String apiKey;
        private String apiUrl;
        private String voice = "en-US-JennyNeural";
        private String format = "mp3";
        
        // Getters and Setters
        public String getApiKey() {
            return apiKey;
        }
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        public String getApiUrl() {
            return apiUrl;
        }
        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
        public String getVoice() {
            return voice;
        }
        public void setVoice(String voice) {
            this.voice = voice;
        }
        public String getFormat() {
            return format;
        }
        public void setFormat(String format) {
            this.format = format;
        }
    }

    public static class TranslationConfig {
        private String apiKey;
        private String apiUrl;
        private String fromLang = "en";
        private String toLang = "zh";
        
        // Getters and Setters
        public String getApiKey() {
            return apiKey;
        }
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        public String getApiUrl() {
            return apiUrl;
        }
        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
        public String getFromLang() {
            return fromLang;
        }
        public void setFromLang(String fromLang) {
            this.fromLang = fromLang;
        }
        public String getToLang() {
            return toLang;
        }
        public void setToLang(String toLang) {
            this.toLang = toLang;
        }
    }
    
    public static class DeepSeekConfig {
        private String apiUrl = "http://localhost:8081/v1";
        private String apiKey = "dummy_key";
        private String model = "deepseek-chat";
        
        // Getters and Setters
        public String getApiUrl() {
            return apiUrl;
        }
        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
        public String getApiKey() {
            return apiKey;
        }
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        public String getModel() {
            return model;
        }
        public void setModel(String model) {
            this.model = model;
        }
    }
}