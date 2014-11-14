'use strict';

angular.module('dplm.services.configuration',[])

.service('ConfigurationService',function(){
    this.configuration = JSON.parse(localStorage.configuration || '{}');
    this.save=function(){
        localStorage.configuration = JSON.stringify(this.configuration);
    }
});