package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * @author: wangda
 * @date: 2023/1/14
 */
public class Dfs {
    @SerializedName("FSNamesystem")
    private FsNameSystem fsNameSystem;

    public FsNameSystem getFsNameSystem() {
        return fsNameSystem;
    }

    public void setFsNameSystem(FsNameSystem fsNameSystem) {
        this.fsNameSystem = fsNameSystem;
    }
}
