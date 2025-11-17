package com.context.memorybook.domain.ai.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContextExtractionService {

    // Common stop words to filter out
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
            "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
            "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
            "or", "an", "will", "my", "one", "all", "would", "there", "their", "is"
    );

    /**
     * Extract context from content text
     * This is a simple implementation - can be enhanced with AI/NLP services
     */
    public String extractContext(String title, String description, String textContent) {
        StringBuilder fullText = new StringBuilder();
        if (title != null) fullText.append(title).append(" ");
        if (description != null) fullText.append(description).append(" ");
        if (textContent != null) fullText.append(textContent).append(" ");

        String text = fullText.toString().toLowerCase();
        
        // Extract context by identifying key phrases and topics
        // Simple keyword extraction and context summarization
        List<String> keywords = extractKeywords(text);
        String context = keywords.stream()
                .limit(10)
                .collect(Collectors.joining(", "));
        
        return context.isEmpty() ? "General content" : context;
    }

    /**
     * Extract keywords from text
     */
    public List<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Clean and tokenize
        String cleaned = text.replaceAll("[^a-zA-Z0-9\\s]", " ");
        String[] words = cleaned.toLowerCase().split("\\s+");

        // Count word frequencies
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : words) {
            word = word.trim();
            if (word.length() > 3 && !STOP_WORDS.contains(word)) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
        }

        // Sort by frequency and return top keywords
        return wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(15)
                .collect(Collectors.toList());
    }

    /**
     * Calculate similarity between two context strings
     * Returns a score between 0 and 1 (1 = identical, 0 = no similarity)
     */
    public double calculateSimilarity(String context1, String context2) {
        if (context1 == null || context2 == null || context1.isEmpty() || context2.isEmpty()) {
            return 0.0;
        }

        List<String> keywords1 = extractKeywords(context1);
        List<String> keywords2 = extractKeywords(context2);

        if (keywords1.isEmpty() || keywords2.isEmpty()) {
            return 0.0;
        }

        // Calculate Jaccard similarity
        Set<String> set1 = new HashSet<>(keywords1);
        Set<String> set2 = new HashSet<>(keywords2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * Generate a summary from content
     * Simple implementation - can be enhanced with AI summarization
     */
    public String generateSummary(String textContent, int maxLength) {
        if (textContent == null || textContent.trim().isEmpty()) {
            return "";
        }

        // Simple summary: take first few sentences
        String[] sentences = textContent.split("[.!?]+");
        StringBuilder summary = new StringBuilder();
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.length() > 10) {
                if (summary.length() + sentence.length() > maxLength) {
                    break;
                }
                if (summary.length() > 0) {
                    summary.append(". ");
                }
                summary.append(sentence);
            }
        }

        if (summary.length() > maxLength) {
            return summary.substring(0, maxLength - 3) + "...";
        }

        return summary.toString().isEmpty() ? textContent.substring(0, Math.min(maxLength, textContent.length())) : summary.toString();
    }
}

