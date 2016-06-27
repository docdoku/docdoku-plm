package com.docdoku.server.example.api;

import com.docdoku.server.api.client.ApiException;
import com.docdoku.server.api.models.CountDTO;
import com.docdoku.server.api.models.DocumentRevisionDTO;
import com.docdoku.server.api.services.DocumentsApi;
import com.docdoku.server.example.utils.ErrorHelper;

import java.util.List;

/**
 * This class calls some DocumentsApi methods
 * @Author Morgan Guimard
 */
public class DocumentsApiExample extends DocdokuPLMApiExample{

    private DocumentsApi documentsApi;

    @Override
    public void run() {
        documentsApi = new DocumentsApi(plmClient.getClient());
        countDocuments();
        listCheckedOutDocuments();
    }

    private void listCheckedOutDocuments() {
        try {
            List<DocumentRevisionDTO> checkedOutDocuments = documentsApi.getCheckedOutDocuments(WORKSPACE);
            System.out.println(checkedOutDocuments);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while getting checked out documents", plmClient.getClient());
        }
    }

    private void countDocuments() {
        try {
            CountDTO countDTO = documentsApi.getDocumentsInWorkspaceCount(WORKSPACE);
            System.out.println(countDTO.getCount());
        } catch (ApiException e) {
            ErrorHelper.onError("Error while getting documents count", plmClient.getClient());
        }
    }
}
