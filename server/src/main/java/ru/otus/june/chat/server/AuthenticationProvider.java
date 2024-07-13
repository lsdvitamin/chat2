package ru.otus.june.chat.server;

public interface AuthenticationProvider {
    void initialize();

    boolean authenticate(ClientHandler clientHandler, String login, String password);

    boolean registration(ClientHandler clientHandler, String login, String password, String username, Integer role);

    boolean isAdmin(String username);

    void kickOn(String username);

    boolean isKick(String username);
}
