angular
    .module('eArkPlatform.header')
    .controller('ModulesMenuController', ModulesMenuController);

function ModulesMenuController(modulesMenuService, $state) {
    
    var menu = this;
    
    menu.items = modulesMenuService.getItems();
    menu.state = $state;
    
}