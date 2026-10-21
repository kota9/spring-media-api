package com.example.springstudy.dto;

/** Setters record property presence so omitted and explicit JSON null remain distinct. */
public class ContentPatchRequest {
    private Object title;
    private Object description;
    private boolean titlePresent;
    private boolean descriptionPresent;

    public Object getTitle() { return title; }
    public Object getDescription() { return description; }

    public void setTitle(Object title) {
        this.titlePresent = true;
        this.title = title;
    }

    public void setDescription(Object description) {
        this.descriptionPresent = true;
        this.description = description;
    }

    public boolean hasTitle() {
        return titlePresent;
    }

    public boolean hasDescription() {
        return descriptionPresent;
    }
}
