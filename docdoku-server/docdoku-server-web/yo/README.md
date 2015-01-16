# DocDokuPLM web client

## Prerequisites

* DocdokuPLM server
* node >= 10.0
* npm 
* git 
* nginx (or other proxy capable software)

## Proxy example configuration

    server {
        listen 8989;
        server_name localhost;
        # DocDoku PLM Server
        location / {
            proxy_pass  http://localhost:8080;
        }    
        # Websocket application
        location /mainChannelSocket {
            proxy_pass http://localhost:8080/mainChannelSocket;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";		
            proxy_read_timeout 7200s;
        }
        # Grunt server
        location ~ ^/(bower_components|fonts|sounds|change-management|images|product-management|visualization|css|img|product-structure|document-management|js|server.properties.json) {
            proxy_pass  http://localhost:9001;
        }
    }

## HTTPS Proxy example configuration

server {
	listen 8787;
	server_name localhost;
	ssl on;
	ssl_certificate     /path/to/local.crt;
    ssl_certificate_key    /path/to/local.key;
	

	# DocDoku PLM Server
	location / {
		proxy_pass  https://localhost:8181;
	}    
	# Websocket application
		location /mainChannelSocket {
		proxy_pass https://localhost:8181/mainChannelSocket;
		proxy_http_version 1.1;
		proxy_set_header Upgrade $http_upgrade;
		proxy_set_header Connection "upgrade";	
		proxy_read_timeout 7200s;
	}
	# Grunt server
		location ~ ^/(bower_components|fonts|sounds|change-management|images|product-management|visualization|css|img|product-structure|document-management|js|server.properties.json) {
		proxy_pass  http://localhost:9001;
	}
}



## Grunt commands

* **npm start** : alias for 'npm install && bower install && grunt deploy'
* **grunt deploy** : build and deploy the entire webapp on a running docdokuplm server
* **grunt build** : build all modules
* **grunt serve** : serve the application files on port 9001
* **grunt copy:webapp** : copy all built files to the server
* **grunt clean:webapp** : remove all deployed files
