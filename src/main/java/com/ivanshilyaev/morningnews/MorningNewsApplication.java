package com.ivanshilyaev.morningnews;

import com.ivanshilyaev.morningnews.services.MessageBuilderService;
import com.ivanshilyaev.morningnews.services.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class MorningNewsApplication {

    private final MessageBuilderService messageBuilderService;
    private final TelegramService telegramService;

    public static void main(String[] args) {
        SpringApplication.run(MorningNewsApplication.class, args);
    }

    @Bean
    public Function<String, String> run() throws Exception {
        String message = messageBuilderService.buildMessage();
        log.info(message);

        telegramService.sendMessage(message);
        log.info("Bot message has been sent");

        // test automatic deployment
        log.info("Function update from GitHub");

        return value -> message;
    }
}
