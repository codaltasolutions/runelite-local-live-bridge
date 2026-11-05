package local.livebridge;

import com.google.gson.Gson;
import java.awt.Shape;
import java.awt.Rectangle;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Filepath;

/** Publishes observations only. All game reads happen on the client thread. */
@PluginDescriptor(name = "Local Live Bridge", internalName = "local-live-bridge",
    description = "Exports live game observations to a local file for external assistants",
    tags = {"export", "local", "assistant"})
public class LiveBridgePlugin extends Plugin
{
    @Inject private Client client;
    @Inject private Gson gson;
    private ScheduledExecutorService writer;
    private ScheduledFuture<?> publication;
    private volatile String latest = "{\"status\":\"starting\"}";
    private volatile String writeError;
    private long lastCapture;
    private long sequence;
    private Filepath output;

    @Override protected void startUp() throws Exception
    {
        writer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "livebridge-file-writer");
            thread.setDaemon(true);
            return thread;
        });
        publication = writer.scheduleWithFixedDelay(this::publish, 0, 250, TimeUnit.MILLISECONDS);
    }

    @Override protected void shutDown()
    {
        if (publication != null) publication.cancel(false);
        if (writer != null) writer.shutdownNow();
        // Consumers must also reject snapshots older than two seconds.
        latest = "{\"status\":\"stopped\"}";
    }

    private void publish()
    {
        try
        {
            if (output == null)
            {
                Filepath directory = getPluginDirectory();
                directory.createDirectories();
                output = directory.join("state.json");
            }
            Filepath temporary = output.getParent().join("state.tmp");
            temporary.write(latest);
            try {
                temporary.moveTo(output, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                temporary.moveTo(output, StandardCopyOption.REPLACE_EXISTING);
            }
            writeError = null;
        }
        catch (Exception error) { writeError = error.getClass().getSimpleName(); }
    }

    @Subscribe public void onGameStateChanged(GameStateChanged event)
    {
        lastCapture = 0;
        capture();
    }

    @Subscribe public void onClientTick(ClientTick event) { capture(); }

    private void capture()
    {
        long now = System.currentTimeMillis();
        if (now - lastCapture < 250) return;
        lastCapture = now;
        Map<String, Object> snapshot = map("schema", 1, "sequence", ++sequence, "capturedAt", now,
            "status", client.getGameState().name(), "writeError", writeError);
        // Never inspect login widgets, credentials, account identity, or other players.
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            snapshot.put("canvas", map("width", client.getCanvasWidth(), "height", client.getCanvasHeight()));
            snapshot.put("camera", map("yaw", client.getCameraYaw(), "pitch", client.getCameraPitch()));
            Player self = client.getLocalPlayer();
            if (self != null) snapshot.put("player", map("location", self.getWorldLocation(), "animation", self.getAnimation(), "moving", self.getPoseAnimation() != self.getIdlePoseAnimation()));
            List<Object> inventory = new ArrayList<>();
            ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
            if (container != null)
            {
                Item[] items = container.getItems();
                for (int slot = 0; slot < items.length; slot++)
                {
                    Item item = items[slot];
                    if (item.getId() >= 0) inventory.add(map("slot", slot, "id", item.getId(), "quantity", item.getQuantity(), "name", client.getItemDefinition(item.getId()).getName()));
                }
            }
            snapshot.put("inventory", inventory);
            List<Object> npcs = new ArrayList<>();
            for (NPC npc : client.getNpcs())
            {
                Shape hull = npc.getConvexHull();
                if (hull == null) continue;
                NPCComposition definition = npc.getTransformedComposition();
                npcs.add(map("index", npc.getIndex(), "id", npc.getId(), "name", npc.getName(),
                    "location", npc.getWorldLocation(), "canvasBounds", bounds(hull.getBounds()),
                    "actions", definition == null ? null : definition.getActions()));
            }
            snapshot.put("npcs", npcs);
            List<Object> widgets = new ArrayList<>();
            IdentityHashMap<Widget, Boolean> visited = new IdentityHashMap<>();
            Widget[] roots = client.getWidgetRoots();
            if (roots != null) for (Widget root : roots) collect(root, visited, widgets, 0);
            snapshot.put("widgets", widgets);
        }
        latest = gson.toJson(snapshot);
    }

    private void collect(Widget widget, IdentityHashMap<Widget, Boolean> visited, List<Object> result, int depth)
    {
        if (widget == null || depth > 20 || visited.size() >= 5000 || visited.put(widget, true) != null || widget.isHidden()) return;
        // The chatbox message history is omitted; dialogue and tutorial widgets remain available.
        boolean chatHistory = (widget.getId() >>> 16) == InterfaceID.CHATBOX;
        String text = widget.getText();
        String[] actions = widget.getActions();
        if (!chatHistory && ((text != null && !text.isEmpty()) || actions != null))
            result.add(map("id", widget.getId(), "index", widget.getIndex(), "text", text, "actions", actions, "canvasBounds", bounds(widget.getBounds())));
        Widget[][] children = {widget.getChildren(), widget.getDynamicChildren(), widget.getStaticChildren(), widget.getNestedChildren()};
        for (Widget[] group : children) if (group != null) for (Widget child : group) collect(child, visited, result, depth + 1);
    }

    private static Map<String, Object> bounds(Rectangle r) { return map("x", r.x, "y", r.y, "width", r.width, "height", r.height); }
    private static Map<String, Object> map(Object... values)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put((String) values[i], values[i + 1]);
        return result;
    }
}
