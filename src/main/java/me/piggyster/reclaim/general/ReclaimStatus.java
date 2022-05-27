package me.piggyster.reclaim.general;

public enum ReclaimStatus {
    LOCKED("&c&lLOCKED"),
    UNLOCKED("&a&lUNLOCKED"),
    USED("&e&lUSED");

    private final String defaultDisplay;

    ReclaimStatus(String defaultDisplay) {
        this.defaultDisplay = defaultDisplay;
    }

    public String getDefaultDisplay() {
        return defaultDisplay;
    }
}
