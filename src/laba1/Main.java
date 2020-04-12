package laba1;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;

public class Main {
    public static void main(String[] args) {
        PlatformConfiguration   config  = PlatformConfiguration.getDefault();

        config.setDefaultTimeout(0);
        // config.addComponent("laba1.BotAgent.class");
        config.addComponent("laba1.RegistryAgent.class");
        config.addComponent("laba1.ChatAgent.class");
        config.addComponent("laba1.ChatAgent.class");
        config.addComponent("laba1.ChatAgent.class");
        Starter.createPlatform(config).get();
    }
}