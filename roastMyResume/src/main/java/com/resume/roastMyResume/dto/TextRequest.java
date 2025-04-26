package com.resume.roastMyResume.dto;
import jakarta.validation.constraints.NotBlank;
public class TextRequest {
    @NotBlank(message = "Text cannot be blank")
    private String text;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}










