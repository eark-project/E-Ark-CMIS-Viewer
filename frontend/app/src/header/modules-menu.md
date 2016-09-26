# Modules menu

The Modules Menu Service is used to inject menu items in the header menu. This enables users to navigate to the various modules.

It works like this:

1. The [Modules Menu Controller](/app/src/header/ModulesMenuController.js) calls the ModulesMenuServiceProvider.

2. [ModulesMenuServiceProvider](/app/src/header/modulesMenuService.js) is a provider that has an `items` array to hold the number of items to render.
It also specifies a `$get` method with the sole purpose of returning the items array. 

3. Any module wishing to add itself to the list of modules to be rendered uses the module config object to add itself to the ModulesMenuServiceProvider items array (via the 'addItem' method).
The config object specifies the templateUrl and the order for where to insert itself in the menu.

Here is an example:

```
angular
    .module('eArkPlatform.files', ['ngMaterial'])
    .config(config);

function config(modulesMenuServiceProvider) {
    modulesMenuServiceProvider.addItem({
        templateUrl: 'app/src/files/view/menuItem.html',
        order: 1
    });
}
```

Above, we call a `config()` function for the module.
The function takes `ModulesMenuServiceProvider` as an input parameter and uses its 'addItem()' method to define a menu item template and its display order.

**Note** The above example points to the template file [app/src/files/view/menuItem.html.](/app/src/files/view/menuItem.html)
Because of the way the `md-sidebar` works in the header, you'll need to add a function call to each link in order to close the menu after clicking a link.
Here, we use the `toggleMenu()` from the [HeaderController.js](/app/src/header/HeaderController.js):
```
<a  class="md-button md-text-icon-button" ui-sref="files" ng-click="vm.toggleMenu()">
    <i class="material-icons">folder</i>
    {{ 'DOCUMENT.DOCUMENTS' | translate }}
</a>
```
