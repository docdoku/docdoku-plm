define([
	"models/role"
], function (
	Role
) {
	var RoleInUseList = Backbone.Collection.extend({
		model: Role,

        url:function(){
            return "/api/workspaces/" + APP_CONFIG.workspaceId + "/roles/inuse";
        }

	});

	return RoleInUseList;
});
