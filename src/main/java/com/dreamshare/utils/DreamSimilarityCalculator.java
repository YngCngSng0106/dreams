package com.dreamshare.utils;

import com.dreamshare.entity.Dream;

import java.util.*;

public class DreamSimilarityCalculator {

    /**
     * 多维度计算梦境相似度
     * 维度: 分类(30%) + 关键词(30%) + 地点(20%) + 清晰度(10%) + 是否重复(10%)
     */
    public static double calculateSimilarity(Dream dream1, Dream dream2) {
        double score = 0.0;

        // 分类权重 30%
        if (dream1.getCategoryId() != null && dream1.getCategoryId().equals(dream2.getCategoryId())) {
            score += 0.30;
        }

        // 关键词权重 30% (Jaccard相似)
        score += 0.30 * jaccardSimilarity(dream1.getKeywords(), dream2.getKeywords());

        // 地点权重 20%
        score += 0.20 * textSimilarity(dream1.getLocation(), dream2.getLocation());

        // 清晰度权重 10% (差值越小越相似)
        if (dream1.getClarity() != null && dream2.getClarity() != null) {
            double clarityDiff = Math.abs(dream1.getClarity() - dream2.getClarity());
            score += 0.10 * (1.0 - clarityDiff / 5.0);
        }

        // 是否重复权重 10%
        if (dream1.getIsRecurring() != null && dream2.getIsRecurring() != null
                && dream1.getIsRecurring().equals(dream2.getIsRecurring())) {
            score += 0.10;
        }

        return Math.round(score * 100.0) / 100.0;
    }

    private static double jaccardSimilarity(String keywords1, String keywords2) {
        if (keywords1 == null || keywords2 == null) return 0.0;
        Set<String> set1 = splitToSet(keywords1);
        Set<String> set2 = splitToSet(keywords2);
        if (set1.isEmpty() && set2.isEmpty()) return 1.0;
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return (double) intersection.size() / union.size();
    }

    private static double textSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;
        if (text1.equals(text2)) return 1.0;
        // 简单的包含判断
        if (text1.contains(text2) || text2.contains(text1)) return 0.8;
        return 0.0;
    }

    private static Set<String> splitToSet(String keywords) {
        Set<String> set = new HashSet<>();
        if (keywords != null) {
            for (String kw : keywords.split("[,，\\s]+")) {
                if (!kw.trim().isEmpty()) {
                    set.add(kw.trim().toLowerCase());
                }
            }
        }
        return set;
    }

    public static List<Map<String, Object>> findSimilar(Dream source, List<Dream> candidates) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Dream candidate : candidates) {
            if (candidate.getId().equals(source.getId())) continue;
            double score = calculateSimilarity(source, candidate);
            if (score > 0.3) {  // 相似度阈值
                Map<String, Object> item = new HashMap<>();
                item.put("dreamId", candidate.getId());
                item.put("similarityScore", score);
                item.put("matchedFields", getMatchedFields(source, candidate));
                result.add(item);
            }
        }
        result.sort((a, b) -> Double.compare((Double) b.get("similarityScore"), (Double) a.get("similarityScore")));
        return result;
    }

    private static String getMatchedFields(Dream d1, Dream d2) {
        StringBuilder sb = new StringBuilder();
        if (d1.getCategoryId() != null && d1.getCategoryId().equals(d2.getCategoryId())) sb.append("分类,");
        if (jaccardSimilarity(d1.getKeywords(), d2.getKeywords()) > 0) sb.append("关键词,");
        if (textSimilarity(d1.getLocation(), d2.getLocation()) > 0) sb.append("地点,");
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
