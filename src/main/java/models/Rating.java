package models;

public class Rating {
    private int id;
    private int peintureId;
    private int note;
    private int user_id;

    public Rating( int peintureId, int user_id, int note) {
        this.peintureId = peintureId;
        this.user_id = user_id;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPeintureId() {
        return peintureId;
    }

    public void setPeintureId(int peintureId) {
        this.peintureId = peintureId;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
