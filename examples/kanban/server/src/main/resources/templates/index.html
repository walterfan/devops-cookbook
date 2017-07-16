<html ng-app="accountApp">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>The Tao of Agile</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script	type="text/javascript" src="js/jquery-1.11.2.min.js"></script>

<!-- Bootstrap -->
<link rel="stylesheet" href="css/bootstrap.css"/>
<link rel="stylesheet" href="css/bootstrap-theme.css"/>

<script type="text/javascript" src="js/angular.js"></script>
    
<link href="css/layout2.css" media="all" rel="stylesheet" />
<link href="css/slide.css" media="all" rel="stylesheet" />
<link href="css/font.css" media="all" rel="stylesheet" />


<script type="text/javascript">


var myApp = angular.module("accountApp", []);
myApp.constant("baseUrl","/agiletao/api/v1/accounts");
myApp.controller("accountContoller", function($scope, $http, baseUrl ){
    $scope.accounts = [];
    $scope.displayMode = "";
    $scope.currentAccount = null;


    $scope.listAccount = function() {
        $('#accountDiv').hide();

        $http.get(baseUrl).success(function(data) {
            $scope.accounts = data;

        })
    }

    $scope.createAccount = function(newAccount) {
        $http.post(baseUrl, newAccount).success(function(data) {
            $scope.accounts.push(data);
        });
    }

    $scope.editAccount = function(account) {
        console.log("-- edit account ---");

        $('#accountsDiv').hide();
        $('#accountDiv').show();

        if(!account) {
            $scope.currentAccount = {};
        } else {
            $scope.currentAccount = angular.copy(account);
        }

    }

    $scope.cancelAccount = function(account) {
        console.log("-- cancel account ---");
        $('#accountDiv').hide();
        $('#accountsDiv').show();
    }

    $scope.saveAccount = function(account) {
        console.log("-- save account ---");
        if(angular.isDefined(account.accountID)) {
            $scope.updateAccount(account);
        } else {
            $scope.createAccount(account);
        }
        $('#accountDiv').hide();
        $('#accountsDiv').show();
    }

    $scope.updateAccount = function(account) {
        console.log("-- update account ---");
        $http({
            url: baseUrl + "/" + account.accountID,
            method: "PUT",
            data: account
        }).success(function(data) {
            console.log("-- put account successfully---" + data);
            for(var i=0; $scope.accounts.length; i++) {
                if($scope.accounts[i].accountID == data.accountID) {
                    $scope.accounts[i] = data;
                    break;
                }
            }
        })
    }

    $scope.deleteAccount = function(newAccount) {

        var url = baseUrl + "/" + newAccount.accountID;
        console.log("-- delete account ---" + url);
        $http.delete(url).success(function(data) {
            //remove the account from $scope.accounts;
            $scope.listAccount();
        });
    }

    $scope.retrieveAccount = function(account) {
        console.log( "-- retrieveAccount:" + JSON.stringify(account));
        if(account.encrypted) {
            var url = baseUrl + "/" + account.accountID;
            $http.get(url).success(function (data) {
                for (var i = 0; $scope.accounts.length; i++) {
                    if ($scope.accounts[i].accountID == data.accountID) {
                        $scope.accounts[i] = data;
                        break;
                    }
                }
            });
        } else {
            $scope.listAccount();
        }
    }


    $scope.listAccount();
});




</script>
<style type="text/css">
#linkTab {
	font-size: 12px;
	font-family: Arial;
	color: #330099;
	line-height: 24px;
	text-align: left
}

ul.mktree  li.liBullet  .bullet {
	cursor: default;
	background: url(images/b1.gif) center left no-repeat;
}
.contentTab {
width: 100%;
margin-bottom: 20px
}
.contentTab caption {
    padding: 6px;
    background-color: #ddeeff;
    color: white;
    font-size: 1.2em;
}
.contentTab thead > tr {
    background-color: #CCFFEE;
    border: 2px groove #ddeeff;
}

.contentTab thead > tr > th {
    font-size: 1.1em;
}


.contentTab tfoot > tr > td {
    text-align: right;
    font-size: 1.1em;
	font-family: Arial;

}

.footer { margin-left: auto; margin-right:auto; *zoom: 1;}
</style>

</head>

<body ng-controller = "accountContoller">
<div id="container">
<ng-include src="'menu.html'"></ng-include>

<div id="leftnav">


</div>
<div id="content">

<div class = "item" id="accountsDiv">
<h2>Accounts</h2>
<div class="alertDiv">*  Please use the master secret key to decrypt the encrypted password.</div>
<table class="contentTab table-striped table-bordered table-hover table-condensed">
  <thead><tr>
    <th width="5%" style="text-align: left"> # </th>
    <th width="20%"><a href="#">User name <span class="glyphicon glyphicon-sort-by-alphabet"></span></a></th>
    <th width="20%">Password</th>
      <th width="10%">Email</th>
    <th width="10%">Site</th>
    <th width="30%">Url</th>
    <th width="5%">
        <span class="glyphicon glyphicon-plus" ng-click="editAccount()"/>
    </th>
  </tr></thead>
    <tbody>
    <tr ng-repeat="account in accounts">
    <td> {{$index + 1}}</td>
    <td><a href="#"  ng-click="editAccount(account)">{{account.userName}}</a></td>
    <td><a href="#"  ng-click="retrieveAccount(account)">{{account.password}}</a>    </td>
    <td> {{account.email}}    </td>
     <td> {{account.siteName}}    </td>
     <td>{{account.siteUrl}}     </td>
    <td nowrap>
        <span class="glyphicon glyphicon-remove" ng-click="deleteAccount(account)"/>
    </td>
  </tr>
  
 
  
  </tbody>
 <tfoot><tr>
    <td colspan="7">
    <table width="100%" class="pageTab" cellpadding="0" cellspacing="0">
    <tr>
    <td  >

    </td></tr></table>
    </td>
    </tr>
    </tfoot>
  </table>
</div>

<div class = "item" id="accountDiv">
    <h2>Account</h2>
    <input type="hidden" ng-model="currentAccount.accountID"/>
    <div class="form-group">
        <label>User Name:</label>
        <input class="form-control" required ng-model="currentAccount.userName"/>
    </div>
    <div class="form-group">
        <label>Password:</label>
        <input class="form-control" required ng-model="currentAccount.password"/>
    </div>

    <div class="checkbox">
        <label>
        <input name="encrypted" type="checkbox" required ng-model="currentAccount.encrypted"/>
            Encrypted?
        </label>
    </div>
    <div class="form-group">
        <label>Email:</label>
        <input class="form-control" required ng-model="currentAccount.email"/>
    </div>
    <div class="form-group">
        <label>Site Name:</label>
        <input class="form-control" required ng-model="currentAccount.siteName"/>
    </div>
    <div class="form-group">
        <label>Site Url:</label>
        <input class="form-control" ng-model="currentAccount.siteUrl"/>
    </div>
    <div class="text-right">
        <button class="btn btn-primary" id="cancelBtn" ng-click="cancelAccount()">Cancel</button>
        <button class="btn btn-primary" id="saveBtn" ng-click="saveAccount(currentAccount)">Save</button>
    </div>
</div>

</div><!-- content div end -->
    <ng-include src="'footer.html'"></ng-include>
</div>


</body>
</html>
