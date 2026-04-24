//package com.github.thundax.modules.sys.controller;
//
//import com.github.thundax.common.utils.FileUtils;
//import com.github.thundax.common.web.BaseApiController;
//import com.github.thundax.modules.assist.utils.StorageUtils;
//import com.github.thundax.modules.sys.entity.UploadFile;
//import com.github.thundax.modules.sys.service.UploadFileService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.imageio.ImageIO;
//import javax.servlet.http.HttpServletResponse;
//import javax.validation.Validator;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.*;
//import java.net.URLEncoder;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Map;
//
//@Controller
//@RequestMapping(value = "api/upload")
//public class UploadController extends BaseApiController {
//
//    private final UploadFileService uploadFileService;
//
//    @Value("${vltava.upload.storage-path}")
//    private String uploadPath;
//
//    public UploadController(UploadFileService uploadFileService, Validator validator){
//        super(validator);
//        this.uploadFileService = uploadFileService;
//    }
//
//    /**
//     * 上传文件
//     */
//    @PostMapping("/upload")
//    @ResponseBody
//    public Map<String,Object> upload(@RequestParam("file") MultipartFile file) throws Exception {
//        Map<String, Object> res = new HashMap<>();
//        try {
//            UploadFile uploadFile = StorageUtils.saveFile(file);
//            res.put("fileId",uploadFile.getId());
//        }catch (Exception e){
//            res.put("fileId","error");
//            e.printStackTrace();
//        }
//        return res;
//    }
//
//    /**
//     * 文件流 - 缺省图
//     *
//     * @param response
//     * @throws IOException
//     */
//    @RequestMapping(value = "file")
//    public void file(HttpServletResponse response) throws IOException {
//        writeDefaultImage(response, 1, 1);
//    }
//
//
//
//    /**
//     * 文件流 - 文件预览
//     *
//     * @param id
//     * @param response
//     * @throws IOException
//     * @throws SQLException
//     */
//    @RequestMapping(value = "file/{id}")
//    public void file(@PathVariable("id") String id, HttpServletResponse response) throws IOException, SQLException {
//        UploadFile file = uploadFileService.get(new UploadFile(id));
//        if (file == null) {
//            writeDefaultImage(response, 1, 1);
//        } else {
//            response.setHeader("Pragma", "no-cache");
//            response.setHeader("Cache-Control", "no-cache");
//            response.setDateHeader("Expires", 0);
//            response.setContentType(file.getMimeType());
//            if (!file.isImage()) {
//                response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(file.getName(),"UTF-8"));
//            }
//            OutputStream outputStream = response.getOutputStream();
//            outputStream.write(FileUtils.readFileToByteArray(new File(uploadPath + file.getPath())));
//            outputStream.close();
//        }
//    }
//
//    @RequestMapping(value = "download/{id}")
//    public void download(HttpServletResponse response, @PathVariable String id) {
//        UploadFile entity = uploadFileService.get(new UploadFile(id));
//        try {
//            // path是指欲下载的文件的路径。
//            File file = new File(uploadPath + entity.getPath());
//            // 取得文件名。
//            String filename = file.getName();
//            // 取得文件的后缀名。
//            String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();
//            // 以流的形式下载文件。
//            InputStream fis = new BufferedInputStream(new FileInputStream(uploadPath + entity.getPath()));
//            byte[] buffer = new byte[fis.available()];
//            fis.read(buffer);
//            fis.close();
//            // 清空response
//            response.reset();
//            // 设置response的Header
//            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
//            response.addHeader("Content-Length", "" + file.length());
//            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
//            response.setContentType("application/octet-stream");
//            toClient.write(buffer);
//            toClient.flush();
//            toClient.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private void writeDefaultImage(HttpServletResponse response, int width, int height) throws IOException {
//        response.setHeader("Pragma", "no-cache");
//        response.setHeader("Cache-Control", "no-cache");
//        response.setDateHeader("Expires", 0);
//        response.setContentType("image/jpeg");
//
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        Graphics graph = image.getGraphics();
//        graph.setColor(new Color(0xC0, 0xC0, 0xC0));
//        graph.fillRect(0, 0, width, height);
//        graph.dispose();
//        OutputStream outputStream = response.getOutputStream();
//        ImageIO.write(image, "JPEG", outputStream);
//        outputStream.close();
//    }
//
//}
