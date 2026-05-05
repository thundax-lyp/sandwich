package com.github.thundax.common.web.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.thundax.common.context.SandwishContext;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class DefaultSandwishContextResolverTest {

    private final DefaultSandwishContextResolver resolver = new DefaultSandwishContextResolver();

    @Test
    public void shouldResolveContextFromHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(DefaultSandwishContextResolver.HEADER_REQUEST_ID, "request-1");
        request.addHeader(DefaultSandwishContextResolver.HEADER_USER_ID, "user-1");
        request.addHeader(DefaultSandwishContextResolver.HEADER_LOGIN_NAME, "admin");
        request.addHeader(DefaultSandwishContextResolver.HEADER_TOKEN, "token-1");

        SandwishContext context = resolver.resolve(request);

        assertEquals("request-1", context.getRequestId());
        assertEquals("user-1", context.getUserId());
        assertEquals("admin", context.getLoginName());
        assertEquals("token-1", context.getToken());
    }

    @Test
    public void shouldGenerateRequestIdWhenHeaderMissing() {
        SandwishContext context = resolver.resolve(new MockHttpServletRequest());

        assertNotNull(context.getRequestId());
    }
}
