package com.tfind.user.scheduler;

import com.tfind.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UserSoftDeleteScheduler {

    private static final Logger log = LoggerFactory.getLogger(UserSoftDeleteScheduler.class);

    @Autowired
    private UserService userService;

    @Scheduled(cron = "0 59 23 * * ?")
    public void physicalDeleteExpiredUsers() {
        log.info("开始扫描软删除超过30天的用户...");
        int count = userService.physicalDeleteUsers();
        log.info("物理删除过期用户完成，共删除{}条", count);
    }
}
