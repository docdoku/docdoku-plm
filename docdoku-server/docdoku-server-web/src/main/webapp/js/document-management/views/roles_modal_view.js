define(
    [
        "common-objects/views/components/modal",
        "text!templates/roles_modal.html",
        "i18n!localization/nls/roles-strings",
        "models/role",
        "collections/roles",
        "views/role_item_view",
        "common-objects/collections/users"
    ],
    function(ModalView,template,i18n,Role,RolesList,RoleItemView,UserList) {

    var RolesModalView = ModalView.extend({

        template: Mustache.compile(template),

        initialize:function(){
            ModalView.prototype.initialize.apply(this, arguments);
            this.events["submit #form-new-role"] = "onSubmitNewRole";
            this.events["submit #form-roles"] = "onSubmitForm";
        },

        render: function() {

            var self = this ;

            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$roleViews = this.$("#form-roles");
            this.$select = this.$("select.role-user-mapped");
            this.$newRoleName = this.$("input.role-name");


            this.userList = new UserList();
            this.collection = new RolesList();

            this.rolesToDelete=[];

            this.listenTo(this.collection,"reset",this.onCollectionReset);
            this.listenTo(this.collection,"add",this.onModelAddedToCollection);

            this.userList.fetch({success:function(){
                self.fillUserSelect();
                self.collection.fetch();
            }});

            return this;
        },

        fillUserSelect:function() {
            var self = this ;
            this.$select.append("<option value=''></option>");
            this.userList.each(function(user){
                self.$select.append("<option value='"+user.get("login")+"'>"+user.get("name")+"</option>");
            });
        },

        onSubmitNewRole:function (e){

            var userDTO = null;

            if(this.$select.val()){
                userDTO = {login: this.$select.val()};
            }

            this.collection.create({workspaceId : APP_CONFIG.workspaceId, name:this.$newRoleName.val(), defaultUserMappedDTO:userDTO});

            this.resetNewRoleForm();

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        onSubmitForm:function(e){

            var self = this ;

            // Update models which has changed
            this.collection.each(function(model){
                if(model.hasChanged("defaultUserMappedDTO") && !_.contains(self.rolesToDelete, model)){
                    model.save();
                }
            });

            // Delete roles marked for delete
            _.each(this.rolesToDelete,function(model){
                model.destroy();
            });

            this.hide();

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        onCollectionReset:function(){
            var self = this;
            this.roleViews = [];
            this.collection.each(function(model){
                self.addRoleView(model);
            });
        },

        resetNewRoleForm:function(){
            this.$newRoleName.val("");
            this.$select.val("");
        },

        onModelAddedToCollection:function(model){
            this.addRoleView(model);
        },

        addRoleView:function(model){
            var self = this ;
            var view = new RoleItemView({model:model, userList:this.userList}).render();
            this.roleViews.push(view);
            this.$roleViews.append(view.$el);

            view.on("view:removed", function(){
                self.rolesToDelete.push(model);
            });
        }


    });

    return RolesModalView;

});