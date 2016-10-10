angular
    .module('eArkPlatform.cmis.repoView')
    .controller('RepoViewController', RepoViewController);

function RepoViewController(cmisRepoService, fileUtilsService, $mdDialog, $window, BRIDGE_URI) {
    var rvc = this;
    rvc.repo = cmisRepoService.repoItems;
    rvc.loadRepoView = loadRepoView;
    rvc.isFile = isFile;
    rvc.getItem = getItem;
    rvc.breadcrumbs = cmisRepoService.breadcrumbs;
    rvc.gotoCrumb = cmisRepoService.goToCrumb;
    rvc.fileInfoDiag = fileInfoDiag;
    rvc.download = download;

    rvc.loadRepoView();

    function loadRepoView(){
        cmisRepoService.registerObserverCallback(repoViewObserver);
        cmisRepoService.connect();
    }

    /**
     * Request folder children for view
     * @param objectId
     * @private
     */
    function _getFolderView(objectId){
        var requestObject = {
            profileName: rvc.profileName,
            folderObjectId: objectId
        };
        cmisRepoService.getFolderChildren(requestObject);
    }

    /**
     * Returns all the information about a document
     * @param objectId
     * @private
     */
    function _getDocument(ev, objectId){
        var requestObject = {
            profileName: rvc.profileName,
            documentObjectId: objectId
        };
        cmisRepoService.getDocument(requestObject).then(function(response){
            rvc.document = response;
            rvc.document.displaySize = fileUtilsService.formatBytes(response.properties.size);
            rvc.fileInfoDiag(ev, rvc.document);
        });
        
    }

    /**
     * Just returns whether the item is a document or folder
     * @param item
     * @returns {boolean}
     */
    function isFile(item){
        return item.type === "document";
    }

    /**
     * Decides which to call between getting information on a document or a folder.
     * @param objectId
     * @param itemType
     */
    function download(document){
        $window.open(BRIDGE_URI.serviceProxy+cmisRepoService.getDocumentUrl(document.objectId));
    }

    /**
     * Decides which to call between getting information on a document or a folder.
     * @param objectId
     * @param itemType
     */
    function getItem(ev, objectId, itemType){
        (itemType === 'folder') ? _getFolderView(objectId) : _getDocument(ev, objectId);
    }

    /**
     * Returns the current path based on the breadcrumbs
     * @private
     */
    function _getBreadcrumbPath(){
        var path = "";
        rvc.breadcrumbs.forEach(function(item){
            path += "/"+item.name;
        });
        return path;
    }

    /**
     * Re-assigns the repo view array on changes to objects in the array
     */
    function repoViewObserver(){
        rvc.breadcrumbs = cmisRepoService.breadcrumbs;
        rvc.repo = cmisRepoService.repoItems;
    }
    
    
    /**
     * Dialog to show info on individual files
     */
    
    function fileInfoDiag(ev, doc) {
        $mdDialog.show({
          controller: fileInfoDialogController,
          templateUrl: 'app/src/cmis/repoView/view/fileInfoDiag.html',
          parent: angular.element(document.body),
          targetEvent: ev,
          locals: { document: doc },
          clickOutsideToClose: true,
          fullscreen: true
        });
    }
    
    function fileInfoDialogController($scope, $mdDialog, document) {
        var fidc = this;
        
        $scope.doc = document.properties;
        
        $scope.hide = function() {
          $mdDialog.hide();
        };
    
        $scope.cancel = function() {
          $mdDialog.cancel();
        };
    
    };

      
}