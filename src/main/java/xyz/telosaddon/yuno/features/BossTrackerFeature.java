package xyz.telosaddon.yuno.features;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.text.Text;
import xyz.telosaddon.yuno.TelosAddon;
import xyz.telosaddon.yuno.event.api.realm.RaphPortalSpawnedEventHandler;
import xyz.telosaddon.yuno.event.api.realm.WorldBossDefeatedEventHandler;
import xyz.telosaddon.yuno.event.api.realm.BossSpawnedEventHandler;
import xyz.telosaddon.yuno.event.HandledScreenRemovedCallback;
import xyz.telosaddon.yuno.utils.data.BossData;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class BossTrackerFeature extends ToggleableFeature implements WorldBossDefeatedEventHandler, BossSpawnedEventHandler, HandledScreenRemovedCallback, RaphPortalSpawnedEventHandler {

    private static final Pattern BOSS_ITEM_NAME_PATTERN = Pattern.compile("^» \\[(\\w+)] «");

    private final Set<BossData> currentAlive = ConcurrentHashMap.newKeySet();

    BossTrackerFeature() {
        super(TelosAddon.CONFIG.keys.bossTrackerFeatureEnabled);
        WorldBossDefeatedEventHandler.EVENT.register(this);
        BossSpawnedEventHandler.EVENT.register(this);
        HandledScreenRemovedCallback.EVENT.register(this);
        RaphPortalSpawnedEventHandler.EVENT.register(this);
    }

    public void addAlive(String name){
        var newBoss = BossData.fromString(name);
        newBoss.ifPresent(currentAlive::add);
    }

    public void removeAlive(String name){
        var newBoss = BossData.fromString(name);
        newBoss.ifPresent(currentAlive::remove);
    }

    public void clearAlive(){
        currentAlive.clear();
    }

    public boolean isBossAlive(String name){
        var bossData = BossData.fromString(name);
        return bossData.isPresent() && currentAlive.contains(bossData.get());
    }

    public Set<BossData> getCurrentAlive (){
        return currentAlive;
    }

    @Override
    public void onBossDefeated(String bossName) {
        removeAlive(bossName);
    }

    @Override
    public void onBossSpawned(String bossName) {
        addAlive(bossName);
    }

    @Override
    public void onRemoved(Screen screen) {
        if (!isEnabled()) {
            return;
        }

        if (!(screen instanceof GenericContainerScreen containerScreen)) {
            return;
        }

        var inventory = containerScreen.getScreenHandler().getInventory();

        if (!(inventory instanceof SimpleInventory simpleInventory)) {
            return;
        }

        for (var stack : simpleInventory.getHeldStacks()) {
            var stackName = stack.getName().getString();
            var bossItemNameMatcher = BOSS_ITEM_NAME_PATTERN.matcher(stackName);

            if (!bossItemNameMatcher.find()) {
                continue;
            }
            var loreData = stack.getComponents().get(DataComponentTypes.LORE);

            if (loreData == null) {
                continue;
            }

            var loreString = loreData
                    .lines()
                    .stream()
                    .map(Text::getString)
                    .collect(Collectors.joining(" "));

            if (loreString.contains("This boss is alive")) {
                addAlive(bossItemNameMatcher.group(1));
            } else if (loreString.contains("This boss has been defeated")) {
                removeAlive(bossItemNameMatcher.group(1));
            } else if (loreString.contains("This boss has not spawned")) {
                removeAlive(bossItemNameMatcher.group(1));
            }
        }
    }

    @Override
    public void onRaphSpawned() {
        // async for timer
        CompletableFuture.runAsync(() -> {
            try {
                currentAlive.clear();
                currentAlive.add(BossData.RAPHAEL);

                int countdown = 60;
                while (countdown > 0) {
                    countdown--;
                    Thread.sleep(1000);
                }
                currentAlive.remove(BossData.RAPHAEL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}