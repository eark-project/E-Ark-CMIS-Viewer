# Working with modules

To make life a little easier on developers, we use a module approach to adding new features in the project.
Here is a step by step approach to creating a module:

## Register a module

Create a folder in [/app/src](/app/src) to contain your module's files and add a file `[modulename].module.js` in this folder.
Here is an example of the [files.module.js](/app/src/files/files.module.js)
```
angular
    .module('angularStubApp.files', ['ngMaterial'])
    .config(config);

function config(modulesMenuServiceProvider) {
    modulesMenuServiceProvider.addItem({
        templateUrl: 'app/src/files/view/menuItem.html',
        order: 2
    });
}
```
This adds a `angularStubApp.files` module to the project and injects `ngMaterial` to use within the module.
Then there is some configuration being done. In this case, a menu item for the module is created using the `modulesMenuServiceProvider`.

Of couse, you'll need to add an entry in the [app.module.js](/app/src/app.module.js) for the new module. Like this:
```
angular
    .module('angularStubApp', [
        ...
        'angularStubApp.files',
        ...
    ])
    .config(config)
    .run(function
```


## Add Controllers, Views, and Services

Now you can add controllers, views, and services to your module. Add the files into your folder.
Every time you run `gulp build`, your module's files will be added into [angular-stub.min.js](/dist/angular-stub.min.js) for use in the website.
So a typical module folder will be organized like this:

* **modulename** (folder)
  * modulename.module.js
  * ModulenameController.js
  * modulenameService.js
  * **view** (folder)
    * modulename.html
    * moduleMenuItem.html (for menu display)

The `.js` files will all need to start with a declaration to attach to the module.

Here is an example controller, [FilesController.js](/app/src/files/FilesController.js)
```
angular
    .module('angularStubApp.files')
    .controller('FilesController', FilesController);

function FilesController($scope, filesService) {
    var vm = this;
    vm.filesProperty = [];

    function filesMethod() {
        vm.filesProperty = [];
    }
    
    ...
}
```

Here is an example service, [filesService.js](/app/src/files/filesService.js)
```
angular
    .module('angularStubApp.files')
    .factory('filesService', FilesService);

function FilesService($http, fileUtilsService, alfrescoNodeUtils) {

    return {
        getFileInfo: getFileInfo,
    };

    function getFileInfo(nodeRef) {
        return $http.get('/api/opendesk/file/' + alfrescoNodeUtils.processNodeRef(nodeRef).uri)
            .then(function(response) {
                return response;
            }
        );
    }
    ...
}
```

And here is an example view, [files.html](/app/src/files/view/files.html)
```
<md-content layout="column">
    ...
    <p>Putting a list of files here</p>
    ...
</md-content>
```


## Access modules

To show the UI part (view) of a module, you'll need two additional steps:

1. Add a routing state to associate the view with a URL.
2. Possibly add a navigation menu item so users can access your module.


### Add a routing state

Open [app.module.js](/app/src/app.module.js) again and look for the configuration function.
You'll need to add a routing state to the `$stateProvider` object.
Here is an example for files again:
```
function config($mdThemingProvider, $stateProvider, $urlRouterProvider, USER_ROLES) {
    ...
    
    $stateProvider.state('site', {
        ...
    }).state('files', {
        parent: 'site',
        url: '/files',
        views: {
            'content@': {
                templateUrl: 'app/src/files/view/files.html',
                controller: 'FilesController',
                controllerAs: 'vm'
            }
        },
        data: {
            authorizedRoles: [USER_ROLES.user]
        }
    }),
    ...
}
```


### Add a menu item

A user should should be able to navigate to a module in order to use its features.
We create access through the header by having the module configure a DOM item to be injected into the menu.
The details are decribed in [Displaying modules in header menu](/app/src/header/modules-menu.md)
