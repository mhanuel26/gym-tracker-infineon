package app.gymassistant.contest.com.gymassistantapp;

/**
 * Created by mhanuel on 9/29/17.
 */

import java.lang.String;

public class LanguageConfig {
    private final String languageCode;
    private final String accessToken;

    public LanguageConfig(final String languageCode, final String accessToken) {
        this.languageCode = languageCode;
        this.accessToken = accessToken;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return languageCode;
    }
}
