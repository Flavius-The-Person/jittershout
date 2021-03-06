package com.jitterted.jittershout;

import com.github.twitch4j.chat.events.CommandEvent;
import com.jitterted.jittershout.adapter.triggering.twitch4j.BotCommandHandler;
import com.jitterted.jittershout.adapter.triggering.twitch4j.PermissionChecker;
import com.jitterted.jittershout.domain.MessageSender;
import com.jitterted.jittershout.domain.Shouter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class BotCommandHandlerTest {

  private static final MessageSender DUMMY_MESSAGE_SENDER = Mockito.mock(MessageSender.class);
  private static final Shouter DUMMY_SHOUTER = Mockito.mock(Shouter.class);

  @Test
  public void statusCommandSendsBotStatusMessage() throws Exception {
    MessageSender senderSpy = Mockito.mock(MessageSender.class);

    Shouter fakeShouter = new FakeShouter();
    BotCommandHandler botCommandHandler = new BotCommandHandler(senderSpy,
                                                                new AlwaysAllowedPermissionChecker(),
                                                                fakeShouter);

    botCommandHandler.handle(createCommandWithText("sob status"));

    verify(senderSpy).send("Shout-out is on.");
  }

  @Test
  public void whenShoutOutIsDisabledStatusSaysItsOff() throws Exception {
    MessageSender senderSpy = Mockito.mock(MessageSender.class);

    Shouter fakeShouter = new FakeShouter();
    fakeShouter.changeShoutOutActiveTo(false);
    BotCommandHandler botCommandHandler = new BotCommandHandler(senderSpy,
                                                                new AlwaysAllowedPermissionChecker(),
                                                                fakeShouter);

    botCommandHandler.handle(createCommandWithText("sob status"));

    verify(senderSpy).send(startsWith("Shout-out is off"));
  }

  @ParameterizedTest
  @CsvSource({"sob off, false", "sob on, true"})
  public void sobOffCommandDisablesShoutOut(String command, boolean expectedEnabled) throws Exception {
    Shouter fakeShouter = new FakeShouter();
    BotCommandHandler botCommandHandler = new BotCommandHandler(DUMMY_MESSAGE_SENDER,
                                                                new AlwaysAllowedPermissionChecker(),
                                                                fakeShouter);

    botCommandHandler.handle(createCommandWithText(command));

    assertThat(fakeShouter.isShoutOutActive())
        .isEqualTo(expectedEnabled);
  }

  @ParameterizedTest
  @ValueSource(strings = {"on", "off"})
  public void sobCommandIsAcknowledgedWithNewStatusMessage(String state) throws Exception {
    MessageSender senderSpy = Mockito.mock(MessageSender.class);

    Shouter fakeShouter = new FakeShouter();
    BotCommandHandler botCommandHandler = new BotCommandHandler(senderSpy,
                                                                new AlwaysAllowedPermissionChecker(),
                                                                fakeShouter);

    botCommandHandler.handle(createCommandWithText("sob " + state));

    verify(senderSpy).send("Shout-out is now " + state);
  }

  @Test
  public void sobCommandWithoutSubcommandIsIgnored() throws Exception {
    MessageSender senderSpy = Mockito.mock(MessageSender.class);

    BotCommandHandler botCommandHandler = new BotCommandHandler(senderSpy,
                                                                new AlwaysAllowedPermissionChecker(),
                                                                DUMMY_SHOUTER);

    botCommandHandler.handle(createCommandWithText("sob"));

    verify(senderSpy, never()).send(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "foo", "foo status"})
  public void invalidCommandsAreIgnored(String commandText) throws Exception {
    MessageSender senderSpy = Mockito.mock(MessageSender.class);

    BotCommandHandler botCommandHandler = new BotCommandHandler(senderSpy,
                                                                new AlwaysAllowedPermissionChecker(),
                                                                DUMMY_SHOUTER);

    botCommandHandler.handle(createCommandWithText(commandText));

    verify(senderSpy, never()).send(any());
  }

  @Test
  public void commandWithoutSufficientPermissionIsIgnored() throws Exception {
    MessageSender senderSpy = Mockito.mock(MessageSender.class);

    PermissionChecker disallowedPermissionChecker = commandPermissions -> false;
    BotCommandHandler botCommandHandler = new BotCommandHandler(senderSpy,
                                                                disallowedPermissionChecker,
                                                                DUMMY_SHOUTER);

    botCommandHandler.handle(createCommandWithText("sob status"));

    verify(senderSpy, never()).send(any());
  }

  @Test
  public void shoutOutStatusResetCommandResetsTracker() throws Exception {
    MessageSender senderSpy = Mockito.mock(MessageSender.class);
    Shouter fakeShouter = new FakeShouter();
    BotCommandHandler botCommandHandler = new BotCommandHandler(senderSpy,
                                                                new AlwaysAllowedPermissionChecker(),
                                                                fakeShouter);


    botCommandHandler.handle(createCommandWithText("sob reset"));

    assertThat(fakeShouter.shoutOutTrackingCount())
        .isZero();

    verify(senderSpy).send("Shout-Out Tracking has been reset.");
  }

  @NotNull
  private CommandEvent createCommandWithText(String commandText) {
    return new CommandEvent(null, null, null, null, commandText, Collections.emptySet());
  }

}