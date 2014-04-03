define([
    "i18n!localization/nls/change-management-strings",
    "common-objects/utils/date",
    "common-objects/utils/acl-checker"
], function(
    i18n,
    Date,
    ACLChecker
    ){
    var MilestoneModel= Backbone.Model.extend({
        urlRoot: "/api/workspaces/" + APP_CONFIG.workspaceId + "/changes/milestones",

        getId :function(){
            return this.get("id");
        },

        getTitle :function(){
            return this.get("title");
        },

        getDueDate : function(){
            return this.get("dueDate");
        },

        getDueDateToPrint : function(){
            return (this.getDueDate()) ? Date.formatTimestamp(
                i18n._DATE_PICKER_DATE_FORMAT,
                this.getDueDate()
            ) : null;
        },

        getDescription : function(){
            return this.get("description");
        },

        getWorkspaceId : function(){
            return this.get("workspaceId");
        },

        getNumberOfRequests : function(){
            return this.get("numberOfRequests");
        },

        getNumberOfOrders : function(){
            return this.get("numberOfOrders");
        },

        getACL:function(){
            return this.get("acl");
        },

        updateACL:function(args){
            $.ajax({
                type: "PUT",
                url: this.url() + "/acl",
                data: JSON.stringify(args.acl),
                contentType: "application/json; charset=utf-8",
                success: args.success,
                error : args.error
            });
        },

        hasACLForCurrentUser:function(){
            return this.getACLPermissionForCurrentUser() != false;
        },

        isForbidden:function(){
            return this.getACLPermissionForCurrentUser() == "FORBIDDEN";
        },

        isReadOnly:function(){
            return this.getACLPermissionForCurrentUser() == "READ_ONLY";
        },

        isFullAccess:function(){
            return this.getACLPermissionForCurrentUser() == "FULL_ACCESS";
        },

        getACLPermissionForCurrentUser:function(){
            return ACLChecker.getPermission(this.getACL());
        },

        isWritable:function(){
            return this.get("writable")
        }
    });

    return MilestoneModel;
});