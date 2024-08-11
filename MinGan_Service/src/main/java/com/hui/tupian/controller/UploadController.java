package com.hui.tupian.controller;

import com.alibaba.fastjson.JSONObject;
import com.hui.tupian.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/tp")
public class UploadController {

    public static String rootPath="D:/pic";

    @PostMapping(value = "/upload")
    public Result<?> upload(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");// 获取上传文件对象

        log.info("/uploadImg->上传图片->开始");
        JSONObject jsonObject = new JSONObject();
        //String rootPath = System.getProperty("catalina.home");
        //rootPath = rootPath + "/" + image_path;
        File f = new File(rootPath);
        if (!f.exists()) {
            f.mkdir();
        }
        String uuidStr = UUID.randomUUID().toString().replace("-", "");
        String fileName = uuidStr + ".png";
        String filePath = rootPath + "/" + fileName;//本地绝对路径
        String filePathTmp = rootPath + "/" + uuidStr + "_tmp.png";
        log.info("[图片接口]上传的图片本地绝对路径为->" + filePath);
        f = new File(filePath);
        try {
            file.transferTo(f);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("[图片接口]写文件到磁盘失败");
        }
        jsonObject.put("path", fileName);
        log.info("/uploadImg->上传图片->结束->" + jsonObject.toJSONString());
        return Result.OK(jsonObject);
    }


    @GetMapping(value = "/download/**")
    public void view(HttpServletRequest request, HttpServletResponse response) {
        // ISO-8859-1 ==> UTF-8 进行编码转换
        String imgPath = extractPathFromPattern(request);
        // 其余处理略
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            imgPath = imgPath.replace("..", "").replace("../","");
            if (imgPath.endsWith(",")) {
                imgPath = imgPath.substring(0, imgPath.length() - 1);
            }
            String filePath = rootPath + File.separator + imgPath;
            File file = new File(filePath);
            if(!file.exists()){
                response.setStatus(404);
                throw new RuntimeException("文件["+imgPath+"]不存在..");
            }
            response.setContentType("application/force-download");// 设置强制下载不打开
            response.addHeader("Content-Disposition", "attachment;fileName=" + new String(file.getName().getBytes("UTF-8"),"iso-8859-1"));
            inputStream = new BufferedInputStream(new FileInputStream(filePath));
            outputStream = response.getOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error("预览文件失败" + e.getMessage());
            response.setStatus(404);
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

    }

    private static String extractPathFromPattern(final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);
    }

}
