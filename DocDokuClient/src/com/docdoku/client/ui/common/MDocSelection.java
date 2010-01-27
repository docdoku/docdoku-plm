package com.docdoku.client.ui.common;

import com.docdoku.core.entities.MasterDocument;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


public class MDocSelection implements Transferable {

    private DataFlavor mMDocFlavor;
    private MasterDocument mMDoc;

    public MDocSelection(MasterDocument pMDoc){
        mMDoc=pMDoc;
        try {
            mMDocFlavor = new DataFlavor(GUIConstants.MDOC_FLAVOR);
        } catch (ClassNotFoundException pCNFEx) {
            throw new RuntimeException("Unexpected error: unrecognized data flavor",pCNFEx);
        }
    }
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{mMDocFlavor,DataFlavor.javaFileListFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor pFlavor) {
        return (pFlavor.equals(mMDocFlavor) || pFlavor.equals(DataFlavor.javaFileListFlavor));
    }

    public Object getTransferData(DataFlavor pFlavor) throws UnsupportedFlavorException, IOException {
        if(pFlavor.equals(mMDocFlavor))
            return mMDoc;
        else if(pFlavor.equals(DataFlavor.javaFileListFlavor)){
            //TODO javaFileListFlavor
            return null;
        }

        else
            throw new UnsupportedFlavorException(pFlavor);
    }
}
