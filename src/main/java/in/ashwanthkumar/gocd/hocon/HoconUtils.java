package in.ashwanthkumar.gocd.hocon;

import com.typesafe.config.Config;

public class HoconUtils {
    public static String getString(Config config, String key, String defaultValue) {
        return config.hasPath(key) ? config.getString(key) : defaultValue;
    }
}
