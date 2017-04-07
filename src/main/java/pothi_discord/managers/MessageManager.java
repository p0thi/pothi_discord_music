package pothi_discord.managers;

import java.util.ArrayList;

/**
 * Created by Pascal Pothmann on 02.02.2017.
 */
public class MessageManager {
    private static final int MAX_MESSAGE_SIZE = 2000;

    private ArrayList<String> allMessages;
    private StringBuilder buffer;

    public MessageManager() {
        this.allMessages = new ArrayList<>();
        this.buffer = new StringBuilder();
    }

    public void append(String part) {
        if(buffer.length() + part.length() < MAX_MESSAGE_SIZE) {
            buffer.append(part);
        }
        else {
            allMessages.add(new String(buffer));

            String tmpBuffer = part;
            while (tmpBuffer.length() > MAX_MESSAGE_SIZE) {
                allMessages.add(tmpBuffer.substring(0, MAX_MESSAGE_SIZE));
                tmpBuffer = tmpBuffer.substring(MAX_MESSAGE_SIZE);
            }

            buffer = new StringBuilder(tmpBuffer);
        }
    }

    public ArrayList<String> complete() {
        allMessages.add(buffer.toString());
        return allMessages;
    }
}
