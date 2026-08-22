package com.aem.poc.pcdf.internal.eligibility;

import com.aem.poc.pcdf.internal.model.Promotion;

import java.util.List;

public final class TargetingRules {

    private TargetingRules() {
    }

    public static List<Promotion> filter(
            List<Promotion> eligible,
            String country,
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
            if (matches(promotion, country, brand, market, property, pageType, promo, tag)) {
                out.add(promotion);
            }
        }
        return out;
    }

    public static boolean matches(Promotion promotion, String country, String brand, String market,
            String property, String pageType, String promo, String tag) {
        return dimensionMatches(promotion.getCountries(), country)
                && dimensionMatches(promotion.getBrands(), brand)
                && dimensionMatches(promotion.getMarkets(), market)
                && dimensionMatches(promotion.getProperties(), property)
                && dimensionMatches(promotion.getPageTypes(), pageType)
                && dimensionMatches(promotion.getUrlParameters(), promo)
                && tagMatches(promotion.getTags(), tag);
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

    static boolean tagMatches(List<String> tags, String requestTag) {
        if (requestTag == null || requestTag.isEmpty()) {
            return true;
        }
        return tags != null && tags.contains(requestTag);
    }
}
