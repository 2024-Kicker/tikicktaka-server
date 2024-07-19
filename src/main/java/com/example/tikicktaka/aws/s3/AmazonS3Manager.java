package com.example.tikicktaka.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.tikicktaka.config.AmazonConfig;
import com.example.tikicktaka.domain.images.Uuid;
import com.example.tikicktaka.repository.UuidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager {

    private final AmazonS3 amazonS3;

    private final AmazonConfig amazonConfig;

    private final UuidRepository uuidRepository;

    public String uploadFile(String path, Uuid uuid, MultipartFile file){
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        String keyName = "";
        switch (path) {
            case "member":
                keyName = generateMemberKeyName(uuid);
                break;

            default:
                keyName = "./" + uuid.getUuid();
        }
        try {
            amazonS3.putObject(new PutObjectRequest(amazonConfig.getBucket(), keyName, file.getInputStream(), metadata));
        }catch (IOException e){
            log.error("error at AmazonS3Manager uploadFile : {}", (Object) e.getStackTrace());
        }

        return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
    }

    public String generateMemberKeyName(Uuid uuid) {
        return amazonConfig.getMemberPath() + '/' + uuid.getUuid();
    }
}
