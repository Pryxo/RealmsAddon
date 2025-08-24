package xyz.telosaddon.yuno.utils;

import net.minecraft.entity.boss.BossBar;
import xyz.telosaddon.yuno.TelosAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static xyz.telosaddon.yuno.TelosAddon.LOGGER;
import static xyz.telosaddon.yuno.utils.BossBarUtils.bossBarMap;
import static xyz.telosaddon.yuno.utils.TabListUtils.*;

// place to put stuff that I don't really know where else to put
public class LocalAPI {

    private static String currentCharacterType = "";
    private static String currentCharacterClass = "";
    private static int currentCharacterLevel = -1;
    private static String currentCharacterWorld = "";
    private static String currentCharacterArea = "";
    private static String currentCharacterFighting = "";
    private static String currentClientPing = "";
    private static String lastKnownBoss = "";
    private static int lastKnownBossHash=0;
    private static boolean countdownLock = false;
    private static String currentPortalCall = "";
    private static int currentPortalCallTime = 0;
    private static int InitialHash=0; // this is the default hash every time you join the server
    private static Boolean InitialHashSet=false;
    public static void updateAPI(){
        CompletableFuture.runAsync(() -> {
                if (!TelosAddon.getInstance().isOnTelos()) return;
                Optional<String> info = TabListUtils.getCharInfo();
                if (info.isEmpty()) return;

                String[] charInfo = info.get().split(" ");
                if (charInfo.length < 4) return;

                // in 1.3 the format is "(MASTERY)(GAMEMODE) (LEVEL) (CLASS)"
                // the spaces are exact, so u have to split the last 2 chars
                switch (charInfo[0].substring(2).hashCode()) {
                    case 880 -> currentCharacterType = "Normal";
                    case 881 -> currentCharacterType = "Hardcore";
                    case 882 -> currentCharacterType = "Seasonal";
                    default -> // 1771717 -> 1771734 inclusive
                            currentCharacterType = "GHardcore";
                }
                try {
                    currentCharacterLevel = Integer.parseInt(charInfo[2]);
                    currentCharacterClass = charInfo[3];

                }catch (Exception e){
                    e.printStackTrace();
                }
                currentClientPing = getPing().isPresent() ? getPing().get() : String.valueOf(-1);
                updateCharacterArea();
                Optional<String> realm = TabListUtils.getServer();
                realm.ifPresent(s -> currentCharacterWorld = s.replaceAll("[\\[\\]]+", ""));
        });
    }

    private static void updateCharacterArea(){
        
        if (bossBarMap != null) {

            Object[] preArray = bossBarMap.values().toArray();

            if (preArray.length == 5 && preArray[0] instanceof BossBar && preArray[1] instanceof BossBar) {

                BossBar areaBar = (BossBar) preArray[0];
                String area = stripAllFormatting(areaBar.getName().getString());
                currentCharacterArea = area.replaceAll("[^a-zA-z ']+", ""); // idk why but theres numbers at the end so we gotta trim that off

                BossBar bossBar = (BossBar) preArray[1]; // add what boss we're fighting
                // LOGGER.log(Level.INFO, "Bossbar hashcode:" + bossBar.getName().hashCode()); // keep this until i can fill out all the bosses
                lastKnownBoss = currentCharacterFighting;
                switch (bossBar.getName().hashCode()){
                    case -1083980771 -> currentCharacterFighting = "Chungus";
                    case 452824575 -> currentCharacterFighting = "Illarius";
                    case 2125535338 -> currentCharacterFighting = "Astaroth";
                    case -1083975966 -> currentCharacterFighting = "Glumi";
                    case 2125159587 -> currentCharacterFighting = "Lotil";
                    case 453134978 -> currentCharacterFighting = "Tidol";
                    case 1757112170 -> currentCharacterFighting = "Valus";
                    case 1472054207 -> currentCharacterFighting = "Oozul";
                    case 2035818623 -> currentCharacterFighting = "Freddy";
                    case -1258344668 -> currentCharacterFighting = "Anubis";
                    case -1240191621 -> currentCharacterFighting = "Hollowbane";
                    case -1048713371 -> currentCharacterFighting = "Claus";

                    case 908391166 -> currentCharacterFighting = "Shadowflare";
                    case 1996713601 -> currentCharacterFighting = "Loa";
                    case -1048545196 -> currentCharacterFighting = "Valerion";
                    // insert astaroth bosses here
                    case -1624135070 -> currentCharacterFighting = "Prismara";
                    case 2125160548 -> currentCharacterFighting = "Omnipotent";
                    case 1757423534 -> currentCharacterFighting = "Thalassar";
                    case 1735775594 -> currentCharacterFighting = "Silex";
                    case -624873662 -> currentCharacterFighting = "Chronos";
                    case -1338784736 -> currentCharacterFighting = "Golden Freddy";
                    case -1258333136 -> currentCharacterFighting = "Kurvaros";

                    case 2008511319 -> currentCharacterFighting = "Warden";
                    case 2008512280 -> currentCharacterFighting = "Herald";
                    case 2008513241 -> currentCharacterFighting = "Reaper";
                    case 2008514202 -> currentCharacterFighting = "Defender";
                    case 1757100638 -> currentCharacterFighting = "Asmodeus";
                    case 1735762140 -> currentCharacterFighting = "Seraphim";

                    case 1216094805 -> currentCharacterFighting = "Onyx Castle";
                    case 1757905956 -> currentCharacterFighting = "Onyx";

                    case -1083171609 -> currentCharacterFighting = "Pirate's Cove";
                    case 1997519880 -> currentCharacterFighting = "Thornwood Wargrove";
                    default -> currentCharacterFighting = "";
                }
                //Improved system to find HashCodes 
                //This can honestly be kept in if needded , it does not spam logs like before very usefull to get Hash's
                //if The initial hash is known and the player is on an actual boss 
                //if((InitialHash!=bossBar.getName().hashCode()) && lastKnownBossHash!=bossBar.getName().hashCode()){
                //    //comparing Hash cause they are unique , else if we fight two unknown bosses back to back it wont print
                //    LOGGER.info("old Last known Boss: "+ lastKnownBoss);
                //    LOGGER.info(Level.INFO+ " Bossbar hashcode:" + bossBar.getName().hashCode()  +" BossName:" + currentCharacterFighting);
                //    LOGGER.info("last known boss is: " + lastKnownBoss + ", current boss is: " + currentCharacterFighting + ", current Area is: " + currentCharacterArea);
                //    LOGGER.info("last known boss Hash is: " + lastKnownBossHash + ", current boss Hash is: " + bossBar.getName().hashCode());
                //    lastKnownBoss=currentCharacterFighting;
                //    lastKnownBossHash=bossBar.getName().hashCode();
                //}
                //System.out.println("last known boss is: " + lastKnownBoss + ", current boss is: " + currentCharacterFighting + "current portal call is: " + currentPortalCall);
                // this means a boss has died recently.
                if (!lastKnownBoss.equals(currentCharacterFighting) && currentCharacterFighting.equals("")) { // addiing a condition to check if this has been called might improve performance 
                    //As it is rn , this is called every tick if a boss died or even if you move away from a boss.
                    //LOGGER.info("Boss has died");
                    //System.out.println("Last known Boss: "+ lastKnownBoss);
                    switch (lastKnownBoss){
                        case "Chungus" -> currentPortalCall = "void";
                        case "Illarius" -> currentPortalCall = "loa";
                        case "Astaroth" -> currentPortalCall = "shatters";
                        case "Glumi" -> currentPortalCall = "fungal";
                        case "Lotil" -> currentPortalCall = "omni";
                        case "Tidol" -> currentPortalCall =  "corsairs";
                        case "Valus" -> currentPortalCall = "cultists";
                        case "Oozul" -> currentPortalCall = "chronos";
                        case "Freddy" -> currentPortalCall = "pizza";
                        case "Anubis" -> currentPortalCall = "alair";
                        case "Defender" -> currentPortalCall = "cprov";
                
                        default -> currentPortalCall = "";
                    }
                    //lastKnownBoss = ""; why is it making it null lol 
                    if (currentPortalCall.isEmpty() || countdownLock){
                        return;
                    }
                    // a boss portal has dropped, start timer
                    CompletableFuture.runAsync(() -> {
                        countdownLock = true;
                        currentPortalCallTime = 32;
                        while(currentPortalCallTime > 0){
                            currentPortalCallTime--;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        currentPortalCall = "";
                        countdownLock = false;
                
                    });
                }
                
                

            }
        }
    }

    public static String getCurrentCharacterType() {
        return currentCharacterType;
    }
    public static String getCurrentCharacterClass() {
        return currentCharacterClass;
    }

    public static int getCurrentCharacterLevel() {
        return currentCharacterLevel;
    }

    public static String getCurrentCharacterWorld() {
        return currentCharacterWorld;
    }


    public static String getCurrentCharacterFighting() {
        return currentCharacterFighting;
    }

    public static String getCurrentCharacterArea() {
        return currentCharacterArea;
    }

    public static String getCurrentClientPing() {
        return currentClientPing;
    }
    public static String getCurrentPortalCall() {
        return currentPortalCall;
    }

    public static int getCurrentPortalCallTime() {
        return currentPortalCallTime;
    }
}
