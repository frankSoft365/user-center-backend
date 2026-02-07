package com.microsoft.controller;

import com.microsoft.commen.Result;
import com.microsoft.utils.AliyunOSSOperator;
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
    private AliyunOSSOperator aliyunOSSOperator;

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile avatar) throws Exception {
        String url = aliyunOSSOperator.upload(avatar.getBytes(), Objects.requireNonNull(avatar.getOriginalFilename()));
        log.info("上传了图片：{}", url);
        return Result.success(url);
    }
}
