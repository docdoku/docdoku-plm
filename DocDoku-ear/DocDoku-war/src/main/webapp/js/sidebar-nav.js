 /* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



	Folder = Backbone.Model.extend({

		defaults:{
			id:null,
			name:null,
			completePath:null
		},
		initialize:function(){
			this.bind("error", function(model, error){
                console.log( error );
            });
		
		},
	    getId : function() {
	        return this.get('id');
	    },
	    setId : function(value) {
	        this.set({ id : value });
	        return this;
	    },		
	    getName : function() {
	        return this.get('name');
	    },
	    setName : function(value) {
	        this.set({ name : value });
	        return this;
	    },
	    getCompletePath : function() {
	        return this.get('completePath');
	    },
	    setCompletePath : function(value) {
	        this.set({ completePath : value });
	        return this;
	    },
	    isOpen : function() {
	        return this.get('isOpen');
	    },
	    setOpen : function(value) {
	        this.set({ isOpen : value });
	        return this;
	    },
	    isHome : function() {
	    	return this.get('folderType')=="home";
	    },	    
	    	    		
	});


FolderList = Backbone.Collection.extend({
		
		model: Folder,

        initialize : function(models, options) {
        	this.path = options.completePath;
        	if (options.homeFolder) {
        		this.homeFolder = options.homeFolder;
        	}
        	this.url =  "../api/folders/"+this.path;
        },

	    parse: function(data) {
	    	var folders = [];
	    	var that = this;
	    	_.each(data,function(item){
		    	var folder = {};
		    	folder.id = that.path +"/"+ item;
	  			folder.name = item;
	  			folder.isOpen = false;
	  			folder.completePath = that.path +"/"+ item;
	  			folder.folderType= "regular"; 
	  			folders.push(folder);	    		
	    	});
  			return folders;        			   	

	    },
	    comparator:function(folderA, folderB){

	    	folderNameA=folderA.getName();
	    	folderNameB=folderB.getName();

	    	if(folderB.isHome()){
	    		return 1;
	    	}

	    	if(folderA.isHome()){
	    		return -1;
	    	}


    		if(folderNameA==folderNameB){
    			return 0;
    		}
	    		
	    	return (folderNameA<folderNameB) ? -1 : 1;
	    }			
	});



FolderView = Backbone.View.extend({

		tagName:"li",

		className: "nav-header closed_folder",

		template:_.template("<%= name %>"),

		events: {"click":"onFolderClick"},

		initialize:function(){
			this.model.bind("change:isOpen", this.setOpen, this);	
		},

		setOpen: function() {
			if (this.model.isOpen()) {
				$(this.el).removeClass("closed_folder");
				$(this.el).addClass("open_folder");
			} else {
				$(this.el).removeClass("open_folder");
				$(this.el).addClass("closed_folder");
			}
		},
		
		render : function() {
	    	$(this.el).html(this.template(this.model.toJSON()));
	    	if(this.model.isHome()){
	    		$(this.el).addClass("account_home_folder");	
	    	}
	        return this;
	    },
            
	    onFolderClick: function(e){

	    		e.stopPropagation();	    		

	    		// View Folder Status Closed
	    		if(!this.model.isOpen()){

		    		this.subFolders = new FolderList([],{completePath : this.model.getCompletePath()});

		    		var subFolderContainerView = new FolderContainerView({collection: this.subFolders});
		    		$(this.el).append(subFolderContainerView.render().el);	

		    		this.model.setOpen(true);

    			
	    		}
	    		else{

	    			this.model.setOpen(false);
		    		// View Folder Status Closed
		    		
		    		$(this.el).find('ul').slideUp('normal', function() {
		    			$(this).remove();
		    		})
		    		
					this.isFolderViewOpen==false;		    			    			
	    		}
	    						    		    		    		
	    }	
});

FolderContainerView = Backbone.View.extend({

		tagName:"ul",

   		initialize: function() {
   			_.bindAll(this,'addOne','addAllAndShow');
    		this.collection.bind('add', this.addOne);
    		this.collection.bind('reset', this.addAllAndShow);
    		this.collection.fetch();
    	},

    	addAllAndShow: function(){
    		if (this.collection.homeFolder) {
    			this.collection.add(this.collection.homeFolder, {silent:true});
    			//this.collection.sort( {silent:true});
    		}
    		this.collection.each(this.addOne);
    		$(this.el).slideDown();
    	},

    	addOne: function(folder){
      			var view = new FolderView({model: folder});
      			$(this.el).append(view.render().el);     		
    	}
		
});

AppView = Backbone.View.extend({
	
	el:$(".sidebar-nav"),

	initialize: function() {

		var home = {};
    	home.id = inputs.workspaceID+"/~"+inputs.login;
		home.name = "~"+inputs.login;
		home.isOpen = false;
		home.completePath = inputs.workspaceID+"/~"+inputs.login;
		home.folderType = "home";

		var firstLevelFolders = new FolderList([],{homeFolder: home, completePath:inputs.workspaceID});
		var firstLevelView = new FolderContainerView({collection: firstLevelFolders});
		$(this.el).append(firstLevelView.render().el);	
	}


});

	$(document).ready(function () {
		new AppView();		
	});	
	
