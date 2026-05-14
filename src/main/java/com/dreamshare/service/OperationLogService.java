package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamshare.entity.OperationLog;
import com.dreamshare.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService extends ServiceImpl<OperationLogMapper, OperationLog> {
    
    public void log(Long adminId, String adminName, String moduleName, String operation, 
                    Long targetId, String targetName, String remark) {
        OperationLog log = new OperationLog();
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setModuleName(moduleName);
        log.setOperation(operation);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setRemark(remark);
        this.save(log);
    }
    
    public java.util.List<OperationLog> query(Long adminId, String moduleName, String operation) {
        LambdaQueryWrapper<OperationLog> qw = new LambdaQueryWrapper<>();
        if (adminId != null) qw.eq(OperationLog::getAdminId, adminId);
        if (moduleName != null && !moduleName.isEmpty()) qw.like(OperationLog::getModuleName, moduleName);
        if (operation != null && !operation.isEmpty()) qw.eq(OperationLog::getOperation, operation);
        qw.orderByDesc(OperationLog::getCreateTime);
        return this.list(qw);
    }
}
