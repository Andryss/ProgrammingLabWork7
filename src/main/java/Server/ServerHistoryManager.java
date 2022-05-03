package Server;

import MovieObjects.UserProfile;

import java.io.*;
import java.util.Hashtable;
import java.util.LinkedList;

public class ServerHistoryManager {
    private static Hashtable<UserProfile, Long> lastModifiedTime;
    private static final String historyFilename = "UserHistories";
    private static Hashtable<UserProfile, LinkedList<String>> userHistories;
    private static Thread watchingThread;

    private ServerHistoryManager() {}

    static void initialize() throws IOException, ClassNotFoundException, IllegalAccessException {
        loadUserHistories();
        lastModifiedTime = new Hashtable<>();
        watchingThread = new Thread(ServerHistoryManager::watchAndDeleteAFKUsers, "LogoutAFKThread");
        watchingThread.start();
    }

    static void updateUser(UserProfile userProfile) {
        lastModifiedTime.put(userProfile, System.currentTimeMillis());
    }

    static void deleteUser(UserProfile userProfile) {
        lastModifiedTime.remove(userProfile);
    }

    static void addUserHistory(UserProfile userProfile, String command) {
        if (!userHistories.containsKey(userProfile)) {
            userHistories.put(userProfile, new LinkedList<>());
        }
        LinkedList<String> history = userHistories.get(userProfile);
        history.add(command);
        if (history.size() > 16) history.removeFirst();
    }

    static void clearUserHistory(String username) {
        ((Hashtable<UserProfile, LinkedList<String>>) userHistories.clone()).keySet().stream()
                .filter(u -> u.getName().equals(username))
                .forEach(u -> userHistories.remove(u));
    }

    public static LinkedList<String> getUserHistory(UserProfile userProfile) {
        return (LinkedList<String>) userHistories.get(userProfile).clone();
    }

    private static void saveUserHistories() {
        try {
            File file = new File(historyFilename);
            if (!file.exists()) {
                file.createNewFile();
            } else if (!file.isFile()) {
                ServerController.info("Can't save histories, because can't create file with name \"" + historyFilename + "\"");
                return;
            } else if (!file.canWrite()) {
                ServerController.info("Can't save histories, because permission to write denied");
                return;
            }
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(historyFilename));
            stream.writeObject(userHistories);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            //ignore
        }
    }

    static void close() {
        saveUserHistories();
        watchingThread.interrupt();
    }

    private static void loadUserHistories() throws IllegalAccessException, ClassNotFoundException, IOException {
        try {
            File file = new File(historyFilename);
            if (!file.exists()) {
                file.createNewFile();
                userHistories = new Hashtable<>();
            } else if (!file.isFile()) {
                throw new FileNotFoundException("Can't load histories, because file with name \"" + historyFilename + "\" can't be created");
            } else if (!file.canRead()) {
                throw new IllegalAccessException("Can't load histories, because permission to read denied");
            } else {
                ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
                userHistories = (Hashtable<UserProfile, LinkedList<String>>) stream.readObject();
                stream.close();
            }
        } catch (IOException e) {
            throw new IOException("Can't load user histories: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Can't read anything from \"" + historyFilename + "\"");
        }
    }

    private static void watchAndDeleteAFKUsers() {
        try {
            while (true) {
                Thread.sleep(10_000);
                long now = System.currentTimeMillis();
                ((Hashtable<UserProfile,Long>) lastModifiedTime.clone()).forEach((u,t) -> {
                    if (now - t > 20_000 * 1) {
                        ServerExecutor.logoutUser(u.getName());
                        lastModifiedTime.remove(u);
                        ServerController.info("User " + u.getName() + " logout (reason: AFK)");
                    }
                });
            }
        } catch (InterruptedException e) {
            //ignore
        }
    }

}
