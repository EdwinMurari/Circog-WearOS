package org.hcilab.circog_watch;

import android.os.AsyncTask;
import android.util.Log;

public abstract class CallbackTask<T> extends AsyncTask<Void, T, T> {
    private OnPostTaskComplete<T> _listener;

    @Override
    protected void onPostExecute(T result) {
        if (_listener != null)
            _listener.onProcessFinished(result);
    }

    protected T doInBackground(Void... params) {
        T result = null;

        try {
            result = doInBackground();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }

        return result;
    }

    protected abstract T doInBackground() throws Exception;

    public final void executeOnThreadPool() {
        executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    public final void setOnTaskCompleteListener(OnPostTaskComplete<T> listener) {
        _listener = listener;
    }

    public interface OnPostTaskComplete<T> {
        void onProcessFinished(T output);
    }
}
