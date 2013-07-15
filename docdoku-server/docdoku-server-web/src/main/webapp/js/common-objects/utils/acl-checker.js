define([],function(){

    var ACLChecker = {

        getPermission:function(acl){

            if(!acl){
                return false;
            }

            var permission = false ;
            var userEntries = acl.userEntries;
            var groupEntries = acl.groupEntries;

            var userLogin = APP_CONFIG.login;
            var userGroups = APP_CONFIG.groups;

            // 1. Find FULL ACCESS
            if(userEntries[userLogin] == "FULL_ACCESS"){
                return "FULL_ACCESS";
            }

            _.each(userGroups,function(group){
                if(groupEntries[group] == "FULL_ACCESS"){
                    permission = "FULL_ACCESS";
                }
            });

            if(permission){
                return permission;
            }

            // 2. Find READ_ONLY
            if(userEntries[userLogin] == "READ_ONLY"){
                return "READ_ONLY";
            }

            _.each(userGroups,function(group){
                if(groupEntries[group] == "READ_ONLY"){
                    permission = "READ_ONLY";
                }
            });

            if(permission){
                return permission;
            }

            // 3. Find FORBIDDEN
            if(userEntries[userLogin] == "FORBIDDEN"){
                return "FORBIDDEN";
            }

            _.each(userGroups,function(group){
                if(groupEntries[group] == "FORBIDDEN"){
                    permission = "FORBIDDEN";
                }
            });

            return permission;


        }
    }

    return ACLChecker;

});