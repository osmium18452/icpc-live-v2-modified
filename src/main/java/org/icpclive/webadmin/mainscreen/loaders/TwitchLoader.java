package org.icpclive.webadmin.mainscreen.loaders;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.Config;
import org.icpclive.webadmin.ContextListener;
import org.icpclive.webadmin.mainscreen.Polls.PollsData;
import org.icpclive.webadmin.mainscreen.Utils;
import org.icpclive.webadmin.mainscreen.statistics.WordStatisticsData;
import org.pircbotx.Configuration;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Aksenov239 on 26.03.2017.
 */
public class TwitchLoader extends Utils.StoppedRunnable {
    private static final Logger logger = LogManager.getLogger(TwitchLoader.class);

    private static TwitchLoader instance;

    private PircBotX bot;
    private MultiBotManager manager;

    private static PollsData pollsData;

    public static void start() {
        if (instance == null) {
            pollsData = PollsData.getInstance();
            instance = new TwitchLoader();
            Utils.StoppedThread twitchThread = new Utils.StoppedThread(instance);
            twitchThread.start();
            ContextListener.addThread(twitchThread);
        }
    }

    private TwitchLoader() {
        Properties properties = new Properties();
        String url = null;
        String username = null;
        String password = null;
        String channels = null;
        try {
//            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
            properties = Config.loadProperties("mainscreen");
            url = properties.getProperty("twitch.chat.server", "irc.chat.twitch.tv");
            username = properties.getProperty("twitch.chat.username");
            password = properties.getProperty("twitch.chat.password");
            channels = properties.getProperty("twitch.chat.channel", "#" + username);
        } catch (IOException e) {
            logger.error("error", e);
        }
        Configuration.Builder configuration = new Configuration.Builder()
                .setAutoNickChange(false)
                .setOnJoinWhoEnabled(false)
                .setCapEnabled(true)
                .setName(username)
                .setServerPassword(password)
                .addServer(url)
                .addListener(new ListenerAdapter() {
                    AtomicLong lastTimestamp = new AtomicLong();

                    @Override
                    public void onMessage(MessageEvent event) throws Exception {
                        logger.log(Level.INFO, "Message: " + event.getUser() + " " + event.getMessage());
//                        System.err.println("Message: " + event.getUser() + " " + event.getMessage());
                        long previousTimestamp = lastTimestamp.get();
                        if (event.getTimestamp() > previousTimestamp + 50) { // The messages are different
                            lastTimestamp.compareAndSet(previousTimestamp, event.getTimestamp());
                            String message = event.getMessage();
                            if (message.startsWith("!vote")) {
                                PollsData.vote(
                                        "Twitch:" + event.getUser().getLogin(),
                                        message.substring(1));
                            }
                            WordStatisticsData.vote(message);
                        }
                    }
                })
                .setEncoding(Charset.forName("UTF-8"));
        manager = new MultiBotManager();
        for (String channel : channels.split(";")) {
            System.err.println(channel);
            manager.addBot(configuration.addAutoJoinChannel(channel).buildConfiguration());
        }
    }

    public void run() {
        manager.start();
        while (!stop) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        manager.stop();
    }
}
