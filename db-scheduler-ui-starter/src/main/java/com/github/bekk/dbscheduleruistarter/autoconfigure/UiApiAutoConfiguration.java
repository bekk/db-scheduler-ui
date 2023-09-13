package com.github.bekk.dbscheduleruistarter.autoconfigure;

import com.github.bekk.dbscheduleruiapi.controller.LogController;
import com.github.bekk.dbscheduleruiapi.controller.TaskController;
import com.github.bekk.dbscheduleruiapi.controller.UIController;
import com.github.bekk.dbscheduleruiapi.service.LogLogic;
import com.github.bekk.dbscheduleruiapi.service.TaskLogic;
import com.github.kagkarlsson.scheduler.Scheduler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import javax.xml.crypto.Data;

@AutoConfiguration
public class UiApiAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(UiApiAutoConfiguration.class);
    public UiApiAutoConfiguration(){
        logger.info("UiApiAutoConfiguration created");
    }


    @Bean
    @ConditionalOnMissingBean
    public TaskLogic taskLogic(Scheduler scheduler){
        return new TaskLogic(scheduler);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogLogic logLogic(DataSource dataSource){
        return new LogLogic(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskController taskController(TaskLogic taskLogic){
        return new TaskController(taskLogic);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogController logController(LogLogic logLogic){
        return new LogController(logLogic);
    }

    @Bean
    @ConditionalOnMissingBean
    public UIController uiController() {
        return new UIController();
    }
}
