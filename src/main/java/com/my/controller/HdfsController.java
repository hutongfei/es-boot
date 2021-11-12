package com.my.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.my.config.HadoopTemplate;
import io.swagger.annotations.Api;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 *
 * @Description: TODO
 * </p>
 * @ClassName HdfsController
 * @Author pl
 * @Date 2020/10/31
 * @Version V1.0.0
 */
@Api(tags = "hdfs控制器")
@RestController
@RequestMapping("/hdfs")
public class HdfsController {

    @Autowired
    private HadoopTemplate hadoopTemplate;

    /**
     * 将本地文件srcFile,上传到hdfs
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public String upload(@RequestParam(value = "file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String newPath = "D://" + originalFilename;
        File newFile = new File(newPath);
        if (!newFile.exists()) newFile.createNewFile();
        newFile = new File(newPath);
        InputStream is = file.getInputStream();
        FileOutputStream os = new FileOutputStream(newFile);
        IoUtil.copy(is, os);
        os.flush();
        os.close();
        is.close();
        hadoopTemplate.uploadFile(newPath);
        newFile.deleteOnExit();
        return originalFilename;
    }

    @DeleteMapping("/delFile")
    public String del(@RequestParam String fileName){
        hadoopTemplate.delFile(fileName);
        return "delFile";
    }

    @GetMapping("/download")
    public String download(@RequestParam String fileName,@RequestParam String savePath){
        hadoopTemplate.download(fileName,savePath);
        return "download";
    }

    @GetMapping("/open")
    public void open(@RequestParam String fileName, HttpServletResponse response) throws IOException {
        InputStream is = hadoopTemplate.getFileInputStream(fileName);
        ServletOutputStream os = response.getOutputStream();
        IoUtil.copy(is, os);
        os.close();
        is.close();
    }
}