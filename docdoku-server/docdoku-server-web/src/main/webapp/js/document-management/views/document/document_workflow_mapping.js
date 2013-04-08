define([
	"common-objects/views/base",
	"common-objects/collections/users",
	"views/role_item_view",
	"models/role",
	"text!templates/document/document_workflow_mapping.html"
], function (
	BaseView,
    Users,
    RoleItemView,
    Role,
	template
) {
	var DocumentWorkflowMappingView = BaseView.extend({

		template: Mustache.compile(template),

        initialize: function() {
            BaseView.prototype.initialize.apply(this, arguments);
            this.on("workflow:change",this.updateMapping);
            this.roles = [];
            this.rolesItemViews = [];
            this.users = new Users();
            this.users.fetch({success:this.render});
        },

        updateMapping:function(workflowModel){

            var self = this ;
            this.rolesInWorkflowModel = [];
            this.rolesItemViews = [];

            if(workflowModel){
                workflowModel.get("activityModels").each(function(activityModel){
                    activityModel.get("taskModels").each(function(taskModel){
                        if(!_.contains(self.rolesInWorkflowModel,taskModel.get("role"))){
                            self.rolesInWorkflowModel.push(taskModel.get("role"));
                            self.rolesItemViews.push(new RoleItemView({model: new Role(taskModel.get("role").attributes), userList:self.users, nullable:false}).render());
                        }
                    });
                });
            }

            this.$el.empty();

            $.each(this.rolesItemViews,function(index,view){
                self.$el.append(view.$el);
            });

        },

        toList:function(){
            var list = [];
            $.each(this.rolesItemViews,function(index,view){
                list.push({roleName: view.model.get("name") , userLogin: view.model.get("defaultUserMapped").login });
            });
            return list;
        }

	});

	return DocumentWorkflowMappingView;
});
