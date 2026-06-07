package com.cookandroid.dailyworkcheck;

public class ChecklistItem {
    private long id;
    private String title;
    private String url;
    private boolean completed;

    public ChecklistItem(long id, String title, String url, boolean completed) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.completed = completed;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}