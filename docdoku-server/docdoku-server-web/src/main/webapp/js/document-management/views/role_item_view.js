define(
    [
        "text!templates/role_item.html",
        "i18n!localization/nls/roles-strings"
    ],
    function(template,i18n) {

        var RoleItemView = Backbone.View.extend({

            template: Mustache.compile(template),

            className:"well roles-item",

            events : {
                "click .icon-remove":"removeAndNotify",
                "change select":"changeModel"
            },

            initialize:function(){
                _.bindAll(this);
            },

            render: function() {
                this.$el.html(this.template({i18n:i18n,model:this.model}));
                this.$select = this.$("select");
                this.fillUserList();
                return this;
            },

            fillUserList:function(){
                var self = this ;

                if(this.options.nullable){
                    this.$select.append("<option value=''></option>");
                }

                this.options.userList.each(function(user){
                    var selected = "";
                    if(self.model.getMappedUserLogin() == user.get("login")){
                        selected = " selected"
                    }
                    self.$select.append("<option value='"+user.get("login")+"'"+selected +">"+user.get("name")+"</option>");
                });

            },

            removeAndNotify:function(){
                this.remove();
                this.trigger("view:removed");
            },

            changeModel:function(){
                var userDTO = null;
                if(this.$select.val()){
                    userDTO = {login : this.$select.val()};
                }
                this.model.set({defaultUserMapped:userDTO});
            }

        });

        return RoleItemView;

    });