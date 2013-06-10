define([],function(){

    var Admin = Backbone.Model.extend({

        url:function(){
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/users/admin";
        },

        getLogin:function(){
            return this.get("login");
        }

    });

    return Admin;

});