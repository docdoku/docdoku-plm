'use strict';

angular.module('dplm.services.configuration',[])

.service('ConfigurationService',function(){
    this.configuration = JSON.parse(localStorage.configuration || '{}');
    this.saveConfiguration=function(){
        localStorage.configuration = JSON.stringify(this.configuration);
    }
});