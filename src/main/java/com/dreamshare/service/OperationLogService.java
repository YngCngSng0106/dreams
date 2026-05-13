package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        QueryWrapper<OperationLog> qw = new QueryWrapper<>();
        if (adminId != null) qw.eq("admin_id", adminId);
        if (moduleName != null && !moduleName.isEmpty()) qw.like("module_name", moduleName);
        if (operation != null && !operation.isEmpty()) qw.eq("operation", operation);
        qw.orderByDesc("create_time");
        return this.list(qw);
    }
}
