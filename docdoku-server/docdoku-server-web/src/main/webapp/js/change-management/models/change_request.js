define([
    "models/change_item"
], function(
    ChangeItemModel
    ){
    var ChangeRequestModel= ChangeItemModel.extend({
        urlRoot: "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/requests",

        getMilestoneId :function(){
            return this.get("milestoneId");
        },

        getAddressedChangeIssues : function(){
            return this.get("addressedChangeIssues");
        },

        saveAffectedIssues: function(issues, callback){
            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/affectedIssues",
                data: JSON.stringify(issues),
                contentType: "application/json; charset=utf-8",
                success: function() {
                    this.fetch();
                    if(callback){
                        callback();
                    }
                }
            });
        }
    });

    return ChangeRequestModel;
});