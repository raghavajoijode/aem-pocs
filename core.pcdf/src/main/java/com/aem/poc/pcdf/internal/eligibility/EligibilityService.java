package com.aem.poc.pcdf.internal.eligibility;

import com.aem.poc.pcdf.internal.model.Promotion;
import com.aem.poc.pcdf.internal.tags.PromotionTags;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.osgi.service.component.annotations.Component;

@Component(service = EligibilityService.class)
public class EligibilityService {

    public static final String STATUS_ACTIVE = PromotionTags.STATUS_ACTIVE;

    public List<Promotion> filterEligible(List<Promotion> published, LocalDate evaluationDate) {
        if (published == null || published.isEmpty()) {
            return Collections.emptyList();
        }
        List<Promotion> out = new ArrayList<>();
        for (Promotion promotion : published) {
            if (isEligible(promotion, evaluationDate)) {
                out.add(promotion);
            }
        }
        return out;
    }

    public boolean isEligible(Promotion promotion, LocalDate evaluationDate) {
        if (promotion == null || evaluationDate == null) {
            return false;
        }
        if (!STATUS_ACTIVE.equalsIgnoreCase(promotion.getStatus())) {
            return false;
        }
        return DateWindow.includes(promotion.getStartDate(), promotion.getEndDate(), evaluationDate);
    }
}
