/*global _,define,App*/
define([], function () {
	'use strict';
    var ACLChecker = {

        getPermission: function (acl) {

            if (!acl) {
                return false;
            }

            var permission = false;
            var userEntries = acl.userEntries.entry;
            var groupEntries = acl.groupEntries.entry;

            var userLogin = App.config.login;
            var userGroups = App.config.groups;

            // 1. Find FULL ACCESS
            var fullAccess = _(userEntries).filter(function (a) {
                return a.key === userLogin && a.value === 'FULL_ACCESS';
            })[0];

            if (fullAccess) {
                return 'FULL_ACCESS';
            }

            _.each(userGroups, function (group) {

                var groupFullAccess = _(groupEntries).filter(function (a) {
                    return a.key === group.memberId && a.value === 'FULL_ACCESS';
                })[0];

                if (groupFullAccess) {
                    permission = 'FULL_ACCESS';
                }

            });

            if (permission) {
                return permission;
            }

            // 2. Find READ_ONLY
            var readOnly = _(userEntries).filter(function (a) {
                return a.key === userLogin && a.value === 'READ_ONLY';
            })[0];

            if (readOnly) {
                return 'READ_ONLY';
            }

            _.each(userGroups, function (group) {

                var groupFullAccess = _(groupEntries).filter(function (a) {
                    return a.key === group.memberId && a.value === 'READ_ONLY';
                })[0];

                if (groupFullAccess) {
                    permission = 'READ_ONLY';
                }

            });

            if (permission) {
                return permission;
            }

            // 3. Find FORBIDDEN
            var forbidden = _(userEntries).filter(function (a) {
                return a.key === userLogin && a.value === 'FORBIDDEN';
            })[0];

            if (forbidden) {
                return 'FORBIDDEN';
            }

            _.each(userGroups, function (group) {

                var groupForbidden = _(groupEntries).filter(function (a) {
                    return a.key === group.memberId && a.value === 'FORBIDDEN';
                })[0];

                if (groupForbidden) {
                    permission = 'FORBIDDEN';
                }

            });

            return permission;
        }
    };

    return ACLChecker;

});
