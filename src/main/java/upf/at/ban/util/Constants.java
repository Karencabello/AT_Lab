package upf.at.ban.util;

public class Constants{
    //Bicing
    public static final String BICING_URL = "https://opendata-ajuntament.barcelona.cat/data/dataset";
    public static final String BICING_PATH = "6aa3416d-ce1a-494d-861b-7bd07f069600/resource/1b215493-9e63-4a12-8980-2d7e0fa19f85/download";
    public static final String BICING_TOKEN = "cb98b0c4b326da635573050e787fb0ca30f5ee426d70e92c3e6f9d5808f67cf8";

    //Telegram
    public static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";   
    public static final String TELEGRAM_TOKEN = "8375476740:AAF16doZ1TCzUBtSSuV4u6xTOK8Od6YUPN4";


    //Cache
    // Temps vida cache --> 120s en ms
    public static final long CACHE_TTL = 120_000;

    //AQI service 
    public static final String AQI_API_URL = "https://api.waqi.info/feed/";
    public static final String AQI_TOKEN = "52ee51d269aa2fd66d9411a7a65d186736d9fe42";
}