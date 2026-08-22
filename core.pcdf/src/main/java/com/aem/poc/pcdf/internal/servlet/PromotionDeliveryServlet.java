package com.aem.poc.pcdf.internal.servlet;

import com.aem.poc.pcdf.internal.eligibility.DateWindow;
import com.aem.poc.pcdf.internal.eligibility.EligibilityService;
import com.aem.poc.pcdf.internal.eligibility.Ranking;
import com.aem.poc.pcdf.internal.eligibility.TargetingRules;
import com.aem.poc.pcdf.internal.model.Promotion;
import com.aem.poc.pcdf.internal.service.PromotionQueryService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
        service = Servlet.class,
        property = {
            "sling.servlet.paths=/services/aem-pocs/pcdf",
            "sling.servlet.methods=GET"
        })
public class PromotionDeliveryServlet extends SlingSafeMethodsServlet {

    @Reference
    private PromotionQueryService queryService;

    @Reference
    private EligibilityService eligibilityService;

    @Reference
    private SlingSettingsService slingSettings;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String locale = request.getParameter("locale");
        if (locale == null || locale.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"locale_required\"}");
            return;
        }

        String previewDateRaw = request.getParameter("previewDate");
        boolean author = slingSettings.getRunModes().contains("author");
        if (previewDateRaw != null && !previewDateRaw.isBlank() && !author) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"preview_not_allowed\"}");
            return;
        }

        LocalDate evaluationDate = DateWindow.todayOnInstance();
        if (previewDateRaw != null && !previewDateRaw.isBlank()) {
            try {
                evaluationDate = LocalDate.parse(previewDateRaw);
            } catch (DateTimeParseException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"invalid_previewDate\"}");
                return;
            }
        }

        if (!queryService.localeFolderExists(request.getResourceResolver(), locale)) {
            writeNoMatch(response);
            return;
        }

        List<Promotion> published = queryService.listPublished(request.getResourceResolver(), locale);
        List<Promotion> eligible = eligibilityService.filterEligible(published, evaluationDate);
        List<Promotion> targeted = TargetingRules.filter(
                eligible,
                request.getParameter("country"),
                request.getParameter("brand"),
                request.getParameter("market"),
                request.getParameter("property"),
                request.getParameter("pageType"),
                request.getParameter("promo"),
                request.getParameter("tag"));
        Promotion winner = Ranking.pickWinner(targeted);
        if (winner == null) {
            writeNoMatch(response);
            return;
        }
        writeMatch(response, winner);
    }

    private static void writeNoMatch(SlingHttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter()
                .write(
                        "{\"contentFound\":false,\"promotionId\":\"\",\"headline\":\"\",\"body\":\"\","
                                + "\"image\":\"\",\"ctaText\":\"\",\"ctaLink\":\"\"}");
    }

    private static void writeMatch(SlingHttpServletResponse response, Promotion p) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter()
                .write(
                        "{\"contentFound\":true"
                                + ",\"promotionId\":"
                                + jsonString(p.getPromotionId())
                                + ",\"headline\":"
                                + jsonString(p.getHeadline())
                                + ",\"body\":"
                                + jsonString(p.getBody())
                                + ",\"image\":"
                                + jsonString(p.getImage())
                                + ",\"ctaText\":"
                                + jsonString(p.getCtaText())
                                + ",\"ctaLink\":"
                                + jsonString(p.getCtaLink())
                                + "}");
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
