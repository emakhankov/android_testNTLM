package ru.emakhankov.testhttpok;

public class UrlData {
    public String url;

    public UrlData(String url, String login, String password, String domain, boolean unsafe) {
        this.url = url;
        this.login = login;
        this.password = password;
        this.domain = domain;
        this.unsafe = unsafe;
    }

    public String login;
    public String password;
    public String domain;

    public boolean unsafe;

}
