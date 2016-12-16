/*global _,define,App*/
define([], function () {
	'use strict';
    var ACLChecker = {

        getPermission: function (acl) {

            if (!acl) {
                return false;
            }

            var permission = false;
            var userEntries = acl.userEntries;
            var groupEntries = acl.groupEntries;

            var userLogin = App.config.login;
            var userGroups = App.config.groups;

            var userAccess = _(userEntries).filter(function (a) {
                return a.key === userLogin && a.value;
            })[0];

            if (userAccess) {
                return userAccess.value;
            }

            var self = this;
            var groupAccess;

            _.each(userGroups, function (group) {

                groupAccess = _(groupEntries).filter(function (a) {
                    return a.key === group.memberId && a.value;
                });
            });

            if (groupAccess && groupAccess.length) {
                permission = _.sortBy(groupAccess,self.accessPriority)[0].value;
            }

            return permission;
        },

        accessPriority: function(access) {
            switch(access) {
                case 'FULL_ACCESS':
                    return 0;
                case 'READ_ONLY':
                    return 1;
                case 'FORBIDDEN':
                    return 2;
                default:
                    //should log this, it's strange, or exception ?
                    return 3;
            }
        }
    };



    return ACLChecker;

});
