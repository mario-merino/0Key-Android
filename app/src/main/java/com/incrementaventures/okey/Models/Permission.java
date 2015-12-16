package com.incrementaventures.okey.Models;

import android.os.AsyncTask;
import android.os.Build;
import android.text.format.Time;

import com.incrementaventures.okey.Bluetooth.BluetoothProtocol;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Permission implements com.incrementaventures.okey.Models.ParseObject {

    public static final int ADMIN_PERMISSION = 0;
    public static final int PERMANENT_PERMISSION = 1;
    public static final int TEMPORAL_PERMISSION = 2;
    public static final int UNKNOWN_PERMISSION = 3;

    public static final String PERMANENT_DATE = "3000-01-01T00:01";

    public static final String PERMISSION_CLASS_NAME = "Permission";
    public static final String USER_UUID = "user_uuid";
    public static final String MASTER_ID = "master_id";
    public static final String SLAVE_ID = "slave_id";
    public static final String UUID = "uuid";
    public static final String TYPE = "type";
    public static final String KEY = "key";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String CREATED_AT = "createdAt";
    public static final String NAME = "name";

    private ParseObject mParsePermission;

    public interface OnNetworkResponseListener {
        void onNewPermissions(HashMap<Master, Permission> permissions);
    }

    private Permission(ParseObject parsePermission){
        mParsePermission = parsePermission;
    }

    private Permission(User user, Master master, int type, String key, String startDate,
                       String endDate, int slaveId) {
        mParsePermission = ParseObject.create(PERMISSION_CLASS_NAME);
        if (user != null) mParsePermission.put(USER_UUID, user.getUUID());
        if (master != null) {
            mParsePermission.put(MASTER_ID, master.getId());
        }
        mParsePermission.put(SLAVE_ID, slaveId);
        mParsePermission.put(TYPE, type);
        mParsePermission.put(KEY, key);
        mParsePermission.put(START_DATE, startDate);
        mParsePermission.put(END_DATE, endDate);
        mParsePermission.put(UUID, java.util.UUID.randomUUID().toString());
    }

    public static Permission create(User user, Master master, int type, String key,
                                    String startDate, String endDate, int slaveId) {
        return new Permission(user, master, type, key, startDate, endDate, slaveId);
    }

    public static Permission create(ParseObject parsePermission){
        return new Permission(parsePermission);
    }

    public class Builder {
        private User mUser;
        private Master mMaster;
        private int mSlaveId;
        private int mType;
        private String mKey;
        private String mStartDate;
        private String mEndDate;

        public Builder() {

        }

        public Builder setUser(User user) {
            mUser = user;
            return this;
        }

        public Builder setMaster(Master master) {
            mMaster = master;
            return this;
        }

        public Builder setType(int type) {
            mType = type;
            return this;
        }

        public Builder setKey(String key) {
            mKey = key;
            return this;
        }

        public Builder setStartDate(String startDate) {
            mStartDate = startDate;
            return this;
        }

        public Builder setEndDate(String endDate) {
            mEndDate = endDate;
            return this;
        }

        public Builder setSlaveId(int slaveId) {
            mSlaveId = slaveId;
            return this;
        }

        public Permission build() {
            return new Permission(mUser, mMaster, mType, mKey, mStartDate, mEndDate, mSlaveId);
        }
    }

    public void share() {
        save();
    }

    public String getUserUuid() {
        return mParsePermission.getString(USER_UUID);
    }

    @Override
    public String getObjectId() {
        return mParsePermission.getObjectId();
    }

    @Override
    public void deleteFromLocal() {
        try {
            mParsePermission.unpin();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllLocal() {
        ParseQuery<com.parse.ParseObject> query = ParseQuery.getQuery(PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        try {
            List<com.parse.ParseObject> localPermissions = query.find();
            for (com.parse.ParseObject localPermission : localPermissions) {
                localPermission.unpin();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        deleteFromLocal();
        mParsePermission.deleteEventually();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(PERMISSION_CLASS_NAME);
        query.whereEqualTo(SLAVE_ID, getSlaveId());
        query.whereEqualTo(USER_UUID, getUserUuid());
        query.whereEqualTo(MASTER_ID, getMaster().getId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e == null && list != null && list.size() != 0) {
                    for (ParseObject permission : list) {
                        permission.deleteEventually();
                    }
                }
            }
        });
    }

    @Override
    public void save() {
        mParsePermission.pinInBackground();
        mParsePermission.saveEventually();
    }

    public String getUUID() {
        return mParsePermission.getString(UUID);
    }

    public Master getMaster(){
        ParseQuery query = new ParseQuery(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Master.ID, mParsePermission.getString(MASTER_ID));
        try {
            ParseObject o = query.getFirst();
            return Master.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMasterId() {
        return mParsePermission.getString(MASTER_ID);
    }

    public static Permission getPermission(String userUuid, String masterId, int slaveId) {
        ParseQuery query = new ParseQuery(PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(MASTER_ID, masterId);
        query.whereEqualTo(USER_UUID, userUuid);
        query.whereEqualTo(SLAVE_ID, slaveId);
        try {
            ParseObject o = query.getFirst();
            return Permission.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Permission getPermission(String uuid) {
        ParseQuery query = new ParseQuery(PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(UUID, uuid);
        try {
            ParseObject o = query.getFirst();
            return Permission.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUser(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.UUID, mParsePermission.getString(USER_UUID));
        try {
            ParseUser o = query.getFirst();
            return User.create(o);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getType(){
        switch (mParsePermission.getInt(TYPE)){
            case TEMPORAL_PERMISSION:
                return "Temporal";
            case PERMANENT_PERMISSION:
                return "Permanent";
            case ADMIN_PERMISSION:
                return  "Administrator";
            default:
                return "Unknown";
        }
    }

    public static int getType(String type){
        switch (type) {
            case "Temporal":
                return TEMPORAL_PERMISSION;
            case "Permanent":
                return PERMANENT_PERMISSION;
            case "Administrator":
                return ADMIN_PERMISSION;
            default:
                return 3;
        }
    }

    public void setType(int type){
        mParsePermission.put(TYPE, type);
    }
    public void setSlaveId(int slaveId){
        mParsePermission.put(SLAVE_ID, slaveId);
    }

    public String getKey(){
        return mParsePermission.getString(KEY);
    }

    private boolean started(Time time){
        if (mParsePermission.getString(START_DATE) == null) return true;
        return mParsePermission.getString(START_DATE).compareTo(BluetoothProtocol.formatDate(time))> 0;
    }

    private boolean finished(Time time){
        if (mParsePermission.getString(END_DATE) == null) return true;
        return mParsePermission.getString(END_DATE).compareTo(BluetoothProtocol.formatDate(time)) > 0;
    }

    public boolean isValid(){
        Time time = new Time();
        if (mParsePermission.getInt(TYPE) == ADMIN_PERMISSION) return true;
        else if (mParsePermission.getInt(TYPE) == PERMANENT_PERMISSION && started(time)){
            return true;
        }
        else if (mParsePermission.getInt(TYPE) == TEMPORAL_PERMISSION && started(time) && !finished(time)){
            return true;
        }
        else if (mParsePermission.getInt(TYPE) == UNKNOWN_PERMISSION){
            return true;
        }
        return false;
    }

    public String getStartDate(){
        return mParsePermission.getString(START_DATE);
    }

    public void setStartDate(String startDate){
        mParsePermission.put(START_DATE, startDate);
    }

    public String getEndDate(){
        return mParsePermission.getString(END_DATE);
    }

    public void setEndDate(String endDate){
        mParsePermission.put(END_DATE, endDate);
    }

    public void setKey(String key){
        mParsePermission.put(KEY, key);
    }

    public static void unpinAll(){
        ParseQuery<ParseObject> query = new ParseQuery<>(Permission.PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                for (ParseObject o : list) {
                    try {
                        o.unpin();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    public boolean isAdmin(){
        if (mParsePermission.getInt(TYPE) == ADMIN_PERMISSION)
            return true;
        return false;
    }

    public int getSlaveId() {
        return mParsePermission.getInt(SLAVE_ID);
    }

    public static void getNewPermissions(final OnNetworkResponseListener listener, User user) {
        ParseQuery<ParseObject> query = new ParseQuery<>(Permission.PERMISSION_CLASS_NAME);
        query.orderByDescending(Permission.CREATED_AT);
        query.whereEqualTo(Permission.USER_UUID, user.getUUID());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parsePermissions, ParseException e) {
                new ProcessNetworkPermissionsTask(listener).execute(parsePermissions);

            }
        });
    }

    private static boolean existsLocal(Permission permission) throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<>(Permission.PERMISSION_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(UUID, permission.getUUID());
        List<ParseObject> list = query.find();
        if (list == null || list.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static class ProcessNetworkPermissionsTask extends
            AsyncTask<List<ParseObject>, Void, HashMap<Master, Permission>> {

        OnNetworkResponseListener mListener;

        public ProcessNetworkPermissionsTask(OnNetworkResponseListener listener) {
            mListener =listener;
        }

        @Override
        protected HashMap<Master, Permission> doInBackground(List<ParseObject>... params) {
            HashMap<Master, Permission> permissionHashMap = new HashMap<>();
            if (params[0] != null) {
                for (ParseObject parsePermission : params[0]) {
                    Permission permission = Permission.create(parsePermission);
                    try {
                        if (!existsLocal(permission)) {
                            Master master = permission.getMaster();
                            if (master != null) {
                                permissionHashMap.put(permission.getMaster(), permission);
                            }
                        }
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return permissionHashMap;
        }

        @Override
        protected void onPostExecute(HashMap<Master, Permission> masterPermissionHashMap) {
            mListener.onNewPermissions(masterPermissionHashMap);
        }
    }
}
