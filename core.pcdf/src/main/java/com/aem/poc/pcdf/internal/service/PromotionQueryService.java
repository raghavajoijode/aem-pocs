package com.aem.poc.pcdf.internal.service;

import com.aem.poc.pcdf.internal.eligibility.DateWindow;
import com.aem.poc.pcdf.internal.model.Promotion;
import com.aem.poc.pcdf.internal.tags.PromotionTags;
import com.aem.poc.pcdf.internal.topology.TopologyPath;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = PromotionQueryService.class)
public class PromotionQueryService {

    public static final String DAM_ROOT = TopologyPath.DAM_ROOT;
    public static final String MODEL_PATH =
            "/conf/aem-poc-pcdf/settings/dam/cfm/models/programmatic-promotion";
    public static final String TAGS_PROPERTY = "jcr:content/metadata/cq:tags";

    private static final Logger LOG = LoggerFactory.getLogger(PromotionQueryService.class);

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private SlingSettingsService slingSettings;

    public boolean localeFolderExists(ResourceResolver resolver, String region, String country, String locale) {
        if (!TopologyPath.isSafe(region, country, locale)) {
            return false;
        }
        return resolver.getResource(TopologyPath.folderPath(region, country, locale)) != null;
    }

    /**
     * QueryBuilder: path = region/country/locale, optional CF node name, ACTIVE (+ brand/topic)
     * tags. Start/end dates are not queried here — callers apply {@code DateWindow} next.
     */
    public List<Promotion> listPublished(
            ResourceResolver resolver,
            String region,
            String country,
            String locale,
            String fragmentName,
            String brand,
            String topicTag) {
        if (!TopologyPath.isSafe(region, country, locale)) {
            return Collections.emptyList();
        }
        if (fragmentName != null && !fragmentName.isBlank() && !TopologyPath.isSafeFragmentName(fragmentName)) {
            return Collections.emptyList();
        }
        Session session = resolver.adaptTo(Session.class);
        if (session == null) {
            LOG.warn("No JCR session; cannot run QueryBuilder");
            return Collections.emptyList();
        }
        Map<String, String> predicates = buildPredicates(region, country, locale, fragmentName, brand, topicTag);
        Query query = queryBuilder.createQuery(PredicateGroup.create(predicates), session);
        SearchResult result = query.getResult();
        boolean author = slingSettings.getRunModes().contains("author");
        List<Promotion> out = new ArrayList<>();
        try {
            for (Hit hit : result.getHits()) {
                Resource asset = resolver.getResource(hit.getPath());
                if (asset == null) {
                    continue;
                }
                Resource jcrContent = asset.getChild("jcr:content");
                if (jcrContent == null || jcrContent.getChild("data") == null) {
                    continue;
                }
                if (author && !isActivated(asset)) {
                    continue;
                }
                Promotion p = mapFragment(asset);
                if (p != null) {
                    out.add(p);
                }
            }
        } catch (RepositoryException e) {
            LOG.warn("QueryBuilder hit read failed under {}", TopologyPath.folderPath(region, country, locale), e);
            return Collections.emptyList();
        }
        return out;
    }

    Map<String, String> buildPredicates(
            String region,
            String country,
            String locale,
            String fragmentName,
            String brand,
            String topicTag) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("path", TopologyPath.folderPath(region, country, locale));
        map.put("type", "dam:Asset");
        map.put("p.limit", "-1");
        map.put("property", "jcr:content/data/cq:model");
        map.put("property.value", MODEL_PATH);
        if (fragmentName != null && !fragmentName.isBlank()) {
            map.put("nodename", fragmentName);
        }
        int tagIndex = 1;
        map.put("group.p.and", "true");
        tagIndex = putTag(map, tagIndex, PromotionTags.NS_STATUS + PromotionTags.STATUS_ACTIVE);
        if (brand != null && !brand.isBlank() && TopologyPath.isSafeFragmentName(brand)) {
            tagIndex = putTag(map, tagIndex, PromotionTags.NS_BRAND + brand);
        }
        if (topicTag != null && !topicTag.isBlank() && TopologyPath.isSafeFragmentName(topicTag)) {
            putTag(map, tagIndex, PromotionTags.NS_TOPIC + topicTag);
        }
        return map;
    }

    private static int putTag(Map<String, String> map, int index, String tagId) {
        String prefix = "group." + index + "_tagid";
        map.put(prefix, tagId);
        map.put(prefix + ".property", TAGS_PROPERTY);
        return index + 1;
    }

    private static boolean isActivated(Resource asset) {
        ReplicationStatus status = asset.adaptTo(ReplicationStatus.class);
        if (status != null) {
            return status.isActivated();
        }
        Resource jcr = asset.getChild("jcr:content");
        if (jcr == null) {
            return false;
        }
        return "Activate".equals(jcr.getValueMap().get("cq:lastReplicationAction", ""));
    }

    private static Promotion mapFragment(Resource asset) {
        Resource master = asset.getChild("jcr:content/data/master");
        if (master == null) {
            LOG.debug("Skipping fragment without master: {}", asset.getPath());
            return null;
        }
        ValueMap vm = master.getValueMap();
        String id = firstString(vm, "promotionId");
        if (id.isBlank()) {
            LOG.debug("Skipping fragment without promotionId: {}", asset.getPath());
            return null;
        }
        List<String> cqTags = collectCqTags(asset, master);
        Promotion p = new Promotion();
        p.setPromotionId(id);
        p.setStatus(PromotionTags.status(cqTags));
        p.setStartDate(DateWindow.toLocalDate(vm.get("startDate")));
        p.setEndDate(DateWindow.toLocalDate(vm.get("endDate")));
        p.setBrands(PromotionTags.brands(cqTags));
        p.setMarkets(stringList(vm, "markets"));
        p.setProperties(stringList(vm, "properties"));
        p.setPageTypes(stringList(vm, "pageTypes"));
        p.setUrlParameters(stringList(vm, "urlParameters"));
        p.setTags(cqTags);
        p.setHeadline(firstString(vm, "headline"));
        p.setBody(firstString(vm, "body"));
        p.setImage(firstString(vm, "image"));
        p.setCtaText(firstString(vm, "ctaText"));
        p.setCtaLink(firstString(vm, "ctaLink"));
        return p;
    }

    private static List<String> collectCqTags(Resource asset, Resource master) {
        Set<String> tags = new LinkedHashSet<>();
        addTags(tags, master.getValueMap());
        Resource jcrContent = asset.getChild("jcr:content");
        if (jcrContent != null) {
            addTags(tags, jcrContent.getValueMap());
            Resource metadata = jcrContent.getChild("metadata");
            if (metadata != null) {
                addTags(tags, metadata.getValueMap());
            }
        }
        return new ArrayList<>(tags);
    }

    private static void addTags(Set<String> tags, ValueMap vm) {
        String[] arr = vm.get("cq:tags", String[].class);
        if (arr != null) {
            for (String tag : arr) {
                if (tag != null && !tag.isBlank()) {
                    tags.add(tag);
                }
            }
        }
    }

    private static String firstString(ValueMap vm, String name) {
        String[] arr = vm.get(name, String[].class);
        if (arr != null && arr.length > 0 && arr[0] != null) {
            return arr[0];
        }
        String value = vm.get(name, String.class);
        return value == null ? "" : value;
    }

    private static List<String> stringList(ValueMap vm, String name) {
        String[] arr = vm.get(name, String[].class);
        if (arr != null && arr.length > 0) {
            return Arrays.asList(arr);
        }
        String value = vm.get(name, String.class);
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(value);
    }
}
