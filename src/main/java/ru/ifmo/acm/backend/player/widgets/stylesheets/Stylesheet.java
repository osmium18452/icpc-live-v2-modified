package ru.ifmo.acm.backend.player.widgets.stylesheets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by Aksenov239 on 12.06.2016.
 */
public class Stylesheet {
    public static HashMap<String, String> colors = new HashMap<>();
    static Properties properties;

    static {
        Properties mainProperties = new Properties();
        properties = new Properties();
        try {
            mainProperties.load(Stylesheet.class.getResourceAsStream("mainscreen.properties"));
            String stylesheet = mainProperties.getProperty("stylesheet");

            properties.load(Stylesheet.class.getResourceAsStream(stylesheet));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String name : properties.stringPropertyNames()) {
            colors.put(name, properties.getProperty(name));
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (String name : properties.stringPropertyNames()) {
                if (colors.containsKey(colors.get(name))) {
                    changed = true;
                    colors.put(name, colors.get(colors.get(name)));
                }
            }
        }
    }
}
