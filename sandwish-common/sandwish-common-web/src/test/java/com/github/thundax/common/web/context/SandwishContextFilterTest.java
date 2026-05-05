package com.github.thundax.common.web.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.github.thundax.common.context.SandwishContextHolder;
import java.io.IOException;
import javax.servlet.ServletException;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class SandwishContextFilterTest {

    @After
    public void tearDown() {
        SandwishContextHolder.clear();
    }

    @Test
    public void shouldBindAndClearContextForRequest() throws ServletException, IOException {
        SandwishContextFilter filter = new SandwishContextFilter(new DefaultSandwishContextResolver());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(DefaultSandwishContextResolver.HEADER_REQUEST_ID, "request-1");
        request.addHeader(DefaultSandwishContextResolver.HEADER_TOKEN, "token-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertEquals("request-1", SandwishContextHolder.getContext().getRequestId());
            assertEquals("token-1", SandwishContextHolder.getContext().getToken());
        });

        assertEquals("request-1", response.getHeader(DefaultSandwishContextResolver.HEADER_REQUEST_ID));
        assertNull(SandwishContextHolder.getContext().getRequestId());
    }

    @Test
    public void shouldTolerateNullResolvedContext() throws ServletException, IOException {
        SandwishContextFilter filter = new SandwishContextFilter(request -> null);

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (servletRequest, servletResponse) ->
                        assertNull(SandwishContextHolder.getContext().getRequestId()));

        assertNull(SandwishContextHolder.getContext().getRequestId());
    }
}
