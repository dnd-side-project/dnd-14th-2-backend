package com.example.demo.infrastructure.aws;

import com.example.demo.domain.FileStorage;
import java.io.InputStream;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@Profile({"dev", "prod"})
public class S3FileStorage implements FileStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3FileStorage(S3Client s3Client,
                         S3Presigner s3Presigner,
                         @Value("${spring.cloud.aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    @Override
    public void upload(String key, InputStream inputStream, long size, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, size));
        } catch (SdkException e) {
            throw new RuntimeException("S3 업로드 실패: " + key, e);
        }
    }

    @Override
    public String generateViewUrl(String key, Duration expires) {
        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(expires)
            .getObjectRequest(request)
            .build();

        return s3Presigner
            .presignGetObject(presignRequest)
            .url()
            .toString();
    }
}
