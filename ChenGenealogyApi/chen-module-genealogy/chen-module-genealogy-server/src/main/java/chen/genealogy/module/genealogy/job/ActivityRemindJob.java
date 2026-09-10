package chen.genealogy.module.genealogy.job;

import chen.genealogy.module.genealogy.service.activity.ActivityService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 活动开始前 1 天提醒（xxl-job 关闭时使用本地定时）
 */
@Component
public class ActivityRemindJob {

    @Resource
    private ActivityService activityService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void remind() {
        activityService.remindTomorrow();
    }
}
