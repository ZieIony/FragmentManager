package pl.zielony.animator;

import android.os.Handler;
import android.os.Looper;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

public class Animator {
    private long duration;
    private long delay;
    private long startTime;
    private List<AnimatorListener> listeners = new ArrayList<>();
    private UpdateListener updateListener;
    private Interpolator interpolator;

    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean running = false;

    public Animator() {
    }

    public Animator(long duration, UpdateListener updateListener) {
        this.duration = duration;
        this.updateListener = updateListener;
    }

    public Animator(long duration, Interpolator interpolator, UpdateListener updateListener) {
        this.duration = duration;
        this.interpolator = interpolator;
        this.updateListener = updateListener;
    }

    private Runnable startRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (Animator.this) {
                for (AnimatorListener listener : listeners)
                    listener.onStart();
                if (updateListener != null)
                    updateListener.onUpdate(0);
                handler.post(updateRunnable);
            }
        }
    };

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (Animator.this) {
                if (!running)
                    return;
                long time = System.currentTimeMillis();
                if (startTime + duration > time) {
                    time = Math.min(time, startTime + duration);
                    if (updateListener != null) {
                        float interpolation = (float) (time - startTime) / duration;
                        if (interpolator != null)
                            interpolation = interpolator.getInterpolation(interpolation);
                        updateListener.onUpdate(interpolation);
                    }
                    handler.post(this);
                } else {
                    if (updateListener != null)
                        updateListener.onUpdate(1);
                    running = false;
                    for (AnimatorListener listener : listeners)
                        listener.onEnd();
                }
            }
        }
    };
    private Runnable cancelRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (Animator.this) {
                running = false;
                for (AnimatorListener listener : listeners)
                    listener.onCancel();
            }
        }
    };

    public void start() {
        synchronized (Animator.this) {
            startTime = System.currentTimeMillis() + delay;
            running = true;
            handler.postDelayed(startRunnable, delay);
        }
    }

    public void cancel() {
        synchronized (Animator.this) {
            if (!running)
                return;
            handler.post(cancelRunnable);
        }
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void addListener(AnimatorListener listener) {
        listeners.add(listener);
    }

    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public boolean isRunning() {
        return running;
    }

    public void removeListener(AnimatorListenerAdapter listener) {
        listeners.remove(listener);
    }
}