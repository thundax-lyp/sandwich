package com.github.thundax.common.web.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class DefaultSandwishContextResolverTest {

    private final DefaultSandwishContextResolver resolver = new DefaultSandwishContextResolver();

    @Test
    public void shouldResolveContextFromHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(DefaultSandwishContextResolver.HEADER_REQUEST_ID, "request-1");

        String requestId = resolver.resolveRequestId(request);

        assertEquals("request-1", requestId);
    }

    @Test
    public void shouldGenerateRequestIdWhenHeaderMissing() {
        String requestId = resolver.resolveRequestId(new MockHttpServletRequest());

        assertNotNull(requestId);
    }
}
