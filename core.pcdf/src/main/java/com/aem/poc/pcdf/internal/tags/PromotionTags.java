package com.aem.poc.pcdf.internal.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * PCDF uses Content Fragment tags (cq:tags), not model fields, for status and brand.
 *
 * <ul>
 *   <li>{@code pcdf:status/ACTIVE} or {@code pcdf:status/INACTIVE}
 *   <li>{@code pcdf:brand/TH} — empty brand tags mean match-all
 *   <li>{@code pcdf:topic/estate} — optional topic tags for request {@code tag}
 * </ul>
 */
public final class PromotionTags {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String NS_STATUS = "pcdf:status/";
    public static final String NS_BRAND = "pcdf:brand/";

    private PromotionTags() {
    }

    public static String status(List<String> cqTags) {
        for (String tag : cqTags == null ? List.<String>of() : cqTags) {
            if (tag != null && tag.startsWith(NS_STATUS)) {
                return leaf(tag).toUpperCase(Locale.ROOT);
            }
        }
        return "";
    }

    public static List<String> brands(List<String> cqTags) {
        return namespaced(cqTags, NS_BRAND);
    }

    public static boolean topicMatches(List<String> cqTags, String requestTag) {
        if (requestTag == null || requestTag.isEmpty()) {
            return true;
        }
        if (cqTags == null || cqTags.isEmpty()) {
            return false;
        }
        for (String tag : cqTags) {
            if (tag == null) {
                continue;
            }
            if (requestTag.equals(tag) || requestTag.equals(leaf(tag))) {
                return true;
            }
        }
        return false;
    }

    private static List<String> namespaced(List<String> cqTags, String prefix) {
        if (cqTags == null || cqTags.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String tag : cqTags) {
            if (tag != null && tag.startsWith(prefix)) {
                String value = leaf(tag);
                if (!value.isEmpty() && seen.add(value)) {
                    out.add(value);
                }
            }
        }
        return out;
    }

    private static String leaf(String tagId) {
        int slash = tagId.lastIndexOf('/');
        return slash < 0 ? tagId : tagId.substring(slash + 1);
    }
}
