package com.bullboard.service;

import com.bullboard.dto.ArticleImageUploadRequest;
import com.bullboard.dto.ArticleImageUploadResponse;
import com.bullboard.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class S3StorageService {

    public static final int MAX_IMAGE_COUNT = 3;
    public static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final Duration expiration;

    public S3StorageService(S3Client s3Client,
                            S3Presigner s3Presigner,
                            @Value("${app.aws.s3.bucket}") String bucket,
                            @Value("${app.aws.s3.upload-expiration-minutes:5}") long expirationMinutes) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public ArticleImageUploadResponse createUpload(Long memberId,
                                                   ArticleImageUploadRequest request) {
        requireConfigured();
        validateTypeAndSize(request.getContentType(), request.getFileSize());

        String originalName = sanitizeFileName(request.getOriginalFileName());
        String objectKey = "articles/" + memberId + "/" + UUID.randomUUID()
                + extensionFor(request.getContentType());
        String encodedName = Base64.getUrlEncoder().encodeToString(
                originalName.getBytes(StandardCharsets.UTF_8));

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(request.getContentType())
                .metadata(Map.of("original-name-base64", encodedName))
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .putObjectRequest(putObjectRequest)
                        .build());

        return new ArticleImageUploadResponse(
                objectKey,
                presigned.url().toString(),
                Map.of(
                        "Content-Type", request.getContentType(),
                        "x-amz-meta-original-name-base64", encodedName
                ),
                expiration.toSeconds()
        );
    }

    public StoredImage verifyUploadedImage(Long memberId, String objectKey) {
        requireConfigured();
        if (objectKey == null || !objectKey.startsWith("articles/" + memberId + "/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        }

        try {
            HeadObjectResponse head = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build());
            validateTypeAndSize(head.contentType(), head.contentLength());
            String encodedName = head.metadata().get("original-name-base64");
            if (encodedName == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST);
            }
            String originalName = new String(
                    Base64.getUrlDecoder().decode(encodedName), StandardCharsets.UTF_8);
            return new StoredImage(objectKey, sanitizeFileName(originalName),
                    head.contentType(), head.contentLength());
        } catch (NoSuchKeyException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new ApiException(HttpStatus.BAD_REQUEST);
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        }
    }

    public String createDownloadUrl(String objectKey) {
        requireConfigured();
        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(objectKey)
                                .build())
                        .build())
                .url().toString();
    }

    public void delete(String objectKey) {
        if (bucket == null || bucket.isBlank()) {
            return;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build());
    }

    public void deleteUnattachedUpload(Long memberId, String objectKey) {
        if (objectKey == null || !objectKey.startsWith("articles/" + memberId + "/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        }
        delete(objectKey);
    }

    private void validateTypeAndSize(String contentType, long fileSize) {
        if (!ALLOWED_TYPES.contains(contentType)
                || fileSize < 1 || fileSize > MAX_FILE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        }
    }

    private String sanitizeFileName(String name) {
        if (name == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        }
        String normalized = name.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (normalized.isBlank() || normalized.length() > 255) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new ApiException(HttpStatus.BAD_REQUEST);
        };
    }

    private void requireConfigured() {
        if (bucket == null || bucket.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public record StoredImage(String objectKey, String originalName,
                              String contentType, long fileSize) {
    }
}
