package io.ourglass.amstelbright.tvui;

/**
 * Created by mkahn on 5/20/16.
 */

// TODO maybe this class should be used in the Broadcast intents as well?
public class UIMessage {

    public String message;
    public int completionPercentage;
    public UIMessageType type;

    public enum UIMessageType {
        BOOT, INFO, REDFLAG
    }

    public UIMessage(String message, int completionPercentage, UIMessageType type){
        this.message = message;
        this.completionPercentage = completionPercentage;
        this.type = type;
    }

    public UIMessage(String message){
        this(message, 100, UIMessageType.INFO);
    }

    public UIMessage(String message, UIMessageType type){
        this(message, 100, type);
    }

}
