package ru.flykby;

import java.util.ArrayList;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.ChatMemberUpdated;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.model.ChatJoinRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;

import ru.flykby.entities.DataBuffer;
import ru.flykby.entities.DataChannel;
import ru.flykby.entities.DataPosting;

public class Bot {
    private final TelegramBot bot = new TelegramBot("6302821997:AAFCKNIancc9qehKvNdOEkVab_Tqq_u75EI");
    private DataBase dataBase = new DataBase();
    private List<DataBuffer> photobuffer = new ArrayList<>();
    int cooldown = 60;

    public void serve() {
        Accept accept = new Accept(bot, dataBase, cooldown);
        accept.start();
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        
    }




    private void process(Update update) {
        // System.out.println(update);
        Message message = update.message();
        ChatMemberUpdated memberUpdated = update.myChatMember();
        ChatJoinRequest joinRequest = update.chatJoinRequest();
        CallbackQuery callback = update.callbackQuery();
        BaseRequest request = null;

        if (message != null) {
            long chatId = message.chat().id();
            String text = message.text();

            if (text != null && message.photo() == null) {
                if (text.equals("/start")) {

                    Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                            new String[][] { {"Posting", "Chech count posts"},
                                    {"Добавить фотокарточки", "Открыть список отложки"},
                                    {"ОтлогоПриниматель"}})
                            .resizeKeyboard(true)
                            .selective(true);

                    String msg = "Чтобы добавить канал в возможности автопостинга, просто добавьте меня в него\u2728";
                    request = new SendMessage(chatId, msg).replyMarkup(replyKeyboardMarkup);
                } else if (text.equals("Posting")) {
                    List<DataPosting> dataPostings = dataBase.getDataPosting();
                    dataPostings.forEach(elem -> System.out.println(elem));
                    for (DataPosting data : dataPostings) {
                        BaseRequest req;
                        if (!data.getMessage().equals("null")) {
                            req = new SendPhoto(data.getChannel(), data.getPhotoId())
                                .caption(data.getMessage());
                        } else {
                            req = new SendPhoto(data.getChannel(), data.getPhotoId());
                        }
                        bot.execute(req);
                        // System.out.println(response);
                    }

                    request = new SendMessage(chatId, "Постинг окончен!");


                } else if (text.equals("Chech count posts")) {
                    List<DataChannel> channels = dataBase.getChanels();
                    List<Integer> counts = dataBase.getCountPost();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < channels.size(); i++) {
                        stringBuilder.append("В канале: " + channels.get(i).getName() + " осталось постов в отложке: " 
                                + counts.get(i) + "\n\n");
                    }
                    
                    request = new SendMessage(chatId, stringBuilder.toString());

                } else if (text.equals("Добавить фотокарточки")) {
                    String msg = "";
                    if (photobuffer.size() != 0) {
                        List<DataChannel> channels = dataBase.getChanels();
                        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                        for (int i = 0; i < channels.size(); i += 2) {
                            List<InlineKeyboardButton> keyboardButtons = new ArrayList<InlineKeyboardButton>();
                            keyboardButtons.add(i % 2, new InlineKeyboardButton(channels.get(i).getName())
                                .callbackData(channels.get(i).getNamechannel()));

                            if (i + 1 < channels.size()) {
                                keyboardButtons.add((i + 1) % 2, new InlineKeyboardButton(channels.get(i + 1).getName())
                                        .callbackData(channels.get(i + 1).getNamechannel()));
                            } else {
                                keyboardButtons.add((i + 1) % 2, new InlineKeyboardButton("Закрыть Х")
                                        .callbackData("close_menu"));
                            }
                            
                            keyboard.addRow(keyboardButtons.toArray(new InlineKeyboardButton[2])); 
                        }
                        if (channels.size() % 2 == 0) {
                            keyboard.addRow(new InlineKeyboardButton("Закрыть Х")
                                        .callbackData("close_menu"));
                        }
                        msg = String.format("Постов %d (из 20) загружено.. Куда будем постить?", photobuffer.size());
                        request = new SendMessage(chatId, msg).replyMarkup(keyboard);
                    } else {
                        msg = "Хмм.. Кажется вы еще не добавили ниодной фотокарточки..\n\nПросто перешлите нужные посты в чат";
                        request = new SendMessage(chatId, msg);
                    }

                } else if (text.equals("ОтлогоПриниматель")) {
                    InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                    List<InlineKeyboardButton> keyboardButtons = new ArrayList<InlineKeyboardButton>();
                    keyboardButtons.add(new InlineKeyboardButton("1 мин")
                        .callbackData("timer_60"));
                    keyboardButtons.add(new InlineKeyboardButton("3 мин")
                        .callbackData("timer_180"));
                    keyboard.addRow(keyboardButtons.toArray(new InlineKeyboardButton[2])); 
                    keyboardButtons.clear();  
                    
                    keyboardButtons.add(new InlineKeyboardButton("5 мин")
                        .callbackData("timer_300"));
                    keyboardButtons.add(new InlineKeyboardButton("10 мин")
                        .callbackData("timer_600"));
                    keyboard.addRow(keyboardButtons.toArray(new InlineKeyboardButton[2])); 
                    keyboardButtons.clear(); 

                    keyboardButtons.add(new InlineKeyboardButton("15 мин")
                        .callbackData("timer_900"));
                    keyboardButtons.add(new InlineKeyboardButton("30 мин")
                        .callbackData("timer_1800"));
                    keyboard.addRow(keyboardButtons.toArray(new InlineKeyboardButton[2])); 
                    keyboardButtons.clear(); 

                    keyboardButtons.add(new InlineKeyboardButton("45 мин")
                        .callbackData("timer_2700"));
                    keyboardButtons.add(new InlineKeyboardButton("1 час")
                        .callbackData("timer_3600"));
                    keyboard.addRow(keyboardButtons.toArray(new InlineKeyboardButton[2])); 
                    keyboardButtons.clear();
                
                    keyboard.addRow(new InlineKeyboardButton("Закрыть Х")
                                .callbackData("close_menu"));
                    
                    String msg = String.format("Выберите время, через которое будет принята заявка\nСейчас: " + cooldown / 60 
                                                    + " мин", photobuffer.size());
                    request = new SendMessage(chatId, msg).replyMarkup(keyboard);

                } else if (text.equals("Открыть список отложки")) {
                    List<DataChannel> channels = dataBase.getChanels();
                    InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                    for (int i = 0; i < channels.size(); i += 2) {
                        List<InlineKeyboardButton> keyboardButtons = new ArrayList<InlineKeyboardButton>();
                        keyboardButtons.add(i % 2, new InlineKeyboardButton(channels.get(i).getName())
                            .callbackData("saver_" + channels.get(i).getNamechannel()));
                        if (i + 1 < channels.size()) {
                            keyboardButtons.add((i + 1) % 2, new InlineKeyboardButton(channels.get(i + 1).getName())
                                    .callbackData("saver_" + channels.get(i + 1).getNamechannel()));
                        } else {
                            keyboardButtons.add((i + 1) % 2, new InlineKeyboardButton("Закрыть Х")
                                    .callbackData("close_menu"));
                        }
                        
                        keyboard.addRow(keyboardButtons.toArray(new InlineKeyboardButton[2])); 
                    }
                    if (channels.size() % 2 == 0) {
                        keyboard.addRow(new InlineKeyboardButton("Закрыть Х")
                                    .callbackData("close_menu"));
                    }
                    String msg = "Выберите канал, чтобы посмотреть отложку:";
                    request = new SendMessage(chatId, msg).replyMarkup(keyboard);

                } else {
                    String msg = "Я не ебу о чем ты, говори по делу";
                    request = new SendMessage(chatId, msg);
                }

            }

            if (message.photo() != null) {
                if (message.mediaGroupId() == null) {
                    DataBuffer dataBuffer = new DataBuffer(message.photo()[message.photo().length - 1].fileId(), message.caption(), "yes");
                    photobuffer.add(dataBuffer);
                } else {
                    DataBuffer dataBuffer = new DataBuffer(message.mediaGroupId(), message.caption(), "no");
                    boolean isContains = false;
                    for (DataBuffer elem : photobuffer) {
                        if (dataBuffer.equals(elem)) {
                            if (dataBuffer.getMessage() != null) {
                                elem.setMessage(dataBuffer.getMessage());
                                isContains = true;
                            }
                        }
                    }
                    if (!isContains) {
                        photobuffer.add(dataBuffer);
                    }
                }
            }

            
            System.out.println(message.chat().username() + " " + message.chat().id() + " " + text);
        } else if (memberUpdated != null) {
            // System.out.println(memberUpdated);
            if (memberUpdated.newChatMember().user().username().equals("autoposting1703bot")) {
                System.out.println(memberUpdated.newChatMember().status());
                if (memberUpdated.newChatMember().status().equals(ChatMember.Status.administrator)) {
                    System.out.println(memberUpdated.chat());
                    String id = Long.toString(memberUpdated.chat().id());
                    if (dataBase.addChanel(memberUpdated.chat().title(), id)) {
                        request = new SendMessage(memberUpdated.from().id(), String.format("Вы добавили бота в канал \"%s\"", memberUpdated.chat().title()));
                    }
                } else if (memberUpdated.newChatMember().status().equals(ChatMember.Status.kicked)) {
                    // dataBase.deleteChanel(memberUpdated.chat())
                    System.out.println(memberUpdated);
                    request = new SendMessage(memberUpdated.from().id(),
                                String.format("Кажется вы удалили бота из канала \"%s\"", memberUpdated.chat().title()));
                }
            }

        } else if (joinRequest != null) {
            // System.out.println(unixTime);
            System.out.println(joinRequest);
            dataBase.addUser(joinRequest.from().id().toString(), joinRequest.chat().id().toString(), joinRequest.date());

        } else if (callback != null) {
            if (callback.data().equals("close_menu")) {
                request = new DeleteMessage(callback.message().chat().id(), callback.message().messageId());
                photobuffer.clear();
            } else {
                if (callback.data().substring(0, 6).equals("saver_")) {
                    String channel = callback.data().substring(6);
                    List<DataPosting> dataPostings = dataBase.getPosts(channel);
                    if (dataPostings.size() == 0) {
                        request = new SendMessage(callback.message().chat().id(), "Ой! Отложка пустая");
                    } else {
                        for (DataPosting elem : dataPostings) {
                            System.out.println(elem);
                            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                            keyboard.addRow(new InlineKeyboardButton("Удалить Х")
                                                    .callbackData("delete_" + elem.getId()));
                            if (!elem.getMessage().equals("null")) {
                                request = new SendPhoto(callback.message().chat().id(), elem.getPhotoId())
                                    .caption(elem.getMessage())
                                    .replyMarkup(keyboard);
                            } else {
                                request = new SendPhoto(callback.message().chat().id(), elem.getPhotoId())
                                    .replyMarkup(keyboard);
    
                            }
                            bot.execute(request);
                        }
                        request = new DeleteMessage(callback.message().chat().id(), callback.message().messageId());
                    }
                } else if (callback.data().substring(0, 6).equals("timer_")) {
                    cooldown = Integer.parseInt(callback.data().substring(6));
                    request = new SendMessage(callback.message().chat().id(), "Новый таймер установлен!");
                } else if (callback.data().substring(0, 7).equals("delete_")) {
                    dataBase.deletePost(Integer.parseInt(callback.data().substring(7)));
                    request = new DeleteMessage(callback.message().chat().id(), callback.message().messageId());
                } else {
                    for (DataBuffer elem : photobuffer) {
                        dataBase.addPost(callback.data(), elem);
                    }
                    request = new SendMessage(callback.message().chat().id(), "Посты успешно забиты!");
                }
            }
        }
        // System.out.println(update);
        if (request != null) {
            bot.execute(request);
        }

    }
}
