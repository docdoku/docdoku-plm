define([
], function () {
    var UserAclEntry = Backbone.Model.extend({

        initialize: function() {
        },

        key:function(){
            return this.get("userLogin");
        },

        isForbidden:function(){
            return this.getPermission() == "FORBIDDEN";
        },
        isReadOnly:function(){
            return this.getPermission() == "READ_ONLY";
        },
        isFullAccess:function(){
            return this.getPermission() == "FULL_ACCESS";
        },

        setPermission:function(permission){
            this.set("permission",permission);
        },

        getPermission:function(){
            return this.get("permission");
        }

    });

    return UserAclEntry;
});