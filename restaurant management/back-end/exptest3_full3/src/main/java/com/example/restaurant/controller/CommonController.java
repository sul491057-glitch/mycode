package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 通用控制器类
 * 处理文件上传等通用功能
 * 路径必须为 /api/common
 */
@RestController
@RequestMapping("/api/common") // 必须是 /api/common
public class CommonController {

    private final String projectPath = System.getProperty("user.dir");
    // 确保这里的路径和你实际项目结构一致
    private final String uploadDir = projectPath + "/src/main/resources/static/images/";

    @PostMapping("/upload")
    public Result<?> upload(MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件为空");

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String originalFilename = file.getOriginalFilename();
        String suffix = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String newFilename = UUID.randomUUID().toString() + suffix;

        try {
            file.transferTo(new File(uploadDir + newFilename));
            // 返回的必须是 /images/ 开头
            return Result.success("/images/" + newFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
}