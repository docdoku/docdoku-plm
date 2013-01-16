define(["modules/notifications-module/collections/notification_collection",
        "modules/notifications-module/views/notification_view"
],function(NotificationCollection,NotificationView){

    NotificationCollectionView = Backbone.View.extend({
        
        collection : new NotificationCollection(),
    
        initialize : function() {

            _.bindAll(this);

            this._notificationViews = [];

            this.collection.each(this.add);

            this.collection.bind('add', this.add);
            this.collection.bind('remove', this.remove);
            
        },
 
        add : function(notification) {

            var that = this ;
            
            var nv = new NotificationView({                
                model : notification
            });
 
            this._notificationViews.push(nv);            
            
            if (this._rendered) {
                
                var $notificationView = $(nv.render().el);
                
                $notificationView.one("click",function(){
                    that.notificationViewClicked(nv);
                });
                
                $(this.el).prepend($notificationView);

            }
            
        },
 
        onNewNotification:function(notification){
            this.collection.push(notification);
        },
 
        remove : function(model) {

            var viewToRemove = _(this._notificationViews).select(function(view) {
                return view.model === model;
            })[0];

            this._notificationViews = _(this._notificationViews).without(viewToRemove); 
            
            if (this._rendered) {
                $(viewToRemove.el).remove();
                if(this.collection.isEmpty()){
                    this.options.onEmptyCollection();
                }
            }          
            
            return this ;
        },
 
        render : function() {
            this._rendered = true; 
            $(this.el).empty();
            return this;
        },
        
        notificationViewClicked:function(nv){
            this.collection.remove(nv.model);
        }

    });

    return NotificationCollectionView;
});