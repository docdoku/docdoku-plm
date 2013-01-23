define(function(){

    NotificationModuleView = Backbone.View.extend({        
            
        el: "#notification_module",
        
        initialize: function(){
            var that = this ;
            this.notificationCollectionView = new NotificationCollectionView({
                el:"#notification_collection",
                onEmptyCollection:function(){
                    that.onEmptyCollection();
                }
            }).render();      
            
            _.bindAll(this);
                
        },
        
        render: function() {
            
            if(this.notificationCollectionView.collection.isEmpty()){
                this.hide();
            }
            
            return this;
        },
        
        
        hide:function(){            
            this.$el.hide();            
        },


        onNewNotification: function(args){
            this.notificationCollectionView.onNewNotification(new Notification(args));
            this.$el.show(); 
            this.$el.addClass("open");
        },

        onRemoveNotificationRequest: function(notificationId){
            this.notificationCollectionView.onRemoveNotificationRequest(notificationId);

        },

        onEmptyCollection:function(){
            this.$el.hide(); 
            this.$el.removeClass("open");
        }
        
    });
        
    return NotificationModuleView;
    
});