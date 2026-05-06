package com.github.thundax.common.swagger.config;

import com.google.common.collect.Ordering;
import java.util.Comparator;
import springfox.documentation.service.ApiListingReference;

final class SwaggerApiOrdering {

    private SwaggerApiOrdering() {}

    static Ordering<ApiListingReference> apiListingReferenceOrdering() {
        return Ordering.from(new ApiListingReferenceComparator());
    }

    private static final class ApiListingReferenceComparator implements Comparator<ApiListingReference> {

        @Override
        public int compare(ApiListingReference left, ApiListingReference right) {
            int descriptionCompare = String.CASE_INSENSITIVE_ORDER.compare(description(left), description(right));
            if (descriptionCompare != 0) {
                return descriptionCompare;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(path(left), path(right));
        }

        private String description(ApiListingReference reference) {
            return reference == null || reference.getDescription() == null ? "" : reference.getDescription();
        }

        private String path(ApiListingReference reference) {
            return reference == null || reference.getPath() == null ? "" : reference.getPath();
        }
    }
}
