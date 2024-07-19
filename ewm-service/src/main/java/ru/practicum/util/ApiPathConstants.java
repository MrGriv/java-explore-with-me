package ru.practicum.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiPathConstants {
    public static final String BY_ID_PATH = "{id}";
    public static final String USERS_PATH = "users";
    public static final String ADMIN_PATH = "admin";
    public static final String EVENTS_PATH = "events";
    public static final String CATEGORY_PATH = "categories";
    public static final String EVENT_ID_PATH = "{eventId}";
    public static final String REQUEST_PATH = "requests";
    public static final String REQUEST_ID_PATH = "{requestId}";
    public static final String CANCEL_PATH = "cancel";
    public static final String COMPILATIONS_PATH = "compilations";
    public static final String FRIENDS_PATH = "friends";
    public static final String FRIEND_ID = "{friendId}";
    public static final String ID_CANCEL_PATH = REQUEST_ID_PATH + "/" + CANCEL_PATH;
    public static final String ADMIN_USERS_PATH = ADMIN_PATH + "/" + USERS_PATH;
    public static final String PRIVATE_USERS_EVENTS = USERS_PATH + "/" + BY_ID_PATH + "/" + EVENTS_PATH;
    public static final String EVENT_ID_REQUESTS_PATH = EVENT_ID_PATH + "/" + REQUEST_PATH;
    public static final String ADMIN_CATEGORY_PATH = ADMIN_PATH + "/" + CATEGORY_PATH;
    public static final String ADMIN_EVENTS_PATH = ADMIN_PATH + "/" + EVENTS_PATH;
    public static final String USER_ID_REQUEST_PATH = USERS_PATH + "/" + BY_ID_PATH + "/" + REQUEST_PATH;
    public static final String ADMIN_COMPILATIONS_PATH = ADMIN_PATH + "/" + COMPILATIONS_PATH;
    public static final String USER_ID_FRIEND_PATH = USERS_PATH + "/" + BY_ID_PATH + "/" + FRIENDS_PATH;
}
