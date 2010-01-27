package com.docdoku.client.actions;

import com.docdoku.client.ui.ExplorerFrame;

public abstract class ClientAbstractAction extends DefaultAbstractAction {
    protected ExplorerFrame mOwner;

    public ClientAbstractAction(String pName,
                                String pImgPath,
                                ExplorerFrame pOwner) {
        super(pName, pImgPath);
        mOwner = pOwner;
    }
}
