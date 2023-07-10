package ru.flykby;

import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.ApproveChatJoinRequest;

import ru.flykby.entities.DataUser;

public class Accept extends Thread {
    TelegramBot bot;
    DataBase dataBase;
    private int cooldown;

    public Accept(TelegramBot bot, DataBase dataBase, int cooldown) {
        this.bot = bot;
        this.dataBase = dataBase;
        this.cooldown = cooldown;
    }
    

    @Override
    public void run() {
        while (true) {

            List<DataUser> users = dataBase.getUsers(cooldown);

            for (DataUser user : users) {
                ApproveChatJoinRequest joinRequest = new ApproveChatJoinRequest(user.getChannelId(), user.getId());
                bot.execute(joinRequest);
                dataBase.deleteUser(user.getId().toString(), user.getChannelId());
                System.out.println("Принят: " + user.getId());
            } 

            try {
                Thread.sleep(cooldown * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
