package com.incrementaventures.okey.Models;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class Master implements com.incrementaventures.okey.Models.ParseObject {
    public static final String MASTER_CLASS_NAME = "Master";
    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    private final String DESCRIPTION = "description";

    public static final String FACTORY_NAME = "UART";

    private ParseObject mParseMaster;

    private List<Slave> mSlaves;

    public interface OnMasterDataListener {
        void masterFinded(Master master);
    }


    private Master(String name, String description){
        mParseMaster = ParseObject.create(MASTER_CLASS_NAME);
        mParseMaster.put(UUID, java.util.UUID.randomUUID().toString());
        mParseMaster.put(NAME, name);
        mParseMaster.put(DESCRIPTION, description);
    }

    private Master(ParseObject parseObject){
        mParseMaster = parseObject;
    }

    public static Master create(String name ,String description){
        return new Master(name, description);
    }

    public static Master create(ParseObject parseObject){
        return new Master(parseObject);
    }

    public ParseObject getParseMaster(){
        return mParseMaster;
    }

    public String getName(){
        return mParseMaster.getString(NAME);
    }

    public String getDescription(){
        return mParseMaster.getString(DESCRIPTION);
    }



    public static void getMasters(final OnMasterDataListener listener){
        ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (list == null || list.size() == 0) {
                    listener.masterFinded(null);
                    return;
                }
                for (ParseObject object : list){
                    listener.masterFinded(Master.create(object));
                }
            }
        });

    }

    public String getObjectId(){
        return mParseMaster.getObjectId();
    }

    @Override
    public void deleteFromLocal(){
        try {
            mParseMaster.unpin();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void save(){
        mParseMaster.pinInBackground();
        mParseMaster.saveEventually();
    }

    @Override
    public String getUUID() {
        if (mParseMaster == null) return null;
        return mParseMaster.getString(UUID);
    }

    public static void deleteAll(){
        ParseQuery<ParseObject> query = new ParseQuery<>(Master.MASTER_CLASS_NAME);
        query.fromLocalDatastore();

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                    for (ParseObject o : list){
                        try {
                            o.deleteEventually();
                            o.unpin();
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }


            }
        });
    }

    public Permission getPermission(){
        if (mParseMaster == null) return null;
        ParseQuery query = new ParseQuery(Permission.PERMISSION_CLASS_NAME);
        query.whereEqualTo(Permission.MASTER_UUID, getUUID());
        query.fromLocalDatastore();
        try {
            ParseObject o = query.getFirst();
            if (o != null){
                return Permission.create(o);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Slave> getSlaves(){
        if (mSlaves != null) return mSlaves;
        ParseQuery query = new ParseQuery(Slave.SLAVE_CLASS_NAME);
        query.fromLocalDatastore();
        query.whereEqualTo(Slave.UUID_MASTER, getUUID());
        query.orderByAscending(NAME);
        try {
            List<ParseObject> objects = query.find();
            mSlaves = new ArrayList<>();
            for (ParseObject o : objects){
                mSlaves.add(Slave.create(o));
            }
            return mSlaves;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addSlave(Slave slave){
        if (mSlaves == null) mSlaves = getSlaves();
        mSlaves.add(slave);
    }

}
