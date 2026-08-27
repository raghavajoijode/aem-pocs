package com.aem.poc.pcdf.internal.service;

import com.aem.poc.pcdf.internal.eligibility.DateWindow;
import com.aem.poc.pcdf.internal.model.Promotion;
import com.aem.poc.pcdf.internal.tags.PromotionTags;
import com.aem.poc.pcdf.internal.topology.TopologyPath;
import com.day.cq.replication.ReplicationStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    private static final Logger LOG = LoggerFactory.getLogger(PromotionQueryService.class);

    @Reference
    private SlingSettingsService slingSettings;

    public boolean localeFolderExists(ResourceResolver resolver, String region, String country, String locale) {
        if (!TopologyPath.isSafe(region, country, locale)) {
            return false;
        }
        return resolver.getResource(TopologyPath.folderPath(region, country, locale)) != null;
    }

    /**
     * Published fragments only (Author: replication activated; Publish: repository content).
     */
    public List<Promotion> listPublished(ResourceResolver resolver, String region, String country, String locale) {
        if (!TopologyPath.isSafe(region, country, locale)) {
            return Collections.emptyList();
        }
        Resource folder = resolver.getResource(TopologyPath.folderPath(region, country, locale));
        if (folder == null) {
            return Collections.emptyList();
        }
        boolean author = slingSettings.getRunModes().contains("author");
        List<Promotion> out = new ArrayList<>();
        for (Resource child : folder.getChildren()) {
            Resource jcrContent = child.getChild("jcr:content");
            if (jcrContent == null || jcrContent.getChild("data") == null) {
                continue;
            }
            if (author && !isActivated(child)) {
                continue;
            }
            Promotion p = mapFragment(child);
            if (p != null) {
                out.add(p);
            }
        }
        return out;
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
        p.setPriority(vm.get("priority", 0));
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
