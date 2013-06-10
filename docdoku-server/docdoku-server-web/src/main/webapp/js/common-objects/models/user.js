define(
    function () {
    var UserModel = Backbone.Model.extend({
        getLogin:function(){
            return this.get("login");
        }
    });
    return UserModel;
});
