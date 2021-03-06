package com.jitterted.jittershout.adapter.triggering.twitch4j;

import com.github.twitch4j.chat.events.CommandEvent;
import com.jitterted.jittershout.domain.MessageSender;
import com.jitterted.jittershout.domain.Shouter;
import org.jetbrains.annotations.NotNull;

public class BotCommandHandler {

  private final MessageSender messageSender;
  private final PermissionChecker permissionChecker;
  private final Shouter shouter;

  public BotCommandHandler(MessageSender messageSender,
                           PermissionChecker permissionChecker,
                           Shouter shouter) {
    this.messageSender = messageSender;
    this.permissionChecker = permissionChecker;
    this.shouter = shouter;
  }

  public void handle(CommandEvent commandEvent) {
    if (!permissionChecker.allows(commandEvent.getPermissions())) {
      return;
    }

    String[] tokens = commandEvent.getCommand().split(" ");
    if (validShoutOutBotCommand(tokens)) {
      handleShoutOutBotCommand(tokens[1]);
    }
  }

  private boolean validShoutOutBotCommand(String[] tokens) {
    return isShoutOutCommand(tokens[0]) && hasSubCommand(tokens);
  }

  private boolean isShoutOutCommand(String token) {
    return token.equals("sob");
  }

  private boolean hasSubCommand(String[] tokens) {
    return tokens.length > 1;
  }

  private void handleShoutOutBotCommand(String subcommand) {
    switch (subcommand) {
      case "status" -> messageSender.send(botStatusMessage());
      case "off" -> changeShoutOutTo(false);
      case "on" -> changeShoutOutTo(true);
      case "reset" -> resetShoutOutTracker();
    }
  }

  private void resetShoutOutTracker() {
    shouter.resetShoutOutTracking();
    messageSender.send("Shout-Out Tracking has been reset.");
  }

  private void changeShoutOutTo(boolean active) {
    shouter.changeShoutOutActiveTo(active);
    messageSender.send("Shout-out is now %s".formatted(shoutOutStatusAsText()));
  }

  private String botStatusMessage() {
    return "Shout-out is %s.".formatted(shoutOutStatusAsText());
  }

  @NotNull
  private String shoutOutStatusAsText() {
    return shouter.isShoutOutActive() ? "on" : "off";
  }

}
