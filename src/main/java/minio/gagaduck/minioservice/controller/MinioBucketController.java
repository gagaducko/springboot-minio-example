package minio.gagaduck.minioservice.controller;

import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import minio.gagaduck.minioservice.utils.MinioClientUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
*
* minio bucket的controller
*
* */
@RestController
@CrossOrigin
@RequestMapping(value = "/bucket")
public class MinioBucketController {

    @Resource
    private MinioClientUtil minioUtil;

    // get所有bucket
    @GetMapping("allBucket")
    public List<JSONObject> getAllBucket() {
        System.out.println("getAllBucket");;
        return minioUtil.getAllBuckets();
    }

    // 看bucket是否存在
    @GetMapping("bucketExists")
    public Boolean bucketExists(@RequestParam("bucketName") String bucketName) {
        return minioUtil.bucketExists(bucketName);
    }

    // 创建新的bucket
    @PostMapping("makeBucket")
    public Boolean makeBucket(@RequestParam("bucketName") String bucketName) {
        System.out.println("bucketname is:" + bucketName);
        return minioUtil.makeBucket(bucketName);
    }

    // 删除bucket
    @PostMapping("removeBucket")
    public Boolean removeBucket(@RequestParam("bucketName") String bucketName) {
        return minioUtil.removeBucket(bucketName);
    }

}
