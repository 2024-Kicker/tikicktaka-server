package com.example.tikicktaka.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
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
        metadata.setContentType(file.getContentType());

        String keyName = "";
        switch (path) {
            case "member":
                keyName = generateMemberKeyName(uuid);
                break;
            case "logo":
                keyName = generateLogoKeyName(uuid);
                break;
            case "stadium":
                keyName = generateStadiumKeyName(uuid);
                break;
            case "companionPost":
                keyName = generateCompanionPostKeyName(uuid);
                break;
            case "storyRoomPost":
                keyName = generateStoryRoomPostKeyName(uuid);
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

    public void deleteFile(String imageUrl) {
        try {
            String bucketName = amazonConfig.getBucket();
            String keyName = extractKeyFromUrl(imageUrl);

            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, keyName));
            log.info("S3 이미지 삭제 완료: {}", imageUrl);
        } catch (Exception e) {
            log.error("S3 이미지 삭제 실패: {}", imageUrl, e);
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.");
        }
    }

    private String extractKeyFromUrl(String imageUrl) {
        String bucketName = amazonConfig.getBucket();
        return imageUrl.replace("https://" + bucketName + ".s3.amazonaws.com/", "");
    }

    public String generateMemberKeyName(Uuid uuid) {
        return amazonConfig.getMemberPath() + '/' + uuid.getUuid();
    }

    public String generateLogoKeyName(Uuid uuid) {
        return amazonConfig.getLogoPath() + '/' + uuid.getUuid();
    }

    public String generateStadiumKeyName(Uuid uuid) {
        return amazonConfig.getStadiumPath() + '/' + uuid.getUuid();
    }
    public String generateCompanionPostKeyName(Uuid uuid){ return amazonConfig.getCompanionPostPath() + '/' + uuid.getUuid();}
    public String generateStoryRoomPostKeyName(Uuid uuid){ return amazonConfig.getStoryRoomPostPath() + '/' + uuid.getUuid();}

}
