package com.example.oop_modul2.service;

import com.example.oop_modul2.config.BotConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    String currentAction;
    Double arg1;
    Double arg2;

    public TelegramBot(BotConfig config){
        this.config=config;
        currentAction="";
        arg1=null;
        arg2=null;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            long chatId = update.getMessage().getChatId();


            if (currentAction.equals("")) {
                switch (messageText) {
                    case "/start" -> startCommand(chatId);
                    case "/help" -> helpCommand(chatId);
                    case "/calculate" -> {
                        calculateCommand(chatId);
                        currentAction = "/calculate";
                    }
                    default -> sendMessage(chatId, "Unknown command");
                }
            } else if (currentAction.equals("/calculate")){
                currentAction = messageText;
                switch (messageText) {
                    case "/plus", "/minus", "/multiple", "/divide" -> {
                        currentAction = messageText;
                        sendMessage(chatId, "Input first argument:");
                    }
                    case "/cancel" -> currentAction = "";
                    default -> sendMessage(chatId, "Unknown command");
                }
            } else if(currentAction.equals("/plus") || currentAction.equals("/minus") || currentAction.equals("/multiple") || currentAction.equals("/divide")) {
                if (arg1==null || arg2==null) {
                    argumentInput(chatId, messageText);
                }
                if (arg1!=null && arg2!=null) {
                    calculate(chatId);
                }
            }
        }
    }

    private void startCommand(long chatId) {
        sendMessage(chatId, "This is calculator bot. To take command list, send \"/help\"");
    }

    private void helpCommand(long chatID) {
        sendMessage(chatID, "Available commands:");
        sendMessage(chatID, "Type \"/calculate\" to start calculating.");
        sendMessage(chatID, "Available actions");
        sendMessage(chatID, "/plus\n/minus\n/multiple\n/divide");
    }

    private void calculateCommand(long chatID) {
        sendMessage(chatID, "Choose one command:");
        sendMessage(chatID, "/plus\n/minus\n/multiple\n/divide\n/cancel");
    }

    private void argumentInput(long chatID, String messageText) {
        if (arg1==null) {
            try {
                arg1=Double.parseDouble(messageText);
                sendMessage(chatID, "Input second argument!");
            } catch (Exception e) {
                sendMessage(chatID, "Incorrect argument!");
            }
            return;
        }
        if (arg2==null) {
            try {
                arg2=Double.parseDouble(messageText);
            } catch (Exception e) {
                sendMessage(chatID, "Incorrect argument!");
            }
            return;
        }
    }

    private void calculate(long chatID) {

        switch (currentAction) {
            case "/plus" -> {
                sendMessage(chatID, "Result: " + (arg1+arg2) + ".");
            }
            case "/minus" -> {
                sendMessage(chatID, "Result: " + (arg1-arg2) + ".");
            }
            case "/multiple" -> {
                sendMessage(chatID, "Result: " + (arg1*arg2) + ".");
            }
            case "/divide" -> {
                try {
                    sendMessage(chatID, "Result: " + (arg1/arg2) + ".");
                } catch (Exception e) {
                    sendMessage(chatID, "Division by zero!");
                }
            }
        }
        arg1=null;
        arg2=null;
        currentAction="";
        sendMessage(chatID,"To make another calculation type \"/calculate\".");
    }

    private void sendMessage(long chatId, String TextToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(TextToSend);

        try{
            execute(message);
        }catch (TelegramApiException e){

        }
    }
    @Override
    public String getBotToken(){
        return config.getToken();
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
}
