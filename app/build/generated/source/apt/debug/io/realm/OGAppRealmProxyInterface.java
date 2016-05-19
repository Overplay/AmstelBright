package io.realm;


public interface OGAppRealmProxyInterface {
    public String realmGet$appId();
    public void realmSet$appId(String value);
    public String realmGet$appType();
    public void realmSet$appType(String value);
    public boolean realmGet$running();
    public void realmSet$running(boolean value);
    public boolean realmGet$onLauncher();
    public void realmSet$onLauncher(boolean value);
    public int realmGet$slotNumber();
    public void realmSet$slotNumber(int value);
    public int realmGet$xPos();
    public void realmSet$xPos(int value);
    public int realmGet$yPos();
    public void realmSet$yPos(int value);
    public int realmGet$height();
    public void realmSet$height(int value);
    public int realmGet$width();
    public void realmSet$width(int value);
    public String realmGet$publicData();
    public void realmSet$publicData(String value);
    public String realmGet$privateData();
    public void realmSet$privateData(String value);
}
