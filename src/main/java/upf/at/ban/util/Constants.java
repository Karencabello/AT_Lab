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
    public static final String AQI_API_SEARCH = "https://api.waqi.info/search/";

    // IpGeo service

    // AgeVerification
    public static final int MIN_AGE = 22; // Pq funiconi
    // HOST BASE SANDBOX TELEFONICA
    public static final String OGW_HOST = "https://sandbox.opengateway.telefonica.com/apigateway";

    // 1. CIBA authorize --> reorna aauth_req_id
    public static final String CIBA_AUTHORIZE_PATH = OGW_HOST + "/bc-authorize";

    // 2. CIBA token --> retorna access_token
    public static final String OGW_TOKEN_URL = OGW_HOST + "/token";
    public static final String OGW_GRANT_TYPE = "urn:openid:params:grant-type:ciba";

    // 3. Verify Age -> retorna ageCheck
    public static final String OGW_VERIFY_URL = OGW_HOST + "/kyc-age-verification/v0.1/verify";

    // 4. Scope requerit per a la verificació d'edat
    public static final String OGW_SCOPE = "dpv:FraudPreventionAndDetection kyc-age-verification:verify";

    // 5. Credencials
    public static final String OGW_CLIENT_ID = "af639f1b-2626-443c-8122-5a87c4c9f1c0";
    public static final String OGW_CLIENT_SECRET = "2b44dcac-5b08-442a-84cd-260c746665db";
    public static final String AQI_TOKEN = "52ee51d269aa2fd66d9411a7a65d186736d9fe42";

    // know your costumer

    // 2. CIBA token --> retorna access_token
    public static final String AV_TOKEN_URL = OGW_HOST + "/token";
    public static final String AV_GRANT_TYPE = "urn:openid:params:grant-type:ciba";

    // 3. matchName -> retorna match
    public static final String AV_VERIFY_URL = OGW_HOST + "/kyc-match/v0.2/match";
    
    // 4. scope
    public static final String AV_SCOPE = "dpv:FraudPreventionAndDetection#kyc-match:match";

    // 5. Credencials
    public static final String AV_CLIENT_ID = "079af024-9d3c-471c-8201-f6cda4d9852b";
    public static final String AV_CLIENT_SECRET = "01c1c240-a32a-4ea0-92bc-4e5094f6ccc8";


}

