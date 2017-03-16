package guichaguri.trackplayer;

import android.content.Context;
import com.facebook.react.bridge.BaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import guichaguri.trackplayer.metadata.Metadata;
import guichaguri.trackplayer.player.Player;
import guichaguri.trackplayer.player.players.AndroidPlayer;
import java.util.Arrays;

/**
 * @author Guilherme Chaguri
 */
public class TrackModule extends BaseJavaModule {

    private final Context context;

    private Player[] players = new Player[0];
    private Metadata[] metadatas = new Metadata[0];

    public TrackModule(Context context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return "TrackPlayer";
    }

    @ReactMethod
    public int createPlayer() {
        int id = players.length;
        players = Arrays.copyOf(players, id + 1);
        players[id] = new AndroidPlayer(context); // TODO
        return id;
    }

    @ReactMethod
    public void destroyPlayer(int id) {
        if(id == -1) {
            // Destroys all players
            for(Player p : players) p.destroy();
            players = new Player[0];
        } else {
            Player[] pls = new Player[players.length - 1];
            for(int o = 0; o < players.length; o++) {
                if(id == o) {
                    players[o].destroy();
                } else {
                    pls[o > id ? o - 1 : o] = players[o];
                }
            }
            players = pls;
        }
    }

    @ReactMethod
    public int createMetadata() {
        int id = metadatas.length;
        metadatas = Arrays.copyOf(metadatas, id + 1);
        metadatas[id] = new Metadata(context);
        return id;
    }

    @ReactMethod
    public void destroyMetadata(int id) {
        if(id == -1) {
            // Destroys all players
            for(Metadata p : metadatas) p.destroy();
            metadatas = new Metadata[0];
        } else {
            Metadata[] pls = new Metadata[metadatas.length - 1];
            for(int o = 0; o < metadatas.length; o++) {
                if(id == o) {
                    metadatas[o].destroy();
                } else {
                    pls[o > id ? o - 1 : o] = metadatas[o];
                }
            }
            metadatas = pls;
        }
    }

}
