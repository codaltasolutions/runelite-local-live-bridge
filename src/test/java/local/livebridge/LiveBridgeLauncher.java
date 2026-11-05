package local.livebridge;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class LiveBridgeLauncher
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(LiveBridgePlugin.class);
        RuneLite.main(args);
    }
}
