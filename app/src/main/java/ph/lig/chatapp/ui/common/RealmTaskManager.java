package ph.lig.chatapp.ui.common;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmAsyncTask;

public class RealmTaskManager {
    private List<RealmAsyncTask> tasks = new ArrayList<RealmAsyncTask>();

    public void add(RealmAsyncTask task) {
        tasks.add(task);
    }

    public void cancelAllRunning() {
        for(int i = 0; i < tasks.size(); i++) {
            RealmAsyncTask task = tasks.get(i);
            cleanupIfOngoing(task);
        }
    }

    private void cleanupIfOngoing(RealmAsyncTask task) {
        if(task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}
