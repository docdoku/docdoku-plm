define([
    "modules/notifications-module/models/notification_model",
    "modules/notifications-module/views/notification_collection_view",
    "modules/notifications-module/views/notification_module_view"
    ],function (
        Notification,
        NotificationCollectionView,
        NotificationModuleView
        ) {
        
        var nmv = new NotificationModuleView().render();
        
        Backbone.Events.on('NewNotification', nmv.onNewNotification);
        
    
    });