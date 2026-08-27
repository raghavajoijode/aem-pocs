package com.aem.poc.pcdf.internal.eligibility;

import com.aem.poc.pcdf.internal.model.Promotion;
import com.aem.poc.pcdf.internal.tags.PromotionTags;

import java.util.List;

public final class TargetingRules {

    private TargetingRules() {
    }

    public static List<Promotion> filter(
            List<Promotion> eligible,
            String brand,
            String market,
            String property,
            String pageType,
            String promo,
            String tag) {
        if (eligible == null || eligible.isEmpty()) {
            return List.of();
        }
        List<Promotion> out = new java.util.ArrayList<>();
        for (Promotion promotion : eligible) {
            if (matches(promotion, brand, market, property, pageType, promo, tag)) {
                out.add(promotion);
            }
        }
        return out;
    }

    public static boolean matches(Promotion promotion, String brand, String market,
            String property, String pageType, String promo, String tag) {
        return dimensionMatches(promotion.getBrands(), brand)
                && dimensionMatches(promotion.getMarkets(), market)
                && dimensionMatches(promotion.getProperties(), property)
                && dimensionMatches(promotion.getPageTypes(), pageType)
                && dimensionMatches(promotion.getUrlParameters(), promo)
                && PromotionTags.topicMatches(promotion.getTags(), tag);
    }

    static boolean dimensionMatches(List<String> authored, String requestValue) {
        if (requestValue == null || requestValue.isEmpty()) {
            return true;
        }
        if (authored == null || authored.isEmpty()) {
            return true;
        }
        return authored.contains(requestValue);
    }
}
