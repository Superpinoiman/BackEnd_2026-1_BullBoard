package com.bullboard.service;

import com.bullboard.domain.Article;
import com.bullboard.domain.ArticleImage;
import com.bullboard.dto.ArticleImageResponse;
import com.bullboard.dto.ArticleImageUploadRequest;
import com.bullboard.dto.ArticleImageUploadResponse;
import com.bullboard.exception.ApiException;
import com.bullboard.repository.ArticleImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ArticleImageService {

    private static final Logger log = LoggerFactory.getLogger(ArticleImageService.class);

    private final ArticleImageRepository articleImageRepository;
    private final S3StorageService storageService;

    public ArticleImageService(ArticleImageRepository articleImageRepository,
                               S3StorageService storageService) {
        this.articleImageRepository = articleImageRepository;
        this.storageService = storageService;
    }

    public ArticleImageUploadResponse createUpload(Long memberId,
                                                   ArticleImageUploadRequest request) {
        return storageService.createUpload(memberId, request);
    }

    @Transactional
    public List<ArticleImageResponse> attachImages(Article article, Long memberId,
                                                   List<String> requestedKeys) {
        List<String> keys = requestedKeys == null ? List.of()
                : new ArrayList<>(new LinkedHashSet<>(requestedKeys));
        long existingCount = articleImageRepository.countByArticleId(article.getId());

        if (existingCount + keys.size() > S3StorageService.MAX_IMAGE_COUNT) {
            throw new ApiException(HttpStatus.BAD_REQUEST);
        }

        int sortOrder = (int) existingCount;
        for (String key : keys) {
            if (articleImageRepository.existsByObjectKey(key)) {
                throw new ApiException(HttpStatus.BAD_REQUEST);
            }
            S3StorageService.StoredImage stored =
                    storageService.verifyUploadedImage(memberId, key);
            articleImageRepository.save(new ArticleImage(
                    article,
                    stored.objectKey(),
                    stored.originalName(),
                    stored.contentType(),
                    stored.fileSize(),
                    sortOrder++
            ));
        }
        return getResponses(article.getId());
    }

    public List<ArticleImageResponse> getResponses(Long articleId) {
        return articleImageRepository.findByArticleIdOrderBySortOrderAsc(articleId)
                .stream()
                .map(ArticleImageResponse::new)
                .toList();
    }

    public URI createDownloadUri(Long imageId) {
        ArticleImage image = articleImageRepository.findById(imageId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        return URI.create(storageService.createDownloadUrl(image.getObjectKey()));
    }

    public void deleteUnattachedUpload(Long memberId, String objectKey) {
        if (articleImageRepository.existsByObjectKey(objectKey)) {
            throw new ApiException(HttpStatus.CONFLICT);
        }
        storageService.deleteUnattachedUpload(memberId, objectKey);
    }

    @Transactional
    public void deleteImage(Long imageId, Long memberId) {
        ArticleImage image = articleImageRepository.findById(imageId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND));
        if (image.getArticle().getAuthor() == null
                || !image.getArticle().getAuthor().getId().equals(memberId)) {
            throw new ApiException(HttpStatus.FORBIDDEN);
        }
        String objectKey = image.getObjectKey();
        articleImageRepository.delete(image);
        deleteAfterCommit(List.of(objectKey));
    }

    public void deleteArticleImagesAfterCommit(Long articleId) {
        List<String> keys = articleImageRepository.findByArticleIdOrderBySortOrderAsc(articleId)
                .stream()
                .map(ArticleImage::getObjectKey)
                .toList();
        deleteAfterCommit(keys);
    }

    private void deleteAfterCommit(List<String> objectKeys) {
        if (objectKeys.isEmpty()) {
            return;
        }
        Runnable delete = () -> objectKeys.forEach(key -> {
            try {
                storageService.delete(key);
            } catch (RuntimeException exception) {
                log.warn("S3 object cleanup failed: {}", key, exception);
            }
        });

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            delete.run();
                        }
                    });
        } else {
            delete.run();
        }
    }
}
