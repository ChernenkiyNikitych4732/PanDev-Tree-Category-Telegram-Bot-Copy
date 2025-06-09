package com.chernenkiy.PanDevTreeCategoryBot.t.bot.command;

import com.chernenkiy.PanDevTreeCategoryBot.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class RemoveChildElementCommand implements BotCommand {

    private static final Logger logger = LoggerFactory.getLogger(RemoveChildElementCommand.class);

    private final CategoryService categoryService;

    public RemoveChildElementCommand(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void execute(DefaultAbsSender sender, Update update) {
        String messageText = update.getMessage().getText();
        String[] parts = messageText.trim().split("\\s+", 2);

        String response;
        if (parts.length == 2) {
            String name = parts[1];
            try {
                categoryService.deleteCategory(name);
                response = "Категория '" + name + "' и все её дочерние категории удалены.";
            } catch (Exception e) {
                logger.error("Ошибка при удалении категории '{}': {}", name, e.getMessage(), e);
                response = "Ошибка: " + e.getMessage();
            }
        } else {
            response = "Удалить пустой дочерний элемент";
        }

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(response);

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при отправке сообщения об удалении: {}", e.getMessage(), e);
        }
    }
}
