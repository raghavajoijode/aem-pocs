package com.aem.poc.pcdf.internal.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class Promotion {

    private String promotionId = "";
    private String status = "";
    private int priority;
    private List<String> tags = Collections.emptyList();
    private String headline = "";
    private String body = "";
    private String image = "";
    private String ctaText = "";
    private String ctaLink = "";
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> countries = Collections.emptyList();
    private List<String> markets = Collections.emptyList();
    private List<String> brands = Collections.emptyList();
    private List<String> properties = Collections.emptyList();
    private List<String> pageTypes = Collections.emptyList();
    private List<String> urlParameters = Collections.emptyList();

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId == null ? "" : promotionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? "" : status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? Collections.emptyList() : tags;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline == null ? "" : headline;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body == null ? "" : body;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image == null ? "" : image;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText == null ? "" : ctaText;
    }

    public String getCtaLink() {
        return ctaLink;
    }

    public void setCtaLink(String ctaLink) {
        this.ctaLink = ctaLink == null ? "" : ctaLink;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries == null ? Collections.emptyList() : countries;
    }

    public List<String> getMarkets() {
        return markets;
    }

    public void setMarkets(List<String> markets) {
        this.markets = markets == null ? Collections.emptyList() : markets;
    }

    public List<String> getBrands() {
        return brands;
    }

    public void setBrands(List<String> brands) {
        this.brands = brands == null ? Collections.emptyList() : brands;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties == null ? Collections.emptyList() : properties;
    }

    public List<String> getPageTypes() {
        return pageTypes;
    }

    public void setPageTypes(List<String> pageTypes) {
        this.pageTypes = pageTypes == null ? Collections.emptyList() : pageTypes;
    }

    public List<String> getUrlParameters() {
        return urlParameters;
    }

    public void setUrlParameters(List<String> urlParameters) {
        this.urlParameters = urlParameters == null ? Collections.emptyList() : urlParameters;
    }
}
