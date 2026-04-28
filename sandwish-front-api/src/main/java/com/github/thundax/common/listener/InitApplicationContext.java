package com.github.thundax.common.listener;

import com.github.thundax.common.utils.SpringContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class InitApplicationContext implements ApplicationContextAware, DisposableBean {

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.setApplicationContext(applicationContext);
    }

    @Override
    public void destroy() {
        SpringContextHolder.clearHolder();
    }
}
