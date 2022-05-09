package server;

import general.element.UserProfile;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class, which working with user histories and banning users, which are not working for a long time
 */
public class ServerHistoryManager {
    private static final ServerHistoryManager instance = new ServerHistoryManager(); // Follow "Singleton" pattern
    private Hashtable<UserProfile, Long> lastModifiedTime;
    private String historyFilename;
    private Hashtable<String, LinkedList<String>> userHistories;
    private int maxUserHistoryLength;
    private long userBanTime;
    private final ScheduledExecutorService watchingThread = Executors.newSingleThreadScheduledExecutor();    // Follow "|scheduled| Singleton" pattern

    private ServerHistoryManager() {}

    static ServerHistoryManager getInstance() {
        return instance;
    }

    void initialize() throws IOException, IllegalAccessException {
        loadUserHistories();
        lastModifiedTime = new Hashtable<>();
        watchingThread.scheduleAtFixedRate(ServerHistoryManager.getInstance()::watchAndDeleteAFKUsers, 10, 10, TimeUnit.SECONDS);
    }

    void setProperties(Properties properties) {
        historyFilename = properties.getProperty("userHistoriesFilename", "UserHistories.xml");
        try {
            maxUserHistoryLength = Integer.parseInt(properties.getProperty("maxUserHistoryLength", "15"));
            if (maxUserHistoryLength < 13) {
                throw new NumberFormatException("property \"maxUserHistoryLength\" must be more at least 13");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Can't parse property \"maxUserHistoryLength\": " + e.getMessage());
        }
        try {
            userBanTime = Long.parseLong(properties.getProperty("userBanTime", "300000"));
            if (userBanTime < 60_000) {
                throw new NumberFormatException("property \"userBanTime\" must be more at least 60000");
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Can't parse property \"userBanTime\": " + e.getMessage());
        }
    }

    void updateUser(UserProfile userProfile) {
        lastModifiedTime.put(userProfile, System.currentTimeMillis());
    }

    void deleteUser(UserProfile userProfile) {
        lastModifiedTime.remove(userProfile);
    }

    void addUserHistory(UserProfile userProfile, String command) {
        if (!userHistories.containsKey(userProfile.getName())) {
            userHistories.put(userProfile.getName(), new LinkedList<>());
        }
        LinkedList<String> history = userHistories.get(userProfile.getName());
        history.add(command);
        if (history.size() > maxUserHistoryLength) history.removeFirst();
    }

    void clearUserHistory(String username) {
        @SuppressWarnings("unchecked")
        Hashtable<String, LinkedList<String>> hashtable = (Hashtable<String, LinkedList<String>>) userHistories.clone();
        hashtable.keySet().stream()
                .filter(u -> u.equals(username))
                .forEach(u -> userHistories.put(u, new LinkedList<>()));
    }

    public LinkedList<String> getUserHistory(UserProfile userProfile) {
        @SuppressWarnings("unchecked")
        LinkedList<String> list = (LinkedList<String>) userHistories.get(userProfile.getName()).clone();
        return list;
    }

    private void saveUserHistories() {
        try {
            File file = new File(historyFilename);
            if (!file.createNewFile()) {
                if (!file.isFile()) {
                    ServerController.getInstance().info("Can't save histories, because can't create file with name \"" + historyFilename + "\"");
                    return;
                } else if (!file.canWrite()) {
                    ServerController.getInstance().info("Can't save histories, because permission to write denied");
                    return;
                }
            }
            try (XMLEncoder encoder = new XMLEncoder(new FileOutputStream(historyFilename))) {
                encoder.writeObject(userHistories);
            }
        } catch (IOException e) {
            //ignore
        }
    }

    void close() {
        try {
            saveUserHistories();
            watchingThread.shutdown();
        } catch (Throwable e) {
            // ignore
        }
    }

    private void loadUserHistories() throws IllegalAccessException, IOException {
        try {
            File file = new File(historyFilename);
            if (file.createNewFile()) {
                userHistories = new Hashtable<>();
                return;
            }
            if (!file.isFile()) {
                throw new FileNotFoundException("Can't load histories, because file with name \"" + historyFilename + "\" can't be created");
            }
            if (!file.canRead()) {
                throw new IllegalAccessException("Can't load histories, because permission to read denied");
            }
            FileInputStream stream = new FileInputStream(file);
            if (stream.available() == 0) {
                userHistories = new Hashtable<>();
                stream.close();
                return;
            }
            try (XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(stream))) {
                decoder.setExceptionListener(e -> {
                    throw new IllegalArgumentException("wrong format of \"" + historyFilename + "\" (" + e.getMessage() + ")");
                });
                @SuppressWarnings("unchecked")
                Hashtable<String, LinkedList<String>> hashtable = (Hashtable<String, LinkedList<String>>) decoder.readObject();
                userHistories = (hashtable == null) ? new Hashtable<>() : hashtable;
            } catch (ArrayIndexOutOfBoundsException | ClassCastException e) {
                userHistories = new Hashtable<>();
            }
        } catch (IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            throw new IOException("Can't load user histories: " + e.getMessage());
        }
    }

    private void watchAndDeleteAFKUsers() {
        long now = System.currentTimeMillis();
        @SuppressWarnings("unchecked")
        Hashtable<UserProfile,Long> hashtable = (Hashtable<UserProfile,Long>) lastModifiedTime.clone();
        hashtable.forEach((u,t) -> {
            if (now - t > userBanTime) {
                ServerExecutor.logoutUser(u.getName());
                lastModifiedTime.remove(u);
                ServerController.getInstance().info("User \"" + u.getName() + "\" logout (reason: AFK)");
            }
        });
    }

}
