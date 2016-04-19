package com.docdoku.server.example;

import com.docdoku.server.example.api.AccountsApiExample;
import com.docdoku.server.example.api.DocumentsApiExample;
import com.docdoku.server.example.api.FoldersApiExample;
import com.docdoku.server.example.api.WorkspacesApiExample;

/**
 * This class bootstraps calls to DocdokuPLM API
 * @Author Morgan Guimard
 */
public class Main {
    public static void main(String[] args) {
        new WorkspacesApiExample().run();
        new AccountsApiExample().run();
        new FoldersApiExample().run();
        new DocumentsApiExample().run();
    }
}
