package ryey.easer.plugins.event;

import android.util.Log;

import java.util.Set;

import ryey.easer.commons.plugindef.eventplugin.EventData;
import ryey.easer.commons.plugindef.eventplugin.EventType;

public abstract class TypedEventData implements EventData {
    protected Set<EventType> availableTypes;
    protected EventType type = null, default_type = null;

    @Override
    public void setType(EventType type) {
        if (type == null) {
            Log.w(getClass().getSimpleName(),
                    String.format("got invalid type. fallback to the default type: %s",
                            default_type));
            this.type = default_type;
        } else {
            if (!isAvailable(type)) {
                throw new IllegalArgumentException("Improper EventType to set");
            }
            this.type = type;
        }
    }

    @Override
    public EventType type() {
        if (type != null)
            return type;
        else
            return default_type;
    }

    @Override
    public Set<EventType> availableTypes() {
        return availableTypes;
    }

    @Override
    public boolean isAvailable(EventType type) {
        return availableTypes().contains(type);
    }
}
