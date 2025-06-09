package com.chernenkiy.PanDevTreeCategoryBot.t.bot.command;

import com.chernenkiy.PanDevTreeCategoryBot.model.Category;
import com.chernenkiy.PanDevTreeCategoryBot.service.CategoryService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class UploadCommand implements BotCommand {

    private static final Logger logger = LoggerFactory.getLogger(UploadCommand.class);

    private final CategoryService categoryService;

    public UploadCommand(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void execute(DefaultAbsSender sender, Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());

        if (!update.getMessage().hasDocument()) {
            message.setText("Пожалуйста, отправьте Excel-файл после команды /upload.");
            sendReply(sender, message);
            return;
        }

        Document document = update.getMessage().getDocument();

        if (!document.getFileName().endsWith(".xlsx")) {
            message.setText("Пожалуйста, загрузите файл в формате .xlsx");
            sendReply(sender, message);
            return;
        }

        try {
            // Получение файла
            File file = sender.execute(new GetFile(document.getFileId()));
            InputStream inputStream = sender.downloadFileAsStream(file.getFilePath());

            // Очистка текущих категорий
            categoryService.deleteAllCategories();

            // Чтение Excel-файла
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // Построение иерархии
            Map<Integer, Category> levelMap = new HashMap<>();

            for (Row row : sheet) {
                for (Cell cell : row) {
                    int level = cell.getColumnIndex();
                    String name = cell.getStringCellValue().trim();

                    if (name.isEmpty()) continue;

                    Category category = new Category();
                    category.setName(name);

                    if (level == 0) {
                        categoryService.createCategory(name, null);
                        levelMap.put(0, category);
                    } else {
                        Category parent = levelMap.get(level - 1);
                        if (parent == null) {
                            String error = "Ошибка: Родительская категория для '" + name + "' не найдена.";
                            logger.error(error);
                            message.setText(error);
                            sendReply(sender, message);
                            return;
                        }
                        categoryService.createCategory(name, parent.getName());
                        levelMap.put(level, category);
                    }

                    break; // обрабатываем только первую заполненную ячейку в строке
                }
            }

            message.setText("Файл успешно загружен и данные обновлены.");
        } catch (Exception e) {
            logger.error("Ошибка при загрузке или обработке Excel-файла", e);
            message.setText("Ошибка при обработке файла: " + e.getMessage());
        }

        sendReply(sender, message);
    }

    private void sendReply(DefaultAbsSender sender, SendMessage message) {
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при отправке сообщения", e);
        }
    }
}
