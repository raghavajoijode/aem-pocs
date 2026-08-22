package com.aem.poc.pcdf.internal.eligibility;

import com.aem.poc.pcdf.internal.model.Promotion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class Ranking {

    private Ranking() {
    }

    public static Optional<Promotion> winner(List<Promotion> matches) {
        if (matches == null || matches.isEmpty()) {
            return Optional.empty();
        }
        return matches.stream()
                .max(Comparator.comparingInt(Promotion::getPriority)
                        .thenComparing(Promotion::getPromotionId, Comparator.reverseOrder()));
    }

    public static Promotion pickWinner(List<Promotion> matches) {
        return winner(matches).orElse(null);
    }
}
