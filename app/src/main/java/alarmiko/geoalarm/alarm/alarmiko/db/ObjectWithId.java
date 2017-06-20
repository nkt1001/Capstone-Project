package alarmiko.geoalarm.alarm.alarmiko.db;

public abstract class ObjectWithId {
    private long id;

    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    public final int getIntId() {
        return (int) id;
    }
}
