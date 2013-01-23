define(function(){
    
    NotificationView = Backbone.View.extend({
        
        tagName: 'li',

        templateWithoutActions: _.template(
            "<span><%= notification.title %></span>" 
            +"<div><%= notification.content %></div>"
        ),
            
        templateWithActions: _.template(
            "<span><b><%= notification.title %></b></span>" 
            +"<div><%= notification.content %></div>"
            +"<div class='notification-actions'></div>"
        ),

        initialize: function(){
        },
        
        render: function() {

            var $html;
            var that  = this ;

            if(this.model.attributes.actions != undefined){

                $html = $(this.templateWithActions({
                    notification:this.model.attributes
                }));

                var $actionZone = $($html[2]);

                _.each(this.model.attributes.actions, function(action){

                    var $action = $("<a class='btn'>"+action.title+"</a>");

                    $action.one('click',function(){
                       action.handler();
                    });

                    $actionZone.append($action);

                });

            }else{

                $html = $(this.templateWithoutActions({
                    notification:this.model.attributes
                }));

            }
             
            this.$el.html($html);
            
            return this;
        
        }       
        
        
    });

    return NotificationView;
    
});