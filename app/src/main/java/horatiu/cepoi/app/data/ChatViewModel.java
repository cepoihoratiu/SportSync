package horatiu.cepoi.app.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import horatiu.cepoi.app.data.models.ChatMessage;
import horatiu.cepoi.app.service.GeminiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatViewModel extends ViewModel {

    private final MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<ChatMessage>> getMessages() {
        ChatMessage botMessage = new ChatMessage("Salut! Sunt aici pentru a te ajuta in sport!", false);
        addMessageToList(botMessage);
        return messagesLiveData;
    }

    private final List<String> sportKeywords = Arrays.asList(
            // Sporturi generale
            "sport", "fotbal", "baschet", "tenis", "volei", "handbal", "campionat",
            "meci", "jucător", "echipă", "antrenor", "olimpiadă", "competiție",
            "liga", "scor", "rezultat", "antrenament", "stadion", "cursă", "înot",
            // Termeni legati de sală și fitness
            "sală", "fitness", "gym", "antrenament", "antrenamente", "cardio",
            "forță", "greutăți", "gantere", "bara", "halteră", "triceps", "biceps",
            "piept", "abdomen", "abdomenul", "spate", "umeri", "picioare", "coapse",
            "fesieri", "genuflexiuni", "flotări", "abdomene", "ridicări", "întinderi",
            "extensii", "încălzire", "stretching", "mobilitate", "flexibilitate",
            "HIIT", "aerobic", "bodybuilding", "powerlifting", "crossfit",
            "anduranță", "suplimente", "proteine", "creatină", "recuperare"
    );


    public void sendMessage(String text) {
        addMessageToList(new ChatMessage(text, true));
        if (isSportsRelated(text)) {
            GeminiService.sendMessageToGemini(text, new GeminiService.GeminiCallback() {
                @Override
                public void onSuccess(String response) {
                    ChatMessage botResponse = new ChatMessage(response, false);
                    addMessageToList(botResponse);
                }

                @Override
                public void onFailure(String error) {
                    ChatMessage errorMessage = new ChatMessage("Eroare la Gemini: " + error, false);
                    addMessageToList(errorMessage);
                }
            });
        } else {
            addMessageToList(new ChatMessage("Te rog să adresezi doar întrebări legate de sport.", false));
        }
    }

    private void addMessageToList(ChatMessage message) {
        List<ChatMessage> currentList = messagesLiveData.getValue();
        if (currentList == null) currentList = new ArrayList<>();
        List<ChatMessage> newList = new ArrayList<>(currentList);
        newList.add(message);
        messagesLiveData.postValue(newList);
    }

    private boolean isSportsRelated(String text) {
        String lower = text.toLowerCase();
        for (String keyword : sportKeywords) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }
}
