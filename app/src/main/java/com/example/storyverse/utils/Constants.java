package com.example.storyverse.utils;

public final class Constants {

    private Constants() {
        // Prevent creating object
    }

    // Story status
    public static final String STORY_STATUS_ONGOING = "ONGOING";
    public static final String STORY_STATUS_COMPLETED = "COMPLETED";
    public static final String STORY_STATUS_DROPPED = "DROPPED";

    // Age rating
    public static final String AGE_ALL = "ALL";
    public static final String AGE_13_PLUS = "13+";
    public static final String AGE_16_PLUS = "16+";
    public static final String AGE_18_PLUS = "18+";

    // User roles
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_AUTHOR = "AUTHOR";

    // Comment status
    public static final String COMMENT_ACTIVE = "ACTIVE";
    public static final String COMMENT_HIDDEN = "HIDDEN";
    public static final String COMMENT_DELETED = "DELETED";

    // Firebase collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_AUTHORS = "authors";
    public static final String COLLECTION_STORIES = "stories";
    public static final String COLLECTION_CHAPTERS = "chapters";
    public static final String COLLECTION_CATEGORIES = "categories";
    public static final String COLLECTION_COMMENTS = "comments";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
}