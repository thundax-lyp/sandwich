package com.github.thundax.common.swagger.support;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import springfox.documentation.service.ApiListingReference;

public class SwaggerApiOrderingTest {

    @Test
    public void shouldSortApiListingReferencesByDescriptionAlphabetically() {
        List<ApiListingReference> references = Arrays.asList(
                new ApiListingReference("/z", "System/User", 0),
                new ApiListingReference("/a", "Auth", 0),
                new ApiListingReference("/s", "system/Current User", 0));

        references = SwaggerApiOrdering.apiListingReferenceOrdering().sortedCopy(references);

        assertEquals("Auth", references.get(0).getDescription());
        assertEquals("system/Current User", references.get(1).getDescription());
        assertEquals("System/User", references.get(2).getDescription());
    }

    @Test
    public void shouldUsePathAsTieBreaker() {
        List<ApiListingReference> references = Arrays.asList(
                new ApiListingReference("/b", "System/User", 0), new ApiListingReference("/a", "System/User", 0));

        references = SwaggerApiOrdering.apiListingReferenceOrdering().sortedCopy(references);

        assertEquals("/a", references.get(0).getPath());
        assertEquals("/b", references.get(1).getPath());
    }
}
