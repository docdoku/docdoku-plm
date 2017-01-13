function readPassword(msg){
    var c = java.lang.System.console();
    return new java.lang.String(c.readPassword(msg));
}

var api = new JavaImporter(
    com.docdoku.api.models,
    com.docdoku.api.models.utils,
    com.docdoku.api.client,
    com.docdoku.api.client.auth,
    com.docdoku.api.services
);

print();
var url = readLine('What is the server url? [https://docdokuplm.net:443/api] ');
if(url.isEmpty()){
    url = 'https://docdokuplm.net:443/api';
}
var username = readLine('What is your username? ');
var password = readPassword('What is your password? ');
var client = com.docdoku.api.DocdokuPLMClientFactory.createBasicClient(url,username,password);
var ws = {};
ws.accounts = new api.AccountsApi(client);
ws.workspaces = new api.WorkspacesApi(client);
print();
print('global objects initialized: api and ws');
print("server url set to '${url}' using username '${username}'");
print('sample: var folder = new api.FolderDTO()');
print('sample: ws.workspaces.getWorkspacesForConnectedUser()');
print();
