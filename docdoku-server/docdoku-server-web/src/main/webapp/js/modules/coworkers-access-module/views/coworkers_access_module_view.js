define(
    [
        "common-objects/collections/users",
        "modules/coworkers-access-module/views/coworkers_item_view"
    ],
    function(Users,CoWorkersItemView){

    CoWorkersAccessModuleView = Backbone.View.extend({

        el: "#coworkers_access_module",

        initialize:function(){

            var that = this ;
            var users = new Users();
            this._coworkersItemViews = [];

            var $ul = this.$("#coworkers_access_module_entries");

            users.fetch({"async": true, "success": function () {

                _.each(users.models, function(user){

                    if(user.attributes.login != APP_CONFIG.login){

                        var cwiv = new CoWorkersItemView({
                            model : user.attributes
                        });

                        that._coworkersItemViews.push(cwiv);

                        $ul.append(cwiv.render().el);
                    }

                });

            }});

            this.$el.show();

            this.$("#coworkers_access_module_toggler").click(function(){
                if( ! $ul.is(":visible") ){
                    that.refreshAvailabilities();
                }
            });

            return this ;

        },

        refreshAvailabilities:function(){

            _.each(this._coworkersItemViews,function(view){
                view.refreshAvailability();
            });

        },

        render:function(){

            return this ;
        }


    });

    return CoWorkersAccessModuleView;

});