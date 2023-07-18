package ru.flykby;

import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.ApproveChatJoinRequest;

import ru.flykby.entities.DataUser;

public class Accept extends Thread {
    TelegramBot bot;
    DataBase dataBase;
    private int cooldown;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    public Accept(TelegramBot bot, DataBase dataBase, int cooldown) {
        this.bot = bot;
        this.dataBase = dataBase;
        this.cooldown = cooldown;
    }
    

    @Override
    public void run() {
        while (running) {
            synchronized (pauseLock) {
                if (!running) {
                    break;
                }
                if (paused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException ex) {
                        break;
                    }
                    if (!running) { // running might have changed since we paused
                        break;
                    }
                }

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

    public void exit() {
        running = false;
        restart();
    }

    public void pause() {
        // you may want to throw an IllegalStateException if !running
        paused = true;
    }

    public void restart() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread
        }
    }

}
