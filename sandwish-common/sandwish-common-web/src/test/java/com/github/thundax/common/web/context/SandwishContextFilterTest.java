package com.github.thundax.common.web.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.github.thundax.common.security.context.SandwishContextHolder;
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
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertEquals("request-1", SandwishContextHolder.requestId());
            assertNull(SandwishContextHolder.currentToken());
        });

        assertEquals("request-1", response.getHeader(DefaultSandwishContextResolver.HEADER_REQUEST_ID));
        assertNull(SandwishContextHolder.requestId());
    }

    @Test
    public void shouldTolerateNullResolvedContext() throws ServletException, IOException {
        SandwishContextFilter filter = new SandwishContextFilter(request -> null);

        filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                (servletRequest, servletResponse) -> assertNull(SandwishContextHolder.requestId()));

        assertNull(SandwishContextHolder.requestId());
    }
}
