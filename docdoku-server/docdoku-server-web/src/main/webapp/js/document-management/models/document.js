define(["collections/document_iteration", "common-objects/utils/acl-checker"], function(DocumentIterationList,ACLChecker) {

    var Document = Backbone.Model.extend({

        urlRoot: function() {
            if(this.isNew()){
                return this.collection.url();
            }
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/documents";
        },

        parse: function(data) {
            this.iterations = new DocumentIterationList(data.documentIterations);
            this.iterations.setDocument(this);
            delete data.documentIterations;
            delete data.lastIteration;
            return data;
        },

        getId:function(){
            return this.get("id");
        },

        getReference: function() {
            var id = this.get("id");
            return id.substr(0,id.lastIndexOf("-"));
        },

        getVersion: function() {
            return this.get("version");
        },

        getWorkspace: function() {
            return this.get("workspaceId");
        },

        getCheckoutUser: function() {
            return this.get('checkOutUser');
        },

        isCheckoutByConnectedUser: function() {
            return this.isCheckout() ? this.getCheckoutUser().login == APP_CONFIG.login : false;
        },

        getUrl: function() {
            return this.url();
        },

        hasIterations: function() {
            return !this.getIterations().isEmpty();
        },

        getLastIteration: function() {
            return this.getIterations().last();
        },

        getIterations: function() {
            return this.iterations;
        },

        isIterationChangedSubscribed: function() {
            return this.get("iterationSubscription");
        },

        isStateChangedSubscribed: function() {
            return this.get("stateSubscription");
        },

        getTags: function() {
            return this.get("tags");
        },

        getPath: function() {
            return this.get("path");
        },

        checkout: function() {
            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/checkout",
                success: function() {
                    this.fetch();
                },
                error:function(xhr,status,errorThrown){
                    alert(xhr.responseText);
                }
            });
        },

        undocheckout: function() {
            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/undocheckout",
                success: function() {
                    this.fetch();
                },
                error:function(xhr,status,errorThrown){
                    alert(xhr.responseText);
                }
            });
        },

        checkin: function() {
            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/checkin",
                success: function() {
                    this.fetch();
                },
                error:function(xhr,status,errorThrown){
                    alert(xhr.responseText);
                }
            });
        },

        toggleStateSubscribe: function(oldState) {

            var action = oldState ? "unsubscribe" : "subscribe";

            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/notification/stateChange/" + action,
                success: function() {
                    this.fetch();
                }
            });
        },

        toggleIterationSubscribe: function(oldState) {

            var action = oldState ? "unsubscribe" : "subscribe";

            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/notification/iterationChange/" + action,
                success: function() {
                    this.fetch();
                }
            });

        },

        isCheckout: function() {
            return this.attributes.checkOutDate;
           // return !_.isNull(this.attributes.checkOutDate);
        },

        getPermalink: function() {
            return encodeURI(
                window.location.origin
                    + "/documents/"
                    + this.getWorkspace()
                    + "/"
                    + this.getReference()
                    + "/"
                    + this.getVersion()
            );
        },

        addTags: function(tags) {

            $.ajax({
                context: this,
                type: "POST",
                url: this.url() + "/tags",
                data: JSON.stringify(tags),
                contentType: "application/json; charset=utf-8",
                success: function() {
                    this.fetch();
                }
            });

        },

        removeTag: function(tag, callback) {
            $.ajax({
                type: "DELETE",
                url:this.url() + "/tags/" +tag,
                success: function() {
                    callback();
                }
            });
        },

        removeTags: function(tags, callback) {
            var baseUrl = this.url() + "/tags/";
            var count = 0;
            var total = _(tags).length;
            _(tags).each(function(tag){
                $.ajax({
                    type: "DELETE",
                    url:baseUrl+tag,
                    success: function() {
                        count ++;
                        if(count >= total){
                            callback();
                        }
                    }
                });
            });

        },

        createNewVersion: function(title, description, workflow, roleMappingList, aclList) {

            var data = {
                title: title,
                description: description,
                workflowModelId: workflow ? workflow.get("id") : null,
                roleMapping:workflow? roleMappingList: null,
                acl:aclList
            };

            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/newVersion",
                data: JSON.stringify(data),
                contentType: "application/json; charset=utf-8",
                success: function() {
                    this.collection.fetch({reset:true});
                }
            });
        },

        moveInto: function(path, callback, error) {

            var data = {
                path: path
            };

            $.ajax({
                context: this,
                type: "PUT",
                url: this.url() + "/move",
                data: JSON.stringify(data),
                contentType: "application/json; charset=utf-8",
                success: function() {
                    if(callback){
                        callback();
                    }
                },
                error:function(xhr,status,errorThrown){
                    alert(xhr.responseText);
                    error();
                }
            });
        },

        createShare:function(args){
            $.ajax({
                type: "POST",
                url: this.url() + "/share",
                data: JSON.stringify(args.data),
                contentType: "application/json; charset=utf-8",
                success: args.success
            });
        },

        publish:function(args){
            $.ajax({
                type: "PUT",
                url: this.url() + "/publish",
                success: args.success
            });
        },

        unpublish:function(args){
            $.ajax({
                type: "PUT",
                url: this.url() + "/unpublish",
                success: args.success
            });
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
            return ACLChecker.getPermission(this.get("acl"));
        },

        isAttributesLocked:function(){
            return this.get("attributesLocked");
        }

    });

    return Document;

});
