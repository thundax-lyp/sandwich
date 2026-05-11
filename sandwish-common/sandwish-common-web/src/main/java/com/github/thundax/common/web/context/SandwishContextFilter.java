package com.github.thundax.common.web.context;

import com.github.thundax.common.security.context.SandwishContextHolder;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

public class SandwishContextFilter extends OncePerRequestFilter {

    private final SandwishContextResolver resolver;

    public SandwishContextFilter(SandwishContextResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = resolver.resolveRequestId(request);
        SandwishContextHolder.setRequestId(requestId);
        if (requestId != null) {
            response.setHeader(DefaultSandwishContextResolver.HEADER_REQUEST_ID, requestId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            SandwishContextHolder.clearRequestContext();
        }
    }
}
