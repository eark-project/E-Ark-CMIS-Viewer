angular
    .module('eArkPlatform.header')
    .provider('modulesMenuService', modulesMenuServiceProvider);

/**
 * for injecting items into the header bar. Any module wishing to inject a menu item into the main header ber must
 * call this
 */
function modulesMenuServiceProvider(USER_ROLES) {
    var _items = [];
    var items = [];
    this.addItem = addItem;
    this.$get = modulesMenuService;

    function addItem(item) {
        items.push(item);
        return this;
    }

    function modulesMenuService() {
        return {
            getItems: getItems,
            fixPerms: fixHeaderPerms
        };

        function getItems() {
            return items;
        }

        function fixHeaderPerms(user){
            items = [];
            var userRole = user.role.toUpperCase();
            if(!user)
                return;
            _items.forEach(function(item){
                if(userRole == USER_ROLES.admin.toUpperCase() || item.authorizedRole.toUpperCase() == userRole)
                    items.push(item);
            });

        }
    }
}