package ru.otus.june.chat.server;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
    private class User {
        private String login;
        private String password;
        private String username;
        private Integer role;
        private boolean kick;

        public User(String login, String password, String username, Integer role, boolean kick) {
            this.login = login;
            this.password = password;
            this.username = username;
            this.role = role;
            this.kick = kick;
        }

        public Integer getRole() {
            return role;
        }
    }

    private Server server;
    public List<User> users;

    public List<User> getUsers() {
        return users;
    }

    public enum Role {
        USER(0),
        ADMIN(1);

        Integer id;


        Role(Integer id) {
            this.id = id;
        }

    }

    public InMemoryAuthenticationProvider(Server server) {
        this.server = server;
        this.users = new ArrayList<>();
        this.users.add(new User("login1", "pass1", "bob", Role.ADMIN.id, false));
        this.users.add(new User("login2", "pass2", "user2", Role.USER.id, false));
        this.users.add(new User("login3", "pass3", "user3", Role.USER.id, false));
    }

    @Override
    public void initialize() {
        System.out.println("Сервис аутентификации запущен: In-Memory режим");
    }

    private String getUsernameByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.username;
            }
        }
        return null;
    }

    private boolean isLoginAlreadyExist(String login) {
        for (User u : users) {
            if (u.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsernameAlreadyExist(String username) {
        for (User u : users) {
            if (u.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean authenticate(ClientHandler clientHandler, String login, String password) {
        String authUsername = getUsernameByLoginAndPassword(login, password);
        if (authUsername == null) {
            clientHandler.sendMessage("Некорретный логин/пароль");
            return false;
        }
        if (server.isUsernameBusy(authUsername)) {
            clientHandler.sendMessage("Указанная учетная запись уже занята");
            return false;
        }
        clientHandler.setUsername(authUsername);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/authok " + authUsername);
        return true;
    }

    @Override
    public boolean registration(ClientHandler clientHandler, String login, String password, String username, Integer role) {
        if (login.trim().length() < 3 || password.trim().length() < 6 || username.trim().length() < 1) {
            clientHandler.sendMessage("Логин 3+ символа, Пароль 6+ символов, Имя пользователя 1+ символ");
            return false;
        }
        if (isLoginAlreadyExist(login)) {
            clientHandler.sendMessage("Указанный логин уже занят");
            return false;
        }
        if (isUsernameAlreadyExist(username)) {
            clientHandler.sendMessage("Указанное имя пользователя уже занято");
            return false;
        }

        users.add(new User(login, password, username, role, false));
        clientHandler.setUsername(username);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/regok " + username);
        return true;
    }

    @Override
    public boolean isAdmin(String username) {
        for (User u : users) {
            String thisUsername = u.username;
            Integer thisRole = u.role;
            if (u.username.equals(username) && u.role.equals(1)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void kickOn(String username) {
        for (User u : users) {
            if (u.username.equals(username)) {
                u.kick = true;
            }
        }
    }

    @Override
    public boolean isKick(String username) {
        for (User u : users) {
            if (u.username.equals(username)) {
                return u.kick;
            }
        }
        return false;
    }

}