package com.microsoft.controller;

import com.microsoft.commen.Result;
import com.microsoft.utils.AliyunOSSOperator;
import com.microsoft.utils.AvatarUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Slf4j
@RestController
public class UploadController {
    @Resource
    private AvatarUtils avatarUtils;

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile avatar) throws Exception {
        // 校验
        avatarUtils.verifyAvatar(avatar);
        // 压缩 上传阿里云
        String url = avatarUtils.compressAndUploadAvatar(avatar);
        log.info("上传了图片：{}", url);
        return Result.success(url);
    }
}
