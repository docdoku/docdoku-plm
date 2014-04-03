define([
    "models/change_item"
], function(
    ChangeItemModel
    ){
    var ChangeIssueModel= ChangeItemModel.extend({
        urlRoot: "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/issues",

        getInitiator:function(){
            return this.get("initiator");
        }
    });

    return ChangeIssueModel;
});