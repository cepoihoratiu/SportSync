package horatiu.cepoi.app.data.models;

public class ChatMessage {
    private String text;
    private boolean isUserMessage;
    private long timestamp;

    // 🔧 Constructor gol – necesar pentru Firebase sau ListAdapter intern
    public ChatMessage() {
        // valori default
        this.text = "";
        this.isUserMessage = false;
        this.timestamp = 0L;
    }

    // 🔧 Constructor principal
    public ChatMessage(String text, boolean isUserMessage) {
        this.text = text;
        this.isUserMessage = isUserMessage;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;
        ChatMessage that = (ChatMessage) o;
        return isUserMessage == that.isUserMessage &&
                timestamp == that.timestamp &&
                text.equals(that.text);
    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + (isUserMessage ? 1 : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
