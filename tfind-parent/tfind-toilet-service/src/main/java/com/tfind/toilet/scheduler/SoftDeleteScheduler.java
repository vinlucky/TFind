package com.tfind.toilet.scheduler;

import com.tfind.toilet.service.ToiletService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SoftDeleteScheduler {

    private final ToiletService toiletService;

    public SoftDeleteScheduler(ToiletService toiletService) {
        this.toiletService = toiletService;
    }

    @Scheduled(cron = "0 59 23 * * ?")
    public void physicalDeleteExpiredToilets() {
        int count = toiletService.physicalDeleteToilets();
        System.out.println("Physical deleted " + count + " expired toilets");
    }
}
