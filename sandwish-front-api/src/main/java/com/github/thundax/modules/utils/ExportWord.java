package com.github.thundax.modules.utils;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @Auther: zhangrudong
 * @Date: 2022/1/10 17:26
 * @Description:
 */
public class ExportWord {

    private FreeMarkerConfigurer configurer;

    /**
     * 构造函数
     * 配置模板路径
     */
    public ExportWord() throws Exception {
        configurer = new FreeMarkerConfigurer();

        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setDefaultEncoding("UTF-8");
        ClassTemplateLoader loader = new ClassTemplateLoader(ExportWord.class, "/templates");
        configuration.setTemplateLoader(loader);

        configurer.setConfiguration(configuration);
    }

    /**
     * 获取模板
     * @param name
     * @return
     * @throws Exception
     */
    public Template getTemplate(String name) throws Exception {
        return configurer.getConfiguration().getTemplate(name);
    }

    /**
     * 导出word文档到客户端
     * @param response
     * @param fileName
     * @param tplName
     * @param data
     * @throws Exception
     */
    public void exportDoc(HttpServletResponse response, String fileName, String tplName, Map<String, Object> data) throws Exception {
        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/msword");
        response.setHeader("Content-Disposition", "attachment; filename=" +  URLEncoder.encode(fileName , "UTF-8"));
        // 把本地文件发送给客户端
        Writer out = response.getWriter();
        Template template = getTemplate(tplName);
        template.process(data, out);
        out.close();
    }
}
