package dev.killergamerpls.vanilla.command;

import dev.killergamerpls.vanilla.utils.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping");
        setAliases(List.of("latency"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
        if (sender instanceof Player player){
            sender.sendMessage("");
            sender.sendMessage(CC.translate("&cLatenza: &f" + player.getPing()));
            sender.sendMessage("");
        }
        return false;
    }
}