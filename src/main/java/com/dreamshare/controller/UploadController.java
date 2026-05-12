package com.dreamshare.controller;

import com.dreamshare.dto.UploadResponse;
import com.dreamshare.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class UploadController {

    @Value("${dream.upload.path}")
    private String uploadPath;

    @PostMapping("/image")
    public Result<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new RuntimeException("文件不能为空");

        String originalName = file.getOriginalFilename();
        String ext = originalName != null ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";

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
