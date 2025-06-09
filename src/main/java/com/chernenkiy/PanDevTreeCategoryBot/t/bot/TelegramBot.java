package com.chernenkiy.PanDevTreeCategoryBot.t.bot;

import com.chernenkiy.PanDevTreeCategoryBot.t.bot.command.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final Map<String, BotCommand> commands = new HashMap<>();

    public TelegramBot(HelpCommand helpCommand,
                       ViewTreeCommand viewTreeCommand,
                       AddElementCommand addElementCommand,
                       AddRootElementCommand addRootElementCommand,
                       AddChildElementCommand addChildElementCommand,
                       RemoveElementCommand removeElementCommand,
                       RemoveRootElementCommand removeRootElementCommand,
                       RemoveChildElementCommand removeChildElementCommand,
                       DownloadCommand downloadCommand,
                       UploadCommand uploadCommand,
                       @Value("${telegram.bot.token}") String botToken) {
        super(botToken); // используем конструктор с токеном, как требует новая версия API
        this.botToken = botToken;

        commands.put("/help", helpCommand);
        commands.put("/viewTree", viewTreeCommand);
        commands.put("/addElement", addElementCommand);
        commands.put("/addRootElement", addRootElementCommand);
        commands.put("/addChildElement", addChildElementCommand);
        commands.put("/removeElement", removeElementCommand);
        commands.put("/removeRootElement", removeRootElementCommand);
        commands.put("/removeChildElement", removeChildElementCommand);
        commands.put("/download", downloadCommand);
        commands.put("/upload", uploadCommand);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                String commandKey = messageText.split("\\s+")[0];

                BotCommand command = commands.get(commandKey);

                if (command != null) {
                    command.execute(this, update);
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(update.getMessage().getChatId().toString());
                    message.setText("Добро пожаловать в Telegram-бот PanDev Tree Category Bot. Введите /help для списка доступных команд.");
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        logger.error("Ошибка при отправке приветственного сообщения", e);
                    }
                }
            } else if (update.getMessage().hasDocument()) {
                BotCommand uploadCommand = commands.get("/upload");
                if (uploadCommand != null) {
                    uploadCommand.execute(this, update);
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @PostConstruct
    public void start() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            logger.info("Telegram bot started successfully!");
        } catch (Exception e) {
            logger.error("Ошибка при запуске Telegram-бота", e);
        }
    }
}
