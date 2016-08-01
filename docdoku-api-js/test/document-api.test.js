var chai = require('chai');
var expect = chai.expect;
var DocdokuPlmClient = require('../lib/docdoku-api');
var config = require('./test.config');

describe('Document api tests', function() {

    it('Should create a document', function(done){

        var client = new DocdokuPlmClient({
            url:config.url,
            login:config.login,
            password:config.password,
            basicAuth:true
        });

        client.getApi().then(function(api){

            expect(api.apis.folders).to.be.defined;
            expect(api.apis.folders.createAccount).to.be.a.function;
            var documentReference = 'DOC-'+Date.now();
            api.apis.folders.createDocumentMasterInFolder({
                workspaceId:config.workspace,
                body:{
                    reference:documentReference,
                    title:'GeneratedDoc'
                },
                folderId:config.workspace
            }).then(function(response){
                var documentCreated = response.obj;
                expect(documentCreated).to.be.an.object;
                expect(documentCreated.documentMasterId).to.equal(documentReference);
                done();
            });

        });
    });

    it('Should list documents', function(done){

        var client = new DocdokuPlmClient({
            url:config.url,
            login:config.login,
            password:config.password,
            basicAuth:true
        });

        client.getApi().then(function(api){

            api.apis.documents.getDocumentsInWorkspace({
                workspaceId:config.workspace,
                start:0,
                configSpecType:null
            }).then(function(response){
                var documents = response.obj;
                expect(documents).to.be.an.array;
                expect(documents.length).not.to.be.empty;
                done();
            });

        });
    });

    it('Should list checkin a document', function(done){

        var client = new DocdokuPlmClient({
            url:config.url,
            login:config.login,
            password:config.password,
            basicAuth:true
        });

        client.getApi().then(function(api){

            var documentReference = 'DOCCHECKIN-'+Date.now();
            api.apis.folders.createDocumentMasterInFolder({
                workspaceId:config.workspace,
                body:{
                    reference:documentReference,
                    title:'GeneratedDoc'
                },
                folderId:config.workspace
            }).then(function(response){

                var documentCreated = response.obj;

                api.apis.document.checkInDocument({
                    workspaceId:config.workspace,
                    documentId:documentCreated.documentMasterId,
                    documentVersion:documentCreated.version
                }).then(function(response){
                    expect(response.status).to.equal(200);
                    done();
                });

            });

        });
    });
});
