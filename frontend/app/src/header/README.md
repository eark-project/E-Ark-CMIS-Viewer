1 - The Modules menu Controller calls the ModulesMenuServiceProvider. <br/>
2 - ModulesMenuServiceProvider is a provider that has an '<b>items array</b>' to hold the number of items to render.<br/> 
    It also specifies a '<b>$get</b>' method with the sole purpose of returning the items array. <br/>
3 - Any module wishing to add itself to the list of modules to be rendered uses the </b>module config object</b> <br/>
    (<b>app.module().config()</b> with a dependency on the ModulesMenuServiceProvider) to add itself to the <br/>
    ModulesMenuServiceProvider items array (via the 'addItem' method) specifying the templateUrl and the order <br/>
    (for where to insert itself in the menu).
