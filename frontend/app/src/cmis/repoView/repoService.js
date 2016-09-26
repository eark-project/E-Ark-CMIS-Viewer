angular
    .module('eArkPlatform.cmis.repoView')
    .service('cmisRepoService', cmisRepoService);

function cmisRepoService($http, fileUtilsService) {

    var cmisSvc = this;
    cmisSvc.observerCallbacks = [];
    cmisSvc.breadcrumbs = [];
    cmisSvc.repoItems = [];
    //methods
    cmisSvc.connect = connect;
    cmisSvc.initRepoView = initRepoView;
    cmisSvc.getFolderChildren = getFolderChildren;
    cmisSvc.getDocument = getDocument;
    cmisSvc.goToCrumb = goToCrumb;

    /**
     * Returns the properties of the root folder of a profile repository along with its children.
     * This should only really be called when clicking on the profile name in the view or the root in the breadcrumb
     * trail.
     * @param profileName
     * @returns {*}
     */
    function connect() {
        return $http.get('/webapi/repository/connect').then(function (response) {
            cmisSvc.breadcrumbs = [];
            initRepoView(response.data.rootFolder);
        });
    }

    /**
     * Will return the properties of a folder and its children pretty much in the same format as the connect function
     * @param requestObject
     * @returns {*}
     */
    function getFolderChildren(requestObject) {
        return $http.post('/webapi/repository/getFolder', requestObject).then(function (response) {
            initRepoView(response.data.folder);
        });
    }

    /**
     * Will return the properties of a document and, optionally, it's content stream.
     * @param requestObject object constructed thus {profileName:[a profile name], objectId:[documentObjectId, includeContentStream:false/true}
     * @returns {*}
     */
    function getDocument(requestObject) {
        return $http.post('/webapi/repository/getDocument', requestObject).then(function (response) {
            return response.data.document;
        });
    }

    /**
     * Adds resource path for the icon matching the mimetype property to the json object of everything in the rvc.repo
     * array.
     */
    function _addThumbnailUrl(repoItems) {
        var mimeTypeProperty = 'contentStreamMimeType';
        repoItems.children.forEach(function (item) {
            if (item.type === 'folder') {
                item.thumbNailURL = fileUtilsService.getFolderIcon(24);
            } else {
                item.thumbNailURL = fileUtilsService.getFileIconByMimetype(item[mimeTypeProperty], 24);
            }
        });
    }

    /**
     * Initialises the view used for displaying folder children in the view
     * @param response
     */
    function initRepoView(response){
        cmisSvc.repoItems = response;
        cmisSvc.repoItems.empty = (!cmisSvc.repoItems.children === Array || cmisSvc.repoItems.children.length == 0);
        if (!cmisSvc.repoItems.empty) {
            _addThumbnailUrl(cmisSvc.repoItems);
            cmisSvc.repoItems.children.forEach(function (item) {
                if (item.type == 'document')
                    item.displaySize = fileUtilsService.formatBytes(item.size);

            })
        }
        var crumb = {
            name: cmisSvc.repoItems.properties.name,
            objectId: cmisSvc.repoItems.properties.objectId
        };
        cmisSvc.breadcrumbs.push(crumb);
        cmisSvc.notifyObservers();
    }

    /**
     * Changes the view to the level selected in the breadcrumb
     * @param index
     */
    function goToCrumb(index) {
        var selected = cmisSvc.breadcrumbs[index];
        cmisSvc.breadcrumbs = cmisSvc.breadcrumbs.slice(0, index);
        (index == 0) ? cmisSvc.connect() : cmisSvc.getFolderChildren({
            folderObjectId: selected.objectId
        });
    }

    /**
     * returns the index of the item in the basket
     * @param item
     * @private
     * @returns number of item in the flat array or -1 indicating it doesn't exist
     */
    function _getItemPos(item) {
        if (cmisSvc.repoItems.length <= 0)
            return -1;
        else
            return cmisSvc.repoItems.map(function (o) {
                return o.objectId
            }).indexOf(item.objectId);
    }

    //register an observer
    cmisSvc.registerObserverCallback = function(callback){
        cmisSvc.observerCallbacks.push(callback);
    };

    //call this when repoItems has been changed
    cmisSvc.notifyObservers = function(){
        angular.forEach(cmisSvc.observerCallbacks, function(callback){
            callback();
        });
    };

}
