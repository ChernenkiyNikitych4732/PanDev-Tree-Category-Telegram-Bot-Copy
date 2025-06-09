package com.chernenkiy.PanDevTreeCategoryBot.t.bot.command;

import com.chernenkiy.PanDevTreeCategoryBot.model.Category;
import com.chernenkiy.PanDevTreeCategoryBot.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class ViewTreeCommand implements BotCommand {

    private static final Logger logger = LoggerFactory.getLogger(ViewTreeCommand.class);

    private final CategoryService categoryService;

    public ViewTreeCommand(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Выполняет команду отображения дерева категорий.
     *
     * @param sender объект для отправки сообщений
     * @param update обновление из Telegram API
     */
    @Override
    public void execute(DefaultAbsSender sender, Update update) {
        List<Category> rootCategories = categoryService.getRootCategories();
        StringBuilder treeBuilder = new StringBuilder();

        for (Category category : rootCategories) {
            buildTreeString(category, treeBuilder, 0);
        }

        String tree = !treeBuilder.isEmpty() ? treeBuilder.toString() : "Дерево категорий пусто.";

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(tree);

        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при отправке дерева категорий", e);
        }
    }

    /**
     * Рекурсивно строит строковое представление дерева категорий.
     *
     * @param category текущая категория
     * @param builder  StringBuilder для накопления строки
     * @param level    уровень вложенности категории
     */
    private void buildTreeString(Category category, StringBuilder builder, int level) {
        builder.append("  ".repeat(level)); // отступы на основе уровня
        builder.append("- ").append(category.getName()).append("\n");

        for (Category child : category.getChildren()) {
            buildTreeString(child, builder, level + 1);
        }
    }
}
