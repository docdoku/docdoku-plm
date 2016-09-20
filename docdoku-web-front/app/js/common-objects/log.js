/*global App*/
define(function(){
    'use strict';
   return {
       log:function(message,colorType){
           if(App.debug){
               if(colorType){
                   switch (colorType) {
                       case 'WS' :
                           window.console.log('%c [WS] ' + message, 'background: #222; color: #bada55','background: none; color:inherit');
                           break;
                       case 'IM' :
                           window.console.log('%c [InstancesManager] ' + message, 'background: #206963; color: #bada55','background: none; color:inherit');
                           break;
                       case 'SM' :
                           window.console.log('%c [SceneManager] ' + message, 'background: #275217; color: #bada55','background: none; color:inherit');
                           break;
                       case 'PTV' :
                           window.console.log('%c [PartsTreeView] ' + message, 'background: #3C4C52; color: #bada55','background: none; color:inherit');
                           break;
                       default :
                           window.console.log(message, 'background: #888; color: #bada55','background: none; color:inherit');
                           break;
                   }
               }else{
                   window.console.log(message);
               }
           }
       }
   };
});
