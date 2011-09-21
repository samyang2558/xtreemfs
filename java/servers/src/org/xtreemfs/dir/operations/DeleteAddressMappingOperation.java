/*
 * Copyright (c) 2009-2011 by Bjoern Kolbeck,
 *               Zuse Institute Berlin
 *
 * Licensed under the BSD License, see LICENSE file for details.
 *
 */

package org.xtreemfs.dir.operations;

import org.xtreemfs.babudb.api.database.Database;
import org.xtreemfs.babudb.api.database.DatabaseInsertGroup;
import org.xtreemfs.babudb.api.exception.BabuDBException;
import org.xtreemfs.common.stage.BabuDBComponent;
import org.xtreemfs.common.stage.BabuDBPostprocessing;
import org.xtreemfs.common.stage.RPCRequestCallback;
import org.xtreemfs.common.stage.BabuDBComponent.BabuDBRequest;
import org.xtreemfs.dir.DIRRequest;
import org.xtreemfs.dir.DIRRequestDispatcher;
import org.xtreemfs.pbrpc.generatedinterfaces.Common.emptyResponse;
import org.xtreemfs.pbrpc.generatedinterfaces.DIRServiceConstants;
import org.xtreemfs.pbrpc.generatedinterfaces.DIR.addressMappingGetRequest;

import com.google.protobuf.Message;

/**
 * 
 * @author bjko
 */
public class DeleteAddressMappingOperation extends DIROperation {
    
    private final BabuDBComponent component;
    private final Database        database;
    
    
    public DeleteAddressMappingOperation(DIRRequestDispatcher master) {
        
        super(master);
        component = master.getBabuDBComponent();
        database = master.getDirDatabase();
    }
    
    @Override
    public int getProcedureId() {
        
        return DIRServiceConstants.PROC_ID_XTREEMFS_ADDRESS_MAPPINGS_REMOVE;
    }
    
    @Override
    public void startRequest(DIRRequest rq, RPCRequestCallback callback) throws BabuDBException {
        
        addressMappingGetRequest request = (addressMappingGetRequest) rq.getRequestMessage();
        
        DatabaseInsertGroup ig = database.createInsertGroup();
        ig.addDelete(DIRRequestDispatcher.INDEX_ID_ADDRMAPS, request.getUuid().getBytes());
        
        component.insert(database, callback, ig, rq.getMetadata(), new BabuDBPostprocessing<Object>() {
            
            @Override
            public Message execute(Object result, BabuDBRequest request) throws Exception {
                return emptyResponse.getDefaultInstance();
            }
        });
    }
}
