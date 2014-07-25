import java.util.ArrayList;

/**
 * Created by Yuri on 25.07.2014.
 */
public class DataExchange {
    public User loginFb() {
        return null;
    }

    public User saveToParseCom(User user) {
        return null;
    }

    public User getFromParseCom(String id) {
        return null;
    }

    private boolean isRegistered(String id) {
        return false;
    }

    public boolean isLogin() {
        return false;
    }

    public boolean createEvent(Event event) {
        return false;
    }

    public boolean respondToEvent(String eventHash, String message, String userHash) {
        return false;
    }

    public ArrayList<Event> getEventsByUser(String userHash) {
        return null;
    }

    public ArrayList<User> getUsersByEvent(String eventHash) {
        return null;
    }

    public ArrayList<Event> getEventsInRadius(float x, float y) {
        return null;
    }

    public boolean isEventAssignedToUser(String eventHash, String userHash) {
        return false;
    }

    public boolean unsubscribeFromEvent(String eventHash, String userHash) {
        return false;
    }

    public boolean removeEvent(String eventHash, String userHash) {
        return false;
    }

    public boolean updateEvent(Event event, String userHash) {
        return false;
    }

    public boolean shareEvent(String eventHash, String type) {
        return false;
    }
}
