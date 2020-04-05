package laba1;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;

public class Main {
    public static void main(String[] args) {
        PlatformConfiguration   config  = PlatformConfiguration.getDefault();

        config.setDefaultTimeout(0);
        config.addComponent("laba1.RegistryE3Agent.class");
        config.addComponent("laba1.ChatD5Agent.class");
        config.addComponent("laba1.ChatD5Agent.class");
        config.addComponent("laba1.ChatD5Agent.class");
        Starter.createPlatform(config).get();
    }
}