package pothi_discord.managers;

import net.dv8tion.jda.core.entities.Guild;
import pothi_discord.commands.Command;

import java.util.*;

/**
 * Created by Pascal Pothmann on 09.03.2017.
 */
public class GuildCommandManager {
    private Guild guild;
    private HashMap<String, Command> commands;
    private HashMap<String, String> alias;

    public GuildCommandManager(Guild guild) {
        this.guild = guild;
        this.commands = new HashMap<>();
        this.alias = new HashMap<>();
    }

    public Command getCommand(String command) throws NoSuchCommandException {
        if(commands.containsKey(command.toLowerCase())) {
            return commands.get(command.toLowerCase());
        }
        else if(alias.containsKey(command.toLowerCase())) {
            return getCommand(alias.get(command.toLowerCase()));
        }
        else {
            throw new NoSuchCommandException();
        }
    }

    public boolean hasCommand(String command) {
        try {
            return getCommand(command) != null;
        } catch (NoSuchCommandException e) {
            return false;
        }
    }

    public void addCommand(String key, Command command) {
        command.prepare();
        commands.put(key, command);
    }

    public void addAlias(String key, String value) throws NoValidAliasException {
        if(!commands.containsKey(value.toLowerCase()) && !alias.containsKey(value.toLowerCase())) {
            throw new NoValidAliasException();
        }
        alias.put(key, value);
    }

    public void removeCommandWithAlias(String command){
        String currentCommand = command;

        while (alias.containsValue(currentCommand)) {
            Iterator it = alias.entrySet().iterator();
            iterator:
            while(it.hasNext()) {
                Map.Entry<String, String> pair = (Map.Entry) it.next();
                if(pair.getValue().equals(currentCommand)) {
                    currentCommand = pair.getKey();
                    alias.remove(currentCommand);
                    break iterator;
                }
            }
        }

        currentCommand = command;
        while (alias.containsKey(currentCommand)){
            currentCommand = alias.get(currentCommand);
            alias.remove(currentCommand);
        }
        commands.remove(command);
    }

    public class NoSuchCommandException extends IllegalArgumentException {

    }

    public class NoValidAliasException extends IllegalArgumentException {

    }
}
