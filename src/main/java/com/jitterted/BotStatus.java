package com.jitterted;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BotStatus {
  private boolean shoutOutEnabled;
}
