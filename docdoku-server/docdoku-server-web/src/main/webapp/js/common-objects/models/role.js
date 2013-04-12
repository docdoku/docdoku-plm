define(function () {
    var Role = Backbone.Model.extend({

        initialize: function(){
            this.className = "Role";
        },

        getName:function(){
            return this.get("name");
        },

        getMappedUser:function(){
            return this.get("defaultUserMapped");
        },

        getMappedUserLogin:function(){
            if(this.getMappedUser()){
                return this.getMappedUser().login;
            }
            return "";
        }
    });
    return Role;
});
