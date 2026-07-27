package com.tili.livetranslation.domain.enums;

public enum Language {
    IT("Italian", "Italiano"),
    EN("English", "English"),
    SQ("Albanian", "Shqip");

    private final String displayName;
    private final String nativeName;

    Language(String displayName, String nativeName) {
        this.displayName = displayName;
        this.nativeName = nativeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getNativeName() {
        return nativeName;
    }

    public static Language fromCode(String code) {
        if (code == null) return null;
        return switch (code.toUpperCase()) {
            case "IT", "ITALIAN", "ITALIANO" -> IT;
            case "EN", "ENGLISH" -> EN;
            case "SQ", "ALBANIAN", "SHQIP" -> SQ;
            default -> null;
        };
    }
}
