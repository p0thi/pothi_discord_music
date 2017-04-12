package pothi_discord.managers;

import java.util.ArrayList;
import java.util.Collection;

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


    public void append(Collection<String> collection, String delimiter) {
        String myDelimiter = delimiter == null ? "" : delimiter;
        for (String string : collection) {
            append(string + myDelimiter);
        }
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
