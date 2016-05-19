package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.ourglass.amstelbright.realm.OGApp;
import io.realm.RealmFieldType;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.android.JsonUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OGAppRealmProxy extends OGApp
    implements RealmObjectProxy, OGAppRealmProxyInterface {

    static final class OGAppColumnInfo extends ColumnInfo {

        public final long appIdIndex;
        public final long appTypeIndex;
        public final long runningIndex;
        public final long onLauncherIndex;
        public final long slotNumberIndex;
        public final long xPosIndex;
        public final long yPosIndex;
        public final long heightIndex;
        public final long widthIndex;
        public final long publicDataIndex;
        public final long privateDataIndex;

        OGAppColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(11);
            this.appIdIndex = getValidColumnIndex(path, table, "OGApp", "appId");
            indicesMap.put("appId", this.appIdIndex);

            this.appTypeIndex = getValidColumnIndex(path, table, "OGApp", "appType");
            indicesMap.put("appType", this.appTypeIndex);

            this.runningIndex = getValidColumnIndex(path, table, "OGApp", "running");
            indicesMap.put("running", this.runningIndex);

            this.onLauncherIndex = getValidColumnIndex(path, table, "OGApp", "onLauncher");
            indicesMap.put("onLauncher", this.onLauncherIndex);

            this.slotNumberIndex = getValidColumnIndex(path, table, "OGApp", "slotNumber");
            indicesMap.put("slotNumber", this.slotNumberIndex);

            this.xPosIndex = getValidColumnIndex(path, table, "OGApp", "xPos");
            indicesMap.put("xPos", this.xPosIndex);

            this.yPosIndex = getValidColumnIndex(path, table, "OGApp", "yPos");
            indicesMap.put("yPos", this.yPosIndex);

            this.heightIndex = getValidColumnIndex(path, table, "OGApp", "height");
            indicesMap.put("height", this.heightIndex);

            this.widthIndex = getValidColumnIndex(path, table, "OGApp", "width");
            indicesMap.put("width", this.widthIndex);

            this.publicDataIndex = getValidColumnIndex(path, table, "OGApp", "publicData");
            indicesMap.put("publicData", this.publicDataIndex);

            this.privateDataIndex = getValidColumnIndex(path, table, "OGApp", "privateData");
            indicesMap.put("privateData", this.privateDataIndex);

            setIndicesMap(indicesMap);
        }
    }

    private final OGAppColumnInfo columnInfo;
    private final ProxyState proxyState;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("appId");
        fieldNames.add("appType");
        fieldNames.add("running");
        fieldNames.add("onLauncher");
        fieldNames.add("slotNumber");
        fieldNames.add("xPos");
        fieldNames.add("yPos");
        fieldNames.add("height");
        fieldNames.add("width");
        fieldNames.add("publicData");
        fieldNames.add("privateData");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    OGAppRealmProxy(ColumnInfo columnInfo) {
        this.columnInfo = (OGAppColumnInfo) columnInfo;
        this.proxyState = new ProxyState(OGApp.class, this);
    }

    @SuppressWarnings("cast")
    public String realmGet$appId() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.appIdIndex);
    }

    public void realmSet$appId(String value) {
        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field appId to null.");
        }
        proxyState.getRow$realm().setString(columnInfo.appIdIndex, value);
    }

    @SuppressWarnings("cast")
    public String realmGet$appType() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.appTypeIndex);
    }

    public void realmSet$appType(String value) {
        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field appType to null.");
        }
        proxyState.getRow$realm().setString(columnInfo.appTypeIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$running() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.runningIndex);
    }

    public void realmSet$running(boolean value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.runningIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$onLauncher() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.onLauncherIndex);
    }

    public void realmSet$onLauncher(boolean value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.onLauncherIndex, value);
    }

    @SuppressWarnings("cast")
    public int realmGet$slotNumber() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.slotNumberIndex);
    }

    public void realmSet$slotNumber(int value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.slotNumberIndex, value);
    }

    @SuppressWarnings("cast")
    public int realmGet$xPos() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.xPosIndex);
    }

    public void realmSet$xPos(int value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.xPosIndex, value);
    }

    @SuppressWarnings("cast")
    public int realmGet$yPos() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.yPosIndex);
    }

    public void realmSet$yPos(int value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.yPosIndex, value);
    }

    @SuppressWarnings("cast")
    public int realmGet$height() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.heightIndex);
    }

    public void realmSet$height(int value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.heightIndex, value);
    }

    @SuppressWarnings("cast")
    public int realmGet$width() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.widthIndex);
    }

    public void realmSet$width(int value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.widthIndex, value);
    }

    @SuppressWarnings("cast")
    public String realmGet$publicData() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.publicDataIndex);
    }

    public void realmSet$publicData(String value) {
        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.publicDataIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.publicDataIndex, value);
    }

    @SuppressWarnings("cast")
    public String realmGet$privateData() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.privateDataIndex);
    }

    public void realmSet$privateData(String value) {
        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.privateDataIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.privateDataIndex, value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_OGApp")) {
            Table table = transaction.getTable("class_OGApp");
            table.addColumn(RealmFieldType.STRING, "appId", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.STRING, "appType", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "running", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "onLauncher", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "slotNumber", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "xPos", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "yPos", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "height", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "width", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.STRING, "publicData", Table.NULLABLE);
            table.addColumn(RealmFieldType.STRING, "privateData", Table.NULLABLE);
            table.addSearchIndex(table.getColumnIndex("appId"));
            table.setPrimaryKey("appId");
            return table;
        }
        return transaction.getTable("class_OGApp");
    }

    public static OGAppColumnInfo validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_OGApp")) {
            Table table = transaction.getTable("class_OGApp");
            if (table.getColumnCount() != 11) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field count does not match - expected 11 but was " + table.getColumnCount());
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < 11; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final OGAppColumnInfo columnInfo = new OGAppColumnInfo(transaction.getPath(), table);

            if (!columnTypes.containsKey("appId")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'appId' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("appId") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'appId' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.appIdIndex) && table.findFirstNull(columnInfo.appIdIndex) != TableOrView.NO_MATCH) {
                throw new IllegalStateException("Cannot migrate an object with null value in field 'appId'. Either maintain the same type for primary key field 'appId', or remove the object with null value before migration.");
            }
            if (table.getPrimaryKey() != table.getColumnIndex("appId")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Primary key not defined for field 'appId' in existing Realm file. Add @PrimaryKey.");
            }
            if (!table.hasSearchIndex(table.getColumnIndex("appId"))) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Index not defined for field 'appId' in existing Realm file. Either set @Index or migrate using io.realm.internal.Table.removeSearchIndex().");
            }
            if (!columnTypes.containsKey("appType")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'appType' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("appType") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'appType' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.appTypeIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'appType' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'appType' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("running")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'running' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("running") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'running' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.runningIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'running' does support null values in the existing Realm file. Use corresponding boxed type for field 'running' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("onLauncher")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'onLauncher' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("onLauncher") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'onLauncher' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.onLauncherIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'onLauncher' does support null values in the existing Realm file. Use corresponding boxed type for field 'onLauncher' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("slotNumber")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'slotNumber' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("slotNumber") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'int' for field 'slotNumber' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.slotNumberIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'slotNumber' does support null values in the existing Realm file. Use corresponding boxed type for field 'slotNumber' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("xPos")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'xPos' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("xPos") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'int' for field 'xPos' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.xPosIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'xPos' does support null values in the existing Realm file. Use corresponding boxed type for field 'xPos' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("yPos")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'yPos' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("yPos") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'int' for field 'yPos' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.yPosIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'yPos' does support null values in the existing Realm file. Use corresponding boxed type for field 'yPos' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("height")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'height' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("height") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'int' for field 'height' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.heightIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'height' does support null values in the existing Realm file. Use corresponding boxed type for field 'height' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("width")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'width' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("width") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'int' for field 'width' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.widthIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'width' does support null values in the existing Realm file. Use corresponding boxed type for field 'width' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("publicData")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'publicData' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("publicData") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'publicData' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.publicDataIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'publicData' is required. Either set @Required to field 'publicData' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("privateData")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'privateData' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("privateData") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'privateData' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.privateDataIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'privateData' is required. Either set @Required to field 'privateData' or migrate using RealmObjectSchema.setNullable().");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(transaction.getPath(), "The OGApp class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_OGApp";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static OGApp createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
        throws JSONException {
        OGApp obj = null;
        if (update) {
            Table table = realm.getTable(OGApp.class);
            long pkColumnIndex = table.getPrimaryKey();
            long rowIndex = TableOrView.NO_MATCH;
            if (!json.isNull("appId")) {
                rowIndex = table.findFirstString(pkColumnIndex, json.getString("appId"));
            }
            if (rowIndex != TableOrView.NO_MATCH) {
                obj = new OGAppRealmProxy(realm.schema.getColumnInfo(OGApp.class));
                ((RealmObjectProxy)obj).realmGet$proxyState().setRealm$realm(realm);
                ((RealmObjectProxy)obj).realmGet$proxyState().setRow$realm(table.getUncheckedRow(rowIndex));
            }
        }
        if (obj == null) {
            if (json.has("appId")) {
                if (json.isNull("appId")) {
                    obj = (OGAppRealmProxy) realm.createObject(OGApp.class, null);
                } else {
                    obj = (OGAppRealmProxy) realm.createObject(OGApp.class, json.getString("appId"));
                }
            } else {
                obj = (OGAppRealmProxy) realm.createObject(OGApp.class);
            }
        }
        if (json.has("appId")) {
            if (json.isNull("appId")) {
                ((OGAppRealmProxyInterface) obj).realmSet$appId(null);
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$appId((String) json.getString("appId"));
            }
        }
        if (json.has("appType")) {
            if (json.isNull("appType")) {
                ((OGAppRealmProxyInterface) obj).realmSet$appType(null);
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$appType((String) json.getString("appType"));
            }
        }
        if (json.has("running")) {
            if (json.isNull("running")) {
                throw new IllegalArgumentException("Trying to set non-nullable field running to null.");
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$running((boolean) json.getBoolean("running"));
            }
        }
        if (json.has("onLauncher")) {
            if (json.isNull("onLauncher")) {
                throw new IllegalArgumentException("Trying to set non-nullable field onLauncher to null.");
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$onLauncher((boolean) json.getBoolean("onLauncher"));
            }
        }
        if (json.has("slotNumber")) {
            if (json.isNull("slotNumber")) {
                throw new IllegalArgumentException("Trying to set non-nullable field slotNumber to null.");
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$slotNumber((int) json.getInt("slotNumber"));
            }
        }
        if (json.has("xPos")) {
            if (json.isNull("xPos")) {
                throw new IllegalArgumentException("Trying to set non-nullable field xPos to null.");
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$xPos((int) json.getInt("xPos"));
            }
        }
        if (json.has("yPos")) {
            if (json.isNull("yPos")) {
                throw new IllegalArgumentException("Trying to set non-nullable field yPos to null.");
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$yPos((int) json.getInt("yPos"));
            }
        }
        if (json.has("height")) {
            if (json.isNull("height")) {
                throw new IllegalArgumentException("Trying to set non-nullable field height to null.");
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$height((int) json.getInt("height"));
            }
        }
        if (json.has("width")) {
            if (json.isNull("width")) {
                throw new IllegalArgumentException("Trying to set non-nullable field width to null.");
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$width((int) json.getInt("width"));
            }
        }
        if (json.has("publicData")) {
            if (json.isNull("publicData")) {
                ((OGAppRealmProxyInterface) obj).realmSet$publicData(null);
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$publicData((String) json.getString("publicData"));
            }
        }
        if (json.has("privateData")) {
            if (json.isNull("privateData")) {
                ((OGAppRealmProxyInterface) obj).realmSet$privateData(null);
            } else {
                ((OGAppRealmProxyInterface) obj).realmSet$privateData((String) json.getString("privateData"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    public static OGApp createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        OGApp obj = realm.createObject(OGApp.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("appId")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((OGAppRealmProxyInterface) obj).realmSet$appId(null);
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$appId((String) reader.nextString());
                }
            } else if (name.equals("appType")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((OGAppRealmProxyInterface) obj).realmSet$appType(null);
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$appType((String) reader.nextString());
                }
            } else if (name.equals("running")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field running to null.");
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$running((boolean) reader.nextBoolean());
                }
            } else if (name.equals("onLauncher")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field onLauncher to null.");
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$onLauncher((boolean) reader.nextBoolean());
                }
            } else if (name.equals("slotNumber")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field slotNumber to null.");
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$slotNumber((int) reader.nextInt());
                }
            } else if (name.equals("xPos")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field xPos to null.");
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$xPos((int) reader.nextInt());
                }
            } else if (name.equals("yPos")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field yPos to null.");
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$yPos((int) reader.nextInt());
                }
            } else if (name.equals("height")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field height to null.");
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$height((int) reader.nextInt());
                }
            } else if (name.equals("width")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field width to null.");
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$width((int) reader.nextInt());
                }
            } else if (name.equals("publicData")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((OGAppRealmProxyInterface) obj).realmSet$publicData(null);
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$publicData((String) reader.nextString());
                }
            } else if (name.equals("privateData")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((OGAppRealmProxyInterface) obj).realmSet$privateData(null);
                } else {
                    ((OGAppRealmProxyInterface) obj).realmSet$privateData((String) reader.nextString());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return obj;
    }

    public static OGApp copyOrUpdate(Realm realm, OGApp object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        OGApp realmObject = null;
        boolean canUpdate = update;
        if (canUpdate) {
            Table table = realm.getTable(OGApp.class);
            long pkColumnIndex = table.getPrimaryKey();
            long rowIndex = table.findFirstString(pkColumnIndex, ((OGAppRealmProxyInterface) object).realmGet$appId());
            if (rowIndex != TableOrView.NO_MATCH) {
                realmObject = new OGAppRealmProxy(realm.schema.getColumnInfo(OGApp.class));
                ((RealmObjectProxy)realmObject).realmGet$proxyState().setRealm$realm(realm);
                ((RealmObjectProxy)realmObject).realmGet$proxyState().setRow$realm(table.getUncheckedRow(rowIndex));
                cache.put(object, (RealmObjectProxy) realmObject);
            } else {
                canUpdate = false;
            }
        }

        if (canUpdate) {
            return update(realm, realmObject, object, cache);
        } else {
            return copy(realm, object, update, cache);
        }
    }

    public static OGApp copy(Realm realm, OGApp newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        OGApp realmObject = realm.createObject(OGApp.class, ((OGAppRealmProxyInterface) newObject).realmGet$appId());
        cache.put(newObject, (RealmObjectProxy) realmObject);
        ((OGAppRealmProxyInterface) realmObject).realmSet$appId(((OGAppRealmProxyInterface) newObject).realmGet$appId());
        ((OGAppRealmProxyInterface) realmObject).realmSet$appType(((OGAppRealmProxyInterface) newObject).realmGet$appType());
        ((OGAppRealmProxyInterface) realmObject).realmSet$running(((OGAppRealmProxyInterface) newObject).realmGet$running());
        ((OGAppRealmProxyInterface) realmObject).realmSet$onLauncher(((OGAppRealmProxyInterface) newObject).realmGet$onLauncher());
        ((OGAppRealmProxyInterface) realmObject).realmSet$slotNumber(((OGAppRealmProxyInterface) newObject).realmGet$slotNumber());
        ((OGAppRealmProxyInterface) realmObject).realmSet$xPos(((OGAppRealmProxyInterface) newObject).realmGet$xPos());
        ((OGAppRealmProxyInterface) realmObject).realmSet$yPos(((OGAppRealmProxyInterface) newObject).realmGet$yPos());
        ((OGAppRealmProxyInterface) realmObject).realmSet$height(((OGAppRealmProxyInterface) newObject).realmGet$height());
        ((OGAppRealmProxyInterface) realmObject).realmSet$width(((OGAppRealmProxyInterface) newObject).realmGet$width());
        ((OGAppRealmProxyInterface) realmObject).realmSet$publicData(((OGAppRealmProxyInterface) newObject).realmGet$publicData());
        ((OGAppRealmProxyInterface) realmObject).realmSet$privateData(((OGAppRealmProxyInterface) newObject).realmGet$privateData());
        return realmObject;
    }

    public static OGApp createDetachedCopy(OGApp realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        OGApp standaloneObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (OGApp)cachedObject.object;
            } else {
                standaloneObject = (OGApp)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            standaloneObject = new OGApp();
            cache.put(realmObject, new RealmObjectProxy.CacheData(currentDepth, standaloneObject));
        }
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$appId(((OGAppRealmProxyInterface) realmObject).realmGet$appId());
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$appType(((OGAppRealmProxyInterface) realmObject).realmGet$appType());
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$running(((OGAppRealmProxyInterface) realmObject).realmGet$running());
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$onLauncher(((OGAppRealmProxyInterface) realmObject).realmGet$onLauncher());
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$slotNumber(((OGAppRealmProxyInterface) realmObject).realmGet$slotNumber());
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$xPos(((OGAppRealmProxyInterface) realmObject).realmGet$xPos());
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$yPos(((OGAppRealmProxyInterface) realmObject).realmGet$yPos());
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$height(((OGAppRealmProxyInterface) realmObject).realmGet$height());
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$width(((OGAppRealmProxyInterface) realmObject).realmGet$width());
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$publicData(((OGAppRealmProxyInterface) realmObject).realmGet$publicData());
        ((OGAppRealmProxyInterface) standaloneObject).realmSet$privateData(((OGAppRealmProxyInterface) realmObject).realmGet$privateData());
        return standaloneObject;
    }

    static OGApp update(Realm realm, OGApp realmObject, OGApp newObject, Map<RealmModel, RealmObjectProxy> cache) {
        ((OGAppRealmProxyInterface) realmObject).realmSet$appType(((OGAppRealmProxyInterface) newObject).realmGet$appType());
        ((OGAppRealmProxyInterface) realmObject).realmSet$running(((OGAppRealmProxyInterface) newObject).realmGet$running());
        ((OGAppRealmProxyInterface) realmObject).realmSet$onLauncher(((OGAppRealmProxyInterface) newObject).realmGet$onLauncher());
        ((OGAppRealmProxyInterface) realmObject).realmSet$slotNumber(((OGAppRealmProxyInterface) newObject).realmGet$slotNumber());
        ((OGAppRealmProxyInterface) realmObject).realmSet$xPos(((OGAppRealmProxyInterface) newObject).realmGet$xPos());
        ((OGAppRealmProxyInterface) realmObject).realmSet$yPos(((OGAppRealmProxyInterface) newObject).realmGet$yPos());
        ((OGAppRealmProxyInterface) realmObject).realmSet$height(((OGAppRealmProxyInterface) newObject).realmGet$height());
        ((OGAppRealmProxyInterface) realmObject).realmSet$width(((OGAppRealmProxyInterface) newObject).realmGet$width());
        ((OGAppRealmProxyInterface) realmObject).realmSet$publicData(((OGAppRealmProxyInterface) newObject).realmGet$publicData());
        ((OGAppRealmProxyInterface) realmObject).realmSet$privateData(((OGAppRealmProxyInterface) newObject).realmGet$privateData());
        return realmObject;
    }

    @Override
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("OGApp = [");
        stringBuilder.append("{appId:");
        stringBuilder.append(realmGet$appId());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{appType:");
        stringBuilder.append(realmGet$appType());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{running:");
        stringBuilder.append(realmGet$running());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{onLauncher:");
        stringBuilder.append(realmGet$onLauncher());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{slotNumber:");
        stringBuilder.append(realmGet$slotNumber());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{xPos:");
        stringBuilder.append(realmGet$xPos());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{yPos:");
        stringBuilder.append(realmGet$yPos());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{height:");
        stringBuilder.append(realmGet$height());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{width:");
        stringBuilder.append(realmGet$width());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{publicData:");
        stringBuilder.append(realmGet$publicData() != null ? realmGet$publicData() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{privateData:");
        stringBuilder.append(realmGet$privateData() != null ? realmGet$privateData() : "null");
        stringBuilder.append("}");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public ProxyState realmGet$proxyState() {
        return proxyState;
    }

    @Override
    public int hashCode() {
        String realmName = proxyState.getRealm$realm().getPath();
        String tableName = proxyState.getRow$realm().getTable().getName();
        long rowIndex = proxyState.getRow$realm().getIndex();

        int result = 17;
        result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0);
        result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0);
        result = 31 * result + (int) (rowIndex ^ (rowIndex >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OGAppRealmProxy aOGApp = (OGAppRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aOGApp.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aOGApp.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aOGApp.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}
