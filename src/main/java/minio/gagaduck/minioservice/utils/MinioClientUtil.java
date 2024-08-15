package minio.gagaduck.minioservice.utils;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MinioClientUtil {

    @Resource
    private MinioClient minioClient;

    // 与bucket相关的内容
    // 是否存在
    public Boolean bucketExists(String bucketName) {
        Boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return found;
    }

    // 创建bucket
    public Boolean makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // 删除bucket
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // 获取全部bucket
    public List<JSONObject> getAllBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            List<JSONObject> jsonObjects = new ArrayList<>();
            for (Bucket bucket : buckets) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", bucket.name());
                jsonObject.put("creationDate", bucket.creationDate());
                jsonObjects.add(jsonObject);
            }
            return jsonObjects;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //文件传输相关内容工具

    // 上传文件
    public String upload(String bucketName, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)){
            throw new RuntimeException();
        }
//        String fileName = UuidUtils.generateUuid() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = originalFilename;
        // 创建一个DateTimeFormatter对象，用于格式化日期时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM/dd");
        // 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();
        // 格式化日期时间
        String formattedDate = now.format(formatter);
        // 拼接日期字符串和文件名
        String objectName = formattedDate + "/" + fileName;
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucketName).object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return objectName;
    }

    // 预览图片和文档
    // 图片就返回对应url
    // 文档返回对应文档
    public String preview(String bucketName, String fileName){
        // 查看文件地址
        GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder().bucket(bucketName).object(fileName).method(Method.GET).build();
        try {
            return minioClient.getPresignedObjectUrl(build);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 文件下载
    // 通过fileName来下载
    // 文件下载，通过fileName来下载
    public void download(String bucketName, String fileName, HttpServletResponse res) {
        GetObjectArgs objectArgs = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build();
        try (GetObjectResponse response = minioClient.getObject(objectArgs);
             ServletOutputStream outputStream = res.getOutputStream()) {
            // 设置响应头
            res.setCharacterEncoding("utf-8");
            res.setContentType("application/octet-stream");
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            res.addHeader("Content-Disposition", "attachment;filename=\"" + encodedFileName + "\"");
            // 使用更大的缓冲区以提高性能
            byte[] buf = new byte[8192];
            int len;
            while ((len = response.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.flush();
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public List<JSONObject> listObjects(String bucketName) {
        return listObjectsRecursive(bucketName, "");
    }

    private List<JSONObject> listObjectsRecursive(String bucketName, String prefix) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(true).build());
        List<JSONObject> items = new ArrayList<>();
        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("isDir", item.isDir());
                jsonObject.put("ObjectName", item.objectName());
                jsonObject.put("size", item.size());
                jsonObject.put("lastModified", item.lastModified());
                items.add(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return items;
    }


    // 删除文件对象
    public boolean remove(String bucketName, String fileName){
        try {
            minioClient.removeObject( RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        }catch (Exception e){
            return false;
        }
        return true;
    }

}

