package com.github.thundax.modules.sys.ueditor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;

/**
 * 为了统一JSON解析的代码，将百度编辑器中的引用的JSONObject换成反序列化的方式 删除了视频上传、涂鸦、截图、文件上传功能，只提供图片上传功能
 *
 * @author wdit
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 上传大小限制，单位B */
    private Long imageMaxSize;
    /** 上传图片格式显示 */
    private List<String> imageAllowFiles;
    /** 提交的图片表单名称 */
    private String imageFieldName;

    private String imagePathFormat;
    /**
     * /ueditor/php/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}" {filename} 会替换成原文件名,配置这项需要注意中文乱码问题
     * {rand:6} 会替换成随机数,后面的数字是随机数的位数 {time} 会替换成时间戳 {yyyy} 会替换成四位年份 {yy} 会替换成两位年份 {mm} 会替换成两位月份 {dd}
     * 会替换成两位日期 {hh} 会替换成两位小时 {ii} 会替换成两位分钟 {ss} 会替换成两位秒 非法字符 \ : * ? " < > | 具请体看线上文档:
     * fex.baidu.com/ueditor/#use-format_upload_filename
     */

    /** 执行上传图片的action名称 */
    private String imageActionName;
    /** 是否压缩图片,默认是true */
    private String imageCompressEnable;
    /** 图片压缩最长边限制 */
    private Integer imageCompressBorder;
    /** 插入的图片浮动方式 */
    private String imageInsertAlign;
    /** 图片访问路径前缀 */
    private String imageUrlPrefix;

    public Long getImageMaxSize() {
        return this.imageMaxSize;
    }

    public void setImageMaxSize(Long imageMaxSize) {
        this.imageMaxSize = imageMaxSize;
    }

    public List<String> getImageAllowFiles() {
        return this.imageAllowFiles;
    }

    public void setImageAllowFiles(List<String> imageAllowFiles) {
        this.imageAllowFiles = imageAllowFiles;
    }

    public String getImageFieldName() {
        return this.imageFieldName;
    }

    public void setImageFieldName(String imageFieldName) {
        this.imageFieldName = imageFieldName;
    }

    public String getImagePathFormat() {
        return this.imagePathFormat;
    }

    public void setImagePathFormat(String imagePathFormat) {
        this.imagePathFormat = imagePathFormat;
    }

    public String getImageActionName() {
        return imageActionName;
    }

    public void setImageActionName(String imageActionName) {
        this.imageActionName = imageActionName;
    }

    public String getImageCompressEnable() {
        return imageCompressEnable;
    }

    public void setImageCompressEnable(String imageCompressEnable) {
        this.imageCompressEnable = imageCompressEnable;
    }

    public Integer getImageCompressBorder() {
        return imageCompressBorder;
    }

    public void setImageCompressBorder(Integer imageCompressBorder) {
        this.imageCompressBorder = imageCompressBorder;
    }

    public String getImageInsertAlign() {
        return imageInsertAlign;
    }

    public void setImageInsertAlign(String imageInsertAlign) {
        this.imageInsertAlign = imageInsertAlign;
    }

    public String getImageUrlPrefix() {
        return imageUrlPrefix;
    }

    public void setImageUrlPrefix(String imageUrlPrefix) {
        this.imageUrlPrefix = imageUrlPrefix;
    }
}
