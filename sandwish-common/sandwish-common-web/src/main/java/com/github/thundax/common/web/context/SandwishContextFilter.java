package com.github.thundax.common.web.context;

import com.github.thundax.common.context.SandwishContext;
import com.github.thundax.common.context.SandwishContextHolder;
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
        SandwishContext context = resolver.resolve(request);
        if (context == null) {
            context = new SandwishContext();
        }
        SandwishContextHolder.setContext(context);
        if (context.getRequestId() != null) {
            response.setHeader(DefaultSandwishContextResolver.HEADER_REQUEST_ID, context.getRequestId());
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            SandwishContextHolder.clear();
        }
    }
}
