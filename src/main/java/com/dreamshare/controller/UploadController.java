package com.dreamshare.controller;

import com.dreamshare.dto.UploadResponse;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class UploadController {

    @Value("${dream.upload.path}")
    private String uploadPath;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    ));
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping("/image")
    public Result<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new RuntimeException("文件不能为空");

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过5MB");
        }

        // 校验文件类型
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new RuntimeException("只支持 JPG、PNG、GIF、WEBP 格式的图片");
        }

        // 额外校验 Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("上传的文件不是有效的图片");
        }

        // 按日期分目录
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM");
        String dateDir = sdf.format(new java.util.Date());
        String dirPath = uploadPath + "/" + dateDir;
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        File dest = new File(dirPath + "/" + fileName);
        file.transferTo(dest);

        UploadResponse resp = new UploadResponse();
        resp.setUrl("/uploads/images/" + dateDir + "/" + fileName);
        return Result.ok(resp);
    }
}
