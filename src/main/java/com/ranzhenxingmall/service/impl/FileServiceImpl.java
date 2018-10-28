package com.ranzhenxingmall.service.impl;

import com.google.common.collect.Lists;
import com.ranzhenxingmall.service.IFileService;
import com.ranzhenxingmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload(MultipartFile file, String path) {
        String originalFilename = file.getOriginalFilename();
        String fileExtensionName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        String uploadName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件，文件名:{},路径：{}，新文件名:{}", originalFilename, path, uploadName);
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadName);
        try {
            file.transferTo(targetFile);
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            targetFile.delete();

        } catch (IOException e) {
            logger.error("上传文件异常", e);
            e.printStackTrace();
        }

        return targetFile.getName();
    }
}
