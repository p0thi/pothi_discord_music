package pothi_discord.managers;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import pothi_discord.handlers.MessageDeleter;

import java.util.ArrayList;

/**
 * Created by Pascal Pothmann on 22.03.2017.
 */
public class HugeMessageSender {
    private ArrayList<String> builder = new ArrayList<>();
    private String header;
    private String mpInfix;
    private MessageTag tag;
    private boolean sendAsCodeblock = true;



    /*
    Infix  ->  $CURRENTPAGE$
    Infix  ->  $TOTALPAGES$

    Infix  ->  $MULTIPAGES$
     */



    public void send(MessageChannel channel){
        if(builder.size()<1 || header == null || tag == null)
            throw new IllegalStateException("Not all values set");

        ArrayList<String> singleMessages = new ArrayList<>();
        String tmp = "";
        int infixLength = 0;
        if(mpInfix != null)
            infixLength = mpInfix.length();
        for(String msg : builder){
            if((msg.length()+tmp.length()+header.length()+ tag.getTag().length() + infixLength + 5)<2000){
                tmp += msg;
            }
            else{
                String current = tmp;
                singleMessages.add(current);
                tmp = msg;
            }
        }
        if(!singleMessages.contains(tmp))
            singleMessages.add(tmp);
        for(int i = 0; i < singleMessages.size(); i++){
            String currentMpInfix = mpInfix;
            if(currentMpInfix != null && singleMessages.size()>1) {
                currentMpInfix = currentMpInfix.replace("$CURRENTPAGE$", "" + (i+1));
                currentMpInfix = currentMpInfix.replace("$TOTALPAGES$", "" + singleMessages.size());
            }

            String currentHeader = header;
            if(currentMpInfix != null && singleMessages.size()>1)
                currentHeader = currentHeader.replace("$MULTIPAGE$", currentMpInfix);
            else
                currentHeader = currentHeader.replace("$MULTIPAGE$", "");

            String current = tag.getTag() + ": " + currentHeader + "\n\n" + singleMessages.get(i);

            MessageBuilder b = new MessageBuilder();
            if (sendAsCodeblock) {
                b.appendCodeBlock(current, null);
            }
            else {
                b.append(current);
            }

            channel.sendMessage(b.build()).queue(new MessageDeleter());
        }
    }

    public void setMpInfix(String mpInfix) { this.mpInfix = mpInfix;}

    public void setTag(MessageTag tag){
        this.tag = tag;
    }

    public void append(String content){
        builder.add(content);
    }

    public void setHeader(String header){
        this.header = header;
    }


    public enum MessageTag{
        INFO("#INFO"), WARNING("#WARNUNG"), ERROR("#ERROR"), SERIAL("#SERIAL");

        private String tag;

        MessageTag(String tag){
            this.tag=tag;
        }
        String getTag(){
            return tag;
        }
    }

    public void setSendAsCodeblock(boolean sendAsCodeblock) {
        this.sendAsCodeblock = sendAsCodeblock;
    }
}
