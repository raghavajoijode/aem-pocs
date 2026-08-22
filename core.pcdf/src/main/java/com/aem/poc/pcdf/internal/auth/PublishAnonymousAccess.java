package com.aem.poc.pcdf.internal.auth;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * Activates only when Publish runmode config is present. Registers an extra
 * {@code sling.auth.requirements} entry so GET {@code /services/aem-poc/pcdf}
 * is anonymous on Publish without replacing the SlingAuthenticator PID.
 * Do not put this property on {@code PromotionDeliveryServlet}: Sling authenticates
 * before the servlet runs, and a servlet-level exemption would also open Author.
 */
@Component(
        service = PublishAnonymousAccess.class,
        configurationPid = "com.aem.poc.pcdf.internal.auth.PublishAnonymousAccess",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = {"sling.auth.requirements=-/services/aem-poc/pcdf"})
public class PublishAnonymousAccess {
}
