package minio.gagaduck.minioservice.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import minio.gagaduck.minioservice.config.MinioClientConfig;
import minio.gagaduck.minioservice.utils.MinioClientUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
*
* file相关的controller
*
* */
@CrossOrigin
@RestController
@RequestMapping(value = "/file")
public class MinioFileController {
    @Resource
    private MinioClientUtil minioUtil;

    @Resource
    private MinioClientConfig prop;

    // 上传file
    @PostMapping("/upload")
    public String upload(@RequestParam("bucketName") String bucketName, @RequestParam("file") MultipartFile file) {
        String objectName = minioUtil.upload(bucketName, file);
        if (null != objectName) {
            return prop.getEndpoint() + "/" + prop.getBucketName() + "/" + objectName;
        }
        return "upload error";
    }

    // 预览file
    @GetMapping("/preview")
    public String preview(@RequestParam("bucketName") String bucketName, @RequestParam("fileName") String fileName) {
        return minioUtil.preview(bucketName, fileName);
    }

    // 下载file
    @GetMapping("/download")
    public String download(@RequestParam("bucketName") String bucketName, @RequestParam("fileName") String fileName, HttpServletResponse res) {
        minioUtil.download(bucketName, fileName, res);
        return "download begins!";
    }

    // 查看当前bucket下的所有file
    @GetMapping("/listAll")
    public List<Map<String, Object>> listAll(@RequestParam("bucketName") String bucketName) throws JsonProcessingException {
        // 获取存储桶中的所有对象
        List<JSONObject> allObject = minioUtil.listObjects(bucketName);
        List<Map<String, Object>> response = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (JSONObject jsonObject : allObject) {
            response.add(mapper.readValue(jsonObject.toString(), Map.class));
        }
        return response;
    }

    @PostMapping("/delete")
    public String remove(@RequestParam("bucketName") String bucketName, @RequestParam("objName") String objName) {
        System.out.println(objName);
        if(minioUtil.remove(bucketName, objName)) {
            return objName + "removed";
        }
        return "remove error";
    }
}
