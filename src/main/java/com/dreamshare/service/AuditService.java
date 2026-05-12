package com.dreamshare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dreamshare.entity.*;
import com.dreamshare.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    @Autowired private ContentAuditMapper contentAuditMapper;
    @Autowired private AuditKeywordMapper auditKeywordMapper;
    @Autowired private DreamMapper dreamMapper;
    @Autowired private CommentMapper commentMapper;

    @Scheduled(cron = "${dream.audit.cron}")
    public void autoAudit() {
        LambdaQueryWrapper<ContentAudit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContentAudit::getAuditStatus, "PENDING");
        List<ContentAudit> pendingAudits = contentAuditMapper.selectList(wrapper);

        // 获取所有关键词
        List<AuditKeyword> keywords = auditKeywordMapper.selectList(null);
        if (keywords.isEmpty()) return;

        for (ContentAudit audit : pendingAudits) {
            for (AuditKeyword kw : keywords) {
                if (audit.getContentSnapshot().contains(kw.getKeyword())) {
                    audit.setAuditStatus("REJECTED");
                    audit.setRejectReason("包含违规内容: " + kw.getKeyword() + " (类型:" + kw.getKeywordType() + ", 级别:" + kw.getSeverity() + ")");
                    audit.setAuditedAt(LocalDateTime.now());
                    contentAuditMapper.updateById(audit);

                    // 根据严重程度处理
                    if ("HIGH".equals(kw.getSeverity())) {
                        // 直接软删除
                        deleteTarget(audit.getTargetType(), audit.getTargetId());
                    }
                    break;  // 匹配到一个关键词就标记
                }
            }
            // 没有匹配到关键词的通过
            if ("PENDING".equals(audit.getAuditStatus())) {
                audit.setAuditStatus("PASSED");
                audit.setAuditedAt(LocalDateTime.now());
                contentAuditMapper.updateById(audit);
            }
        }
    }

    private void deleteTarget(String targetType, Long targetId) {
        if ("DREAM".equals(targetType)) {
            Dream d = dreamMapper.selectById(targetId);
            if (d != null) {
                d.setIsDeleted(1);
                dreamMapper.updateById(d);
            }
        } else if ("COMMENT".equals(targetType)) {
            Comment c = commentMapper.selectById(targetId);
            if (c != null) {
                c.setIsDeleted(1);
                commentMapper.updateById(c);
            }
        }
    }
}
