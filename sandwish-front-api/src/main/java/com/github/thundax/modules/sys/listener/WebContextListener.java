// package com.github.thundax.modules.sys.listener;
//
// import javax.servlet.ServletContext;
//
// import org.springframework.web.context.WebApplicationContext;
//
// import com.github.thundax.common.config.Global;
//
// public class WebContextListener extends org.springframework.web.context.ContextLoaderListener {
//
//    @Override
//    public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
//        System.out.println("==============================");
//        System.out.println(" Welcome to " + Global.getConfig("productName"));
//        System.out.println("==============================");
//
//        return super.initWebApplicationContext(servletContext);
//    }
// }
