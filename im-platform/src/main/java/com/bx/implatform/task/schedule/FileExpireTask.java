package com.bx.implatform.task.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bx.implatform.annotation.RedisLock;
import com.bx.implatform.config.props.StorageProperties;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.entity.FileInfo;
import com.bx.implatform.service.FileService;
import com.bx.implatform.thirdparty.storage.FileStorageAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 过期文件清理任务
 *
 * @author Blue
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileExpireTask {

    private final FileService fileService;
    private final FileStorageAPI fileStorageAPI;
    private final StorageProperties storageProps;

    @RedisLock(prefixKey = RedisKey.IM_LOCK_FILE_TASK)
    @Scheduled(cron = "0 0 3 * * ?")
    public void run() {
        log.info("【定时任务】过期文件处理...");
        int batchSize = 100;
        List<FileInfo> files = loadBatch(batchSize);
        while (true) {
            for (FileInfo fileInfo : files) {
                String url = fileInfo.getFilePath();
                String objectKey = fileStorageAPI.resolveObjectKey(url);
                if (StringUtils.isBlank(objectKey)) {
                    log.warn("无法解析文件路径, id:{}, url:{}", fileInfo.getId(), url);
                    continue;
                }
                if (fileStorageAPI.isExist(objectKey)) {
                    if (!fileStorageAPI.remove(objectKey)) {
                        log.error("删除过期文件异常, id:{},文件名:{}", fileInfo.getId(), fileInfo.getFileName());
                        return;
                    }
                }
                fileService.removeById(fileInfo.getId());
            }
            if (files.size() < batchSize) {
                break;
            }
            files = loadBatch(batchSize);
        }
    }

    List<FileInfo> loadBatch(int size) {
        Date minDate = DateUtils.addDays(new Date(), -storageProps.getExpireIn());
        LambdaQueryWrapper<FileInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FileInfo::getIsPermanent, false);
        wrapper.le(FileInfo::getUploadTime, minDate);
        wrapper.orderByAsc(FileInfo::getId);
        wrapper.last("limit " + size);
        return fileService.list(wrapper);
    }

}
