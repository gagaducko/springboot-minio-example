package minio.gagaduck.minioservice.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
*
* minio的配置
*
* */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioClientConfig {

    // endpoint
    private String endpoint;

    // accessKey
    private String accessKey;

    // secretKey
    private String secretKey;

    // bucketName
    private String bucketName;


    @Bean
    public MinioClient  minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
