package app.gymassistant.contest.com.gymassistantapp;

/**
 * Created by mhanuel on 9/29/17.
 */

public abstract class Config {
    // copy this keys from your developer dashboard
    public static final String ACCESS_TOKEN = "851d8611f35b44928be07a3d31aad0b5";

    public static final LanguageConfig[] languages = new LanguageConfig[]{
            new LanguageConfig("en", "a11ea1d839e3446d84e402cb97cdadfb"),
            new LanguageConfig("ru", "c8acebfbeeaa42ccb986e30573509055"),
            new LanguageConfig("de", "ae2afb2dfd3f4a02bb0da9dd32b78ff6"),
            new LanguageConfig("pt", "b27372e24ee44db48df4dccbd57ea021"),
            new LanguageConfig("pt-BR", "a4e08b5bc87a41098237e3f23a5e1351"),
            new LanguageConfig("es", "49be4c10b6a543dfb41d49d88731bd49"),
            new LanguageConfig("fr", "62161233bc094a75b3acfe16aeeed203"),
            new LanguageConfig("it", "57f80c9c9a2b4e0eae1739349a46e342"),
            new LanguageConfig("ja", "b92617a3f82e4b52b3db44436d2d4b8b"),
            new LanguageConfig("ko", "447a595234d74561a76b669a88ab3d99"),
            new LanguageConfig("zh-CN", "52d2b2bd992749409fc3a7d0605c3db4"),
            new LanguageConfig("zh-HK", "760c7a5efe5d43b9a90d62f73251de6a"),
            new LanguageConfig("zh-TW", "9cadea114425436cbaeaa504ea56555b"),
    };

    public static final String[] events = new String[]{
            "hello_event",
            "goodbye_event",
            "how_are_you_event"
    };
}
