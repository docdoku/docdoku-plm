package com.docdoku.server.rest.util;

import com.docdoku.core.configuration.DocumentConfigSpec;
import com.docdoku.core.exceptions.BaselineNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.services.IDocumentConfigSpecManagerLocal;

/**
 * Created by morgan on 15/04/16.
 */
public class ConfigSpecHelper {
    public static final String BASELINE_LATEST = "latest";
    public static final String BASELINE_UNDEFINED = "undefined";

    /**
     * Get a configuration specification according a string params
     *
     * @param workspaceId    The current workspace
     * @param configSpecType The string discribing the configSpec
     * @return A configuration specification
     * @throws com.docdoku.core.exceptions.UserNotFoundException      If the user login-workspace doesn't exist
     * @throws com.docdoku.core.exceptions.UserNotActiveException     If the user is disabled
     * @throws com.docdoku.core.exceptions.WorkspaceNotFoundException If the workspace doesn't exist
     * @throws com.docdoku.core.exceptions.BaselineNotFoundException  If the baseline doesn't exist
     */
    public static DocumentConfigSpec getConfigSpec(String workspaceId, String configSpecType, IDocumentConfigSpecManagerLocal documentConfigSpecService) throws UserNotActiveException, WorkspaceNotFoundException, UserNotFoundException, BaselineNotFoundException {
        DocumentConfigSpec cs;
        switch (configSpecType) {
            case BASELINE_LATEST:
            case BASELINE_UNDEFINED:
                cs = documentConfigSpecService.getLatestConfigSpec(workspaceId);
                break;
            default:
                cs = documentConfigSpecService.getConfigSpecForBaseline(Integer.parseInt(configSpecType));
                break;
        }
        return cs;
    }
}
